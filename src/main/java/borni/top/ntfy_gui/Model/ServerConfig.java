package borni.top.ntfy_gui.Model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServerConfig {

    @EqualsAndHashCode.Include
    private String id =  UUID.randomUUID().toString();

    private String name;
    private String url;
    private boolean authRequired;
    private String token;

    public ServerConfig(String name, String url, boolean authRequired, String token) {
        this.name = name;
        this.url = url;
        this.authRequired = authRequired;
        this.token = token;
    }

    @Override
    public String toString() {
        return name + " (" + url + ")";
    }
}
