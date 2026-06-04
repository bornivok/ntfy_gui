package borni.top.ntfy_gui.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;

    public NtfyClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
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

            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode.has("error")) {
                    String errorText = jsonNode.get("error").asText().toLowerCase();

                    if (errorText.contains("unauthorized")) {
                        return new NtfyResponse(NtfyResponse.Status.UNAUTHORIZED, responseBody);
                    } else if (errorText.contains("invalid request")) {
                        return new NtfyResponse(NtfyResponse.Status.INVALID_REQUEST, responseBody);
                    } else {
                        return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, responseBody);
                    }
                }

                else if (jsonNode.has("id") && jsonNode.has("title")) {
                    String receivedTitle = jsonNode.get("title").asText();
                    if (receivedTitle.equals(title)) {
                        return new NtfyResponse(NtfyResponse.Status.SUCCESS, responseBody);
                    }
                }
                return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, responseBody);
            } catch (JsonProcessingException e) {
                if (statusCode == 400 || statusCode == 404) {
                    return new NtfyResponse(NtfyResponse.Status.SERVER_NOT_FOUND, responseBody);
                }
                return new NtfyResponse(NtfyResponse.Status.SERVER_NOT_FOUND, "A szerveren nem fut Ntfy API.");
            }

        } catch (UnknownHostException | ConnectException | IllegalArgumentException e) {
            return new NtfyResponse(NtfyResponse.Status.SERVER_NOT_FOUND, e.getMessage());
        } catch (IOException | InterruptedException e) {
            return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
