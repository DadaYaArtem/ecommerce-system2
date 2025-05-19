package org.example;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class LoadTest {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 1; i <= 100; i++) {
            String body = """
            {
              "customerId": "load-test-%d",
              "items": [
                { "productId": "p123", "quantity": 1 }
              ]
            }
            """.formatted(i);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/orders"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long end = System.currentTimeMillis();

            System.out.println("Запит #" + i + ": статус = " + response.statusCode() + ", час = " + (end - start) + " мс");
        }
    }
}