package borni.top.ntfy_gui.Model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigRepository {

    private final String filePath;
    private final ObjectMapper objectMapper;

    public ConfigRepository() {
        this.filePath = "servers.json";
        this.objectMapper = new ObjectMapper();
    }

    public ConfigRepository(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    public List<ServerConfig> loadServers() {
        File file = new File(filePath);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(file, new  TypeReference<List<ServerConfig>>() {});
        } catch (IOException e) {
            System.err.println("Hiba a konfiguráció beolvasásakor: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveServers(List<ServerConfig> servers) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), servers);
        } catch (IOException e) {
            System.err.println("Hiba a konfiguráció mentésekor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
