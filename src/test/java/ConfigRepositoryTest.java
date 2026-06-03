import borni.top.ntfy_gui.Model.ConfigRepository;
import borni.top.ntfy_gui.Model.ServerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadServers() {
        String testFilePath = tempDir.resolve("test_servers.json").toString();
        ConfigRepository repository = new ConfigRepository(testFilePath);

        List<ServerConfig> originalServers = new ArrayList<>();
        originalServers.add(new ServerConfig("Private", "example.com", true, "tk_123"));
        originalServers.add(new ServerConfig("Public", "example.com", false, null));

        repository.saveServers(originalServers);
        List<ServerConfig> loadedServers = repository.loadServers();

        assertNotNull(loadedServers, "A betöltött lista nem lehet null");
        assertEquals(2, loadedServers.size(), "Két szervernek kell lennie a listában");

        assertEquals(originalServers.get(0), loadedServers.get(0));

        assertEquals("Private", loadedServers.get(0).getName());
        assertTrue(loadedServers.get(0).isAuthRequired());
        assertEquals("tk_123", loadedServers.get(0).getToken());
    }

    @Test
    void testLoadFromNonExistentFileReturnsEmptyList() {
        String nonExistentFile = tempDir.resolve("non_existent_file.json").toString();
        ConfigRepository repository = new ConfigRepository(nonExistentFile);

        List<ServerConfig> loadedServers = repository.loadServers();

        assertNotNull(loadedServers);
        assertTrue(loadedServers.isEmpty(), "Nem létező fájl esetén üres listát kell kapnunk");
    }
}
