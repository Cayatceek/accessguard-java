package dev.cayatceek.accessguard;

/** Protected resource owned by an actor in a tenant. */
public record Resource(String id, String tenantId, String ownerId) {
    public Resource {
        id = Actor.require(id, "id");
        tenantId = Actor.require(tenantId, "tenantId");
        ownerId = Actor.require(ownerId, "ownerId");
    }
}
