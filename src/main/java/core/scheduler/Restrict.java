package core.scheduler;

import core.common.ServerMode;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Restrict {

    ServerMode[] value() default ServerMode.PRODUCTION;
}
