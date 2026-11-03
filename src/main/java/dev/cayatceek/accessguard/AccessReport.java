package dev.cayatceek.accessguard;

import java.util.List;

/** Immutable authorization test report. */
public final class AccessReport {
    private final List<CaseResult> results;

    AccessReport(List<CaseResult> results) {
        this.results = List.copyOf(results);
    }

    public List<CaseResult> results() { return results; }

    public long violations() {
        return results.stream().filter(r -> r.verdict() == CaseResult.Verdict.VIOLATION).count();
    }

    public long inconclusive() {
        return results.stream().filter(r -> r.verdict() == CaseResult.Verdict.INCONCLUSIVE).count();
    }

    /** Fails closed on uncertain results instead of silently passing 5xx or redirect statuses. */
    public void assertSecure() {
        if (violations() != 0 || inconclusive() != 0) {
            StringBuilder msg = new StringBuilder("AccessGuard: ")
                .append(violations()).append(" violations and ").append(inconclusive()).append(" inconclusive cases");
            results.stream().filter(r -> r.verdict() != CaseResult.Verdict.PASS).limit(10)
                .forEach(r -> msg.append("\n- ").append(r.scenario().label())
                    .append(": ").append(r.explanation()));
            throw new AssertionError(msg.toString());
        }
    }
}
