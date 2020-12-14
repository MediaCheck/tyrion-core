package core.auth;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomerKeyAuthenticator extends Security.Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(HomerKeyAuthenticator.class);

    private List<String> keys;

    @Inject
    public HomerKeyAuthenticator(Config config) {
        try {
            this.keys = config.getStringList("homer.keys");
        } catch (ConfigException e) {
            logger.warn("constructor - no keys for homer authentication were configured, if you wish, you can specify it in 'homer.keys' in application.conf");
            this.keys = new ArrayList<>();
        }
    }

    @Override
    public String getUsername(Http.Context ctx) {
        Optional<String> header = ctx.request().getHeaders().get("x-auth-token");

        if (header.isPresent()) {

            String token = header.get();

            if (this.keys.contains(token)) {
                return token;
            }
        } else {
            logger.info("getUsername - auth header was not found");
        }

        return null;
    }

    @Override
    public Optional<String> getUsername(Http.Request request) {
        return Optional.ofNullable(this.getUsername(Http.Context.current())); // TODO migrate from Http.Context
    }
}
