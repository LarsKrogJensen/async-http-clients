import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static ch.qos.logback.core.util.CloseUtil.closeQuietly;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) {
        var config = config();
        AsyncHttpClient client = asyncHttpClient(config);

        Supplier<String> serviceDisco = () -> "http://httpbn.org/delay/1";
        Supplier<CompletableFuture<String>> withRetry = () -> retry(() -> fetch(client, serviceDisco), 3);
        BiPredicate<String, Throwable> evalResult = (result, error) -> true;

        circuitBreaker(withRetry, evalResult)
                .whenComplete((result, error) -> {
                    System.out.println("Result: " + result + ", Error: " + error.getMessage());
                    closeQuietly(client);
                });
    }

    private static CompletableFuture<String> fetch(AsyncHttpClient client, Supplier<String> urlSupplier) {
        try {
            return client
                    .prepareGet(urlSupplier.get())
                    .execute()
                    .toCompletableFuture()
                    .thenApplyAsync(NingAsyncHttpClient::transformResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static String transformResponse(Response response) {
        return response.getResponseBody();
    }

    private static <T> CompletableFuture<T> retry(Supplier<CompletableFuture<T>> action, int attempts) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        retry(action, attempts, promise);
        return promise;
    }

    private static <T> void retry(Supplier<CompletableFuture<T>> action, int attempts, CompletableFuture<T> promise) {
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

    private static <T> CompletableFuture<T> circuitBreaker(Supplier<CompletableFuture<T>> action,
                                                           BiPredicate<T, Throwable> isTrigger) {
        // if open return early
        // ...

        // otherwise run
        return action.get().whenComplete((result, error) -> {
            if (isTrigger.test(result, error)) {
                System.out.println("CircuitBreak: Caught faulty request");
                // handle opening
                // ..
            } else {
                // should open half open CB
            }

            // otherwise we are fine
        });
    }

}
