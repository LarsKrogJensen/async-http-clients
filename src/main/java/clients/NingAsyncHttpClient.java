package clients;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.Optional;

import static clients.KazbiHttpClient.kazbiClient;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) {
        var config = config();

        // setup once
        AsyncHttpClient asyncClient = asyncHttpClient(config);
        var template = kazbiClient()
                .withAsyncClient(asyncClient)
                .withServiceLookup(() -> Optional.of("http://httpbin.org"))
                .withCircuitBreakerFactory(baseUrl -> new CircuitBreakerImpl())
                .withRetryAttempts(10)
                .withRetryDelayMs(1_000)
                .build();

        // Per request
        kazbiClient(template)
                .withRequestFactory(baseUrl -> Dsl.get(baseUrl + "/delay/1").setReadTimeout(500))
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
