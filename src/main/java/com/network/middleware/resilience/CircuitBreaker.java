package com.network.middleware.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Implementation of the Circuit Breaker pattern.
 * 
 * <p>The Circuit Breaker pattern prevents an application from performing operations that are
 * likely to fail. It monitors operations for failures and when the failure rate reaches a
 * threshold, it "trips" to prevent further execution. After a timeout period, the circuit
 * allows a limited number of test requests to determine if the operation has recovered.
 * 
 * <p>This implementation has three states:
 * <ul>
 *   <li>CLOSED: Requests are allowed to execute normally.</li>
 *   <li>OPEN: Requests are not allowed to execute and fail immediately.</li>
 *   <li>HALF_OPEN: A limited number of test requests are allowed to execute to determine
 *       if the operation has recovered.</li>
 * </ul>
 * 
 * <p>The circuit breaker automatically transitions between states based on the success
 * or failure of requests and configurable thresholds.
 */
public class CircuitBreaker {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreaker.class);
    
    private final String name;
    private final int failureThreshold;
    private final Duration resetTimeout;
    private final int halfOpenTestRequests;
    private final Predicate<Throwable> failurePredicate;
    private final List<CircuitBreakerStateChangeListener> stateChangeListeners = new ArrayList<>();
    
    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<>(CircuitBreakerState.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger consecutiveSuccessCount = new AtomicInteger(0);
    private volatile Instant lastFailure;
    private volatile Instant lastTransitionToOpen;
    
    /**
     * Creates a new CircuitBreaker with the specified configuration.
     * 
     * @param builder the builder used to create this instance
     */
    private CircuitBreaker(Builder builder) {
        this.name = builder.name;
        this.failureThreshold = builder.failureThreshold;
        this.resetTimeout = builder.resetTimeout;
        this.halfOpenTestRequests = builder.halfOpenTestRequests;
        this.failurePredicate = builder.failurePredicate;
    }
    
    /**
     * Gets the name of this circuit breaker.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the current state of the circuit breaker.
     * 
     * @return the current state
     */
    public CircuitBreakerState getState() {
        return state.get();
    }
    
    /**
     * Gets the number of failures since the last reset.
     * 
     * @return the failure count
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * Gets the number of successes since the last reset.
     * 
     * @return the success count
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * Gets the number of consecutive successes since the last failure.
     * 
     * @return the consecutive success count
     */
    public int getConsecutiveSuccessCount() {
        return consecutiveSuccessCount.get();
    }
    
    /**
     * Gets the instant when the last failure occurred.
     * 
     * @return the instant when the last failure occurred, or null if no failure has occurred
     */
    public Instant getLastFailure() {
        return lastFailure;
    }
    
    /**
     * Gets the instant when the circuit breaker last transitioned to the OPEN state.
     * 
     * @return the instant when the circuit breaker last transitioned to the OPEN state,
     *         or null if it has never been opened
     */
    public Instant getLastTransitionToOpen() {
        return lastTransitionToOpen;
    }
    
    /**
     * Gets the time remaining until the circuit breaker will transition to the HALF_OPEN state.
     * 
     * @return the time remaining, or Duration.ZERO if the circuit breaker is not OPEN
     */
    public Duration getTimeUntilHalfOpen() {
        if (state.get() != CircuitBreakerState.OPEN || lastTransitionToOpen == null) {
            return Duration.ZERO;
        }
        
        Duration elapsed = Duration.between(lastTransitionToOpen, Instant.now());
        if (elapsed.compareTo(resetTimeout) >= 0) {
            return Duration.ZERO;
        }
        
        return resetTimeout.minus(elapsed);
    }
    
    /**
     * Checks if the circuit breaker will allow a request to be executed.
     * 
     * <p>If the circuit breaker is OPEN, this method returns false. If the circuit breaker
     * is CLOSED, this method returns true. If the circuit breaker is HALF_OPEN, this method
     * returns true if the number of test requests has not been exceeded.
     * 
     * @return true if the request should be allowed, false otherwise
     */
    public boolean allowRequest() {
        CircuitBreakerState currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                return true;
                
            case OPEN:
                // Check if it's time to transition to HALF_OPEN
                if (lastTransitionToOpen != null && 
                    Duration.between(lastTransitionToOpen, Instant.now()).compareTo(resetTimeout) >= 0) {
                    // Transition to HALF_OPEN
                    if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                        LOG.info("Circuit breaker '{}' transitioned from OPEN to HALF_OPEN", name);
                        notifyStateChanged(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN);
                        
                        // Reset counters
                        successCount.set(0);
                        failureCount.set(0);
                        consecutiveSuccessCount.set(0);
                    }
                    return true;
                }
                return false;
                
            case HALF_OPEN:
                // Allow a limited number of test requests
                return successCount.get() + failureCount.get() < halfOpenTestRequests;
                
            default:
                return false;
        }
    }
    
    /**
     * Records a successful request.
     * 
     * <p>If the circuit breaker is HALF_OPEN and the number of consecutive successes
     * reaches the threshold, the circuit breaker will transition to CLOSED.
     */
    public void recordSuccess() {
        CircuitBreakerState currentState = state.get();
        
        // Increment counters
        successCount.incrementAndGet();
        int consecutive = consecutiveSuccessCount.incrementAndGet();
        
        switch (currentState) {
            case HALF_OPEN:
                // If we've had enough consecutive successes, transition to CLOSED
                if (consecutive >= halfOpenTestRequests) {
                    if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                        LOG.info("Circuit breaker '{}' transitioned from HALF_OPEN to CLOSED after {} consecutive successes",
                                name, consecutive);
                        notifyStateChanged(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED);
                        
                        // Reset counters
                        successCount.set(0);
                        failureCount.set(0);
                        // Keep the consecutive success count
                    }
                }
                break;
                
            case CLOSED:
                // Nothing special to do
                break;
                
            case OPEN:
                // This shouldn't happen, but just in case
                LOG.warn("Unexpected success recorded when circuit breaker '{}' is OPEN", name);
                break;
        }
    }
    
    /**
     * Records a failed request.
     * 
     * <p>If the circuit breaker is CLOSED and the number of failures reaches the threshold,
     * the circuit breaker will transition to OPEN. If the circuit breaker is HALF_OPEN,
     * any failure will cause it to transition back to OPEN.
     * 
     * @param exception the exception that caused the failure, or null if not applicable
     */
    public void recordFailure(Throwable exception) {
        // Check if this exception should be counted as a failure
        if (exception != null && failurePredicate != null && !failurePredicate.test(exception)) {
            // This exception is not counted as a failure
            return;
        }
        
        CircuitBreakerState currentState = state.get();
        
        // Update timestamps and counters
        lastFailure = Instant.now();
        failureCount.incrementAndGet();
        consecutiveSuccessCount.set(0);
        
        switch (currentState) {
            case CLOSED:
                // If we've had too many failures, trip the circuit
                if (failureCount.get() >= failureThreshold) {
                    if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
                        lastTransitionToOpen = Instant.now();
                        LOG.info("Circuit breaker '{}' transitioned from CLOSED to OPEN after {} failures",
                                name, failureCount.get());
                        notifyStateChanged(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN);
                    }
                }
                break;
                
            case HALF_OPEN:
                // Any failure in HALF_OPEN should transition back to OPEN
                if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
                    lastTransitionToOpen = Instant.now();
                    LOG.info("Circuit breaker '{}' transitioned from HALF_OPEN back to OPEN after a failure", name);
                    notifyStateChanged(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN);
                }
                break;
                
            case OPEN:
                // Nothing special to do
                break;
        }
    }
    
    /**
     * Manually resets the circuit breaker to the CLOSED state.
     * 
     * <p>This method is primarily intended for testing and maintenance purposes.
     */
    public void reset() {
        CircuitBreakerState previousState = state.getAndSet(CircuitBreakerState.CLOSED);
        if (previousState != CircuitBreakerState.CLOSED) {
            LOG.info("Circuit breaker '{}' manually reset from {} to CLOSED", name, previousState);
            notifyStateChanged(previousState, CircuitBreakerState.CLOSED);
        }
        
        // Reset counters
        successCount.set(0);
        failureCount.set(0);
        consecutiveSuccessCount.set(0);
    }
    
    /**
     * Manually trips the circuit breaker to the OPEN state.
     * 
     * <p>This method is primarily intended for testing and maintenance purposes.
     */
    public void trip() {
        CircuitBreakerState previousState = state.getAndSet(CircuitBreakerState.OPEN);
        if (previousState != CircuitBreakerState.OPEN) {
            lastTransitionToOpen = Instant.now();
            LOG.info("Circuit breaker '{}' manually tripped from {} to OPEN", name, previousState);
            notifyStateChanged(previousState, CircuitBreakerState.OPEN);
        }
    }
    
    /**
     * Adds a listener to be notified when the circuit breaker state changes.
     * 
     * @param listener the listener to add
     */
    public void addStateChangeListener(CircuitBreakerStateChangeListener listener) {
        if (listener != null) {
            stateChangeListeners.add(listener);
        }
    }
    
    /**
     * Removes a previously added state change listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeStateChangeListener(CircuitBreakerStateChangeListener listener) {
        return stateChangeListeners.remove(listener);
    }
    
    /**
     * Notifies all registered listeners that the circuit breaker state has changed.
     * 
     * @param fromState the previous state
     * @param toState the new state
     */
    private void notifyStateChanged(CircuitBreakerState fromState, CircuitBreakerState toState) {
        for (CircuitBreakerStateChangeListener listener : stateChangeListeners) {
            try {
                listener.onStateChange(this, fromState, toState);
            } catch (Exception e) {
                LOG.warn("Exception in circuit breaker state change listener", e);
            }
        }
    }
    
    /**
     * Creates a new builder for creating CircuitBreaker instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link CircuitBreaker} instances.
     */
    public static class Builder {
        private String name = "default";
        private int failureThreshold = 5;
        private Duration resetTimeout = Duration.ofSeconds(30);
        private int halfOpenTestRequests = 3;
        private Predicate<Throwable> failurePredicate;
        
        /**
         * Sets the name of the circuit breaker.
         * 
         * @param name the name
         * @return this builder instance
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        /**
         * Sets the failure threshold.
         * 
         * <p>The circuit breaker will trip after this many consecutive failures.
         * 
         * @param failureThreshold the failure threshold
         * @return this builder instance
         */
        public Builder failureThreshold(int failureThreshold) {
            if (failureThreshold <= 0) {
                throw new IllegalArgumentException("failureThreshold must be > 0");
            }
            this.failureThreshold = failureThreshold;
            return this;
        }
        
        /**
         * Sets the reset timeout.
         * 
         * <p>The circuit breaker will remain open for this duration before transitioning
         * to half-open.
         * 
         * @param resetTimeout the reset timeout
         * @return this builder instance
         */
        public Builder resetTimeout(Duration resetTimeout) {
            if (resetTimeout.isNegative() || resetTimeout.isZero()) {
                throw new IllegalArgumentException("resetTimeout must be > 0");
            }
            this.resetTimeout = resetTimeout;
            return this;
        }
        
        /**
         * Sets the number of test requests to allow in the half-open state.
         * 
         * <p>The circuit breaker will allow this many requests to pass through in the
         * half-open state before deciding whether to close or re-open the circuit.
         * 
         * @param halfOpenTestRequests the number of test requests
         * @return this builder instance
         */
        public Builder halfOpenTestRequests(int halfOpenTestRequests) {
            if (halfOpenTestRequests <= 0) {
                throw new IllegalArgumentException("halfOpenTestRequests must be > 0");
            }
            this.halfOpenTestRequests = halfOpenTestRequests;
            return this;
        }
        
        /**
         * Sets a predicate to determine if an exception should be counted as a failure.
         * 
         * <p>If the predicate returns true, the exception will be counted as a failure.
         * If the predicate returns false, the exception will be ignored.
         * 
         * @param failurePredicate the failure predicate
         * @return this builder instance
         */
        public Builder failurePredicate(Predicate<Throwable> failurePredicate) {
            this.failurePredicate = failurePredicate;
            return this;
        }
        
        /**
         * Builds a new {@link CircuitBreaker} instance with the current settings.
         * 
         * @return a new CircuitBreaker instance
         */
        public CircuitBreaker build() {
            return new CircuitBreaker(this);
        }
    }
}
