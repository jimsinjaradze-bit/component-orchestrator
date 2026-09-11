package com.solveva.component_orchestrator.orchestration;

import com.solveva.component_orchestrator.billing.InsufficientTokensException;
import com.solveva.component_orchestrator.component.ComponentInvocationException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrations")
public class OrchestrationController {

    private final OrchestrationService orchestrationService;

    OrchestrationController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping
    public OrchestrationResponse orchestrate(@Valid @RequestBody OrchestrationRequest request) {
        return orchestrationService.orchestrate(request);
    }

    @ExceptionHandler(ComponentInvocationException.class)
    ProblemDetail handleComponentFailure(ComponentInvocationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setProperty("component", ex.getComponent());
        return problem;
    }

    @ExceptionHandler(InsufficientTokensException.class)
    ProblemDetail handleInsufficientTokens(InsufficientTokensException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
        problem.setProperty("component", ex.getComponent());
        problem.setProperty("required", ex.getRequired());
        problem.setProperty("available", ex.getAvailable());
        return problem;
    }
}
