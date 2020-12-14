package core.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import core.cache.CacheService;
import core.http.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

@Singleton
public abstract class BaseAuthenticator extends Security.Authenticator {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    protected static final Logger logger = LoggerFactory.getLogger(BaseAuthenticator.class);

    /* VALUES  -------------------------------------------------------------------------------------------------------------*/

    /* Constructor  --------------------------------------------------------------------------------------------------------*/

    @Inject
    public BaseAuthenticator(CacheService cacheService) {}

    /* PUBLIC  ----------------------------------------------------------------------------------------------------------*/

    @Override
    public Result onUnauthorized(Http.Request request) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", request.method(), request.path());
        return Results.unauthorized();
    }


    /* PROTECTED  --------------------------------------------------------------------------------------------------------*/

    /**
     * ITs for common or same UUID tokens across projects - but the same token can be used in many projects with
     * different "Person".
     * @return
     */
    public abstract String getProjectPrefix();
}
