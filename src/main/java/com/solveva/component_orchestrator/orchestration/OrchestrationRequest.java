package com.solveva.component_orchestrator.orchestration;

import java.util.Map;

import com.solveva.component_orchestrator.billing.Client;

import jakarta.validation.constraints.NotNull;

public record OrchestrationRequest(
        @NotNull Client client, // who requests the execution
        @NotNull Map<String, Object> payload) {
}
