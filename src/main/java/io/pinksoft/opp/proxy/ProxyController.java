package io.pinksoft.opp.proxy;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @GetMapping
    public ResponseEntity<String> fetch(@RequestParam String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ResponseEntity.badRequest().body("URL must start with http:// or https://");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(response.body());
            }
            return ResponseEntity.status(response.statusCode()).body("Remote returned: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to fetch URL: " + e.getMessage());
        }
    }
}
