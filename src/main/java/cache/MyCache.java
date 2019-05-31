package cache;

import cache.cacheSegments.EdenCache;
import cache.cacheSegments.ProbationCache;
import cache.cacheSegments.ProtectedCache;
import cache.countMinSketch.FrequencySketch;
import cache.utils.TimeUnit;
import lombok.Data;



@Data
public class MyCache {
    //最小缓存容量
    public static final int minCacheSize = 64;
    //最大缓存容量
    public static final int maxCacheSize = 4096;
    //缓存容量
    private int cacheSize;
    //安全队列
    private EdenCache edenCache;
    //缓刑队列
    private ProbationCache probationCache;
    //保护队列
    private ProtectedCache protectedCache;
    //热度表
    private FrequencySketch frequencySketch;
    //缓存过期时间，0表示无过期时间
    private long timeout;

    public MyCache(int cacheSize){
        this.cacheSize = cacheSize<minCacheSize?minCacheSize:(cacheSize>maxCacheSize)?maxCacheSize:cacheSize;
        this.frequencySketch = new FrequencySketch(cacheSize);
        this.edenCache = new EdenCache(cacheSize);
        this.probationCache = new ProbationCache(cacheSize,frequencySketch);
        this.protectedCache = new ProtectedCache(cacheSize);
        protectedCache.setProbationCache(probationCache);
        probationCache.setProtectedCache(protectedCache);
        edenCache.setProbationCache(probationCache);
        edenCache.setProtectedCache(protectedCache);
        timeout = 0;
    }

    public void expireTime(long length, TimeUnit timeUnit){
        switch (timeUnit){
            case SECOND:
                this.timeout = length * 1000 * 1000 * 1000;
                break;
            case MINUTE:
                this.timeout = length * 60 * 1000 * 1000 * 1000;
                break;
            case HOUR:
                this.timeout = length * 60 * 60 * 1000 * 1000 * 1000;
                break;
        }
    }

}
