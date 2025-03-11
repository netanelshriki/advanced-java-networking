package com.network.examples;

import com.network.NetworkLib;
import com.network.api.udp.UdpClient;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Example of using the UDP client.
 */
public class UdpClientExample {

    public static void main(String[] args) {
        // Get server address and port
        String server = "localhost";
        int port = 9000;
        
        if (args.length >= 2) {
            server = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        // Create UDP client
        UdpClient client = NetworkLib.createUdpClient()
                .withAddress(server, port)
                .build();
        
        // Add event listeners
        client.onConnect(conn -> System.out.println("UDP client started"))
              .onDisconnect(conn -> System.out.println("UDP client stopped"))
              .onError(ex -> System.err.println("Error: " + ex.getMessage()))
              .onDataReceived((conn, data) -> {
                  String message = new String(data, StandardCharsets.UTF_8);
                  System.out.println("Received: " + message);
              });
        
        try {
            // Connect
            System.out.println("Starting UDP client...");
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
