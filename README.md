# Component orchestrator — per-client pipeline order

## What this is

The service is responsible for orchestrating components for workflows. It gives us a "plug and play" type of
architecture that lets us declare the execution paths modularly.

The current implementation is quite simple — every request goes through the same ordering:
`validation` → `calculation-adapter` → `calculation`

Your task is to take this to the next level. We have many new clients, and releasing a code change for each one is not
feasible, so your target is to build a system that is driven by configuration. The configuration is a file and business
will own it. They have also asked you to write 2 simple JSON files for 2 clients, so they can see examples.

The empty files are in `src/main/resources/schemas/`, one JSON file per client.

There is also a billing part here. We offer a "pay as you go" model, and that is the agreement — clients are charged per
executed component.

To summarize: until now this service only served a single client, and it is up to you to scale it so it can serve
multiple.

## Modification request

**As a** business owner onboarding new clients

**I want** to decide in the JSON files which components run for a client and in what order

**So that** a client with a different flow can go live without waiting for a code change

**Acceptance Criteria**

- **Given** the file for `clientA` lists `validation`, `calculation-adapter`, `calculation`, **When** I send a request
  for `clientA`, **Then** the components run in that order and the response tells me the order that actually ran
- **Given** the file for `clientB` lists `calculation-adapter`,`calculation`, **When** I send a request for `clientB`,
  **Then** that order runs instead, and the code is the same for both clients
- **Given** a file mentions a component we do not have, **When** the service starts, **Then** it tells us and does not
  start, instead of failing later on a real request
- **Given** a request for a client we have no file for, **When** it is sent, **Then** the caller gets a `4xx`
  that explains what is wrong, with no stack trace in the body
- **Given** a client does not have enough tokens for the next component, **When** their request comes in, **Then** we
  tell them they are out of tokens and we do not run the rest of the pipeline

**Definition of Done**

- No ordering is hardcoded in Java anymore, onboarding a client is a configuration change
- When a component fails, the caller can still see which one it was, same as today
- Billing still works after the change and the response still shows what is left
- There are tests, including one for a client whose order is not the default one, and `./mvnw test` passes
- It is in a state you would be happy to put in production# component-orchestrator
