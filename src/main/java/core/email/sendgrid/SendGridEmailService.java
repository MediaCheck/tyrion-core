package core.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import core.email.EmailInterface;
import core.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendGridEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmailService.class);

    private final SendGrid sendGridApi;

    public SendGridEmailService(String apiKey) {
        this.sendGridApi = new SendGrid(apiKey);
    }

    @Override
    public void send(EmailInterface email) {
        try {

            logger.trace("Mail Template: {}", email.getTemplate());
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            request.setBody(((SendGridEmail) email).getMessage().build());
            Response response = sendGridApi.api(request);
        } catch (Exception e){
            logger.error("sending SendGrid message error {}", e);
        }
    }
}
