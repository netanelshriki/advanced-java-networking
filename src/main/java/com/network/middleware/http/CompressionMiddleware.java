package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * HTTP middleware that handles compression and decompression of HTTP request and response bodies.
 * 
 * <p>This middleware can automatically compress request bodies and decompress response bodies
 * using GZIP or Deflate algorithms. It handles the appropriate HTTP headers for content encoding
 * and adds the Accept-Encoding header to requests.
 * 
 * <p>By default, this middleware only compresses requests larger than a specified threshold
 * and only when the Content-Type indicates compressible content.
 */
public class CompressionMiddleware implements HttpMiddleware {

    private static final Logger LOG = LoggerFactory.getLogger(CompressionMiddleware.class);
    
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String GZIP = "gzip";
    private static final String DEFLATE = "deflate";
    
    private final boolean compressRequests;
    private final boolean decompressResponses;
    private final int compressionThreshold;
    private final Set<String> supportedEncodings;
    private final Set<String> compressibleContentTypes;
    
    /**
     * Creates a new CompressionMiddleware with the specified configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private CompressionMiddleware(Builder builder) {
        this.compressRequests = builder.compressRequests;
        this.decompressResponses = builder.decompressResponses;
        this.compressionThreshold = builder.compressionThreshold;
        this.supportedEncodings = new HashSet<>(builder.supportedEncodings);
        this.compressibleContentTypes = new HashSet<>(builder.compressibleContentTypes);
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Add Accept-Encoding header to indicate supported encodings
        if (decompressResponses && !supportedEncodings.isEmpty()) {
            String acceptEncoding = String.join(", ", supportedEncodings);
            addHeader(context, ACCEPT_ENCODING_HEADER, acceptEncoding);
        }
        
        // Check if request compression is enabled
        if (!compressRequests) {
            return;
        }
        
        // Check if the request already has a Content-Encoding header
        if (context.getRequest().getHeaders().containsKey(CONTENT_ENCODING_HEADER)) {
            LOG.debug("Skipping request compression because Content-Encoding is already set");
            return;
        }
        
        // Check if the request has a body
        byte[] body = context.getRequest().getBody();
        if (body == null || body.length < compressionThreshold) {
            LOG.debug("Skipping request compression because body is too small ({} bytes)", 
                    body != null ? body.length : 0);
            return;
        }
        
        // Check if the Content-Type is compressible
        String contentType = context.getRequest().getHeaders().get(CONTENT_TYPE_HEADER);
        if (contentType == null || !isCompressibleContentType(contentType)) {
            LOG.debug("Skipping request compression because Content-Type is not compressible: {}", 
                    contentType);
            return;
        }
        
        // Compress the request body
        try {
            byte[] compressedBody = compressBody(body, GZIP);
            
            // Only use compression if it actually reduces the size
            if (compressedBody.length < body.length) {
                LOG.debug("Compressed request body from {} bytes to {} bytes", 
                        body.length, compressedBody.length);
                
                if (context.getRequest() instanceof MutableHttpRequest) {
                    MutableHttpRequest req = (MutableHttpRequest) context.getRequest();
                    req.setBody(compressedBody);
                    req.addHeader(CONTENT_ENCODING_HEADER, GZIP);
                }
            } else {
                LOG.debug("Skipping request compression because it would increase size");
            }
        } catch (IOException e) {
            LOG.warn("Failed to compress request body", e);
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Check if response decompression is enabled
        if (!decompressResponses) {
            return;
        }
        
        // Check if the response has a body
        byte[] body = response.getBody();
        if (body == null || body.length == 0) {
            return;
        }
        
        // Check if the response has a Content-Encoding header
        String encoding = response.getHeader(CONTENT_ENCODING_HEADER);
        if (encoding == null) {
            return;
        }
        
        // Check if we support this encoding
        encoding = encoding.trim().toLowerCase();
        if (!supportedEncodings.contains(encoding)) {
            LOG.warn("Unsupported Content-Encoding: {}", encoding);
            return;
        }
        
        // Decompress the response body
        try {
            byte[] decompressedBody = decompressBody(body, encoding);
            
            LOG.debug("Decompressed response body from {} bytes to {} bytes", 
                    body.length, decompressedBody.length);
            
            if (response instanceof MutableHttpResponse) {
                MutableHttpResponse resp = (MutableHttpResponse) response;
                resp.setBody(decompressedBody);
                resp.removeHeader(CONTENT_ENCODING_HEADER);
            }
        } catch (IOException e) {
            LOG.warn("Failed to decompress response body", e);
        }
    }
    
    /**
     * Compresses a byte array using the specified encoding.
     * 
     * @param data the data to compress
     * @param encoding the encoding to use
     * @return the compressed data
     * @throws IOException if an I/O error occurs
     */
    private byte[] compressBody(byte[] data, String encoding) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
        
