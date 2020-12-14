package core.homer.scheduler_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import core.exceptions.BadRequestException;
import core.exceptions.ExternalErrorException;
import core.exceptions.InvalidBodyException;
import core.homer.scheduler_service.out._Homer_Message_type;
import core.http.form.FormFactory;
import core.mongo._BaseMongoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public abstract  class _Homer_Common_TimeOut_Service extends _BaseMongoController {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_Homer_Common_TimeOut_Service.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private Map<UUID, CompletableFuture<JsonNode>> running_operation = new HashMap<>(); // <Ioda_UUID , ID running_operation

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    protected _Homer_Common_TimeOut_Service(WSClient ws, FormFactory formFactory) {
        super(ws, formFactory);
    }

    /* METHODS  --------------------------------------------------------------------------------------------------------*/

    /**
     * @param future
     * @param request
     * @param instance_url
     */
    protected void sendMessage(CompletableFuture<JsonNode> future, _Homer_Message_type request, URL instance_url) {
        try {

            logger.trace("sendMessage: add to running_operation id: {}", request.message_uuid);
            this.running_operation.put(request.message_uuid, future);

            logger.trace("sendMessage: URL: {} Json: {}", instance_url.toString(), Json.prettyPrint(request.json()));

            this.POST(instance_url, 20, Json.toJson(request), null, null);
        } catch (Exception e) {
            logger.error("send: error: {}", e);

            if (this.running_operation.containsKey(request.message_uuid)) {
                this.running_operation.get(request.message_uuid).completeExceptionally(e);
            }
        }
    }

    protected void removeMessage(UUID message_id) {
        this.running_operation.remove(message_id);
    }

    public void response_from_homer(JsonNode jsonNode) {
        try {
            logger.trace("response_from: {}", jsonNode);

            if (jsonNode.has("message_uuid")) {

                UUID uuid = UUID.fromString(jsonNode.get("message_uuid").asText());
                logger.trace("running operation check if exist for message_uuid: {} Size: {}", uuid, this.running_operation.size());
                if (this.running_operation.containsKey(uuid)) {
                    logger.trace("running operation exist with uuid: {}", uuid);
                    this.running_operation.get(uuid).complete(jsonNode);
                } else {
                    logger.warn("running operation not exist with uuid: {}", uuid);
                }
            } else {
                logger.error("response_from_homer not contains ioda_uuid: message: {}", jsonNode);
                throw new BadRequestException("Message Must Contains message_uuid parameter");
            }

        } catch (IllegalArgumentException e) {
            logger.warn("message_uuid is not uuid - probably manual Rest Request: message uuid: ", jsonNode.get("message_uuid"));
            throw new ExternalErrorException("Response From Homer - Backend Error " + e.getMessage());
        }
    }
}
