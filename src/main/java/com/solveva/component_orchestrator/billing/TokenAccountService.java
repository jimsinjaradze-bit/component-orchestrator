package com.solveva.component_orchestrator.billing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Charges a client for running a component.
 */
@Service
public class TokenAccountService {

    private static final Logger log = LoggerFactory.getLogger(TokenAccountService.class);

    private final TokenBalanceRepository balances;
    private final ComponentCostRepository costs;

    TokenAccountService(TokenBalanceRepository balances, ComponentCostRepository costs) {
        this.balances = balances;
        this.costs = costs;
    }

    public long charge(Client client, String component) {
        long cost = costs.find(component).tokens();

        long available = balances.find(client);

        if (available < cost) {
            throw new InsufficientTokensException(client, component, cost, available);
        }

        long remaining = available - cost;
        balances.save(client, remaining);

        log.info("[{}] charged {} tokens for {}, {} left", client, cost, component, remaining);
        return remaining;
    }

    public long balanceOf(Client client) {
        return balances.find(client);
    }
}
