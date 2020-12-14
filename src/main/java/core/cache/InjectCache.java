package core.cache;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.UUID;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that annotated field should be injected with cache. Annotated field has to implement {@link ModelCache}
 * or should be of type {@link org.ehcache.Cache}.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface InjectCache {

    /**
     * Shortcuts for TimeToIdle
     */
    public static final long YearCacheConstant = 60 * 60 * 24 * 30 * 12;
    public static final long MonthCacheConstant = 60 * 60 * 24 * 30;
    public static final long TwoDayCacheConstant = 60 * 60 * 24 * 2;
    public static final long DayCacheConstant = 60 * 60 * 24;
    public static final long HalfDayCacheConstant = 60 * 60 * 12;
    public static final long HourCacheConstant = 60 * 60;
    public static final long ThirtyMinutesCacheConstant = 60 * 30;
    public static final long FifteenMinutesCacheConstant = 60 * 15;
    public static final long TenMinutesCacheConstant = 60 * 10;
    public static final long FiveMinutesCacheConstant = 60 * 5;
    public static final long TwoMinutesCacheConstant = 60 * 2;


    /**
     * Value type that will be cached.
     * @return value
     */
    Class<?> value();

    /**
     * Type that will be used as a key for stored object.
     * @return key value
     */
    Class<?> keyType() default UUID.class;

    /**
     * Time to idle in seconds.
     * If the entity is not touched for the given time, it will be evicted.
     * @return time to idle in seconds
     */
    long duration() default 600;  // 10 minutes

    /**
     * Maximal count of elements held in cache.
     * @return maximal count
     */
    long maxElements() default 100;

    /**
     * Name alias for the cache.
     * @return name
     */
    String name() default "";

    /**
     * Automatic prolonging of storage time.
     * @return name
     */
    boolean automaticProlonging() default true;
}
