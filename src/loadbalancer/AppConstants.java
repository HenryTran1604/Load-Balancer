/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package loadbalancer;

/**
 *
 * @author QuangHuy
 */
public class AppConstants {

    public static final int LB_PORT = 12345;

    public static final int MAX_CONNECTION = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);
    public static final int MAX_CONNECTION_PER_MINUTE = 10;
    public static final int WAITING_TIME = 10;

    public static final String WEBHOOK_LOG_ENDPOINT = "http://localhost:8080/api/webhook/log";
    public static final String WEBHOOK_SCOREBOARD_ENDPOINT = "http://localhost:8080/api/webhook/scoreboard";
    public static final int TIME_OUT_DURATION = 5000;
    
    public static final int REQUEST_LIMIT_EXCEED = 105;
    public static final String HTTP_POST = "POST";
    
}
