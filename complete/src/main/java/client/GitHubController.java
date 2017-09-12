package client;

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import shared.ClientResponse;
import shared.ServerResponse;

import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class GitHubController {
    private static final String BASE_URL = "http://localhost:8080/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    @RequestMapping("/sync/user/{name}")
    public ClientResponse findUserSync(@PathVariable(value = "name") String name)
            throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        ResponseEntity<ServerResponse> entity = restTemplate.getForEntity(BASE_URL + name, ServerResponse.class);
        ClientResponse clientResponse = new ClientResponse(Thread.currentThread().getName());
        clientResponse.setData(entity.getBody());
        clientResponse.setTimeMs(System.currentTimeMillis() - start);
        return clientResponse;
    }

    @RequestMapping("/async/user/{name}")
    public ListenableFuture<ClientResponse> findUserAsync(@PathVariable(value = "name") String name)
            throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        ClientResponse clientResponse = new ClientResponse(Thread.currentThread().getName());
        ListenableFuture<ResponseEntity<ServerResponse>> entity = asyncRestTemplate.getForEntity(BASE_URL + name, ServerResponse.class);
        entity.addCallback(new ListenableFutureCallback<ResponseEntity<ServerResponse>>() {
            @Override
            public void onFailure(Throwable ex) {
                clientResponse.setError(true);
                clientResponse.setCompletingThread(Thread.currentThread().getName());
                clientResponse.setTimeMs(System.currentTimeMillis() - start);
            }

            @Override
            public void onSuccess(ResponseEntity<ServerResponse> result) {
                clientResponse.setData(result.getBody());
                clientResponse.setCompletingThread(Thread.currentThread().getName());
                clientResponse.setTimeMs(System.currentTimeMillis() - start);
            }
        });

    }

}
