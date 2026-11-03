package dev.cayatceek.accessguard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Generates a bounded cross-product from explicit reference authorization rules. */
public final class ScenarioGenerator {
    public static final int MAX_SCENARIOS = 10_000;

    private ScenarioGenerator() { }

    /** Same-tenant owner access; a tenant administrator can access resources in their own tenant. */
    public static AccessRule ownerOrTenantAdmin() {
        return (actor, resource, operation) ->
            actor.tenantId().equals(resource.tenantId())
                && (actor.id().equals(resource.ownerId()) || actor.role().equals("TENANT_ADMIN"));
    }

    public static List<Scenario> generate(List<Actor> actors, List<Resource> resources,
                                           List<Operation> operations, AccessRule policy) {
        Objects.requireNonNull(actors, "actors");
        Objects.requireNonNull(resources, "resources");
        Objects.requireNonNull(operations, "operations");
        Objects.requireNonNull(policy, "policy");
        if (actors.isEmpty() || resources.isEmpty() || operations.isEmpty()) {
            throw new IllegalArgumentException("actors, resources and operations must not be empty");
        }
        long possible = (long) actors.size() * resources.size() * operations.size();
        if (possible > MAX_SCENARIOS) {
            throw new IllegalArgumentException("Too many scenarios (" + possible + "); limit: " + MAX_SCENARIOS);
        }
        List<Scenario> scenarios = new ArrayList<>((int) possible);
        for (Actor actor : actors) {
            for (Resource resource : resources) {
                for (Operation operation : operations) {
                    scenarios.add(new Scenario(actor, resource, operation,
                        policy.allows(actor, resource, operation)));
                }
            }
        }
        return List.copyOf(scenarios);
    }
}
