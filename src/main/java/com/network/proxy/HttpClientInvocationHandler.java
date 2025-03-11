package com.network.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.network.annotation.http.Body;
import com.network.annotation.http.DELETE;
import com.network.annotation.http.GET;
import com.network.annotation.http.Header;
import com.network.annotation.http.PathVariable;
import com.network.annotation.http.POST;
import com.network.annotation.http.PUT;
import com.network.annotation.http.RequestParam;
import com.network.annotation.http.Timeout;
import com.network.annotation.resilience.CircuitBreaker;
import com.network.annotation.resilience.RateLimit;
import com.network.annotation.resilience.Retry;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequestBuilder;
import com.network.api.http.HttpResponse;
import com.network.api.http.TypedHttpRequestBuilder;

/**
 * InvocationHandler for HTTP client interfaces.
 * <p>
 * This class handles method invocations on a proxy created by ClientProxyFactory.
 * It converts method calls to HTTP requests based on method annotations.
 * </p>
 */
class HttpClientInvocationHandler implements InvocationHandler {

    private final Class<?> interfaceType;
    private final HttpClient client;
    private final Map<Method, MethodHandler> methodHandlers = new HashMap<>();
    
    /**
     * Creates a new invocation handler.
     *
     * @param interfaceType the interface type
     * @param client        the HTTP client to use
     */
    public HttpClientInvocationHandler(Class<?> interfaceType, HttpClient client) {
        this.interfaceType = interfaceType;
        this.client = client;
        initializeMethodHandlers();
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods like toString(), equals(), etc.
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        
        // Get handler for this method
        MethodHandler handler = methodHandlers.get(method);
        if (handler == null) {
            throw new IllegalStateException("No handler for method: " + method);
        }
        
        // Execute the method
        return handler.invoke(args);
    }
    
    /**
     * Initializes method handlers for all methods in the interface.
     */
    private void initializeMethodHandlers() {
        for (Method method : interfaceType.getMethods()) {
            MethodHandler handler = createMethodHandler(method);
            methodHandlers.put(method, handler);
        }
    }
    
    /**
     * Creates a method handler for the given method.
     *
     * @param method the method to create a handler for
     * @return a method handler
     */
    private MethodHandler createMethodHandler(Method method) {
        // Determine HTTP method
        HttpMethod httpMethod = null;
        String path = "";
        
        if (method.isAnnotationPresent(GET.class)) {
            httpMethod = HttpMethod.GET;
            path = method.getAnnotation(GET.class).value();
        } else if (method.isAnnotationPresent(POST.class)) {
            httpMethod = HttpMethod.POST;
            path = method.getAnnotation(POST.class).value();
        } else if (method.isAnnotationPresent(PUT.class)) {
            httpMethod = HttpMethod.PUT;
            path = method.getAnnotation(PUT.class).value();
        } else if (method.isAnnotationPresent(DELETE.class)) {
            httpMethod = HttpMethod.DELETE;
            path = method.getAnnotation(DELETE.class).value();
        } else {
            throw new IllegalArgumentException("Method must be annotated with an HTTP method annotation: " + method);
        }
        
        // Get return type info
        boolean isAsync = CompletableFuture.class.isAssignableFrom(method.getReturnType());
        Type responseType = getResponseType(method);
        
        // Create parameter processors
        ParameterProcessor[] paramProcessors = createParameterProcessors(method);
        
        // Get timeout info
        Integer timeout = null;
        if (method.isAnnotationPresent(Timeout.class)) {
            timeout = (int) method.getAnnotation(Timeout.class).value();
        }
        
        // Check for resilience annotations
        CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
        Retry retry = method.getAnnotation(Retry.class);
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        // Create the handler
        return new HttpMethodHandler(
                client, httpMethod, path, responseType, isAsync,
                paramProcessors, timeout, circuitBreaker, retry, rateLimit);
    }
    
