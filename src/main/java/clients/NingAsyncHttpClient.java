package clients;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.Optional;

import static clients.KazbiAsyncHttpClient.fromKazbiClient;
import static clients.KazbiAsyncHttpClient.newKazbiClient;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) {
        var config = config();

        // setup once
        AsyncHttpClient asyncClient = asyncHttpClient(config);
        var template = newKazbiClient()
                .withAsyncClient(asyncClient)
                .withRetryOptions(new RetryOptions(10, 1_000))
                .withServiceLookup(() -> Optional.of("http://httpbinm.org"))
                .withCircuitBreakerFactory(baseUrl -> new CircuitBreakerImpl())
                .build();

        // Per request
        fromKazbiClient(template)
                .withRequestFactory(baseUrl -> Dsl.get(baseUrl + "/delay/1").setReadTimeout(5_000))
                .execute()
                .thenApply(Response::getResponseBody)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        System.out.println("ERROR: " + error.getMessage());
                    } else {
                        System.out.println("OK: " + result);
                    }
                });
    }
}
