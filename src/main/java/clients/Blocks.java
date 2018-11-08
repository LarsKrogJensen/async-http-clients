package clients;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Blocks {


    public static CompletableFuture<String> lookup(){
        return CompletableFuture.completedFuture("http://httpbin.org/delay/1");
    }
    
}
