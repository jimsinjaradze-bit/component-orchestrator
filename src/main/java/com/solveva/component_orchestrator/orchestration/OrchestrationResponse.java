package com.solveva.component_orchestrator.orchestration;

import java.util.List;
import java.util.Map;

import com.solveva.component_orchestrator.billing.Client;

public record OrchestrationResponse(
        Client client,
        List<String> executedComponents,
        Map<String, Long> durationsMs,
        Map<String, Object> result,
        long remainingTokens) {
}
