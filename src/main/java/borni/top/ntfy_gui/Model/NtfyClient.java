package borni.top.ntfy_gui.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

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
            HttpResponse<String> response;
            try {
                HttpRequest request = createRequest(urlString, server, title, message, tag);
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                if (urlString.startsWith("https://")) {
                    System.out.println("HTTPS hiba, próbálkozás HTTP-vel ... (" +e.getMessage() + ")");
                    String fallbackUrl = urlString.replaceFirst("https://", "http://");
                    HttpRequest fallbackRequest = createRequest(fallbackUrl, server, title, message, tag);

                    response = httpClient.send(fallbackRequest, HttpResponse.BodyHandlers.ofString());
                } else {
                    throw e;
                }
            }

            int statusCode = response.statusCode();
            String responseBody = response.body();

            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode.has("code") && jsonNode.has("http") && jsonNode.has("error")) {
                    int code = jsonNode.get("code").asInt();
                    int http = jsonNode.get("http").asInt();
                    String error = jsonNode.get("error").asText();

                    if (code == 40101 && http == 401 && error.equals("unauthorized")) {
                        return new NtfyResponse(NtfyResponse.Status.UNAUTHORIZED, responseBody);
                    }
                    else if (code == 40301 && http == 403 && error.equals("forbidden")) {
                        return new NtfyResponse(NtfyResponse.Status.FORBIDDEN, responseBody);
                    }
                    else if (code == 40024 && http == 400 && error.equals("invalid request: request body must be valid JSON")) {
                        return new NtfyResponse(NtfyResponse.Status.INVALID_REQUEST, responseBody);
                    }
                    else {
                        return new NtfyResponse(NtfyResponse.Status.UNKNOWN_ERROR, responseBody);
                    }
                } else if (jsonNode.has("id") && jsonNode.has("title") &&  jsonNode.has("message")) {
                    String receivedTitle = jsonNode.get("title").asText();
                    String receivedMessage = jsonNode.get("message").asText();
                    if (receivedTitle.equals(title) && receivedMessage.equals(message)) {
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

    private HttpRequest createRequest(String targetUrl, ServerConfig server, String title, String message, String tag) {

        String encodedTitle = "=?UTF-8?B?" + Base64.getEncoder().encodeToString(title.getBytes(StandardCharsets.UTF_8)) + "?=";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Title", encodedTitle)
                .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8));

        if (tag != null && !tag.trim().isEmpty() && !tag.equalsIgnoreCase("empty")) {
            requestBuilder.header("Tags", tag);
        }

        if (server.isAuthRequired() && server.getToken() != null && !server.getToken().isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + server.getToken());
        }
        return requestBuilder.build();
    }
}
