package core.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import core.auth._ProjectAuth;
import core.email.EmailFactory;
import core.email.EmailInterface;
import core.email.EmailService;
import core.exceptions.*;
import core.http.form.FormFactory;
import core.http.responses.*;
import core.mongo.model.*;
import core.storage.StorageService;
import core.storage.StoredItem;
import core.swagger.*;
import core.util.RegistrationState;
import io.swagger.annotations.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.libs.json.Json;
import play.libs.Files;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;


import java.io.NotActiveException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class _CommonPersonActionController<
        PERSON_MODEL extends _MongoModel_AbstractPerson,
        RECOVERY_TOKEN extends _MongoModel_RecoveryToken,
        CONNECTION_TOKEN extends _MongoModel_AbstractToken> extends _BaseMongoController {

// CONTROLLER CONFIGURATION  ###########################################################################################

    private final _ProjectAuth project_authenticartion;
    private final StorageService storageService;
    private final EmailFactory emailFactory;
    private final EmailService emailService;
    private final Config config;

    private final Class<RECOVERY_TOKEN> recovery_token;
    private final Class<PERSON_MODEL> person_class;
    private final Class<CONNECTION_TOKEN> connection_token;

// LOGGER ##############################################################################################################

    private static final Logger logger = LoggerFactory.getLogger(_CommonPersonActionController.class);

// COMMON CONSTRUCTOR ###################################################################################################

    @Inject
    public _CommonPersonActionController(
                                         WSClient ws,
                                         FormFactory formFactory,
                                         StorageService storageService,
                                         EmailFactory emailFactory,
                                         Config config,
                                         _ProjectAuth project_authenticartion,
                                         Class<PERSON_MODEL> person_class,
                                         Class<CONNECTION_TOKEN> connection_token,
                                         Class<RECOVERY_TOKEN> recovery_token
                                         ) {
        super(ws, formFactory);
        this.project_authenticartion = project_authenticartion;
        this.emailService = emailFactory.getEmailService();
        this.storageService = storageService;
        this.emailFactory = emailFactory;
        this.config = config;
        this.person_class = person_class;
        this.connection_token = connection_token;
        this.recovery_token = recovery_token;
    }

// ***** Create Person *************************************************************************************************

    protected _Registration_PersonResult create_person(Swagger_Person_Create request) throws Exception {

        _Registration_PersonResult result = new _Registration_PersonResult();

        // Try to find Invitation
        _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getInvitedAccount(request.email);

        // Allow  self registration without invitation?
        if (person == null && !config.getBoolean("permission_config.self_registration_allowed")) {
            throw new NotActiveException();
        }

        // Self Registration is Allowed - user is not invited
        if (person == null) {

            if (person_class.newInstance().getByEmail(request.email) != null) {
                throw new BadRequestException("Email is already in used");
            }

            person = person_class.newInstance();
            person.first_name = request.first_name;
            person.last_name = request.last_name;
            person.email = request.email;
            person.setPassword(request.password);

            if (!config.getBoolean("permission_config.required_approve_email_registration")) {
                person.state = RegistrationState.REGISTERED;
                person.validated = true;
                person.save();
                result.new_user_required_email_approve = false;

            } else {
                person.state = RegistrationState.REGISTERED_NOT_APPROVED;
                person.validated = false;
                person.save();

                RECOVERY_TOKEN passwordRecoveryToken = recovery_token.newInstance();
                passwordRecoveryToken.setPasswordRecoveryToken();
                passwordRecoveryToken.person_id = person.id;
                passwordRecoveryToken.type = Enum_RecoveryType.ACCOUNT_EMAIL_VALIDATION;
                passwordRecoveryToken.save();

                String url = (
                        this.config.getString("server.mode").toLowerCase().equals("developer") ? "http://" : "https://") +
                        this.config.getString("server." + this.config.getString("server.mode").toLowerCase() + ".backend_url");

                emailService.send(emailFactory.getEmail()
                        .setSubject("Account Validation")
                        .text("Please - approve your registration.")
                        .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                        .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                        .divider()
                        .link("Approve Email", (url + "/person_email_authentication/" + passwordRecoveryToken.token))
                        .setReceiver(person().email)
                );

                result.new_user_required_email_approve = true;
            }

            result.person = person;

        // User is Invited
        } else {

            /** Registration from Invitation Email not required approve from Email
             *
             */
            person.first_name = request.first_name;
            person.last_name = request.last_name;
            person.state = RegistrationState.REGISTERED;
            person.validated = true;
            person.setPassword(request.password);
            person.update();

            result.person = person;
            result.registration_on_invitation = true;

        }

        return result;
    }


// ***** Validate Some Property ****************************************************************************************

    protected  Result something_validate_property(Swagger_Entity_Validation_In request) {
        try {

            Swagger_Entity_Validation_Out validation = new Swagger_Entity_Validation_Out();


            switch (request.key) {

                case "email":{
                    if (person_class.newInstance().getByEmail(request.value) == null) {

                        validation.valid = true;
                        return ok(validation);
                    }

                    validation.valid = false;
                    validation.message = "email is used";

                    break;
                }

                case "vat_number":{

                    try {

                        logger.debug("person_validateProperty:: Link:: " + "https://www.isvat.eu/" + request.value.substring(0, 2) + "/" + request.value.substring(2));

                        WSResponse wsResponse = ws.url("https://www.isvat.eu/" + request.value.substring(0, 2) + "/" + request.value.substring(2))
                                .setRequestTimeout(Duration.ofSeconds(10))
                                .get()
                                .toCompletableFuture()
                                .get();

                        JsonNode result = wsResponse.asJson();

                        logger.debug("person_validateProperty: http request: {} ", wsResponse.getStatus());
                        logger.debug("person_validateProperty: vat_number: {} ", result);

                        if (result.get("valid").asBoolean()) {

                            validation.valid = true;
                            try {
                                validation.message = result.get("name").get("0").asText();
                            } catch (Exception e) {
                                // do nothing
                            }
                            return ok(validation);
                        }

                    } catch (RuntimeException e) {

                        validation.message = "vat_number is not valid or could not be found";
                        validation.valid = false;
                        return  ok(validation);

                    } catch (Exception e) {
                        logger.error("something_validateProperty", e);
                        validation.valid = false;
                        validation.message = "vat_number is not valid or could not be found";

                        return  ok(validation);
                    }

                    break;
                }

                default:return badRequest("Key does not exist, use only {email, nick_name or vat_number}");
            }

            return ok(validation);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ***** Frozen Account ************************************************************************************************

    protected Result person_activate(ObjectId person_id) {
        try {

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getFinder().byId(person_id);
            if (!person.frozen) return badRequest("Person is already active.");

            person.frozen = false;
            person.update();

            EmailInterface email = emailFactory.getEmail()
                    .setSubject("Your Account has been Activated")
                    .setAndOverrideDefaultSender(config.getString( config.getString("emailService.type") + ".sender_email"), config.getString( config.getString("emailService.type") + ".sender_email"))
                    .setTemplate( config.getString( config.getString("emailService.type") + ".templates.restart_password"))
                    .divider()
                    .text("Hello, we have good news. Your Account was activated")
                    .setReceiver(person.email);

            this.emailService.send(email);


            return ok(person);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    protected Result person_deactivate(ObjectId person_id) {
        try {

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getFinder().byId(person_id);

            if (person.frozen) return badRequest("Person is already deactivated.");
            person.frozen = true;

            connection_token.newInstance().clear_all(person_id);
            person.update();

            EmailInterface email = emailFactory.getEmail()
                    .setSubject("Your Account has been suspended")
                    .setAndOverrideDefaultSender(config.getString( config.getString("emailService.type") + ".sender_email"), config.getString( config.getString("emailService.type") + ".sender_email"))
                    .setTemplate( config.getString( config.getString("emailService.type") + ".templates.restart_password"))
                    .divider()
                    .text("Sorry, your account has been deactivated. Please contact your administrator.")
                    .setReceiver(person.email);

            this.emailService.send(email);


            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ***** Email Authentication if its Required by Config ****************************************************************

    protected Result person_email_authentication(UUID auth_token) {
        try {

            _MongoModel_RecoveryToken validationToken = (_MongoModel_RecoveryToken) recovery_token.newInstance().getByToken(auth_token.toString());

            if (validationToken == null) return badRequest("Token not Found");

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getByEmail(validationToken.email);

            if (person == null) return badRequest("User not Found");

            person.state = RegistrationState.REGISTERED;
            person.validated = true;
            person.update();

            validationToken.deletePermanent();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    protected Result person_send_authentication_email_again(Swagger_EmailRequired request) {
        try {
            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getInvitedAccount(request.email);

            if(person.state != RegistrationState.INVITED) {
                throw new BadRequestException("User state is not 'INVITED'");
            }

            _MongoModel_RecoveryToken previousToken = (_MongoModel_RecoveryToken) recovery_token.newInstance().getByEmail(request.email);

            if (previousToken != null) {
                previousToken.deletePermanent();
            }

            previousToken = recovery_token.newInstance();
            previousToken.setPasswordRecoveryToken();
            previousToken.person_id = person.id;
            previousToken.type = Enum_RecoveryType.ACCOUNT_EMAIL_VALIDATION;
            previousToken.save();

            String url = (
                    this.config.getString("server.mode").toLowerCase().equals("developer") ? "http://" : "https://") +
                    this.config.getString("server." + this.config.getString("server.mode").toLowerCase() + ".backend_url");

            emailService.send(emailFactory.getEmail()
                    .setSubject("Account Validation")
                    .text("Please - approve your registration.")
                    .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                    .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                    .divider()
                    .link("Approve Email", (url + "/person_email_authentication/" + previousToken.token))
                    .setReceiver(person().email)
            );

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// Change Own Password **************************************************************************************************

    protected Result person_change_own_password(Swagger_ChangeOwnPassword request) {
        try {

            RECOVERY_TOKEN passwordRecoveryToken = recovery_token.newInstance();
            passwordRecoveryToken.setPasswordRecoveryToken();
            passwordRecoveryToken.person_id = person().id;
            passwordRecoveryToken.type = Enum_RecoveryType.CHANGE_PASSWORD;
            passwordRecoveryToken.new_password = request.new_password;
            passwordRecoveryToken.save();

            String url =
                    (this.config.getString("server.mode").toLowerCase().equals("developer") ? "http://" : "https://") +
                    this.config.getString("server." + this.config.getString("server.mode").toLowerCase() + ".backend_url");

            emailService.send(emailFactory.getEmail()
                    .setSubject("Password Change")
                    .text("Password reset was requested for this email.")
                    .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                    .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                    .divider()
                    .link("Approve Password Change", (url + "/person_aprove_change_password/" + passwordRecoveryToken.token))
                    .setReceiver(person().email)
            );

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    protected Result person_approve_changing_own_password(String request_token) {
        try {

            _MongoModel_RecoveryToken previousToken = (_MongoModel_RecoveryToken) recovery_token.newInstance().getByToken(request_token);

            if (previousToken == null) throw new Exception("Password change was unsuccessful");
            if (new Date().getTime() - (previousToken.created.toEpochSecond(ZoneOffset.UTC) * 1000) > (25 * 60 * 60 * 1000)) {
                previousToken.deletePermanent();
                return badRequest("You must Approve Change of your password in 24 hours.");
            }

            // Delete All active tokens
            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getByEmail(previousToken.email);
            person.setPassword(previousToken.new_password);
            person.update();
            previousToken.deletePermanent();

            emailService.send(emailFactory.getEmail()
                    .setSubject("Password Changed Successfully")
                    .text("Password reset done.")
                    .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                    .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                    .setReceiver(person.email)
            );

            return redirect(
                    this.config.getString("server.mode").toLowerCase().equals("developer") ? "http://" : "https://" +
                    this.config.getString("server." + this.config.getString("server.mode").toLowerCase() + ".front_end_url")
            );

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ***** Recovery Password *********************************************************************************************

    protected Result person_ask_for_password_restart(Swagger_EmailRequired request) {
        try {
            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getByEmail(request.email);
            if (person == null) return badRequest("User doesn't exist");

            _MongoModel_RecoveryToken previousToken = (_MongoModel_RecoveryToken) recovery_token.newInstance().getByEmail(request.email);

            if (previousToken != null) {
                previousToken.deletePermanent();
            }

            previousToken = recovery_token.newInstance();
            previousToken.type = Enum_RecoveryType.PASSWORD_RESTART;
            previousToken.setPasswordRecoveryToken();
            previousToken.person_id = person.id;
            previousToken.email = person.email;
            previousToken.save();


            try {

                String url = (this.config.getString("server.mode").toLowerCase().equals("developer") ? "http://" : "https://") +
                        this.config.getString("server." + this.config.getString("server.mode").toLowerCase() + ".front_end_url");

                emailService.send(emailFactory.getEmail()
                        .setSubject("Password Reset")
                        .text("Password reset was requested for this email.")
                        .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                        .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                        .divider()
                        .link("Reset your password", (url + "/login?action=restart_password&token=" + previousToken.token + "&email=" + URLEncoder.encode(person.email, "UTF-8")))
                        .setReceiver(request.email)
                );

            } catch (Exception e) {
                logger.error("person_passwordRecoverySendEmail - send mail", e);
            }
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    protected Result person_change_password_after_ask_for_restart(Swagger_RestartPasswordEmail request) {
        try {

            logger.trace("ask_for_restart_with_new_pass: - request: {}", request.json());

            _MongoModel_RecoveryToken previousToken = (_MongoModel_RecoveryToken) recovery_token.newInstance().getByToken(request.password_recovery_token);

            if (previousToken == null) {
                throw new NotFoundException(_MongoModel_RecoveryToken.class);
            }

            logger.trace("ask_for_restart_with_new_pass: - previousToken: {}", previousToken.json());

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getByEmail(previousToken.email);

            if (person == null) {
                throw new Exception("Email not Found");
            }

            if (!person.email.equals(previousToken.email)) {
                return badRequest("Email is not Same as Email From Request");
            }


            if (new Date().getTime() - (previousToken.created.toEpochSecond(ZoneOffset.UTC) * 1000) > (25 * 60 * 60 * 1000)) {
                logger.trace("ask_for_restar: - platnost vypršela");
                previousToken.deletePermanent();
                return badRequest("You must recover your password in 24 hours.");
            }

            person.setPassword(request.new_password);
            person.validated = true;
            person.update();

            previousToken.deletePermanent();
            try {
                EmailInterface email = emailFactory.getEmail()
                        .setSubject("Password Reset")
                        .setAndOverrideDefaultSender(config.getString(config.getString("emailService.type") + ".sender_email"), config.getString(config.getString("emailService.type") + ".sender_email"))
                        .setTemplate(config.getString(config.getString("emailService.type") + ".templates.restart_password"))
                        .divider()
                        .text("Password was successfully changed.")
                        .setReceiver(person.email);

                this.emailService.send(email);

            } catch (Exception e) {
                logger.error("person_passwordRecovery - send mail", e);
            }

            return ok("Password was changed successfully");

        } catch (Exception e) {

            // Person Not Found
            try {
                ((_MongoModel_RecoveryToken) recovery_token.newInstance().getByToken(request.password_recovery_token)).deletePermanent();
            } catch (Exception dfe) {}

            return controllerServerError(e);
        }
    }

// ***** Profile Picture ***********************************************************************************************

    protected CompletionStage<Result> update_person_profile_picture(){
        try {

            _MongoModel_AbstractPerson person = person();

            Http.MultipartFormData<Files.TemporaryFile> files = Controller.request().body().asMultipartFormData();

            if (files == null) {
                return CompletableFuture.completedFuture(badRequest("Missing file"));
            }

            Http.MultipartFormData.FilePart<Files.TemporaryFile> file = files.getFile("file");

            if (file == null) {
                return CompletableFuture.completedFuture(badRequest("Missing file"));
            }

            Files.TemporaryFile temporaryFile = file.getRef();

            String type = file.getContentType();

            if (!type.equals("image/png") && !type.equals("image/jpg") && !type.equals("image/jpeg")) {
                return CompletableFuture.completedFuture(badRequest("Wrong file type: " + type + ", must be PNG, JPG or JPEG"));
            }

            if (person.picture != null) {
                this.storageService.remove(person.picture); // TODO maybe handle result?
            }

            return this.storageService.store(temporaryFile.path().toFile(), type, UUID.randomUUID().toString() + type.replace("image/", "."), person.get_path())
                    .thenApply(item -> {
                        person.picture = (StoredItem) item;
                        person.update();

                        return ok(person);
                    });

        } catch (Throwable e) {
            return CompletableFuture.completedFuture(controllerServerError(e));
        }
    }

    protected CompletionStage<Result> person_remove_picture() {
        try {

            _MongoModel_AbstractPerson person = person();

            if (person.picture != null) {

                StoredItem blob = person.picture;
                person.picture = null;
                person.update();
               return this.storageService.remove(blob).thenApply(item -> {
                   return ok(person);
               });

            } else {
                return CompletableFuture.completedFuture(badRequest("There is no picture to remove."));
            }

        } catch (Exception e) {
            return CompletableFuture.completedFuture(controllerServerError(e));
        }
    }

// ***** Get By Token - Get ME *****************************************************************************************

    protected Result person_get_by_token() {
        try {

            PERSON_MODEL person = (PERSON_MODEL) person();
            if (person == null) return forbidden("Account is not authorized");

            return ok(person);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ***** Login & Logout **********************************************************************************************

    protected Result login(Swagger_EmailAndPassword request) {
        try {

            _MongoModel_AbstractPerson person = (_MongoModel_AbstractPerson) person_class.newInstance().getByEmail(request.email.toLowerCase());

            logger.trace("login: request: person found by email: {} state: {}", person.email, person.state);

            if (person.state == RegistrationState.INVITED) {
                logger.trace("login: request: user is not yet registered, but invited {}", person.email);
                return notYetRegistered();
            }

            if(person.state == RegistrationState.REGISTERED_NOT_APPROVED) {
                logger.trace("login: request: user is not yet approved from email, but already registered {}", person.email);
                return notValidated();
            }

            if (!person.checkPassword(request.password)) {
                logger.trace("Email {} -> password are wrong", request.email.toLowerCase());
                return forbidden("Email or password are wrong");
            }

            if (!person.validated) {
                logger.trace("Email {} user is not valid", person.email);
                return notValidated();
            }
            if (person.frozen) {
                logger.trace("Email {} user is frozen", person.email);
                return badRequest("Your account has been temporarily suspended");
            }

            CONNECTION_TOKEN token = connection_token.newInstance();
            token.person_id = person.getObjectId();


            token.save();

            this.project_authenticartion.addPersonAndToken(UUID.fromString(token.token), person.id);

            // Chache Update
            person_class.newInstance().getFinder().byId(person.id);

            Swagger_Login_Token swagger_login_token = new Swagger_Login_Token();
            swagger_login_token.auth_token = UUID.fromString(token.token);

            return ok(swagger_login_token);

        } catch (NotFoundException e) {
            return notFound("User with this Email not Found");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    protected Result logout() {
        try {

            Optional<String> header = Controller.request().header("X-AUTH-TOKEN");
            if (header.isPresent()) {
                UUID token = UUID.fromString(header.get());
                this.project_authenticartion.clean_token(token);
            }

            return ok();

        } catch (Exception e) {
            logger.error("logout", e);
            return ok();
        }
    }
}
