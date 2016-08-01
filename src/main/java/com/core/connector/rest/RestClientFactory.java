package com.core.connector.rest;

import org.springframework.stereotype.Component;

@Component("restClientFactory")
public class RestClientFactory {

	public RestClient getClient() {
		return new RestClient();
	}

}
