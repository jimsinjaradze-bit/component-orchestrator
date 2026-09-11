package com.solveva.component_orchestrator.component;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidationClient {

    private final ComponentHttpCaller http;
    private final String url;

    ValidationClient(ComponentHttpCaller http, @Value("${components.validation.url}") String url) {
        this.http = http;
        this.url = url;
    }

    public Map<String, Object> validate(String clientId, Map<String, Object> payload) {
        return http.post("validation", url, clientId, payload);
    }
}