    /**
     * Gets the response type for a method.
     *
     * @param method the method
     * @return the response type
     */
    private Type getResponseType(Method method) {
        if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
            // For async methods, get the CompletableFuture's type parameter
            ParameterizedType futureType = (ParameterizedType) method.getGenericReturnType();
            return futureType.getActualTypeArguments()[0];
        } else {
            // For sync methods, return the method's return type
            return method.getGenericReturnType();
        }
    }
    
    /**
     * Creates parameter processors for a method.
     *
     * @param method the method
     * @return an array of parameter processors
     */
    private ParameterProcessor[] createParameterProcessors(Method method) {
        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        ParameterProcessor[] processors = new ParameterProcessor[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            processors[i] = createParameterProcessor(parameters[i], parameterAnnotations[i], i);
        }
        
        return processors;
    }
    
    /**
     * Creates a parameter processor for a single parameter.
     *
     * @param parameter    the parameter
     * @param annotations  the parameter annotations
     * @param index        the parameter index
     * @return a parameter processor
     */
    private ParameterProcessor createParameterProcessor(Parameter parameter, Annotation[] annotations, int index) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof PathVariable) {
                String name = ((PathVariable) annotation).value();
                if (name.isEmpty()) {
                    name = parameter.getName();
                }
                return new PathVariableProcessor(name, index);
            } else if (annotation instanceof RequestParam) {
                String name = ((RequestParam) annotation).value();
                if (name.isEmpty()) {
                    name = parameter.getName();
                }
                boolean required = ((RequestParam) annotation).required();
                return new RequestParamProcessor(name, index, required);
            } else if (annotation instanceof Body) {
                return new BodyProcessor(index);
            } else if (annotation instanceof Header) {
                String name = ((Header) annotation).value();
                if (name.isEmpty()) {
                    name = parameter.getName();
                }
                return new HeaderProcessor(name, index);
            }
        }
        
        // If no annotation found, try to guess based on parameter name and type
        return new DefaultProcessor(index);
    }
    
    /**
     * Interface for handling method invocations.
     */
    interface MethodHandler {
        /**
         * Invokes the method with the given arguments.
         *
         * @param args the method arguments
         * @return the method result
         * @throws Exception if an error occurs
         */
        Object invoke(Object[] args) throws Exception;
    }
    
    /**
     * Interface for processing method parameters.
     */
    interface ParameterProcessor {
        /**
         * Processes a parameter and applies it to the request builder.
         *
         * @param builder the request builder
         * @param args    the method arguments
         */
        void process(HttpRequestBuilder builder, Object[] args);
    }
    
    /**
     * Handler for HTTP method invocations.
     */
    class HttpMethodHandler implements MethodHandler {
        private final HttpClient client;
        private final HttpMethod httpMethod;
        private final String path;
        private final Type responseType;
        private final boolean isAsync;
        private final ParameterProcessor[] paramProcessors;
        private final Integer timeout;
        private final CircuitBreaker circuitBreaker;
        private final Retry retry;
        private final RateLimit rateLimit;
        
        public HttpMethodHandler(
                HttpClient client, HttpMethod httpMethod, String path,
                Type responseType, boolean isAsync, ParameterProcessor[] paramProcessors,
                Integer timeout, CircuitBreaker circuitBreaker, Retry retry, RateLimit rateLimit) {
            this.client = client;
            this.httpMethod = httpMethod;
            this.path = path;
            this.responseType = responseType;
            this.isAsync = isAsync;
            this.paramProcessors = paramProcessors;
            this.timeout = timeout;
            this.circuitBreaker = circuitBreaker;
            this.retry = retry;
            this.rateLimit = rateLimit;
        }
        
        @Override
        public Object invoke(Object[] args) throws Exception {
            // Create request builder
            HttpRequestBuilder builder = client.request().path(path).method(httpMethod);
            
            // Apply parameter processors
            for (ParameterProcessor processor : paramProcessors) {
                processor.process(builder, args);
            }
            
            // Apply timeout if specified
            if (timeout != null) {
                builder.timeout(timeout);
            }
            
            // TODO: Apply resilience annotations (circuit breaker, retry, rate limit)
            
            // Create typed builder based on response type
            if (responseType instanceof Class) {
                @SuppressWarnings("unchecked")
                TypedHttpRequestBuilder<?> typedBuilder = builder.deserializeAs((Class<?>) responseType);
                
                // Execute request
                if (isAsync) {
                    // For async methods, return a CompletableFuture
                    return typedBuilder.executeAsync();
                } else {
                    // For sync methods, return the response body
                    HttpResponse<?> response = typedBuilder.execute();
                    if (Void.TYPE.equals(responseType)) {
                        return null; // For void methods
                    } else {
                        return response.getBody();
                    }
                }
            } else {
                // For complex generic types, we'll need to use a type token or similar
                // This is a simplification - a real implementation would handle this case
                throw new UnsupportedOperationException("Complex generic return types not supported yet");
            }
        }
    }
    
    /**
     * Processor for path variables.
     */
    class PathVariableProcessor implements ParameterProcessor {
        private final String name;
        private final int index;
        
        public PathVariableProcessor(String name, int index) {
            this.name = name;
            this.index = index;
        }
        
        @Override
        public void process(HttpRequestBuilder builder, Object[] args) {
            Object value = args[index];
            if (value != null) {
                builder.pathParam(name, value.toString());
            }
        }
    }
    
    /**
     * Processor for query parameters.
     */
    class RequestParamProcessor implements ParameterProcessor {
        private final String name;
        private final int index;
        private final boolean required;
        
        public RequestParamProcessor(String name, int index, boolean required) {
            this.name = name;
            this.index = index;
            this.required = required;
        }
        
        @Override
        public void process(HttpRequestBuilder builder, Object[] args) {
            Object value = args[index];
            if (value != null) {
                builder.queryParam(name, value.toString());
            } else if (required) {
                throw new IllegalArgumentException("Required query parameter '" + name + "' is null");
            }
        }
    }
    
    /**
     * Processor for request body.
     */
    class BodyProcessor implements ParameterProcessor {
        private final int index;
        
        public BodyProcessor(int index) {
            this.index = index;
        }
        
        @Override
        public void process(HttpRequestBuilder builder, Object[] args) {
            Object value = args[index];
            if (value != null) {
                builder.body(value);
            }
        }
    }
    
    /**
     * Processor for headers.
     */
    class HeaderProcessor implements ParameterProcessor {
        private final String name;
        private final int index;
        
        public HeaderProcessor(String name, int index) {
            this.name = name;
            this.index = index;
        }
        
        @Override
        public void process(HttpRequestBuilder builder, Object[] args) {
            Object value = args[index];
            if (value != null) {
                builder.header(name, value.toString());
            }
        }
    }
    
    /**
     * Default processor for parameters without annotations.
     */
    class DefaultProcessor implements ParameterProcessor {
        private final int index;
        
        public DefaultProcessor(int index) {
            this.index = index;
        }
        
        @Override
        public void process(HttpRequestBuilder builder, Object[] args) {
            // By default, do nothing
        }
    }
}
