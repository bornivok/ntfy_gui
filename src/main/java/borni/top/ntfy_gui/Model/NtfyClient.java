package borni.top.ntfy_gui.Model;

import java.time.Duration;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NtfyClient {

    private final HttpClient httpClient;

    public NtfyClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public NtfyResponse sendMessage(ServerConfig server, String title, String message, String tag) {

        String urlString = server.getUrl();
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "https://" + urlString;
        }

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("Title", title)
                    .POST(HttpRequest.BodyPublishers.ofString(message));

            if (tag != null && !tag.trim().isEmpty() && !tag.equalsIgnoreCase("empty")) {
                requestBuilder.header("Tag", tag);
            }

            if (server.isAuthRequired() && server.getToken() != null && !server.getToken().isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + server.getToken());
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 200) {
                return new NtfyResponse(NtfyResponse.Status.SUCCESS, responseBody);
            } else if (statusCode == 401) {
                return new NtfyResponse(NtfyResponse.Status.UNAUTHORIZED, responseBody);
            } else if (statusCode == 400 || statusCode == 404) {
                return new NtfyResponse(NtfyResponse.Status.INVALID_REQUEST, responseBody);
            } else {
                return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, responseBody);
            }
        } catch (UnknownHostException | ConnectException | IllegalArgumentException e) {
            return new NtfyResponse(NtfyResponse.Status.SERVER_NOT_FOUND, e.getMessage());
        } catch (IOException | InterruptedException e) {
            return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
