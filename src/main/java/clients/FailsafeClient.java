package clients;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class FailsafeClient {
    private final AsyncHttpClient client;
    private final int retryAttempts;
    private final Function<String, RequestBuilder> requestFactory;
    private final Supplier<Optional<String>> serviceLookup;
    private final Function<String, CircuitBreaker> circuitBreakerFactory;

    public FailsafeClient(AsyncHttpClient client,
                          int retryAttempts,
                          Function<String, RequestBuilder> requestFactory,
                          Supplier<Optional<String>> serviceLookup,
                          Function<String, CircuitBreaker> circuitBreakerFactory) {
        this.client = client;
        this.retryAttempts = retryAttempts;
        this.requestFactory = requestFactory;
        this.serviceLookup = serviceLookup;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public CompletableFuture<Response> execute() {

        Retry retry = new Retry(retryAttempts);
        return retry.execute(() -> )
    }

    private CompletableFuture<String> 

    public static Builder failSafeClient(FailsafeClient failsafeClient) {
        return new Builder()
                .withRetryAttempts(failsafeClient.retryAttempts)
                .withRequestFactory(failsafeClient.requestFactory)
                .withClient(failsafeClient.client)
                .withServiceLookup(failsafeClient.serviceLookup)
                .withCircuitBreakerFactory(failsafeClient.circuitBreakerFactory);
    }

    public static Builder failSafeClient() {
        return new Builder();
    }


    private static class Retry {
        private final int attempts;

        private Retry(int attempts) {
            this.attempts = attempts;
        }

        public <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> operation) {
            CompletableFuture<T> promise = new CompletableFuture<>();
            retry(operation, attempts, promise);
            return promise;
        }

        private <T> void retry(Supplier<CompletableFuture<T>> action, int attempts, CompletableFuture<T> promise) {
            action.get().whenComplete((result, error) -> {
                if (error != null) {
                    if (attempts > 0) {
                        System.out.println("Retry: Failed with error: " + error.getMessage() + ", will try " + attempts + " more time(s).");
                        retry(action, attempts - 1, promise);
                    } else {
                        System.out.println("Retry: Giving up");
                        promise.completeExceptionally(error);
                    }
                } else {
                    promise.complete(result);
                }
            });
        }

    }


    public static class Builder {
        private AsyncHttpClient client;
        private int retryAttempts;
        private Function<String, RequestBuilder> requestFactory;
        private Supplier<Optional<String>> serviceLookup;
        private Function<String, CircuitBreaker> circuitBreakerFactory;

        public Builder withClient(AsyncHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder withRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public Builder withRequestFactory(Function<String, RequestBuilder> requestFactory) {
            this.requestFactory = requestFactory;
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

        public FailsafeClient build() {
            return new FailsafeClient(
                    client,
                    retryAttempts,
                    requestFactory,
                    serviceLookup,
                    circuitBreakerFactory);
        }

        public CompletableFuture<Response> execute() {
            return build().execute();
        }
    }

}
