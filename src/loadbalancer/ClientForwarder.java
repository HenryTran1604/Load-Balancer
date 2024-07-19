/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package loadbalancer;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author QuangHuy
 */
public class ClientForwarder extends Thread{
    private final Socket clientSocket;
    private final String workerHost;
    private final int workerPort;

    public ClientForwarder(Socket clientSocket, String workerHost, int workerPort) {
        this.clientSocket = clientSocket;
        this.workerHost = workerHost;
        this.workerPort = workerPort;
    }

    @Override
    public void run() {
        try (Socket workerSocket = new Socket(workerHost, workerPort)) {
            Thread clientTransferHanlder = new Thread(() -> {
                try {
                    clientSocket.getInputStream().transferTo(workerSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Client to worker exception: " + e.getMessage());
                }
            });

            Thread workerTransferHandler = new Thread(() -> {
                try {
                    workerSocket.getInputStream().transferTo(clientSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("worker to Client exception: " + e.getMessage());
                }
            });

            clientTransferHanlder.start();
            workerTransferHandler.start();

            clientTransferHanlder.join();
            workerTransferHandler.join();
        } catch (IOException e) {
            System.out.println("ClientForwarder exception: " + e.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientForwarder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
