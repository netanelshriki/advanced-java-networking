package com.network.examples.annotation;

import java.util.List;

import com.network.NetworkLib;

/**
 * Example demonstrating the use of annotation-based HTTP clients.
 */
public class AnnotationExample {

    public static void main(String[] args) {
        // Create a client from the annotated interface
        UserService userService = NetworkLib.createClient(UserService.class);
        
        // Get users
        List<User> users = userService.getUsers(0, 10, null);
        System.out.println("Users: " + users);
        
        // Get user by ID
        User user = userService.getUserById("123");
        System.out.println("User: " + user);
        
        // Create a new user
        User newUser = new User(null, "John Doe", "john@example.com", 30);
        User createdUser = userService.createUser(newUser);
        System.out.println("Created user: " + createdUser);
        
        // Update a user
        createdUser.setName("John Smith");
        User updatedUser = userService.updateUser(createdUser.getId(), createdUser);
        System.out.println("Updated user: " + updatedUser);
        
        // Delete a user
        userService.deleteUser("123", "auth-token-123");
        System.out.println("User deleted");
        
        // Search users asynchronously
        userService.searchUsersAsync("John")
            .thenAccept(results -> System.out.println("Search results: " + results))
            .exceptionally(e -> {
                System.err.println("Search failed: " + e.getMessage());
                return null;
            });
    }
}