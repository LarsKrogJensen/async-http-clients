package clients;

import org.asynchttpclient.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class KazbiHttpClient {
    private final AsyncHttpClient client;
    private final int retryAttempts;
    private final long retryDelayMs;
    private final Function<String, RequestBuilder> requestFactory;
    private final Supplier<Optional<String>> serviceLookup;
    private final Function<String, CircuitBreaker> circuitBreakerFactory;

    public KazbiHttpClient(AsyncHttpClient client,
                           int retryAttempts,
                           long retryDelayMs,
                           Function<String, RequestBuilder> requestFactory,
                           Supplier<Optional<String>> serviceLookup,
                           Function<String, CircuitBreaker> circuitBreakerFactory) {
        this.client = requireNonNull(client, "Client null");
        this.retryAttempts = retryAttempts;
        this.retryDelayMs = retryDelayMs;
        this.requestFactory = requireNonNull(requestFactory, "Request factort null");
        this.serviceLookup = requireNonNull(serviceLookup, "Service lookup null");
        this.circuitBreakerFactory = requireNonNull(circuitBreakerFactory, "Circuit breaker null");
    }

    public CompletableFuture<Response> execute() {
        AsyncRetry retry = new AsyncRetry(retryAttempts, retryDelayMs);
        return retry.execute(this::run);
    }

    private CompletableFuture<Response> run() {
        return serviceLookup.get().map(baseUrl -> {
            RequestBuilder requestBuilder = requestFactory.apply(baseUrl);
            return circuitBreakerFactory.apply(baseUrl).executeAsync(() -> client.executeRequest(requestBuilder).toCompletableFuture());
        }).orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException("Service not available in lookup")));
    }

    public static Builder kazbiClient(KazbiHttpClient kazbiHttpClient) {
        return new Builder()
                .withRetryAttempts(kazbiHttpClient.retryAttempts)
                .withRequestFactory(kazbiHttpClient.requestFactory)
                .withAsyncClient(kazbiHttpClient.client)
                .withServiceLookup(kazbiHttpClient.serviceLookup)
                .withCircuitBreakerFactory(kazbiHttpClient.circuitBreakerFactory);
    }

    public static Builder kazbiClient() {
        return new Builder();
    }

    public static class Builder {
        private AsyncHttpClient asyncClient;
        private int retryAttempts = 3;
        private long retryDelayMs = 1_000;
        private Function<String, RequestBuilder> requestFactory = Dsl::get;
        private Supplier<Optional<String>> serviceLookup;
        private Function<String, CircuitBreaker> circuitBreakerFactory;


        public Builder withRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public Builder withRetryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public Builder withRequestFactory(Function<String, RequestBuilder> requestFactory) {
            this.requestFactory = requestFactory;
            return this;
        }

        public Builder withAsyncClient(AsyncHttpClient client) {
            this.asyncClient = client;
            return this;
        }

        public Builder withServiceLookup(Supplier<Optional<String>> serviceLookup) {
            this.serviceLookup = serviceLookup;
            return this;
        }

        public Builder withCircuitBreakerFactory(Function<String, CircuitBreaker> circuitBreakerFactory) {
            this.circuitBreakerFactory = circuitBreakerFactory;
            return this;
        }

        public KazbiHttpClient build() {
            return new KazbiHttpClient(
                    asyncClient,
                    retryAttempts,
                    retryDelayMs,
                    requestFactory,
                    serviceLookup,
                    circuitBreakerFactory);
        }

        public CompletableFuture<Response> execute() {
            return build().execute();
        }
    }

}
