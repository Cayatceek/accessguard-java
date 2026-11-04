# AccessGuard (Java)

Model-based test generation for **REST API authorization boundaries**. AccessGuard creates actor × resource × operation scenarios using a reference policy, probes your API and reports unexpected access, including BOLA/IDOR and tenant-isolation failures.

**Status:** 0.1.0-SNAPSHOT developer preview. Not published to Maven Central. **Java 21**, no runtime dependencies.

## Quick start

```java
var alice = new Actor("alice", "org-A", "USER");
var bob = new Actor("bob", "org-B", "USER");
var document = new Resource("doc-123", "org-A", "alice");

var scenarios = ScenarioGenerator.generate(
    List.of(alice, bob), List.of(document), List.of(Operation.READ),
    ScenarioGenerator.ownerOrTenantAdmin());

AccessProbe probe = scenario -> {
    // Build a request using the corresponding test identity's credentials.
    // Return the HTTP status code from your test server.
    return callApiAs(scenario.actor(), scenario.resource()).statusCode();
};

AccessGuard.verify(scenarios, probe).assertSecure();
```

The built-in `AccessProbe.http(HttpClient, requestFactory)` performs real HTTP requests using a caller-supplied `HttpRequest` factory. Credentials are never held in the scenario or report classes.

## Safety guarantees added during validation

- An empty scenario set fails instead of reporting a misleading success.
- Scenario generation rejects oversized Cartesian products before integer overflow.
- HTTP adapters reject clients configured to follow redirects; a redirect to a 200 page must not mask a denial.
- HTTP 404 is treated as a denial only under the caller's reference policy; it can also represent a missing endpoint. Confirm resource fixtures exist.

## What it checks

- Allowed operations: HTTP 2xx is a pass; 401/403/404 is an authorization failure.
- Forbidden operations: 401/403/404 is a pass; HTTP 2xx indicates an authorization failure.
- Redirects, 400s, server errors and transport exceptions are **inconclusive** and fail `assertSecure()`.
- `ScenarioGenerator.ownerOrTenantAdmin()` is only an example policy: replace it with your application's independently documented authorization requirements.

## Safety

**Run against isolated test or staging environments only.** Authorization tests can mutate data when running `UPDATE` or `DELETE` scenarios; use disposable fixtures, synthetic users and resources. Start with `Operation.READ`. The SDK does not discover live endpoints, acquire credentials, or bypass security controls.

## Development

```bash
mvn verify
mvn install
```

See [architecture](docs/ARCHITECTURE.md) and [roadmap](docs/ROADMAP.md).

GPL-3.0; consult [LICENSE](LICENSE). The choice of GPL may affect adoption by proprietary clients.
