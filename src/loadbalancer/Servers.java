/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package loadbalancer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author QuangHuy
 */
class Worker {

    private final String hostName;
    private final Integer port;


    public Worker(String hostName, Integer port) {
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }
    
    public Integer getPort() {
        return port;
    }
}

public class Servers {

    private static List<Worker> servers = new ArrayList<>();
    private static int count = 0;

    static {
        servers.addAll(List.of(
                new Worker("localhost", 807),
                new Worker("localhost", 808)));
    }

    public static Worker getWorkerServer() { // round robin
        Worker selectedWorker = servers.get(count % servers.size());
        count = (count + 1) % servers.size();
        return selectedWorker;
    }
}
