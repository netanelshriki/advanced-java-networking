package com.network.middleware;

/**
 * A simple middleware implementation for testing purposes.
 */
public class SampleMiddleware {
    private String name = "sample";
    private boolean enabled = true;
    
    /**
     * Gets the middleware name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the middleware name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Checks if the middleware is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the middleware is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Process a request.
     * 
     * @param request the request to process
     * @return the processed request
     */
    public Object process(Object request) {
        if (!enabled) {
            return request;
        }
        
        // Do some processing
        return request;
    }
}
