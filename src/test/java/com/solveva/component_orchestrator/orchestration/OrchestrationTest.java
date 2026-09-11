package com.solveva.component_orchestrator.orchestration;

import java.util.List;
import java.util.Map;

import com.solveva.component_orchestrator.billing.Client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=18080", "components.calculation.api-key=test-key"})
class OrchestrationTest {

    @Autowired
    OrchestrationService orchestrationService;

    @Test
    void runsTheThreeComponentsInOrderAndReturnsTheCalculatedTotal() {
        OrchestrationResponse response = orchestrationService.orchestrate(
                new OrchestrationRequest(Client.ACME, Map.of("amount", 100, "currency", "eur")));

        assertThat(response.executedComponents())
                .containsExactly("validation", "calculation-adapter", "calculation");
        assertThat(response.result())
                .containsEntry("currencyCode", "EUR")
                .containsEntry("validated", true)
                .containsEntry("total", 120.00);
    }

    @Test
    void surfacesTheFailingComponent() {
        assertThatThrownBy(() -> orchestrationService.orchestrate(
                new OrchestrationRequest(Client.ACME, Map.of("amount", 100))))
                .hasMessageContaining("validation")
                .hasMessageContaining("currency is required");
    }

    @Test
    void executionOrderIsTheSameForEveryClient() {
        List<String> acme = orchestrationService.orchestrate(
                new OrchestrationRequest(Client.ACME, Map.of("amount", 10, "currency", "eur"))).executedComponents();
        List<String> globex = orchestrationService.orchestrate(
                new OrchestrationRequest(Client.GLOBEX, Map.of("amount", 10, "currency", "usd"))).executedComponents();

        assertThat(acme).isEqualTo(globex);
    }
}
