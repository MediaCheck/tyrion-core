package core.mongo;

import com.google.inject.Inject;
import play.Environment;
import xyz.morphia.mapping.DefaultCreator;

public class ObjectCreator extends DefaultCreator {

    private final Environment environment;

    @Inject
    public ObjectCreator(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected ClassLoader getClassLoaderForClass() {
        return this.environment.classLoader();
    }
}
