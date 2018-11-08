package clients;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CircuitBreakerImpl implements CircuitBreaker {
    @Override
    public <T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> operation) {
        return  operation.get();
    }
}
