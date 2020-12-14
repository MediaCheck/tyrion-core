package core.mongo;

import core.auth.Attributes;
import core.http.Results;
import core.mongo.model._MongoModel_AbstractPerson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import core.http.form.FormFactory;
import core.exceptions.*;
import io.sentry.Sentry;
import io.sentry.event.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import core.http.responses.*;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static play.mvc.Controller.request;

/**
 * This class provides some common API for Tyrion REST Controller.
 * Creates results with given content.
 */
@SuppressWarnings("rawtypes")
public abstract class _BaseMongoController extends Results {

// LOGGER ##############################################################################################################

    private static final Logger logger = LoggerFactory.getLogger(_BaseMongoController.class);

// COMMON CONSTRUCTOR ###################################################################################################

    protected final FormFactory formFactory;
    protected final WSClient ws;

    @Inject
    public _BaseMongoController(WSClient ws, FormFactory formFactory) {
        this.ws = ws;
        this.formFactory = formFactory;
    }

    public List<ObjectId> stringListToObjectList(List<String> list) {

        List<ObjectId> new_list = new ArrayList<>();

        for(String id: list) {
            new_list.add( new ObjectId(id));
        }

        return new_list;
    }

    /**
     * Converts this model to printable string
     * @return formatted string
     */
    public String prettyPrint(JsonNode node) {

        return this.getClass() + ":\n" + Json.prettyPrint(node) + ":\n" ;
    }

// PERSON OPERATIONS ###################################################################################################

    /**
     * Shortcuts for automatic validation and parsing of incoming JSON to MODEL class
     * @param clazz whatever
     * @param <T> ???
     * @return the same class
     * @throws InvalidBodyException for unstable
     */
    @Deprecated
    public <T> T formFromRequestWithValidation(Class<T> clazz) {
        return formFactory.formFromRequestWithValidation(clazz);
    }

    public <T> T bodyFromRequest(Http.Request request, Class<T> clazz) {
        return formFactory.bodyFromRequest(request, clazz);
    }

    /**
     * Shortcuts for automatic validation and parsing of incoming JSON to MODEL class
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T formFromJsonWithValidation(Class<T> clazz, JsonNode node) {
        return this.formFactory.formFromJsonWithValidation(clazz, node);
    }

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     *
     * @return current person {@link _MongoModel_AbstractPerson}
     */
    @Deprecated
    public static _MongoModel_AbstractPerson person() throws UnauthorizedException {
        try {

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) Controller.ctx().args.get("person");

            if (person != null) {
                return person;
            } else {
                throw new UnauthorizedException();
            }
        } catch (Exception e) {
            throw new UnauthorizedException();
        }
    }

    public static _MongoModel_AbstractPerson<?> person(Http.Request request) {
        return request.attrs().getOptional(Attributes.PERSON).orElseThrow(UnauthorizedException::new); // TODO must create authenticator for this to work
    }

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     *
     * @return current person id {@link ObjectId}
     */
    @Deprecated
    public static ObjectId personId() throws UnauthorizedException {
        try {
            ObjectId id = ((_MongoModel_AbstractPerson) Controller.ctx().args.get("person")).id;
            if(id != null) {
                return id;
            } else {
                throw new UnauthorizedException();
            }
        } catch (Exception e) {
            throw new UnauthorizedException();
        }
    }

    /**
     * Returns true if there is a authenticated person in the context.
     *
     * @return boolean true if there is a person
     */
    @Deprecated
    public static boolean isAuthenticated() {
        try {
            return Controller.ctx().args.containsKey("person");
        } catch (Exception e) {
            return false;
        }
    }

// REQUEST OPERATIONS ###################################################################################################

    /**
     * Returns true if there is a authenticated person in the context
     * and if he has specified permission.
     *
     * @return boolean true if person is permitted
     */
    @Deprecated
    public JsonNode getBodyAsJson() {
        try {
            return request().body().asJson();
        } catch (Exception e) {
            logger.error(" getBodyAsJson:: ERROR EXCEPTION");
            // logger.error(e);
            throw new NullPointerException();
        }
    }

    /**
     * Creates result with code 400. Used when binding incoming json to form.
     * If form has error, they are returned in the exception field.
     *
     * @param errors JsonNode with errors
     * @return 400 result
     */
    public static Result invalidBody(JsonNode errors) {
        return badRequest(Json.toJson(new Result_InvalidBody(errors)));
    }

