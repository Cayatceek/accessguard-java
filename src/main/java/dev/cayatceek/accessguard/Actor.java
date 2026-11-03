package dev.cayatceek.accessguard;

import java.util.Objects;

/** Identity used by a test case; credentials remain the caller's responsibility. */
public record Actor(String id, String tenantId, String role) {
    public Actor {
        id = require(id, "id");
        tenantId = require(tenantId, "tenantId");
        role = require(role, "role");
    }

    static String require(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
