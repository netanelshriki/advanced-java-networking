package com.network.examples;

import com.network.NetworkLib;
import com.network.api.tcp.TcpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

/**
 * Example of using the TCP client.
 */
public class TcpClientExample {

    public static void main(String[] args) {
        // Get server address and port
        String server = "localhost";
        int port = 8000;
        
        if (args.length >= 2) {
            server = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        // Create TCP client
        TcpClient client = NetworkLib.createTcpClient()
                .withAddress(server, port)
                .withConnectionTimeout(Duration.ofSeconds(10))
                .withKeepAlive(true)
                .withTcpNoDelay(true)
                .build();
        
        // Add event listeners
        client.onConnect(conn -> System.out.println("Connected to server"))
              .onDisconnect(conn -> System.out.println("Disconnected from server"))
              .onError(ex -> System.err.println("Error: " + ex.getMessage()))
              .onDataReceived((conn, data) -> {
                  String message = new String(data, StandardCharsets.UTF_8);
                  System.out.println("Received: " + message);
              });
        
        try {
            // Connect to server
            System.out.println("Connecting to server...");
            client.connect();
            
            // Start sending messages
            System.out.println("Enter messages to send (type 'exit' to quit):");
            
            // Read from standard input
            Scanner scanner = new Scanner(System.in);
            String message;
            
            while (!(message = scanner.nextLine()).equalsIgnoreCase("exit")) {
                // Send message
                client.send(message.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent: " + message);
            }
            
            // Disconnect
            client.disconnect();
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
