package com.network.examples;

import com.network.NetworkLib;
import com.network.api.websocket.WebSocketClient;

import java.util.Scanner;

/**
 * Example of using the WebSocket client.
 */
public class WebSocketClientExample {

    public static void main(String[] args) {
        // Create WebSocket client
        WebSocketClient client = NetworkLib.createWebSocketClient()
                .withUrl("wss://echo.websocket.org")
                .build();
        
        // Add event listeners
        client.onConnect(conn -> System.out.println("Connected to WebSocket server"))
              .onDisconnect(conn -> System.out.println("Disconnected from WebSocket server"))
              .onError(ex -> System.err.println("Error: " + ex.getMessage()))
              .onTextMessage((conn, message) -> System.out.println("Received: " + message));
        
        try {
            // Connect to server
            System.out.println("Connecting to WebSocket server...");
            client.connect();
            
            // Start sending messages
            System.out.println("Enter messages to send (type 'exit' to quit):");
            
            // Read from standard input
            Scanner scanner = new Scanner(System.in);
            String message;
            
            while (!(message = scanner.nextLine()).equalsIgnoreCase("exit")) {
                // Send message
                client.send(message);
            }
            
            // Disconnect
            client.disconnect();
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
