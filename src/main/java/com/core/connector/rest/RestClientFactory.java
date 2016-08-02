package com.core.connector.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import com.servicecore.cfg.ServiceConfiguration;
import com.servicecore.cfg.ServiceConfigurationGroup;

@Component("restClientFactory")
public class RestClientFactory {

	
	@Autowired
	private RestClient restClient;
	
	private static Logger LOG = LoggerFactory.getLogger(RestClientFactory.class);

    public static final String CONFIG_PREFIX = "restclient.def.";
    public static final String KEY_CONNECT_TIMEOUT = "connectTimeout";
    public static final String KEY_READ_TIMEOUT = "readTimeout";
    public static final String KEY_BUFFERED = "buffered";
    
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    public static final boolean DEFAULT_BUFFERED = true;
    
    @Resource(name = "esp.service.Configuration")
    private ServiceConfiguration serviceConfig;

    @Resource
    private ApplicationContext appContext;
    
    private Map<String,RestClient> clientMap = new HashMap<>();
    
	public RestClient getClient() {
		return restClient;
	}
	

    /**
     * Creates and stores a REST client.  If a client with a specific ID was retrieved before, the same client will not be recreated and the original client will be returned.
     * @param id The ID of the client to create, used to retrieve the properties that define the client's functionality.
     * @return
     */
    public RestClient getClient(String id) {
        return this.getClient(id,CONFIG_PREFIX);
    }

    /**
     * Creates and stores a REST client.  If a client with a specific ID was retrieved before, the same client will not be recreated and the original client will be returned.
     * @param id The ID of the client to create, used to retrieve the properties that define the client's functionality.
     * @param prefix The prefix of the client properties.  If <code>null</code>, defaults to {@link #CONFIG_PREFIX}
     * @return The new or 
     */
    public RestClient getClient(String id, String prefix) {
        if(!this.clientMap.containsKey(id)) {
            this.clientMap.put(id,this.createClient(id,prefix));
        }
        return this.clientMap.get(id);
    }


    /**
     * Override this to make modifications to created clients.
     * @param id The ID of the client to create, used to retrieve the properties of that client.
     * @param prefix The prefix of the client properties.  If <code>null</code>, defaults to {@link #CONFIG_PREFIX}
     * @return A new REST client.
     */
    protected RestClient createClient(String id, String prefix) {
        prefix = (StringUtils.isBlank(prefix) ? CONFIG_PREFIX : prefix);
        ServiceConfigurationGroup propGrp = serviceConfig.getStringGroup(prefix+id+".");
        int connTimeout = ServiceConfiguration.extractInteger(propGrp,KEY_CONNECT_TIMEOUT,DEFAULT_CONNECT_TIMEOUT);
        int readTimeout = ServiceConfiguration.extractInteger(propGrp,KEY_READ_TIMEOUT,DEFAULT_READ_TIMEOUT);
        boolean useBuffered = (propGrp==null ? DEFAULT_BUFFERED : propGrp.getBoolean(KEY_BUFFERED,DEFAULT_BUFFERED));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connTimeout);
        factory.setReadTimeout(readTimeout);
        RestClient newClient = new RestClient(useBuffered ? new BufferingClientHttpRequestFactory(factory) : factory);
        this.initializeClient(newClient,propGrp);
        LOG.info("New REST client initialized: ");
        return newClient;
    }

    /**
     * Override this to add more initialization to a new client.
     * @param client The new client to be initialized.
     * @param propGrp The properties group containing the configuration values for the client.
     * @return The client, fully initialized.
     */
    protected void initializeClient(RestClient client,ServiceConfigurationGroup propGrp) {
        client.setInterceptors(this.getInterceptorList());
    }

    /**
     * By default, searches the application context for any beans which implement the {@link ClientHttpRequestInterceptor} and returns them.
     * <p>Override this method in your own factory to change which interceptors are collected.</p>
     * @return The list of available interceptors.
     */
    protected List<ClientHttpRequestInterceptor> getInterceptorList() {
        Map<String,ClientHttpRequestInterceptor> interceptorMap = this.appContext.getBeansOfType(ClientHttpRequestInterceptor.class);
        return (interceptorMap==null ? new ArrayList<>() : new ArrayList<>(interceptorMap.values()));
    }

	
}
