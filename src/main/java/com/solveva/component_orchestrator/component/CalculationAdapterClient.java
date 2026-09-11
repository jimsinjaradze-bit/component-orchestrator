package com.solveva.component_orchestrator.component;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CalculationAdapterClient {

    private final ComponentHttpCaller http;
    private final String url;

    CalculationAdapterClient(ComponentHttpCaller http, @Value("${components.calculation-adapter.url}") String url) {
        this.http = http;
        this.url = url;
    }

    public Map<String, Object> adapt(String clientId, Map<String, Object> payload) {
        return http.post("calculation-adapter", url, clientId, payload);
    }
}
