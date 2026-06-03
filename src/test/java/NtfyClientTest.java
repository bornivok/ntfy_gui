import borni.top.ntfy_gui.Model.NtfyClient;
import borni.top.ntfy_gui.Model.NtfyResponse;
import borni.top.ntfy_gui.Model.ServerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NtfyClientTest {

    private final NtfyClient ntfyClient = new NtfyClient();

    @Test
    void testSuccessfulMessageSending() {
        ServerConfig publicServer = new ServerConfig(
                "Public test",
                "https://ntfy.sh/szoft_test_channel",
                false,
                null
        );

        NtfyResponse response = ntfyClient.sendMessage(
                publicServer,
                "Teszt header",
                "Ez egy automata JUnit teszt üzenet.",
                "computer"
        );

        assertEquals(NtfyResponse.Status.SUCCESS, response.status(),
                "A publikus szerverre küldött üzenetnek sikeresnek kell lennie.");
        assertNotNull(response.rawBody());
        assertTrue(response.rawBody().contains("\"topic\":\"szoft_test_channel\""), "A válasznak tartalmaznia kell a csatorna nevét.");
    }

    @Test
    void testServerNotFoundHandling() {
        ServerConfig badHostServer = new ServerConfig(
                "Rossz szerver",
                "https://nem-letezo-server-qwertz.sh/test",
                false,
                null
        );

        NtfyResponse response = ntfyClient.sendMessage(
                badHostServer,
                "Header",
                "Message",
                "empty"
        );

        assertEquals(NtfyResponse.Status.SERVER_NOT_FOUND, response.status(),
                "Nem létező host esetén SERVER_NOT_FOUND státuszt kell kapni.");
    }

    @Test
    void testInvalidRequestHandling() {
        ServerConfig invalidRequestServer = new ServerConfig(
                "No Channel Server",
                "https://ntfy.sh",
                false,
                null
        );

        NtfyResponse response = ntfyClient.sendMessage(
                invalidRequestServer,
                "Header",
                "Message",
                "empty"
        );

        assertEquals(NtfyResponse.Status.INVALID_REQUEST, response.status(),
                "Csatorna nélküli URL esetén INVALID_REQUEST státuszt kell kani.");
    }
}
