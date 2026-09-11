package com.solveva.component_orchestrator.billing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * In-memory store of the token cost of every component.
 */
@Repository
public class ComponentCostRepository {

    private final Map<String, ComponentCost> costs = new LinkedHashMap<>();

    public ComponentCostRepository() {
        costs.put("validation", new ComponentCost("validation", 5));
        costs.put("calculation-adapter", new ComponentCost("calculation-adapter", 10));
        costs.put("calculation", new ComponentCost("calculation", 20));
    }

    public ComponentCost find(String component) {
        ComponentCost cost = costs.get(component);
        if (cost == null) {
            throw new IllegalArgumentException("No token cost configured for component '" + component + "'");
        }
        return cost;
    }
}
