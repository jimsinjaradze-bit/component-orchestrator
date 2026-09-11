package com.solveva.component_orchestrator.component;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationClientTest {

    @Mock
    ComponentHttpCaller http;

    @Captor
    ArgumentCaptor<Map<String, String>> headersCaptor;

    @Test
    void sendsTheConfiguredApiKeyAsAHeader() {
        when(http.post(eq("calculation"), eq("http://calculation.test/calculate"), eq("acme"), anyMap(), anyMap()))
                .thenReturn(Map.of("total", 120));
        CalculationClient client = new CalculationClient(http, "http://calculation.test/calculate", "test-key");

        Map<String, Object> result = client.calculate("acme", Map.of("baseAmount", 100));

        assertThat(result).containsEntry("total", 120);
        verify(http).post(eq("calculation"), eq("http://calculation.test/calculate"), eq("acme"),
                headersCaptor.capture(), anyMap());
        assertThat(headersCaptor.getValue()).containsEntry("X-Api-Key", "test-key");
    }
}
