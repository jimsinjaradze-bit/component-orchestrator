package com.solveva.component_orchestrator.stub;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubComponentsControllerTest {

    private final StubComponentsController components = new StubComponentsController();

    @Test
    void validationPassesThePayloadThroughAndMarksIt() {
        Map<String, Object> result = components.validate(Map.of("amount", 100, "currency", "eur"));

        assertThat(result)
                .containsEntry("amount", 100)
                .containsEntry("currency", "eur")
                .containsEntry("validated", true);
    }

    @Test
    void validationRejectsAMissingCurrency() {
        assertThatThrownBy(() -> components.validate(Map.of("amount", 100)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("currency is required");
    }

    @Test
    void validationRejectsANonPositiveAmount() {
        assertThatThrownBy(() -> components.validate(Map.of("amount", 0, "currency", "eur")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("amount must be positive");
    }

    @Test
    void adapterMapsTheRequestOntoTheCalculationEngineFields() {
        Map<String, Object> result = components.adapt(Map.of("amount", 100, "currency", "eur"));

        assertThat(result)
                .containsEntry("baseAmount", new BigDecimal("100"))
                .containsEntry("currencyCode", "EUR")
                .containsEntry("taxRate", new BigDecimal("0.20"));
    }

    @Test
    void calculationAddsTaxToTheBaseAmount() {
        Map<String, Object> result = components.calculate("local-dev-key",
                Map.of("baseAmount", new BigDecimal("100"), "taxRate", new BigDecimal("0.20")));

        assertThat(result)
                .containsEntry("tax", new BigDecimal("20.00"))
                .containsEntry("total", new BigDecimal("120.00"));
    }

    @Test
    void calculationNeedsTheAdapterToHaveRunFirst() {
        assertThatThrownBy(() -> components.calculate("local-dev-key", Map.of("amount", 100)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("baseAmount is required");
    }

    @Test
    void calculationRejectsARequestWithoutAnApiKey() {
        assertThatThrownBy(() -> components.calculate(null,
                Map.of("baseAmount", new BigDecimal("100"), "taxRate", new BigDecimal("0.20"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("X-Api-Key is required");
    }
}
