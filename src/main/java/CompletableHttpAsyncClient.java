import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CompletableHttpAsyncClient  {
    private final HttpAsyncClient httpAsyncClient;

    public CompletableHttpAsyncClient(HttpAsyncClient httpAsyncClient) {
        this.httpAsyncClient = httpAsyncClient;
    }

    public <T> CompletableFuture<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer, HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer, HttpContext httpContext) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpAsyncRequestProducer, httpAsyncResponseConsumer, httpContext, fc));
    }

    public <T> CompletableFuture<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer, HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpAsyncRequestProducer, httpAsyncResponseConsumer, fc));
    }

    public CompletableFuture<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpHost, httpRequest, httpContext, fc));
    }

    public CompletableFuture<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpHost, httpRequest, fc));
    }

    public CompletableFuture<HttpResponse> execute(HttpUriRequest httpUriRequest, HttpContext httpContext) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpUriRequest, httpContext, fc));
    }

    public CompletableFuture<HttpResponse> execute(HttpUriRequest httpUriRequest) {
        return toCompletableFuture(fc -> httpAsyncClient.execute(httpUriRequest, fc));
    }

    private static <T> CompletableFuture<T> toCompletableFuture(Consumer<FutureCallback<T>> c) {
        CompletableFuture<T> promise = new CompletableFuture<>();

        c.accept(new FutureCallback<T>() {
            @Override
            public void completed(T t) {
                promise.complete(t);
            }

            @Override
            public void failed(Exception e) {
                promise.completeExceptionally(e);
            }

            @Override
            public void cancelled() {
                promise.cancel(true);
            }
        });
        return promise;
    }
}