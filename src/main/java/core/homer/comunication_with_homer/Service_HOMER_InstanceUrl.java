package core.homer.comunication_with_homer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class Service_HOMER_InstanceUrl {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(Service_HOMER_InstanceUrl.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private URL default_instance_url;
    private String default_instance_url_as_string;
    private String homer_server_url; // Default Homer server url

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    @Inject
    public Service_HOMER_InstanceUrl(Config config) {
        try {

            this.default_instance_url = new URL(config.getString("server." + config.getString("server.mode").toLowerCase() + ".instance_url"));
            this.default_instance_url_as_string = config.getString("server." + config.getString("server.mode").toLowerCase() + ".instance_url");
            this.homer_server_url     = config.getString("server." + config.getString("server.mode").toLowerCase() + ".homer_url");


            logger.debug("Service_InstanceUrl: default_instance_url: {}", default_instance_url.getPath());
            logger.debug("Service_InstanceUrl: default_instance_url_as_string: {}", default_instance_url_as_string);
            logger.debug("Service_InstanceUrl: homer_server_url: {}", homer_server_url);

        } catch (Exception e) {
            logger.error("Warning - Service_InstanceUrl required default instance URL for Homer. Instance_url is missing for tyrion api");
        }
    }

    /**
     * Hardware model compatible with Homer
     * @param device_compatible_with_homer
     * @return URL
     */
    public URL getInstanceUrl(HomerHardwareAbstractModel device_compatible_with_homer) {
        try {
            // logger.debug("getInstanceUrl (can be null) url: {}", device_compatible_with_homer.getInstanceURL().toString());
            // logger.debug("getInstanceUrl (cannot be null) default url: {}", this.default_instance_url);

            return device_compatible_with_homer.getInstanceURL();

        } catch (MalformedURLException malformedException) {
            return this.default_instance_url;
        }
    }

}