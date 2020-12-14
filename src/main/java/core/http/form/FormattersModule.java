package core.http.form;

import com.google.inject.AbstractModule;
import core.http.form.formaters.FormattersProvider_ObjectId;
import play.data.format.Formatters;

public class FormattersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Formatters.class).toProvider(FormattersProvider_ObjectId.class);
    }
}

