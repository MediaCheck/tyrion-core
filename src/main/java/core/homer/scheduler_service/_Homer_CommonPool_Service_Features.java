package core.homer.scheduler_service;

import com.fasterxml.jackson.databind.JsonNode;
import core.homer.scheduler_service.out._Homer_Message_type;
import core.http.form.FormFactory;
import core.mongo._BaseMongoController;
import core.swagger._Swagger_Abstract_Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public abstract class _Homer_CommonPool_Service_Features<T extends _Swagger_Abstract_Default> extends _BaseMongoController {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_Homer_CommonPool_Service_Features.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private URL instance_url;
    private TimeOutService timeOut;
    private CompletableFuture<JsonNode> homer_request_message; // Response From Homer
    protected CompletableFuture<T> final_future;
    protected T final_result;

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    protected _Homer_CommonPool_Service_Features(Class<T> typeArgumentClass, WSClient ws, FormFactory formFactory, TimeOutService timeOut, URL instance_url){
        super(ws, formFactory);
        this.timeOut = timeOut;
        this.instance_url = instance_url;

        try {
            this.final_result = typeArgumentClass.newInstance();
        } catch (Exception e) {
            logger.error("WARNING WARNING WARNING WARNING WARNING WARNING !!! \n" +
                   "Class _Homer_CommonPool_Service_Features in CORE required Valid Class<T ..> Type!  there is a problem with new Instance of this class! check IT. Constructor with arguments is prohibited!");
        }
    }

    /* METHODS  --------------------------------------------------------------------------------------------------------*/

    public abstract void start();

    public void setFuture(CompletableFuture<T> final_future) {
        this.final_future = final_future;
    }

    public void stop() {
        if(this.homer_request_message != null) {
            homer_request_message.completeExceptionally(new TimeoutException("STOP action called"));
        }
    }

    protected void sendCycleRepeatedMessage(_Homer_Message_type command, Integer time, TimeUnit timeUnit, CompletableFuture<JsonNode> future) {
        try {

            homer_request_message = new CompletableFuture<>();
            homer_request_message.applyToEither( this.timeOut.after(time, timeUnit), done -> done
            ).whenComplete( (message, exception) -> {

                if(message != null) {
                    future.complete(message);
                } else {

                    if (exception instanceof ExecutionException) {

                        sendCycleRepeatedMessage(command, 5, TimeUnit.MINUTES, future);

                    } else if (exception instanceof TimeoutException) {

                        sendCycleRepeatedMessage(command, 5, TimeUnit.MINUTES, future);

                    } else {
                        future.completeExceptionally(exception);
                    }
                }
            });

            logger.trace("sendCycleRepeatedMessage: url: {}, object: \n {} ", instance_url, Json.prettyPrint(command.json()));

            this.POST(instance_url, 20, command.json(), null, null);

        } catch (Exception e) {
            logger.error("send: error: {}", e);
            this.homer_request_message.completeExceptionally(e);
        }
    }

    protected void sendOneOffMessage(_Homer_Message_type command, Integer time, TimeUnit timeUnit, CompletableFuture<JsonNode> future) {
        try {
            logger.debug("sendOneOffMessage URL: {}", instance_url.getPath());

            homer_request_message = new CompletableFuture<>();
            homer_request_message.applyToEither( this.timeOut.after(time, timeUnit), done -> done
            ).whenComplete( (message, exception) -> {

                if(message != null) {
                    future.complete(message);
                } else {
                    future.completeExceptionally(exception);
                }
            });

            logger.trace("sendOneOffMessage: send message start: \n{}", Json.prettyPrint(command.json()));

            this.POST(instance_url, 20, command.json(), null, null);

        } catch (Throwable e) {
            logger.error("sendOneOffMessage: error: {}", e);
            this.homer_request_message.completeExceptionally(e);
        }
    }

    public void  response_from_homer(JsonNode jsonNode) {
        // logger.trace("response_from_homer: \n {} ", Json.prettyPrint(jsonNode));
        if(this.homer_request_message != null) {
            // logger.trace("response_from_homer json assign on this.homer_request_message ");
            this.homer_request_message.complete(jsonNode);
        } else {
            logger.error("response_from_homer json is not assign on this.homer_request_message ");
        }
    }
}
