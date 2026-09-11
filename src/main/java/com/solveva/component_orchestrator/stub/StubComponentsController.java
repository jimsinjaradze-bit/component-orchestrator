package com.solveva.component_orchestrator.stub;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Stand-ins for the three services the orchestrator talks to, so the app runs on its own.
 * They are deliberately dumb: each one only knows the fields it needs and rejects the
 * request when they are missing.
 */
@RestController
@RequestMapping("/stub")
public class StubComponentsController {

    @PostMapping("/validation")
    Map<String, Object> validate(@RequestBody Map<String, Object> payload) {
        BigDecimal amount = number(payload, "amount");
        if (amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "amount must be positive");
        }
        if (!(payload.get("currency") instanceof String currency) || currency.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "currency is required");
        }
        Map<String, Object> result = new LinkedHashMap<>(payload);
        result.put("validated", true);
        return result;
    }

    @PostMapping("/calculation-adapter")
    Map<String, Object> adapt(@RequestBody Map<String, Object> payload) {
        BigDecimal amount = number(payload, "amount");
        if (!(payload.get("currency") instanceof String currency) || currency.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "currency is required");
        }
        Map<String, Object> result = new LinkedHashMap<>(payload);
        result.put("baseAmount", amount);
        result.put("currencyCode", currency.toUpperCase());
        result.put("taxRate", new BigDecimal("0.20"));
        return result;
    }

    @PostMapping("/calculation")
    Map<String, Object> calculate(@RequestHeader(name = "X-Api-Key", required = false) String apiKey,
                                  @RequestBody Map<String, Object> payload) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Api-Key is required");
        }
        BigDecimal baseAmount = number(payload, "baseAmount");
        BigDecimal taxRate = number(payload, "taxRate");
        Map<String, Object> result = new LinkedHashMap<>(payload);
        result.put("tax", baseAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP));
        result.put("total", baseAmount.add(baseAmount.multiply(taxRate)).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    private static BigDecimal number(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, field + " is required");
    }
}
