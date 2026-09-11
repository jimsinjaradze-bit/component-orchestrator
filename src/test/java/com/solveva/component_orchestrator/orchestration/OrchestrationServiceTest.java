package com.solveva.component_orchestrator.orchestration;

import java.util.Map;

import com.solveva.component_orchestrator.billing.Client;
import com.solveva.component_orchestrator.billing.TokenAccountService;
import com.solveva.component_orchestrator.component.CalculationAdapterClient;
import com.solveva.component_orchestrator.component.CalculationClient;
import com.solveva.component_orchestrator.component.ComponentInvocationException;
import com.solveva.component_orchestrator.component.ValidationClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestrationServiceTest {

    @Mock
    ValidationClient validationClient;

    @Mock
    CalculationAdapterClient calculationAdapterClient;

    @Mock
    CalculationClient calculationClient;

    @Mock
    TokenAccountService tokenAccountService;

    OrchestrationService service() {
        return new OrchestrationService(validationClient, calculationAdapterClient, calculationClient,
                tokenAccountService);
    }

    @Test
    void callsTheComponentsInOrderAndFeedsEachOneThePreviousResult() {
        when(validationClient.validate("ACME", Map.of("amount", 100))).thenReturn(Map.of("validated", true));
        when(calculationAdapterClient.adapt("ACME", Map.of("validated", true))).thenReturn(Map.of("baseAmount", 100));
        when(calculationClient.calculate("ACME", Map.of("baseAmount", 100))).thenReturn(Map.of("total", 120));

        OrchestrationResponse response = service()
                .orchestrate(new OrchestrationRequest(Client.ACME, Map.of("amount", 100)));

        InOrder order = inOrder(validationClient, calculationAdapterClient, calculationClient);
        order.verify(validationClient).validate(anyString(), anyMap());
        order.verify(calculationAdapterClient).adapt(anyString(), anyMap());
        order.verify(calculationClient).calculate(anyString(), anyMap());

        assertThat(response.executedComponents())
                .containsExactly("validation", "calculation-adapter", "calculation");
        assertThat(response.result()).containsEntry("total", 120);
    }

    @Test
    void stopsAtTheFirstFailingComponent() {
        when(validationClient.validate(anyString(), anyMap()))
                .thenThrow(new ComponentInvocationException("validation", HttpStatus.UNPROCESSABLE_ENTITY,
                        "currency is required"));

        assertThatThrownBy(() -> service().orchestrate(new OrchestrationRequest(Client.ACME, Map.of("amount", 100))))
                .isInstanceOf(ComponentInvocationException.class)
                .hasMessageContaining("currency is required");

        verify(calculationAdapterClient, never()).adapt(anyString(), anyMap());
        verify(calculationClient, never()).calculate(anyString(), anyMap());
    }
}
