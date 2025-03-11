# Advanced Java Networking Library Documentation

Welcome to the documentation for the Advanced Java Networking Library. This documentation provides in-depth information about the architecture, design principles, and class hierarchy of the library.

## Documentation Index

### Architecture and Design

- [Architecture Overview](ARCHITECTURE.md) - Detailed explanation of the layered architecture
- [Class Hierarchy](CLASS_HIERARCHY.md) - Comprehensive overview of interfaces and classes

### Diagrams

- [Architecture Diagram](architecture.svg) - Visual representation of the library architecture
- [Class Hierarchy Diagram](class-hierarchy.svg) - Visual representation of the class hierarchy

## Getting Started

For quick examples and getting started information, please see the [main README](../README.md).

## Protocols

The library supports the following network protocols:

- **HTTP/HTTPS**: RESTful APIs, web services
- **TCP**: Raw socket communication
- **UDP**: Connectionless datagram communication
- **WebSocket**: Real-time bidirectional communication

## Features

- **Fluent API Design**: Intuitive builder patterns
- **Middleware Architecture**: Pluggable request/response processing
- **Resilience Patterns**: Circuit breakers, retry mechanisms
- **Serialization Framework**: Multiple format support
- **Async Operations**: Non-blocking I/O with CompletableFuture

## Design Principles

The library adheres to the following design principles:

1. **Interface-First Design**: Clean separation between contract and implementation
2. **SOLID Principles**: Single responsibility, open-closed, Liskov substitution, interface segregation, dependency inversion
3. **Composition over Inheritance**: Favoring object composition over class inheritance
4. **Immutability**: Immutable objects where appropriate
5. **Consistent Error Handling**: Structured exception hierarchy