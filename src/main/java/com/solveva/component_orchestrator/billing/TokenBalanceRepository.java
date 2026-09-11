package com.solveva.component_orchestrator.billing;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * In-memory store of the token balance of every client.
 */
@Repository
public class TokenBalanceRepository {

    private final Map<Client, Long> balances = new EnumMap<>(Client.class);

    public TokenBalanceRepository() {
        balances.put(Client.ACME, 1_000L);
        balances.put(Client.GLOBEX, 1_000L);
        balances.put(Client.INITECH, 50L);
    }

    public long find(Client client) {
        return balances.get(client);
    }

    public void save(Client client, long tokens) {
        balances.put(client, tokens);
    }
}
