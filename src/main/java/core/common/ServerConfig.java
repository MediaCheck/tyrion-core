package core.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.common.ServerMode;
import core.network.LocalAddress;

@Singleton
public class ServerConfig {

    private final ServerMode mode;
    private final String version;
    private final String host;

    @Inject
    public ServerConfig(Config config, LocalAddress localAddress) {
        this.mode = config.getEnum(ServerMode.class, "server.mode");
        this.version = config.getString("api.version");

        if (this.isDevelopment()) {
            String address = localAddress.get();
            if (address != null) {
                this.host = address + ":9000";
            } else {
                this.host = config.getString("server.host");
            }
        } else {
            this.host = config.getString("server.host");
        }
    }

    public boolean isDevelopment() {
        return this.mode.equals(ServerMode.DEVELOPER);
    }

    public boolean isStage() {
        return this.mode.equals(ServerMode.STAGE);
    }

    public boolean isProduction() {
        return this.mode.equals(ServerMode.PRODUCTION);
    }

    public ServerMode getMode() {
        return mode;
    }

    public String getVersion() {
        return version;
    }

    public String getHost() {
        return this.host;
    }

    public String getURI() {
        return (this.isDevelopment() ? "http://" : "https://") + this.host;
    }
}
