/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package loadbalancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author QuangHuy
 */
public class LoadBalancer {

    private final ServerSocket lbServer;
    private final ExecutorService pool;
    private final Map<String, Integer> requestCounts; // count number of request in 1 time unit
    private final Map<String, Integer> remainDeniedTime;
    private final WebhookService webhookService;

    public LoadBalancer() throws IOException {
        this.lbServer = new ServerSocket(AppConstants.LB_PORT);
        this.requestCounts = new ConcurrentHashMap<>();
        this.remainDeniedTime = new ConcurrentHashMap<>();
        this.pool = Executors.newFixedThreadPool(AppConstants.MAX_CONNECTION);
        this.webhookService = new WebhookService();
    }

    public void run() {
        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                this.requestCounts.clear();
                Set<Map.Entry<String, Integer>> entrySet = this.remainDeniedTime.entrySet();
                for (Map.Entry<String, Integer> entry : entrySet) {
                    String key = entry.getKey();
                    int remain = entry.getValue();
                    if (remain > 1) {
                        this.remainDeniedTime.put(entry.getKey(), entry.getValue() - 1);
                    } else {
                        this.remainDeniedTime.remove(key);
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

            System.out.println("LB Server is ready ...");

            while (true) {
                Socket conn = this.lbServer.accept();
                String clientIP = conn.getInetAddress().getHostAddress();

                if (this.remainDeniedTime.containsKey(clientIP)) {
                    this.closeClientSocket(conn);
                } else {
                    int count = this.requestCounts.getOrDefault(clientIP, 0);
                    this.requestCounts.put(clientIP, count + 1);
                    if (isSpam(clientIP, count + 1)) {
                        String message = String.format("IP: %s reach limit %d request per %s -> Denied, Wait for %d %s\n",
                                clientIP, AppConstants.MAX_CONNECTION_PER_MINUTE, TimeUnit.SECONDS, AppConstants.WAITING_TIME, TimeUnit.SECONDS);
                        webhookService.sendWebhookLogs(message);
                        this.closeClientSocket(conn);
                    } else {
                        Worker selectedWorker = Servers.getWorkerServer();
                        ClientForwarder forwarder = new ClientForwarder(conn, selectedWorker.getHostName(), selectedWorker.getPort());
                        pool.execute(forwarder);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Loadbalancing Server exception: " + e.getMessage());
        }
    }

    public void closeClientSocket(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public void shutdown() throws IOException {
        if (!this.lbServer.isClosed()) {
            try (this.lbServer) {
                this.pool.shutdown();
            }
        }
    }

    public boolean isSpam(String clientIP, int requestCounts) {
        if (requestCounts > AppConstants.MAX_CONNECTION_PER_MINUTE) {
            this.remainDeniedTime.put(clientIP, AppConstants.WAITING_TIME);
            return true;
        }
        return false;
    }

}
