package core.http;

import com.fasterxml.jackson.databind.JsonNode;
import core.common.JsonSerializable;
import core.exceptions.*;
import core.http.responses.*;
import io.swagger.annotations.ApiModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class Results {

    private static Logger logger = LoggerFactory.getLogger(Results.class);

// RESPONSE OPERATIONS ##################################################################################################

    /**
     * Creates a result based on the provided status code.
     *
     * @param statusCode integer of
     * @param message    string to send in result
     * @return result with status code and message
     */
    public static Result customResult(int statusCode, String status, String message) {
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.state = status;
        result.message = message;
        return Controller.status(statusCode, Json.toJson(result));
    }

    public static Result customResult(int statusCode, String message) {
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.state = "Unknown";
        result.message = message;
        return Controller.status(statusCode, Json.toJson(result));
    }

// CREATE JSON! - 201 ##################################################################################################

    /**
     * Creates result created. Body of this result is some object itself instead of classic result json.
     *
     * @param object of BaseModel to send
     * @return 201 result
     */
    public static Result created(JsonSerializable object) {
        return Controller.created(object.json());
    }

// OK JSON! - 200 ######################################################################################################

    /**
     * Create an empty ok result.
     *
     * @return 200 result
     */
    public static Result ok() {
        return Controller.ok(Json.toJson(new Result_Ok()));
    }

    public static Result ok(List<? extends JsonSerializable> objects){
        return Controller.ok(Json.toJson(objects)); // TODO tato metoda je nesystemová a list by neměl v tyrionovi být - Oprava TZ!
    }

    /**
     * Create stream result
     *
     * @return 200 result
     */
    public static Result ok(InputStream stream, long content_length) {
        return Controller.ok(stream, content_length);
    }


    /**
     * Return native Mongo Objects
     * @param documents
     * @return
     */
    public static Result ok_mongo(List<Document> documents) {
        return Controller.ok(Json.toJson(documents));
    }

    /**
     * Creates a result with the code 200 and provided json as the body.
     *
     * @param object BaseModel serialized object
     * @return 200 result ok with json
     */
    public static Result ok(JsonSerializable object) {
        return Controller.ok(object.json());
    }

    /**
     * Creates an ok result with given message.
     *
     * @param message string to be sent
     * @return 200 result with message
     */
    public static Result ok(String message) {
        return Controller.ok(Json.toJson(new Result_Ok(message)));
    }

    /**
     * Creates an ok result with given File.
     * @param file set File
     * @return
     */
    public static Result ok(File file) {
        return Controller.ok(file);
    }

// FILES - 200 #########################################################################################################

    /**
     * Create response with File in PDF
     * @param byte_array Array of Bites
     * @param file_name Name of file
     * @return 200 result with file in body
     */
    public static Result file(byte[] byte_array, String file_name) {
        Controller.response().setHeader("filename", file_name);
        return Controller.ok(byte_array);
    }

// REDIRECT 303 ########################################################################################################

    /**
     * Creates redirect result.
     *
     * @param url to redirect to
     * @return 303 result
     */
    public static Result redirect(String url) {
        if(url == null) throw new NotFoundException(Result.class, "Redirect Parameter not found");
        return Controller.redirect(url);
    }

// BAD REQUEST - JSON! 400 #############################################################################################

    /**
     * Creates result bad request, when there is a client error.
     *
     * @return 400 result
     */
    public static Result badRequest() {
        return badRequest(Json.toJson(new Result_BadRequest()));
    }

    /**
     * Creates result bad request with message, when there is a client error.
     *
     * @param message to send
     * @return 400 result with message
     */
    public static Result badRequest(String message) {
        return badRequest(Json.toJson(new Result_BadRequest(message)));
    }

    /**
     * Creates result bad request with provided json as a body, when there is a client error.
     *
     * @param json to send
     * @return 400 result with message
     */
    public static Result badRequest(JsonNode json) {
        return Controller.badRequest(Json.toJson(json));
    }

// BAD REQUEST - JSON! 404 #############################################################################################

    /**
     * Creates a not found result with message from Class where code try to find annotation for Swagger
     *
     * @param cls model what is missing
     * @return 404 result with message
     */
    public static Result notFound(Class cls) {
        logger.info("notFound - object of type: {} was not found", cls.getName());
        if (cls.isAnnotationPresent(ApiModel.class)) {
            ApiModel annotation = (ApiModel) cls.getAnnotation(ApiModel.class);
            return Controller.notFound(Json.toJson(new Result_NotFound("Object " + annotation.value() + " not found.")));
        } else {
            return Controller.notFound(Json.toJson(new Result_NotFound("Object " + cls.getSimpleName().replace("Model_", "").replace("Swagger_", "") + " not found.")));
        }
    }

    /**
     * Creates a not found result with message from Class where code try to find annotation for Swagger
     * @param message
     * @return
     */
    public static Result notFound(String message) {
        return Controller.notFound(Json.toJson(new Result_NotFound(message)));
    }

// FOR COMPILATOR ######################################################################################################

    /**
     * Creates result when compilation was unsuccessful.
     *
     * @param json describing errors
     * @return 422 result
     */
    public static Result buildErrors(JsonNode json) {
        return Controller.status(422, Json.toJson(json));
    }

    /**
     * Creates result when target server is offline
     *
     * @param message to send
     * @return 477 result
     */
    public static Result externalServerOffline(String message) {
        return Controller.status(477, Json.toJson(new Result_ServerOffline(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @return 478 result
     */
    public static Result externalServerError() {
        return Controller.status(478, Json.toJson(new Result_ExternalServerSideError()));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @param message describing error
     * @return 478 result with message
     */
    public static Result externalServerError(String message) {
        return Controller.status(478, Json.toJson(new Result_ExternalServerSideError(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @param json with error
     * @return 478 result with json body
     */
    public static Result externalServerError(JsonNode json) {
        return Controller.status(478, json);
    }


// UNAUTHORIZED 401 - when token is missing (user login required) ##########################################################

    /**
     * Creates result unauthorized.
     * Signals that user have to be logged in.
     *
     * @return 401 result
     */
    public static Result unauthorized() {
        return Controller.unauthorized(Json.toJson(new Result_Unauthorized()));
    }

// FORBIDEN 403 - when user do illegal operations ######################################################################

    /**
     * Creates result forbidden
     *
     * @return 403 result
     */
    public static Result forbidden() {
        return Controller.forbidden(Json.toJson(new Result_Forbidden()));
    }

    /**
     * Creates result forbidden with custom message.
     *
     * @param message to send
     * @return 403 result with message
     */
    public static Result forbidden(String message) {
        return Controller.forbidden(Json.toJson(new Result_Forbidden(message)));
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


// SPECIAL RESPONSE 70x for Login, Registration etc. ###################################################################


    /**
     * Creates result to reject the user, when his email is not validated.
     *
     * @return 705 result
     */
    public static Result notValidated() {
        return Controller.status(705, Json.toJson(new Result_NotValidated()));
    }

    /**
     * Creates result with custom message to reject the user, when his email is not validated.
     *
     * @return 705 result with message
     */
    public static Result notValidated(String message) {
        return Controller.status(705, Json.toJson(new Result_NotValidated(message)));
    }

    /**
     * Creates result to reject the user, when his email is not registred.
     * Its only invited! Registration is required
     *
     * @return 705 result
     */
    public static Result notYetRegistered() {
        return Controller.status(706, Json.toJson(new Result_NotYetRegistered()));
    }


    /**
     * Creates result to reject the user, when his email is not validated.
     *
     * @return 705 result
     */
    public static Result registerAllowOnlyForInvitedUsers() {
        return Controller.status(707, Json.toJson(new Result_OnlyForInvited()));
    }

    /**
     * Its After Succesfull Creation of Account - used for Frontend to show next steps
     * User account was created, but it required to approve it throw button in email sent to user email address
     *
     * @return 718 result
     */
    public static Result registration_successful_email_validation_required() {
        return Controller.status(718, Json.toJson(new Result_Ok(718)));
    }


// EXCEPTION - ALL GENERAL EXCEPTIONS ##################################################################################


    /**
     *
     * General Flow Exception for Controllers Method.
     * Here we recognized and logged all exception like Object not found, Incoming Json is not valid according Form Exception
     * @param error Throwable
     * @return
     */
    public static Result controllerServerError(Throwable error) {
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

            return internalServerError(error);

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    /**
     * Creates result internal server error.
     *
     * @param error that was thrown
     * @return 500 result
     */
    public static Result internalServerError(Throwable error) {
        logger.error("internalServerError - exception during request handling", error);
        return Controller.internalServerError(Json.toJson(new Result_InternalServerError()));
    }
}
