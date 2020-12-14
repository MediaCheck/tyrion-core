package core.mongo;

import java.lang.annotation.*;

/**
 * For override default database Model
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface _MongoCollectionConfig {
    String database_name() default "";
    String database_url() default "";
}

