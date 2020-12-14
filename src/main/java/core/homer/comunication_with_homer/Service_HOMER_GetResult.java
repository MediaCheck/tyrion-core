package core.homer.comunication_with_homer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.homer.scheduler_service.TimeOutService;
import core.homer.scheduler_service._Homer_Common_TimeOut_Service;
import core.homer.scheduler_service._Swagger_BLOCKO_Response_Common;
import core.homer.scheduler_service.out._Homer_Message_type;
import core.http.form.FormFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Slouží k získávání hodnot podle message_type -> variabilní natoli, aby si to mohl na hardwaru někdo stavět sám bez
 * uprav backendu.
 */
@Singleton
public class Service_HOMER_GetResult extends _Homer_Common_TimeOut_Service {


    // LOGGER ##########################################################################################################

    private static final Logger logger = LoggerFactory.getLogger(Service_HOMER_GetResult.class);


    // CONTROLLER CONFIGURATION ########################################################################################

    private TimeOutService timeOut;
    private Service_HOMER_InstanceUrl service_instanceUrl;

    @Inject
    public Service_HOMER_GetResult(WSClient ws, FormFactory formFactory, Service_HOMER_InstanceUrl service_instanceUrl, Config config, TimeOutService timeOut) {
        super(ws, formFactory);
        this.service_instanceUrl = service_instanceUrl;
        this.timeOut = timeOut;

    }

    // METHODS #########################################################################################################

    public CompletionStage<JsonNode> promise_response(HomerHardwareAbstractModel device, _Homer_Message_type request, Integer time_out) {

        CompletableFuture<JsonNode> response = new CompletableFuture<>();

        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        future.applyToEither( this.timeOut.after(time_out, TimeUnit.SECONDS), done -> done
        ).whenComplete( (message, exception) -> {
            try {
                if (message != null) {
                    response.complete(message);
                } else {

                    MessageResponse response_json = new MessageResponse();
                    response_json.ioda_uuid = device.getUUID();
                    response_json.message_uuid = request.ioda_uuid;

                    if (exception instanceof ExecutionException) {

                        response_json.status = "error";
                        response_json.error_message = "ExecutionException";

                    } else if (exception instanceof TimeoutException) {

                        response_json.status = "error";
                        response_json.error_message = "TimeoutException";

                    } else {
                        response_json.status = "error";
                        response_json.error_message = "UnknownException";
                    }
                    response.complete(Json.toJson(response_json));
                }

            } catch (Exception e) {
                logger.error("get_status", e);
                response.completeExceptionally(e);
            }

            this.removeMessage(request.ioda_uuid);
        });

        this.sendMessage(future, request, this.service_instanceUrl.getInstanceUrl(device));
        return response;
    }

    private class MessageResponse extends _Swagger_BLOCKO_Response_Common {
        public String status;
    }

}
