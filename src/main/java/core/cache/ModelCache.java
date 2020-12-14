package core.cache;

import org.ehcache.Cache;

public interface ModelCache<K, V> {

    void setCache(Cache<K, V> cache);

    Cache<K, V> getCache();

    void setQueryCache(Cache<Integer, K> cache);

    Cache<Integer, K> getQueryCache();
}
