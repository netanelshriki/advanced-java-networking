package com.network.middleware.resilience;

/**
 * Interface for listeners that want to be notified when a CircuitBreaker changes state.
 */
@FunctionalInterface
public interface CircuitBreakerStateChangeListener {

    /**
     * Called when a CircuitBreaker changes state.
     * 
     * @param circuitBreaker the circuit breaker that changed state
     * @param fromState the previous state
     * @param toState the new state
     */
    void onStateChange(CircuitBreaker circuitBreaker, CircuitBreakerState fromState, CircuitBreakerState toState);
}
