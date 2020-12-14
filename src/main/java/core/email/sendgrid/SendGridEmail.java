package core.email.sendgrid;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Personalization;
import core.email.EmailInterface;
import core.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SendGridEmail implements EmailInterface {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmail.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private Mail message = new Mail();
    private Personalization personalization = new Personalization();
    private StringBuilder emailContentHtml = new StringBuilder();
    private String sender_email = "info@byzance.cz";
    private String set_from = "Byzance IoT Platform";
    private String template = "d-4fac1b8a2a324dce98224b7f33432e09";
    private List<String> recievers = new ArrayList<>();

    /* OPERATION -----------------------------------------------------------------------------------------------------------*/


    public String getTemplate() {
        return this.template;
    }

    public Mail getMessage() {

        message.setFrom(new com.sendgrid.helpers.mail.objects.Email(sender_email, set_from));
        message.setTemplateId(template);

        if(recievers.isEmpty()) {
            throw new BadRequestException("There is no receivers in this email");
        }

        logger.trace("getMessage recievers: {}", recievers);

        recievers.forEach(receiver -> {
            logger.trace("getMessage: set recivers: reciver: {} ", receiver);
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(receiver));
        });

        logger.trace("getMessage subject: {}", this.message.getSubject());

        personalization.setSubject(this.message.getSubject());
        personalization.addDynamicTemplateData("code", emailContentHtml.toString());
        personalization.addDynamicTemplateData("subject", this.message.getSubject());

        message.addPersonalization(personalization);
        return message;
    }

    public SendGridEmail setSubject(String subject) {
        this.message.setSubject(subject);
        return this;
    }

    public SendGridEmail setReceivers(List<String> receivers) {
        logger.trace("setReceivers: {}" , receivers);
        this.recievers.addAll(receivers);
        return this;
    }

    public SendGridEmail setReceiver(String receiver) {
        logger.trace("setReceiver: {}" , receiver);
        this.recievers.add(receiver);
        return this;
    }

    public SendGridEmail setAndOverrideDefaultSender(String sender_email, String set_from) {
        logger.trace("setAndOverrideDefaultSender sender_email: {} set_from {}", sender_email, set_from);

        if(sender_email != null) this.sender_email = sender_email;
        if(set_from != null) this.set_from = set_from;
        return this;
    }

    public SendGridEmail setTemplate(String template) {
        this.template = template;
        return this;
    }

    public SendGridEmail table(List<String> headers, List<List<String>> rows){
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
        personalization.addDynamicTemplateData(key, data);
        return this;
    }

    public SendGridEmail attachmentPDF(String name, byte[] file) {

        Attachments attachments = new Attachments();
        String encodedString = Base64.getEncoder().encodeToString(file);
        attachments.setContent(encodedString);
        attachments.setDisposition("attachment");
        attachments.setFilename("name");
        attachments.setType("application/pdf");
        message.addAttachments(attachments);
        return this;
    }

    @Override
    public SendGridEmail divider() {
        emailContentHtml.append(html.divider.render().body());
        return this;
    }

    @Override
    public SendGridEmail text(String text) {
        text("13", text);
        return this;
    }

    @Override
    public SendGridEmail text(String size, String text) {
        emailContentHtml.append(html.text.render(size + "pt",text).body());

        return this;
    }

    @Override
    public SendGridEmail link(String text, String link) {
        emailContentHtml.append(html.link.render(text,link).body());
        return this;
    }

}
