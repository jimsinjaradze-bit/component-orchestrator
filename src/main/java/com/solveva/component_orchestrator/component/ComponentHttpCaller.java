package com.solveva.component_orchestrator.component;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Thin HTTP plumbing shared by the component clients. Every component speaks the same
 * protocol: POST a JSON payload, get a JSON payload back.
 */
@Component
public class ComponentHttpCaller {

    private static final ParameterizedTypeReference<Map<String, Object>> PAYLOAD =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient = RestClient.create();

    public Map<String, Object> post(String componentName, String url, String clientId, Map<String, Object> payload) {
        return post(componentName, url, clientId, Map.of(), payload);
    }

    public Map<String, Object> post(String componentName, String url, String clientId,
                                    Map<String, String> extraHeaders, Map<String, Object> payload) {
        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Client-Id", clientId)
                    .headers(headers -> extraHeaders.forEach(headers::add))
                    .body(payload)
                    .retrieve()
                    .body(PAYLOAD);
        } catch (RestClientResponseException ex) {
            throw new ComponentInvocationException(componentName,
                    HttpStatus.valueOf(ex.getStatusCode().value()), ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            throw new ComponentInvocationException(componentName, HttpStatus.BAD_GATEWAY, ex.getMessage());
        }
    }
}
