package com.core.connector.rest;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * A simple wrapper around Spring's RestTemplate to allow for easy creation with V4's instrumentation.  This is available for edge-case needs.  You should use {@link RestClientFactory} to get your client if at all possible.
 * 
 * Use of this class does not guarantee the availability of timeout functionality. 
 */
@Component
public class RestClient extends RestTemplate {

    /**
     * 
     */
    public RestClient() {
        super();
    }
    
    public RestClient(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }

    public RestClient(List<ClientHttpRequestInterceptor> interceptors) {
        super();
        setInterceptors(interceptors);
    }

    public RestClient(ClientHttpRequestFactory requestFactory, List<ClientHttpRequestInterceptor> interceptors) {
        super(requestFactory);
        setInterceptors(interceptors);
    }

    public RestClient(List<HttpMessageConverter<?>> messageConverters, List<ClientHttpRequestInterceptor> interceptors) {
        super(messageConverters);
        setInterceptors(interceptors);
    }
}
