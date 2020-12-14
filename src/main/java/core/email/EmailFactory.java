package core.email;

import com.typesafe.config.Config;
import core.email.mandrill.ManDrillEmail;
import core.email.mandrill.MandrillEmailService;
import core.email.sendgrid.SendGridEmail;
import core.email.sendgrid.SendGridEmailService;
import core.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class EmailFactory {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(EmailFactory.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private EmailService emailService;
    private Config config;

    @Inject
    public EmailFactory(Config config){
        this.config = config;
        try {
            switch (config.getString("emailService.type")) {
                case "mandrill" : {
                    this.emailService = new MandrillEmailService(config.getString( config.getString("emailService.type") + ".apiKey"));
                    break;
                }
                case "sendgrid" : {
                    this.emailService = new SendGridEmailService(config.getString( config.getString("emailService.type") + ".apiKey"));
                    break;
                }
                default: {
                    System.err.println("EmailFactory - Missing Config emailService with type. Supported only mandrill od sendgrid");
                    this.emailService = new MandrillEmailService(config.getString("mandrill.apiKey"));
                }
            }

        } catch (Exception e){
            System.err.println("EmailFactory - Error - Please check it");
            logger.error("Error initializing EmailFactory: {}", e);
        }
    }

    public EmailService getEmailService(){
        return this.emailService;
    }

    public EmailInterface getEmail(){
        switch (config.getString("emailService.type")) {
            case "mandrill" : {
                return new ManDrillEmail();
            }
            case "sendgrid" : {
                return new SendGridEmail();
            }
            default: {
                System.err.println("EmailFactory - Missing Config emailService with type. Supported only mandrill od sendgrid");
                throw new BadRequestException("EmailFactory  Missing Config emailService with type");
            }
        }
    }
}


