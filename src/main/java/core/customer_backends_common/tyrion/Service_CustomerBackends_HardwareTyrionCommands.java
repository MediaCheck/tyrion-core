package core.customer_backends_common.tyrion;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.customer_backends_common.help.Enum_BoardCommand;
import core.swagger.Swagger_ToTyrion_Board_Command;
import core.homer.comunication_with_homer.HomerHardwareAbstractModel;
import core.http.form.FormFactory;
import core.mongo._BaseMongoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Singleton
public class Service_CustomerBackends_HardwareTyrionCommands extends _BaseMongoController {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(Service_CustomerBackends_HardwareTyrionCommands.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private Config config;
    private Map<String, List<String>> headers = new HashMap<>();
    private URL tyrion_url;

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/


    @Inject
    public Service_CustomerBackends_HardwareTyrionCommands(WSClient ws, FormFactory formFactory,
                                                           Config config) {
        super(ws, formFactory);
        this.config = config;

        List<String> list = new ArrayList<>();
        list.add(config.getString("server." + config.getString("server.mode").toLowerCase()  + ".tyrion_api_key"));
        headers.put("X-AUTH-TOKEN", list);

        try {
            this.tyrion_url = new URL(config.getString("server." + config.getString("server.mode").toLowerCase() + ".backend_url") + "/hardware/command");

        } catch (Exception e) {
            logger.error("Service_HardwareTyrionCommands - Invalid TYRION URL! {}", config.getString("server." + config.getString("server.mode").toLowerCase() + ".backend_url") + "/hardware/command");
        }
    }

    public JsonNode sendCommand(HomerHardwareAbstractModel hardware, Enum_BoardCommand cmd) throws ExecutionException, InterruptedException {

        Swagger_ToTyrion_Board_Command command = new Swagger_ToTyrion_Board_Command();
        command.command = cmd;
        command.hardware_id = hardware.getUUID();

        return PUT(tyrion_url, 60 , null, command.json(), headers);
    }
}
