import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) throws Exception {
        var config = config();
        try (AsyncHttpClient client = asyncHttpClient(config)) {
//            String result = asyncHttpClient
//                    .prepareGet("http://httpbin.org/delay/1")
//                    .execute()
//                    .toCompletableFuture()
//                    .thenApplyAsync(AsyncHttpClient::transformResponse)
//                    .exceptionally(error ->  "eerrroor")
//                    .handle()
//                    .whenComplete((body, error) -> {
//                        if (error == null) {
//                            System.out.println(body);
//                        } else {
//                            System.out.println("Error: " + error.getMessage());
//                        }
//                    })
//                    .handle((body, error) -> {
//                        if (error != null) {
//                            return "hanlde error: " + error.getMessage();
//                        }
//                        return body;
//                    })
////                    .completeOnTimeout("Tiiiimedout", 1, TimeUnit.MILLISECONDS)
////                    .orTimeout(1, TimeUnit.MICROSECONDS)
//                    .exceptionally(error -> "eerrroor: " + error.getMessage())
            String result = retry(() -> fetch(client), 3).join();
            System.out.println("Result: " + result);
        }
    }

    private static String transformResponse(Response response) {
//        throw new RuntimeException("oh");
        return response.getResponseBody();
    }

    private static CompletableFuture<String> fetch(AsyncHttpClient client) {
        return client
                .prepareGet("http://httpbin.org/delay/1")
                .execute()
                .toCompletableFuture()
                .thenApplyAsync(NingAsyncHttpClient::transformResponse);
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
                   System.out.println("Failed with error: " + error.getMessage());
                   retry(action, attempts - 1, promise);
               } else {
                   promise.completeExceptionally(error);
               }
           } else {
               promise.complete(result);
           }
        });
    }


}