        try {
            if (GZIP.equalsIgnoreCase(encoding)) {
                try (GZIPOutputStream gzipStream = new GZIPOutputStream(output)) {
                    gzipStream.write(data);
                }
            } else if (DEFLATE.equalsIgnoreCase(encoding)) {
                try (DeflaterOutputStream deflateStream = new DeflaterOutputStream(output)) {
                    deflateStream.write(data);
                }
            } else {
                throw new IllegalArgumentException("Unsupported encoding: " + encoding);
            }
            
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
    
    /**
     * Decompresses a byte array using the specified encoding.
     * 
     * @param data the data to decompress
     * @param encoding the encoding to use
     * @return the decompressed data
     * @throws IOException if an I/O error occurs
     */
    private byte[] decompressBody(byte[] data, String encoding) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            if (GZIP.equalsIgnoreCase(encoding)) {
                try (GZIPInputStream gzipStream = new GZIPInputStream(input)) {
                    copyStream(gzipStream, output);
                }
            } else if (DEFLATE.equalsIgnoreCase(encoding)) {
                try (InflaterInputStream inflateStream = new InflaterInputStream(input)) {
                    copyStream(inflateStream, output);
                }
            } else {
                throw new IllegalArgumentException("Unsupported encoding: " + encoding);
            }
            
            return output.toByteArray();
        } finally {
            input.close();
            output.close();
        }
    }
    
    /**
     * Copies data from an input stream to an output stream.
     * 
     * @param input the input stream
     * @param output the output stream
     * @throws IOException if an I/O error occurs
     */
    private void copyStream(java.io.InputStream input, java.io.OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int length;
        
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
    }
    
    /**
     * Checks if a content type is compressible.
     * 
     * @param contentType the content type
     * @return true if compressible, false otherwise
     */
    private boolean isCompressibleContentType(String contentType) {
        String type = contentType.toLowerCase();
        
        for (String compressibleType : compressibleContentTypes) {
            if (type.startsWith(compressibleType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds a header to the request if the implementation supports it.
     * 
     * @param context the request context
     * @param name the header name
     * @param value the header value
     */
    private void addHeader(HttpRequestContext context, String name, String value) {
        if (context.getRequest() instanceof MutableHttpRequest) {
            MutableHttpRequest req = (MutableHttpRequest) context.getRequest();
            req.addHeader(name, value);
        }
    }
    
    /**
     * Creates a new builder for creating CompressionMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link CompressionMiddleware} instances.
     */
    public static class Builder {
        private boolean compressRequests = true;
        private boolean decompressResponses = true;
        private int compressionThreshold = 1024;
        private final Set<String> supportedEncodings = new HashSet<>();
        private final Set<String> compressibleContentTypes = new HashSet<>();
        
        /**
         * Creates a new Builder with default values.
         */
        public Builder() {
            // Add default supported encodings
            supportedEncodings.add(GZIP);
            supportedEncodings.add(DEFLATE);
            
            // Add default compressible content types
            compressibleContentTypes.add("text/");
            compressibleContentTypes.add("application/json");
            compressibleContentTypes.add("application/xml");
            compressibleContentTypes.add("application/javascript");
            compressibleContentTypes.add("application/xhtml+xml");
            compressibleContentTypes.add("image/svg+xml");
        }
        
        /**
         * Sets whether to compress request bodies.
         * 
         * @param compressRequests true to compress requests, false otherwise
         * @return this builder instance
         */
        public Builder compressRequests(boolean compressRequests) {
            this.compressRequests = compressRequests;
            return this;
        }
        
        /**
         * Sets whether to decompress response bodies.
         * 
         * @param decompressResponses true to decompress responses, false otherwise
         * @return this builder instance
         */
        public Builder decompressResponses(boolean decompressResponses) {
            this.decompressResponses = decompressResponses;
            return this;
        }
        
        /**
         * Sets the minimum size in bytes for a request body to be compressed.
         * 
         * @param compressionThreshold the compression threshold
         * @return this builder instance
         */
        public Builder compressionThreshold(int compressionThreshold) {
            this.compressionThreshold = compressionThreshold;
            return this;
        }
        
        /**
         * Sets the supported encodings.
         * 
         * @param encodings the supported encodings
         * @return this builder instance
         */
        public Builder supportedEncodings(String... encodings) {
            supportedEncodings.clear();
            supportedEncodings.addAll(Arrays.asList(encodings));
            return this;
        }
        
        /**
         * Adds a supported encoding.
         * 
         * @param encoding the encoding to add
         * @return this builder instance
         */
        public Builder addSupportedEncoding(String encoding) {
            supportedEncodings.add(encoding);
            return this;
        }
        
        /**
         * Sets the compressible content types.
         * 
         * @param contentTypes the compressible content types
         * @return this builder instance
         */
        public Builder compressibleContentTypes(String... contentTypes) {
            compressibleContentTypes.clear();
            compressibleContentTypes.addAll(Arrays.asList(contentTypes));
            return this;
        }
        
        /**
         * Adds a compressible content type.
         * 
         * @param contentType the content type to add
         * @return this builder instance
         */
        public Builder addCompressibleContentType(String contentType) {
            compressibleContentTypes.add(contentType);
            return this;
        }
        
        /**
         * Builds a new {@link CompressionMiddleware} instance with the current settings.
         * 
         * @return a new CompressionMiddleware instance
         */
        public CompressionMiddleware build() {
            return new CompressionMiddleware(this);
        }
    }
    
    /**
     * Interface for modifiable HTTP requests.
     * 
     * <p>This interface is used internally by the middleware to add headers to the request.
     * Implementations of HTTP request should also implement this interface.
     */
    private interface MutableHttpRequest {
        /**
         * Adds a header to the request.
         * 
         * @param name the header name
         * @param value the header value
         */
        void addHeader(String name, String value);
        
        /**
         * Sets the request body.
         * 
         * @param body the request body
         */
        void setBody(byte[] body);
    }
    
    /**
     * Interface for modifiable HTTP responses.
     * 
     * <p>This interface is used internally by the middleware to add headers to the response.
     * Implementations of {@link HttpResponse} should also implement this interface.
     */
    private interface MutableHttpResponse {
        /**
         * Adds a header to the response.
         * 
         * @param name the header name
         * @param value the header value
         */
        void addHeader(String name, String value);
        
        /**
         * Removes a header from the response.
         * 
         * @param name the header name
         */
        void removeHeader(String name);
        
        /**
         * Sets the response body.
         * 
         * @param body the response body
         */
        void setBody(byte[] body);
    }
}
