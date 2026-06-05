package borni.top.ntfy_gui.Model;

public record NtfyResponse(Status status, String rawBody) {

    public enum Status {
        SUCCESS,
        UNAUTHORIZED,
        FORBIDDEN,
        INVALID_REQUEST,
        SERVER_NOT_FOUND,
        UNKNOWN_ERROR
    }
}
