import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class NingAsyncHttpClient {
    public static void main(String[] args) throws Exception {
        var config = config().setIoThreadsCount(2);
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(config)) {
            asyncHttpClient
                    .prepareGet("http://httpbin.org/delay/1")
                    .execute()
                    .toCompletableFuture()
                    .thenApply(Response::getResponseBody)
                    .thenAccept(System.out::println)
                    .join();
        }
        System.out.println();
    }
}
