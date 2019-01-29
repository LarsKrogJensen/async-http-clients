package clients;

import ch.qos.logback.core.util.CloseUtil;
import com.spotify.futures.CompletableFutures;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static clients.KazbiAsyncHttpClient.fromKazbiClient;
import static clients.KazbiAsyncHttpClient.newKazbiClient;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) {
        var config = config().setThreadPoolName("MYCLIENT");

        // setup once
        AsyncHttpClient asyncClient = asyncHttpClient(config);
        var template = newKazbiClient()
                .withAsyncClient(asyncClient)
                .withRetryOptions(new RetryOptions(10, 1_000))
                .withServiceLookup(() -> Optional.of("http://httpbin.org"))
                .withCircuitBreakerFactory(baseUrl -> new CircuitBreakerImpl())
                .build();

        // Per request
        List<CompletableFuture<String>> futures = IntStream.range(0, 100).mapToObj(id -> runRequest(template, id)).collect(Collectors.toList());

        CompletableFutures.allAsList(futures)
                .whenComplete((response, error) -> {
                    System.out.println("Done on thread " + Thread.currentThread().getName());
                    CloseUtil.closeQuietly(asyncClient);
                });


//        CompletableFutures.exceptionallyCompose()
    }

    private static CompletableFuture<String> runRequest(KazbiAsyncHttpClient template, int id) {
        return fromKazbiClient(template)
                .withRequestFactory(baseUrl -> Dsl.get(baseUrl + "/delay/1"))
                .execute()
                .thenApply(Response::getResponseBody)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        System.out.println("ERROR: " + error.getMessage());
                    } else {
                        System.out.println(id + " - OK on thread " + Thread.currentThread().getName());
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
                    }
                });
    }
}
