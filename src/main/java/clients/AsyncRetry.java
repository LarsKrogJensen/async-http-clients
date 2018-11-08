package clients;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class AsyncRetry {
    private final int attempts;
    private final long retryDelayMs;

    AsyncRetry(int attempts, long retryDelayMs) {
        this.attempts = attempts;
        this.retryDelayMs = retryDelayMs;
    }

    <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> operation) {
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
