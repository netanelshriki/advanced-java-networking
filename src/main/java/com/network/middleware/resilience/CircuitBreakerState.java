package com.network.middleware.resilience;

/**
 * Enum representing the possible states of a CircuitBreaker.
 */
public enum CircuitBreakerState {
    /**
     * The circuit is closed and requests are allowed to go through normally.
     */
    CLOSED,
    
    /**
     * The circuit is open and requests will be rejected without attempting to execute them.
     */
    OPEN,
    
    /**
     * The circuit is partially open to allow a limited number of test requests to determine
     * if the system has recovered and the circuit can be closed again.
     */
    HALF_OPEN
}
