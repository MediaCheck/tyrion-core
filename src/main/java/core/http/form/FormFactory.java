package core.http.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import core.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.format.Formatters;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.libs.typedmap.TypedMap;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

/**
 * Helper to create better forms.
 */
@Singleton
public class FormFactory extends play.data.FormFactory {

// LOGGER ##############################################################################################################

    private static final Logger logger = LoggerFactory.getLogger(FormFactory.class);

// Methods  ##############################################################################################################

    @Inject
    public FormFactory(MessagesApi messagesApi, Formatters formatters, ValidatorFactory validatorFactory, Config config) {
       super(messagesApi, formatters, validatorFactory, config);
    }

    /**
     * Automaticaly get Json from request, and valid that with hasErrors immediately in one step
     *
     * @param <T>   the type of value in the form.
     * @param clazz    the class to map to a form.
     * @return a new form that wraps the specified class.
     * @deprecated Use {@link #bodyFromRequest(Http.Request, Class)}
     */
    @Deprecated
    public <T> T formFromRequestWithValidation(Class<T> clazz) {
        try {

            Form<T> form = super.form(clazz);
            Form<T> bind = form.bindFromRequest();

            if (bind.hasErrors()) {
                JsonNode node_errors = bind.errorsAsJson(Lang.forCode("en-US"));
                throw new InvalidBodyException(node_errors);
            }

            return bind.get();
        } catch (ValidationException e) {

            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }

            throw e;
        }
    }

    /**
     * Retrieves the POJO from the JSON body of the request and validates any form constraints.
     * @param request from which the body will be extracted
     * @param clazz return value type
     * @param <T> return value type
     * @return validated instance of T
     */
    public <T> T bodyFromRequest(Http.Request request, Class<T> clazz) {
        try {
            Form<T> bind = this.form(clazz).bindFromRequest(request);
            if (bind.hasErrors()) {
                throw new InvalidBodyException(bind.errorsAsJson());
            }
            return bind.get();
        } catch (ValidationException e) {

            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }

            throw e;
        }
    }

    /**
     * Binds Json data to this form - that is, handles form submission.
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     * @deprecated Use {@link #bodyFromJson(JsonNode, Class)}
     */
    @Deprecated
    public <T> T formFromJsonWithValidation(Class<T> clazz, JsonNode jsonNode) {
        try {

            Form<T> form =  super.form(clazz);
            Form<T> bind =  form.bind(jsonNode);

            if (bind.hasErrors()){
                throw new InvalidBodyException(bind.errorsAsJson(Lang.forCode("en-US")));
            }

            return bind.get();

        } catch (ValidationException e) {

            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }

            throw e;
        }
    }

    /**
     * Retrieves the POJO body from the given JSON and validates any form constraints.
     * @param clazz return value type
     * @param <T> return value type
     * @return validated instance of T
     */
    public <T> T bodyFromJson(JsonNode json, Class<T> clazz) {
        try {
            Form<T> bind = this.form(clazz).bind(Lang.forCode("en-US"), TypedMap.empty(), json);
            if (bind.hasErrors()) {
                throw new InvalidBodyException(bind.errorsAsJson());
            }

            return bind.get();

        } catch (ValidationException e) {

            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }

            throw e;
        }
    }
}
