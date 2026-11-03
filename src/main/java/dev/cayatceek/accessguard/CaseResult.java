package dev.cayatceek.accessguard;

import java.util.Objects;

public record CaseResult(Scenario scenario, Integer statusCode, Verdict verdict, String explanation) {
    public enum Verdict { PASS, VIOLATION, INCONCLUSIVE }

    public CaseResult {
        Objects.requireNonNull(scenario, "scenario");
        Objects.requireNonNull(verdict, "verdict");
        Objects.requireNonNull(explanation, "explanation");
    }
}
