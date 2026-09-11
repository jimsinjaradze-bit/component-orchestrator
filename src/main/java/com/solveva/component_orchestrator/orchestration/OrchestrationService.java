package com.solveva.component_orchestrator.orchestration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.solveva.component_orchestrator.billing.Client;
import com.solveva.component_orchestrator.billing.TokenAccountService;
import com.solveva.component_orchestrator.component.CalculationAdapterClient;
import com.solveva.component_orchestrator.component.CalculationClient;
import com.solveva.component_orchestrator.component.ComponentInvocationException;
import com.solveva.component_orchestrator.component.ValidationClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Calls the three components over HTTP, one after the other, feeding each one the
 * payload the previous one returned. Every component is paid for with the client's
 * tokens before it runs.
 */
@Service
public class OrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    private final ValidationClient validationClient;
    private final CalculationAdapterClient calculationAdapterClient;
    private final CalculationClient calculationClient;
    private final TokenAccountService tokenAccountService;

    OrchestrationService(ValidationClient validationClient,
                         CalculationAdapterClient calculationAdapterClient,
                         CalculationClient calculationClient,
                         TokenAccountService tokenAccountService) {
        this.validationClient = validationClient;
        this.calculationAdapterClient = calculationAdapterClient;
        this.calculationClient = calculationClient;
        this.tokenAccountService = tokenAccountService;
    }

    public OrchestrationResponse orchestrate(OrchestrationRequest request) {
        Client client = request.client();
        String clientId = client.name();
        List<String> executed = new ArrayList<>();
        Map<String, Long> durations = new LinkedHashMap<>();
        Map<String, Object> payload = request.payload();
        long startedAt;
        long remainingTokens;

        log.info("[{}] calling validation", clientId);
        remainingTokens = tokenAccountService.charge(client, "validation");
        startedAt = System.currentTimeMillis();
        try {
            payload = validationClient.validate(clientId, payload);
        } catch (ComponentInvocationException ex) {
            log.warn("[{}] validation failed: {}", clientId, ex.getMessage());
            throw ex;
        }
        durations.put("validation", System.currentTimeMillis() - startedAt);
        executed.add("validation");

        log.info("[{}] calling calculation-adapter", clientId);
        remainingTokens = tokenAccountService.charge(client, "calculation-adapter");
        startedAt = System.currentTimeMillis();
        try {
            payload = calculationAdapterClient.adapt(clientId, payload);
        } catch (ComponentInvocationException ex) {
            log.warn("[{}] calculation-adapter failed: {}", clientId, ex.getMessage());
            throw ex;
        }
        durations.put("calculation-adapter", System.currentTimeMillis() - startedAt);
        executed.add("calculation-adapter");

        log.info("[{}] calling calculation", clientId);
        remainingTokens = tokenAccountService.charge(client, "calculation");
        startedAt = System.currentTimeMillis();
        try {
            payload = calculationClient.calculate(clientId, payload);
        } catch (ComponentInvocationException ex) {
            log.warn("[{}] calculation failed: {}", clientId, ex.getMessage());
            throw ex;
        }
        durations.put("calculation", System.currentTimeMillis() - startedAt);
        executed.add("calculation");

        log.info("[{}] finished: {}, {} tokens left", clientId, executed, remainingTokens);
        return new OrchestrationResponse(client, List.copyOf(executed), Map.copyOf(durations), payload,
                remainingTokens);
    }
}
