package core.email.mandrill;

import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import core.email.EmailInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManDrillEmail implements EmailInterface {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(ManDrillEmail.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private MandrillMessage message = new MandrillMessage();
    private List<MandrillMessage.MergeVar> globalMergeVars = new ArrayList<>();

    private StringBuilder emailContentHtml = new StringBuilder();
    private StringBuilder emailContentText = new StringBuilder();
    private String sender_email = "info@byzance.cz";
    private String set_from = "Byzance IoT Platform";
    private String mandrill_template = "byzance-transactional";

    /* OPERATION -----------------------------------------------------------------------------------------------------------*/

    public String getTemplate() {
        return this.mandrill_template;
    }

    public MandrillMessage getMessage() {

        this.globalMergeVars.add(new MandrillMessage.MergeVar("html_content", this.emailContentHtml.toString()));
        this.globalMergeVars.add(new MandrillMessage.MergeVar("text_content", this.emailContentText.toString()));
        this.globalMergeVars.add(new MandrillMessage.MergeVar("subject", this.message.getSubject()));

        this.message.setMergeLanguage("handlebars");
        this.message.setGlobalMergeVars(this.globalMergeVars);
        this.message.setFromEmail(this.sender_email);
        this.message.setFromName(this.set_from);

        return this.message;
    }

    public ManDrillEmail setSubject(String subject) {
        this.message.setSubject(subject);
        return this;
    }

    public ManDrillEmail setReceivers(List<String> receivers) {
        this.message.setTo(receivers.stream().map(email -> {
            MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
            recipient.setEmail(email);
            recipient.setType(MandrillMessage.Recipient.Type.TO);
            return recipient;
        }).collect(Collectors.toList()));
        return this;
    }

    public ManDrillEmail setReceiver(String receiver) {
        MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
        recipient.setEmail(receiver);
        recipient.setType(MandrillMessage.Recipient.Type.TO);
        this.message.setTo(Collections.singletonList(recipient));
        return this;
    }

    public ManDrillEmail setAndOverrideDefaultSender(String sender_email, String set_from) {
        this.sender_email = sender_email;
        this.set_from = set_from;
        return this;
    }
    public ManDrillEmail setTemplate(String mandrill_template) {
        this.mandrill_template = mandrill_template;
        return this;
    }


    public ManDrillEmail attachmentPDF(String name, byte[] file) {

        MandrillMessage.MessageContent content = new MandrillMessage.MessageContent();
        content.setName(name);
        content.setType("application/pdf");
        content.setContent(Base64.getEncoder().encodeToString(file));

        List<MandrillMessage.MessageContent> contents = new ArrayList<>();
        contents.add(content);

        message.setAttachments(contents);

        return this;
    }

    public ManDrillEmail divider() {

        emailContentHtml.append(html.divider.render().body());
        emailContentText.append("\n----------------------------------------------------------\n");

        logger.info("divider():: setting divider");

        return this;
    }

    public ManDrillEmail text(String text) {

        text("13", text);

        return this;
    }

    public ManDrillEmail text(String size, String text) {

        emailContentHtml.append(html.text.render(size + "pt",text).body());
        emailContentText.append("\n");
        emailContentText.append(text);
        emailContentText.append("\n");

        logger.info("text():: setting text");

        return this;
    }

    public ManDrillEmail link(String text, String link) {

        emailContentHtml.append(html.link.render(text,link).body());
        emailContentText.append("\n");
        emailContentText.append(link);
        emailContentText.append("\n");

        logger.info("link():: setting link");

        return this;
    }

    public ManDrillEmail table(List<String> headers, List<List<String>> rows){
        emailContentHtml.append("<table>");
        emailContentHtml.append("<thead>");
        headers.forEach(header -> emailContentHtml.append("<th>").append(header).append("</th>"));
        emailContentHtml.append("</thead>");
        emailContentHtml.append("<tbody>");
        rows.forEach(row -> {
            emailContentHtml.append("<tr>");
            row.forEach(element -> emailContentHtml.append("<td>").append(element).append("</td>"));
            emailContentHtml.append("</tr>");
        });
        emailContentHtml.append("</tbody>");
        emailContentHtml.append("</table>");
        return this;
    }

    @Override
    public EmailInterface customKeyData(String key, Object data) {
        // not supported in mandrill;;
        return this;
    }

}
