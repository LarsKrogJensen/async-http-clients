package clients;

import org.asynchttpclient.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.failedFuture;

public class KazbiAsyncHttpClient {
    private final AsyncHttpClient client;
    private final RetryOptions retryOptions;
    private final Function<String, RequestBuilder> requestFactory;
    private final Supplier<Optional<String>> serviceLookup;
    private final Function<String, CircuitBreaker> circuitBreakerFactory;

    public KazbiAsyncHttpClient(AsyncHttpClient client,
                                RetryOptions retryOptions,
                                Function<String, RequestBuilder> requestFactory,
                                Supplier<Optional<String>> serviceLookup,
                                Function<String, CircuitBreaker> circuitBreakerFactory) {
        this.client = requireNonNull(client, "Client null");
        this.retryOptions = requireNonNull(retryOptions, "Retry options null");
        this.requestFactory = requireNonNull(requestFactory, "Request factort null");
        this.serviceLookup = requireNonNull(serviceLookup, "Service lookup null");
        this.circuitBreakerFactory = requireNonNull(circuitBreakerFactory, "Circuit breaker null");
    }

    public CompletableFuture<Response> execute() {
        AsyncRetry retry = new AsyncRetry(retryOptions);
        return retry.execute(this::run);
    }

    private CompletableFuture<Response> run() {
        return serviceLookup.get().map(baseUrl -> {
            RequestBuilder requestBuilder = requestFactory.apply(baseUrl);
            return circuitBreakerFactory.apply(baseUrl).executeAsync(() -> client.executeRequest(requestBuilder).toCompletableFuture());
        }).orElseGet(() -> failedFuture(new RuntimeException("Service not available in lookup")));
    }

    public static Builder kazbiClient(KazbiAsyncHttpClient kazbiHttpClient) {
        return new Builder()
                .withRetryOptions(kazbiHttpClient.retryOptions)
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
        private RetryOptions retryOptions = new RetryOptions(3, 1_000);
        private Function<String, RequestBuilder> requestFactory = Dsl::get;
        private Supplier<Optional<String>> serviceLookup;
        private Function<String, CircuitBreaker> circuitBreakerFactory;


        public Builder withRetryOptions(RetryOptions retryOptions) {
            this.retryOptions = retryOptions;
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

        public KazbiAsyncHttpClient build() {
            return new KazbiAsyncHttpClient(
                    asyncClient,
                    retryOptions,
                    requestFactory,
                    serviceLookup,
                    circuitBreakerFactory);
        }

        public CompletableFuture<Response> execute() {
            return build().execute();
        }
    }

}
