package clients;

import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.delayedExecutor;

class AsyncRetryer {
    private static final Logger log = LoggerFactory.getLogger(AsyncRetryer.class);
    private final int attempts;
    private final long delayMs;
    private Uri uri;

    AsyncRetryer(RetryOptions options) {
        this.attempts = options.attempts;
        this.delayMs = options.delayMs;
    }

    void currentUri(Uri uri) {
        this.uri = uri;
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
                    log.warn("Retryer failed for '{}' with error: {} will try {} more time(s) in {} ms.", uri.toUrl(), error, attempts, delayMs);
                    if (delayMs > 0) {
                        delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> retry(action, attempts - 1, promise));
                    } else {
                        retry(action, attempts - 1, promise);
                    }
                } else {
                    log.error("Retryer failed for '{}' with error: {} giving up after {} attempts.", uri.toUrl(), error.getMessage(), this.attempts);
                    promise.completeExceptionally(error);
                }
            } else {
                promise.complete(result);
            }
        });
    }


}
