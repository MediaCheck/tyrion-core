package core.email.mandrill;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import core.email.EmailInterface;
import core.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MandrillEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MandrillEmailService.class);

    private final MandrillApi mandrillApi;

    public MandrillEmailService(String apiKey) {
        this.mandrillApi = new MandrillApi(apiKey);
    }

    @Override
    public void send(EmailInterface email) {
        try {
            MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().sendTemplate(email.getTemplate(), null , ((ManDrillEmail) email).getMessage(), false);
            logger.info("send():: status:" + messageStatusReports[0].getStatus());
            if (messageStatusReports[0].getRejectReason() != null) {
                logger.info("send():: reject_reason:" + messageStatusReports[0].getRejectReason());
            }
        } catch (Exception e) {
            logger.error("sendBulk", e);
        }
    }
}
