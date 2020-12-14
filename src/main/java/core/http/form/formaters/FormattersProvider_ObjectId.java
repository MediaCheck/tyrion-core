package core.http.form.formaters;

import org.bson.types.ObjectId;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.time.LocalTime;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.i18n.MessagesApi;

import java.text.ParseException;
import java.util.regex.Pattern;

@Singleton
public class FormattersProvider_ObjectId implements Provider<Formatters> {

    private final MessagesApi messagesApi;

    @Inject
    public FormattersProvider_ObjectId(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    @Override
    public Formatters get() {
        Formatters formatters = new Formatters(messagesApi);

        formatters.register(ObjectId.class, new SimpleFormatter<ObjectId>() {

            @Override
            public ObjectId parse(String input, Locale l) throws ParseException {
                try {

                    System.out.println("FormattersProvider_ObjectId parse " + input);

                    return new ObjectId(input);
                } catch (Exception e) {
                    throw new ParseException("Invalid ObjectId parse Exception", 0);
                }
            }

            /**
             * Unbinds this field - transforms a concrete value to plain string.
             *
             * @param objectId the value to unbind
             * @param objectId   the current <code>Locale</code>
             * @return printable version of the value
             */
            @Override
            public String print(ObjectId objectId, Locale locale) {
                return objectId.toString();
            }

        });

        return formatters;
    }
}
