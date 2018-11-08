package clients;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import java.io.IOException;

public class ApacheHttpAsyncClient {
    public static void main(String[] args) throws Exception {
//        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
//        client.start();

        var config = IOReactorConfig.custom().setIoThreadCount(2).build();
        var ioReactor = new DefaultConnectingIOReactor(config);
        var cm = new PoolingNHttpClientConnectionManager(ioReactor);
        CloseableHttpAsyncClient client = HttpAsyncClients.custom().setConnectionManager(cm).build();
        client.start();
        CompletableHttpAsyncClient asyncClient = new CompletableHttpAsyncClient(client);

        HttpGet request = new HttpGet("http://httpbin.org/delay/1");

        asyncClient.execute(request).whenComplete((response, error) -> {
            try {
                response.getEntity().writeTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


//        client.close();
    }
}
