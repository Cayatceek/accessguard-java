package dev.cayatceek.accessguard;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Integration test of the real java.net.http adapter against a local HTTP server. */
class HttpAccessProbeTest {
    @Test
    void refusesClientsThatSilentlyFollowAuthenticationRedirects() {
        try (HttpClient redirecting = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS).build()) {
            assertThrows(IllegalArgumentException.class,
                () -> AccessProbe.http(redirecting, ignored ->
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1/")).GET().build()));
        }
    }

    @Test
    void evaluatesRealHttpResponsesForTwoActors() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/documents/doc-1", exchange -> {
            String actor = exchange.getRequestHeaders().getFirst("X-Test-Actor");
            int status = "alice".equals(actor) ? 200 : 403;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        try {
            URI target = URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/documents/doc-1");
            var alice = new Actor("alice", "org-A", "USER");
            var bob = new Actor("bob", "org-B", "USER");
            var document = new Resource("doc-1", "org-A", "alice");
            var scenarios = ScenarioGenerator.generate(
                List.of(alice, bob), List.of(document), List.of(Operation.READ),
                ScenarioGenerator.ownerOrTenantAdmin());

            try (HttpClient http = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NEVER).build()) {
                AccessProbe probe = AccessProbe.http(http, scenario ->
                    HttpRequest.newBuilder(target)
                        .header("X-Test-Actor", scenario.actor().id())
                        .GET()
                        .build());
                var report = AccessGuard.verify(scenarios, probe);
                assertEquals(2, report.results().size());
                assertEquals(0, report.violations());
                assertEquals(0, report.inconclusive());
                report.assertSecure();
            }
        } finally {
            server.stop(0);
        }
    }
}
