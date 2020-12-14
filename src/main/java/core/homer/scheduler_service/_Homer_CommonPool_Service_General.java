package core.homer.scheduler_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;
import core.homer.comunication_with_homer.HomerHardwareAbstractModel;
import core.homer.comunication_with_homer.Service_HOMER_InstanceUrl;
import core.homer.scheduler_service.out._Homer_Message_type;
import core.http.form.FormFactory;
import core.mongo.BaseMongoModel;
import core.mongo._BaseMongoController;
import core.swagger._Swagger_Abstract_Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Singleton
public abstract class _Homer_CommonPool_Service_General<T extends _Swagger_Abstract_Default> extends _BaseMongoController {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_Homer_CommonPool_Service_General.class);

    /* CONTROLLER CONFIGURATION  -------------------------------------------------------------------------------------------*/

    private Service_HOMER_InstanceUrl service_instanceUrl;

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    protected Map<UUID, _Homer_CommonPool_Service_Features> running_operation = new HashMap<>(); // <Ioda_UUID , ID running_operation

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    protected _Homer_CommonPool_Service_General(WSClient ws, FormFactory formFactory, Service_HOMER_InstanceUrl service_instanceUrl) {
        super(ws, formFactory);
        this.service_instanceUrl = service_instanceUrl;
    }

    /* METHODS  --------------------------------------------------------------------------------------------------------*/


    protected CompletionStage<T> start_pool_service(
            HomerHardwareAbstractModel hardware_compatible_with_homer,
            _Homer_CommonPool_Service_Features<T> pool){

        CompletableFuture<T> response = new CompletableFuture<>();

        if (this.running_operation.containsKey(hardware_compatible_with_homer.getUUID())) {
            this.running_operation.get(hardware_compatible_with_homer.getUUID()).stop();
            this.running_operation.remove(hardware_compatible_with_homer.getUUID());
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        future.whenComplete((message, exception) -> {
            if (message != null) {
                response.complete(message);

            } else {
                response.completeExceptionally(exception);
            }
        });

        this.running_operation.put(hardware_compatible_with_homer.getUUID(), pool);

        pool.setFuture(future);

        pool.start();
        return response;
    }

    public void response_from_homer(JsonNode jsonNode) {
        if (jsonNode.has("ioda_uuid")) {
            UUID uuid = UUID.fromString(jsonNode.get("ioda_uuid").asText());
            if (this.running_operation.containsKey(uuid)) {
                this.running_operation.get(uuid).response_from_homer(jsonNode);
            }
        } else {
            logger.error("response_from_homer not contains ioda_uuid: message: {}", jsonNode);
        }
    }
}
