package com.solveva.component_orchestrator.component;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CalculationClient {

    private final ComponentHttpCaller http;
    private final String url;
    private final String apiKey;

    CalculationClient(ComponentHttpCaller http,
                      @Value("${components.calculation.url}") String url,
                      @Value("${components.calculation.api-key}") String apiKey) {
        this.http = http;
        this.url = url;
        this.apiKey = apiKey;
    }

    public Map<String, Object> calculate(String clientId, Map<String, Object> payload) {
        return http.post("calculation", url, clientId, Map.of("X-Api-Key", apiKey), payload);
    }
}
