# Architecture

AccessGuard deliberately separates the **reference authorization model** from the implementation being tested. It creates bounded test cases and calls a user-supplied probe.

- `Actor`, `Resource`, `Operation` are typed inputs; no access tokens are stored.
- `AccessRule` describes independently expected permissions.
- `ScenarioGenerator` performs a limited Cartesian product (10,000 maximum) to avoid accidental unbounded test generation.
- `AccessProbe` can call a local service, integration test fixture or HTTP API.
- `AccessReport` preserves observed cases and fails closed on inconclusive responses.

## Limitations

It does not prove comprehensive authorization security. Tests cover only supplied identities, resources, operations and reference policies. HTTP 404 can be a valid denial, but may also hide unrelated failures. Add data assertions when necessary. No OpenAPI parsing, role hierarchy discovery, browser login or CI report export in v0.1.
