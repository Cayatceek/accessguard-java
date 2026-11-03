package dev.cayatceek.accessguard;

/** A reference access policy supplied by the testing team, not the application under test. */
@FunctionalInterface
public interface AccessRule {
    boolean allows(Actor actor, Resource resource, Operation operation);
}
