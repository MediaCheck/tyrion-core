package core.auth;

import core.mongo.model._MongoModel_AbstractPerson;
import core.mongo.model._MongoModel_AbstractToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import core.cache.CacheService;
import core.exceptions.NotFoundException;
import core.exceptions.UnauthorizedException;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import play.mvc.Http;

import java.util.Optional;
import java.util.UUID;

import static core.cache.InjectCache.TwoDayCacheConstant;

@Singleton
public abstract class _ProjectAuth extends BaseAuthenticator {


    private static Cache<String, ObjectId> data_cache_projects;   // < UUID_TOKEN, UUID_PERSON>

    @Inject
    public _ProjectAuth(CacheService cacheService) {
        super(cacheService);

        if( data_cache_projects == null)
        data_cache_projects = cacheService.getCache( "COMMON_CACHE_TOKEN_PROJECTS",
                String.class, ObjectId.class,  10000, TwoDayCacheConstant,  false);
    }

    protected String getPersonFromToken(_MongoModel_AbstractPerson person_model, _MongoModel_AbstractToken token_model, Http.Context ctx) {
        try {

            logger.info("getUsername - authorization begins");
            Optional<String> header = ctx.request().getHeaders().get("x-auth-token");


            if (header.isPresent()) {


                UUID token = UUID.fromString(header.get());

                // Its in Cache
                if(!data_cache_projects.containsKey(getProjectPrefix() + token)) {

                    _MongoModel_AbstractToken authorizationToken = (_MongoModel_AbstractToken) token_model.getByToken(token);

                    // Check Validation
                    authorizationToken.isValid();

                    data_cache_projects.put(getProjectPrefix() + token, (person_model.getFinder().byId(authorizationToken.person_id)).getObjectId());

                }

                _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_model.getFinder().byId( data_cache_projects.get(getProjectPrefix() + token));

                ctx.args.put("person", person);
                return person.email;


            } else {
                logger.info("getUsername - authorization header is missing");
            }

        } catch (UnauthorizedException e) {
            logger.warn("_ProjectAuth: - token UnauthorizedException " );
            // Nothing
        } catch (NotFoundException e) {
            logger.warn("_ProjectAuth: - token NotFoundException " );
            // nothing
        } catch (Exception e) {
            logger.error("getUsername", e);
        }

        return null;

    }

    public Cache<String, ObjectId> project_cache() {
        return data_cache_projects;
    }

    public void addPersonAndToken(UUID token, ObjectId person_id ) {
        data_cache_projects.put(getProjectPrefix() + token, person_id);
    }

    public abstract String getProjectPrefix();
    public abstract void clean_token(UUID token);
}
