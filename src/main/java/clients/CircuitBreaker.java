package clients;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface CircuitBreaker {
    <T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> operation);
}
