package dev.cayatceek.accessguard;

import java.util.Objects;

/** Expected authorization decision for a single actor/resource/operation combination. */
public record Scenario(Actor actor, Resource resource, Operation operation, boolean allowed) {
    public Scenario {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(operation, "operation");
    }

    public String label() {
        return actor.id() + " / " + resource.id() + " / " + operation;
    }
}
