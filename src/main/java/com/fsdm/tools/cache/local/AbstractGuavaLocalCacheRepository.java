package com.fsdm.tools.cache.local;

import com.fsdm.tools.thread.ThreadPoolFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by @author fsdm on 2022/2/16 11:24 上午.
 */
@Slf4j
public abstract class AbstractGuavaLocalCacheRepository<K, V> {
    private Long refreshAfterWriteMs;
    private Long expireAfterWriteMs;
    private Long maximumSize;
    private ThreadPoolExecutor threadPoolExecutor;

    public AbstractGuavaLocalCacheRepository(Long refreshAfterWriteMs, Long expireAfterWriteMs, Long maximumSize,
                                             ThreadPoolExecutor threadPoolExecutor) {
        this.refreshAfterWriteMs = refreshAfterWriteMs;
        this.expireAfterWriteMs = expireAfterWriteMs;
        this.maximumSize = maximumSize;
        this.threadPoolExecutor = threadPoolExecutor;
        initCacheItem();
    }

    public AbstractGuavaLocalCacheRepository() {
        initCacheItem();
    }


    @PostConstruct
    public void init() {
        /*
            数据启动预热
         */
        final Map<K, V> all = warmUp();
        if (null == all) {
            return;
        }
        cacheItem.putAll(all);
        // 防止putAll预热无效
        for (K k : all.keySet()) {
            getValue(k);
        }
        log.info("cacheItem warm up done~");
    }


    /**
     * 获取值
     *
     * @param key key
     * @return value
     */
    protected V getValue(K key) {
        try {
            return cacheItem.get(key);
        } catch (ExecutionException e) {
            log.error("AbstractCacheRepository getValue err key={}", key, e);
            return null;
        }
    }

    /**
     * 数据加载
     *
     * @param key key
     * @return value
     */
    protected abstract V fetchData(K key);

    /**
     * 数据预热
     *
     * @return value map
     */
    protected abstract Map<K, V> warmUp();


    /**
     * 主动put值
     *
     * @param key   key
     * @param value value
     */
    protected void put(K key, V value) {
        cacheItem.put(key, value);
    }

    /**
     * cache conf
     */
    private LoadingCache<K, V> cacheItem;

    private void initCacheItem() {
        if (null == this.cacheItem) {
            this.cacheItem = newCacheItem();
        }
    }

    private LoadingCache<K, V> newCacheItem() {
        return CacheBuilder.newBuilder()
                .maximumSize(getMaximumSize())
                .recordStats()
                .refreshAfterWrite(getRefreshAfterWriteMs(), TimeUnit.MILLISECONDS)
                .expireAfterWrite(getExpireAfterWriteMs(), TimeUnit.MILLISECONDS)
                .build(CacheLoader.asyncReloading(new CacheLoader<K, V>() {
                    @Override
                    @ParametersAreNonnullByDefault
                    public V load(K key) {
                        return fetchData(key);
                    }

                    @Override
                    @ParametersAreNonnullByDefault
                    public ListenableFuture<V> reload(K key, V oldValue) throws Exception {
                        return super.reload(key, oldValue);
                    }
                }, getExecutorService()));
    }

    protected long getExpireAfterWriteMs() {
        return null == this.expireAfterWriteMs ? TimeUnit.MINUTES.toMillis(2) : expireAfterWriteMs;
    }

    protected long getRefreshAfterWriteMs() {
        return null == this.refreshAfterWriteMs ? TimeUnit.SECONDS.toMillis(10) : refreshAfterWriteMs;
    }

    protected long getMaximumSize() {
        return null == this.maximumSize ? 128 : maximumSize;
    }

    protected ExecutorService getExecutorService() {
        return null == this.threadPoolExecutor ?
                ThreadPoolFactory.getThreadPool("cache-common-load") : threadPoolExecutor;
    }

}