// EXCEPTION - ALL GENERAL EXCEPTIONS ##################################################################################

    /**
     *
     * General Flow Exception for Controllers Method.
     * Here we recognized and logged all exception like Object not found, Incoming Json is not valid according Form Exception
     * @param error Throwable
     * @return
     */
    public static Result controllerServerError(Http.Request request, Throwable error) {
        try {

            if (error instanceof BadRequestException) {

                return badRequest(error.getMessage());

            } else if (error instanceof InvalidBodyException) {

                return badRequest(Json.toJson(new Result_InvalidBody(((InvalidBodyException) error).getErrors())));

            } else if (error instanceof NotFoundException) {

                return notFound(((NotFoundException) error).getEntity());

            } else if (error instanceof ForbiddenException) {

                return forbidden();

            } else if (error instanceof UnauthorizedException) {

                return unauthorized();

            } else if (error instanceof NotSupportedException) {

                return badRequest(Json.toJson(new Result_UnsupportedException()));

            } else if (error instanceof ServerOfflineException) {

                return externalServerOffline(error.getMessage());

            } else if (error instanceof ExternalErrorException) {

                return externalServerError();
            }

            return internalServerError(request, error);

        } catch (Exception e) {
            return internalServerError(request, e);
        }
    }

    @Deprecated // Use method above
    public static Result controllerServerError(Throwable error) {
        return controllerServerError(request(), error);
    }

    /**
     * Creates result internal server error.
     *
     * @param error that was thrown
     * @return 500 result
     */
    public static Result internalServerError(Http.Request request, Throwable error) {
        // Set sentry
        try {

            _MongoModel_AbstractPerson person = person(request);
            User sentry_user = new User(person.getObjectId().toString(), person.first_name + " " + person.last_name, null, person.email, null);
            Sentry.getContext().setUser(sentry_user);

        } catch (UnauthorizedException e) {
            // Nothing to do
        }

        logger.error("internalServerError - exception during request handling", error);
        return Controller.internalServerError(Json.toJson(new Result_InternalServerError()));
    }

    @Deprecated // Use method above
    public static Result internalServerError(Throwable error) {
        return internalServerError(request(), error);
    }



// EXTERNAL REST API **********************************************************************************************************************

    @Deprecated // Use async requests
    protected JsonNode POST(URL url, int timeOut, JsonNode node, String auth,  Map<String, List<String>> headers) throws ExecutionException, InterruptedException{

            logger.debug("URL:POST {}  Json: {} ", url.getHost(), node.toString());

            WSRequest request = ws.url(url.toString())
                    .setContentType("application/json")
                    .setRequestTimeout(Duration.ofSeconds(timeOut));

            if(auth != null) {
                request.setAuth(auth);
            }

            if(headers != null) {
                request.setHeaders(headers);
            }

            CompletionStage<WSResponse> responsePromise  = request.post(node);

            WSResponse wsResponse = responsePromise.toCompletableFuture().get(); // FIXME This will block the thread! Do not call .get() method.
            JsonNode response = wsResponse.asJson();

            logger.debug("REST:: POST:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), prettyPrint(response));
            return response;
    }

    @Deprecated // Use async requests
    protected JsonNode PUT(URL url, int timeOut, String auth, JsonNode node, Map<String, List<String>> headers) throws ExecutionException, InterruptedException {

            logger.debug("URL:PUT " + url + "  Json: " + node.toString());

            WSRequest request = ws.url(url.toString())
                    .setContentType("application/json")
                    .setRequestTimeout(Duration.ofSeconds(timeOut));

            if(auth != null) {
                request.setAuth(auth);
            }

            if(headers != null) {
                request.setHeaders(headers);
            }

            CompletionStage<WSResponse> responsePromise  = request.put(node);

            WSResponse wsResponse = responsePromise.toCompletableFuture().get(); // FIXME This will block the thread! Do not call .get() method.
            JsonNode response = wsResponse.asJson();

            logger.debug("REST:: PUT:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), prettyPrint(response));
            return response;

    }

    @Deprecated // Use async requests
    protected JsonNode GET(URL url, int timeOut, String auth, Map<String, List<String>> headers) throws ExecutionException, InterruptedException {

            logger.debug("URL:GET: " + url);

            WSRequest request = ws.url(url.toString())
                    .setContentType("application/json")
                    .setRequestTimeout(Duration.ofSeconds(timeOut));

            if(auth != null) {
                request.setAuth(auth);
            }

            if(headers != null) {
                request.setHeaders(headers);
            }


            CompletionStage<WSResponse> responsePromise  = request.get();


            WSResponse wsResponse = responsePromise.toCompletableFuture().get(); // FIXME This will block the thread! Do not call .get() method.
            logger.debug("REST:: GET:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), wsResponse.getBody());


            JsonNode response = wsResponse.asJson();
            logger.debug("REST:: GET:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), prettyPrint(response));


            return response;

    }

    @Deprecated // Use async requests
    protected JsonNode DELETE(URL url, int timeOut, String auth, Map<String, List<String>> headers) throws ExecutionException, InterruptedException {

            logger.debug("URL:DELETE: " + url);

            WSRequest request = ws.url(url.toString())
                    .setContentType("application/json")
                    .setRequestTimeout(Duration.ofSeconds(timeOut));

            if(auth != null) {
                request.setAuth(auth);
            }

            if(headers != null) {
                request.setHeaders(headers);
            }


            CompletionStage<WSResponse> responsePromise  = request.delete();

            WSResponse wsResponse = responsePromise.toCompletableFuture().get(); // FIXME This will block the thread! Do not call .get() method.
            logger.debug("REST:: DELETE:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), wsResponse.getBody());


            JsonNode response = wsResponse.asJson();
            logger.debug("REST:: DELETE:{}  Code: {} Result: {}", url.toString(), wsResponse.getStatus(), prettyPrint(response));

            return response;

    }

}
