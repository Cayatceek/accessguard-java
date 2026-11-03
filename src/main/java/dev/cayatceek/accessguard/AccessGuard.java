package dev.cayatceek.accessguard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Executes generated authorization cases against the supplied (test) API. */
public final class AccessGuard {
    private AccessGuard() { }

    public static AccessReport verify(List<Scenario> scenarios, AccessProbe probe) {
        Objects.requireNonNull(scenarios, "scenarios");
        Objects.requireNonNull(probe, "probe");
        List<CaseResult> results = new ArrayList<>();
        for (Scenario scenario : scenarios) {
            Objects.requireNonNull(scenario, "scenario");
            try {
                int status = probe.execute(scenario);
                results.add(classify(scenario, status));
            } catch (Exception ex) {
                results.add(new CaseResult(scenario, null, CaseResult.Verdict.INCONCLUSIVE,
                    "Probe failed: " + ex.getClass().getSimpleName()));
            }
        }
        return new AccessReport(results);
    }

    private static CaseResult classify(Scenario scenario, int status) {
        boolean success = status >= 200 && status < 300;
        boolean denied = status == 401 || status == 403 || status == 404;
        if (success == scenario.allowed() && (success || denied)) {
            return new CaseResult(scenario, status, CaseResult.Verdict.PASS,
                "Observed HTTP " + status + " as expected");
        }
        if (success || denied) {
            return new CaseResult(scenario, status, CaseResult.Verdict.VIOLATION,
                "Expected " + (scenario.allowed() ? "allowed" : "denied") + ", observed HTTP " + status);
        }
        return new CaseResult(scenario, status, CaseResult.Verdict.INCONCLUSIVE,
            "HTTP " + status + " does not establish an authorization decision");
    }
}
