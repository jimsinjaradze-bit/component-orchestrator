package com.solveva.component_orchestrator.billing;

/**
 * Raised when a client cannot pay for the component about to be executed.
 */
public class InsufficientTokensException extends RuntimeException {

    private final Client client;
    private final String component;
    private final long required;
    private final long available;

    public InsufficientTokensException(Client client, String component, long required, long available) {
        super("Client '" + client + "' needs " + required + " tokens to run '" + component
                + "' but only has " + available);
        this.client = client;
        this.component = component;
        this.required = required;
        this.available = available;
    }

    public Client getClient() {
        return client;
    }

    public String getComponent() {
        return component;
    }

    public long getRequired() {
        return required;
    }

    public long getAvailable() {
        return available;
    }
}
