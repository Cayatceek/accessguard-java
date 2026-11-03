package dev.cayatceek.accessguard;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccessGuardTest {
    private static final Actor OWNER = new Actor("alice", "tenant-A", "USER");
    private static final Actor STRANGER = new Actor("bob", "tenant-B", "TENANT_ADMIN");
    private static final Actor ADMIN = new Actor("carol", "tenant-A", "TENANT_ADMIN");
    private static final Resource RECORD = new Resource("doc-1", "tenant-A", "alice");

    @Test
    void generatesCrossTenantOwnerAndAdminCases() {
        List<Scenario> cases = ScenarioGenerator.generate(
            List.of(OWNER, STRANGER, ADMIN), List.of(RECORD), List.of(Operation.READ),
            ScenarioGenerator.ownerOrTenantAdmin());
        assertEquals(3, cases.size());
        assertTrue(cases.get(0).allowed());
        assertFalse(cases.get(1).allowed());
        assertTrue(cases.get(2).allowed());
    }

    @Test
    void detectsBrokenObjectLevelAuthorization() {
        List<Scenario> cases = ScenarioGenerator.generate(
            List.of(OWNER, STRANGER), List.of(RECORD), List.of(Operation.READ),
            ScenarioGenerator.ownerOrTenantAdmin());
        AccessReport report = AccessGuard.verify(cases, ignored -> 200);
        assertEquals(1, report.violations());
        assertThrows(AssertionError.class, report::assertSecure);
    }

    @Test
    void acceptsDenialsAndAllowedCases() {
        List<Scenario> cases = ScenarioGenerator.generate(
            List.of(OWNER, STRANGER), List.of(RECORD), List.of(Operation.READ),
            ScenarioGenerator.ownerOrTenantAdmin());
        AccessReport report = AccessGuard.verify(cases,
            scenario -> scenario.allowed() ? 200 : 404);
        assertEquals(0, report.violations());
        assertEquals(0, report.inconclusive());
        report.assertSecure();
    }

    @Test
    void failsClosedOnServerErrorsRedirectsAndProbeExceptions() {
        Scenario denied = new Scenario(STRANGER, RECORD, Operation.READ, false);
        assertEquals(1, AccessGuard.verify(List.of(denied), s -> 500).inconclusive());
        assertEquals(1, AccessGuard.verify(List.of(denied), s -> 302).inconclusive());
        AccessReport report = AccessGuard.verify(List.of(denied), s -> {
            throw new java.io.IOException("sensitive details should not be copied");
        });
        assertEquals(1, report.inconclusive());
        assertFalse(report.results().getFirst().explanation().contains("sensitive details"));
    }

    @Test
    void rejectsUnboundedCrossProducts() {
        var hugeActors = java.util.Collections.nCopies(10001, OWNER);
        assertThrows(IllegalArgumentException.class, () -> ScenarioGenerator.generate(
            hugeActors, List.of(RECORD), List.of(Operation.READ), ScenarioGenerator.ownerOrTenantAdmin()));
    }
}
