package dev.cayatceek.accessguard;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Function;

/** Executes an API request and returns the HTTP status code. */
@FunctionalInterface
public interface AccessProbe {
    int execute(Scenario scenario) throws Exception;

    /** Creates a real HTTP probe; do not follow redirects when checking authorization. */
    static AccessProbe http(HttpClient client, Function<Scenario, HttpRequest> requestFactory) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(requestFactory, "requestFactory");
        if (client.followRedirects() != HttpClient.Redirect.NEVER) {
            throw new IllegalArgumentException("Authorization probes must not follow redirects");
        }
        return scenario -> client.send(Objects.requireNonNull(requestFactory.apply(scenario), "request"),
            HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
