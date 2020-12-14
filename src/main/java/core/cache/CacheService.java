package core.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.exceptions.NotSupportedException;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final CacheManager cacheManager;
    private final Config config;

    private final List<Cache> caches = new ArrayList<>();

    @Inject
    @SuppressWarnings("unchecked")
    public CacheService(ApplicationLifecycle appLifecycle, Config config) {
        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        this.config = config;

        appLifecycle.addStopHook(() -> {
            this.cacheManager.close();
            return CompletableFuture.completedFuture(null);
        });

        logger.info("init - cache layer initiating");

        long start = System.currentTimeMillis();

        List<String> packages = new ArrayList<>();

        try {
            packages = config.getStringList("cache.packages");
        } catch (Exception e) {
            logger.warn("constructor - error loading scheduled jobs configuration", e);
        }

        ConfigurationBuilder builder = new ConfigurationBuilder().setScanners(new FieldAnnotationsScanner());

        packages.forEach(pack -> builder.addUrls(ClasspathHelper.forPackage(pack)));

        Set<Field> fields = new Reflections(builder).getFieldsAnnotatedWith(InjectCache.class);

        logger.info("init - scanning classes took {} ms", System.currentTimeMillis() - start);



        fields.forEach(field -> {
            try {

                InjectCache annotation = field.getAnnotation(InjectCache.class);

                String name = annotation.name();

                String name_before = name;

                if (name.equals("")) {
                    name = field.getDeclaringClass().getSimpleName();
                }

                logger.debug("init - setting cache before: {} after: {}, field: {}", name_before, name, field.getClass().getSimpleName());

                Object obj = field.get(null);

                if (obj instanceof ModelCache) {
                    ModelCache modelCache = (ModelCache) obj;
                    modelCache.setCache(this.getCache(name, annotation.keyType(), annotation.value(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                    modelCache.setQueryCache(this.getCache(name + "_Query", Integer.class, annotation.keyType(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                } else if (field.getType().equals(Cache.class)) {
                    field.set(null, this.getCache(name, annotation.keyType(), annotation.value(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                } else {
                    throw new NotSupportedException("Cannot inject cache into " + obj.getClass() + ", because it does not implement ModelCache interface.");
                }

            } catch (NullPointerException e) {

            } catch (Exception e) {
                logger.error("init - cache init failed:", e);
            }
        });
    }


    public <K, V> Cache<K, V> getCache(String name, Class<K> keyType, Class<V> cachedType, long maxElements, long duration, boolean timeToIdle) {

        Duration duration1 = Duration.of(duration, TimeUnit.SECONDS);

        Cache<K, V> cache = this.cacheManager.createCache(name,
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(keyType, cachedType, ResourcePoolsBuilder.heap(maxElements))
                        .withExpiry(timeToIdle ? Expirations.timeToIdleExpiration(duration1) : Expirations.timeToLiveExpiration(duration1)).build());

        this.caches.add(cache); // Every cache created by this service is added to this list for future clearing.

        return cache;
    }

    public void clear() {
        this.caches.forEach(Cache::clear);
    }
}
