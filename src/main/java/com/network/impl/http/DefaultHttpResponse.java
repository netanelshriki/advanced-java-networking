package com.network.impl.http;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;
import com.network.api.http.HttpResponseException;
import com.network.middleware.http.MutableHttpResponse;
import com.network.serialization.Serializer;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link HttpResponse} interface.
 */
class DefaultHttpResponse implements HttpResponse, MutableHttpResponse {

    private final int statusCode;
    private final byte[] body;
    private final Map<String, String> headers;
    private final URI uri;
    private final HttpRequest request;
    
    /**
     * Creates a new DefaultHttpResponse.
     * 
     * @param statusCode the HTTP status code
     * @param body       the response body
     * @param headers    the response headers
     * @param uri        the request URI
     * @param request    the original request
     */
    DefaultHttpResponse(int statusCode, byte[] body, Map<String, String> headers, URI uri, HttpRequest request) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.uri = uri;
        this.request = request;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusMessage() {
        return HttpStatusCodes.getMessage(statusCode);
    }

    @Override
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getBodyAsString() {
        return body == null ? null : new String(body);
    }

    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (body == null || body.length == 0) {
            return null;
        }
        
        // Get the serializer from the client
        Serializer serializer = getSerializer();
        if (serializer == null) {
            throw new IllegalStateException("No serializer configured");
        }
        
        return serializer.deserialize(body, type);
    }
    
    /**
     * Gets the serializer to use for deserialization.
     * 
     * @return the serializer or null if not available
     */
    private Serializer getSerializer() {
        if (request != null && request instanceof DefaultHttpRequest) {
            DefaultHttpRequest httpRequest = (DefaultHttpRequest) request;
            if (httpRequest.getClient() instanceof DefaultHttpClient) {
                return ((DefaultHttpClient) httpRequest.getClient()).getSerializer();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public String getContentType() {
        return headers.get("Content-Type");
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpResponse throwIfNotSuccessful() throws HttpResponseException {
        if (!isSuccessful()) {
            throw new HttpResponseException(this);
        }
        return this;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String toString() {
        return "HTTP " + statusCode + " " + getStatusMessage() + ", " +
               "headers: " + headers.size() + ", " +
               "body: " + (body == null ? "null" : body.length + " bytes");
    }
    
    /**
     * Utility class for HTTP status code messages.
     */
    private static class HttpStatusCodes {
        private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
        
        static {
            // 1xx Informational
            STATUS_MESSAGES.put(100, "Continue");
            STATUS_MESSAGES.put(101, "Switching Protocols");
            STATUS_MESSAGES.put(102, "Processing");
            STATUS_MESSAGES.put(103, "Early Hints");
            
            // 2xx Success
            STATUS_MESSAGES.put(200, "OK");
            STATUS_MESSAGES.put(201, "Created");
            STATUS_MESSAGES.put(202, "Accepted");
            STATUS_MESSAGES.put(203, "Non-Authoritative Information");
            STATUS_MESSAGES.put(204, "No Content");
            STATUS_MESSAGES.put(205, "Reset Content");
            STATUS_MESSAGES.put(206, "Partial Content");
            STATUS_MESSAGES.put(207, "Multi-Status");
            STATUS_MESSAGES.put(208, "Already Reported");
            STATUS_MESSAGES.put(226, "IM Used");
            
            // 3xx Redirection
            STATUS_MESSAGES.put(300, "Multiple Choices");
            STATUS_MESSAGES.put(301, "Moved Permanently");
            STATUS_MESSAGES.put(302, "Found");
            STATUS_MESSAGES.put(303, "See Other");
            STATUS_MESSAGES.put(304, "Not Modified");
            STATUS_MESSAGES.put(305, "Use Proxy");
            STATUS_MESSAGES.put(307, "Temporary Redirect");
            STATUS_MESSAGES.put(308, "Permanent Redirect");
            
            // 4xx Client Errors
            STATUS_MESSAGES.put(400, "Bad Request");
            STATUS_MESSAGES.put(401, "Unauthorized");
            STATUS_MESSAGES.put(402, "Payment Required");
            STATUS_MESSAGES.put(403, "Forbidden");
            STATUS_MESSAGES.put(404, "Not Found");
            STATUS_MESSAGES.put(405, "Method Not Allowed");
            STATUS_MESSAGES.put(406, "Not Acceptable");
            STATUS_MESSAGES.put(407, "Proxy Authentication Required");
            STATUS_MESSAGES.put(408, "Request Timeout");
            STATUS_MESSAGES.put(409, "Conflict");
            STATUS_MESSAGES.put(410, "Gone");
            STATUS_MESSAGES.put(411, "Length Required");
            STATUS_MESSAGES.put(412, "Precondition Failed");
            STATUS_MESSAGES.put(413, "Payload Too Large");
            STATUS_MESSAGES.put(414, "URI Too Long");
            STATUS_MESSAGES.put(415, "Unsupported Media Type");
            STATUS_MESSAGES.put(416, "Range Not Satisfiable");
            STATUS_MESSAGES.put(417, "Expectation Failed");
            STATUS_MESSAGES.put(418, "I'm a teapot");
            STATUS_MESSAGES.put(421, "Misdirected Request");
            STATUS_MESSAGES.put(422, "Unprocessable Entity");
            STATUS_MESSAGES.put(423, "Locked");
            STATUS_MESSAGES.put(424, "Failed Dependency");
            STATUS_MESSAGES.put(425, "Too Early");
            STATUS_MESSAGES.put(426, "Upgrade Required");
            STATUS_MESSAGES.put(428, "Precondition Required");
            STATUS_MESSAGES.put(429, "Too Many Requests");
            STATUS_MESSAGES.put(431, "Request Header Fields Too Large");
            STATUS_MESSAGES.put(451, "Unavailable For Legal Reasons");
            
            // 5xx Server Errors
            STATUS_MESSAGES.put(500, "Internal Server Error");
            STATUS_MESSAGES.put(501, "Not Implemented");
            STATUS_MESSAGES.put(502, "Bad Gateway");
            STATUS_MESSAGES.put(503, "Service Unavailable");
            STATUS_MESSAGES.put(504, "Gateway Timeout");
            STATUS_MESSAGES.put(505, "HTTP Version Not Supported");
            STATUS_MESSAGES.put(506, "Variant Also Negotiates");
            STATUS_MESSAGES.put(507, "Insufficient Storage");
            STATUS_MESSAGES.put(508, "Loop Detected");
            STATUS_MESSAGES.put(510, "Not Extended");
            STATUS_MESSAGES.put(511, "Network Authentication Required");
        }
        
        /**
         * Gets the message for the specified status code.
         * 
         * @param statusCode the status code
         * @return the message, or "Unknown" if not found
         */
        static String getMessage(int statusCode) {
            return STATUS_MESSAGES.getOrDefault(statusCode, "Unknown");
        }
    }
}
