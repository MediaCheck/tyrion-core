package core.email;

import java.util.List;

public interface EmailInterface {

    EmailInterface setSubject(String subject);
    EmailInterface setReceivers(List<String> receivers);
    EmailInterface setReceiver(String receiver);
    EmailInterface setAndOverrideDefaultSender(String sender_email, String set_from);
    EmailInterface setTemplate(String mandrill_template);

    /**
     * Get default template, or set template
     * @return Name of Template
     */
    String getTemplate();



    EmailInterface attachmentPDF(String name, byte[] file);

    /* In following methods single html is formed. This html will be inserted in `code` or `text` section of selected template */
    EmailInterface divider();
    EmailInterface text(String text);

    EmailInterface text(String size, String text);
    EmailInterface link(String text, String link);
    EmailInterface table(List<String> headers, List<List<String>> rows);



    /*
        This serves for cases where template is more complex and you need to pass data in some special form
        see SendGrid `Smart Lights Error List` for example.
    */
    EmailInterface customKeyData(String key, Object data);

    public static String bold(String text) {
        return "<strong>" + text + "</strong>";
    }

    public static String text_link(String text, String link) {
        return "<a href=\"" + link + " target=\"_blank\">" + text + "</a>";
    }

    public static String italics(String text) {
        return "<em>" + text + "</em>";
    }

    public static String underline(String text) {
        return "<span style=\"text-decoration: underline;\">" + text + "</span>";
    }

    public static String newLine() {
        return "<br>";
    }
}
