package core.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.util.Optional;
import java.util.UUID;

public interface Authenticator {

    Logger logger = LoggerFactory.getLogger(Authenticator.class);

    static Optional<UUID> safeUUID(String uuid) {
        try {
            return Optional.of(UUID.fromString(uuid));
        } catch (Exception e) {
            logger.warn("safeUUID - error when parsing UUID", e);
            return Optional.empty();
        }
    }

    /**
     * This method should do authentication of the given request.
     * If empty optional is returned the request will be considered unauthorized.
     * @param request for authentication
     * @return Optional of the authenticated request
     */
    Optional<Http.Request> authenticate(Http.Request request);
}


