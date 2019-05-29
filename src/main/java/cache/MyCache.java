package cache;

import cache.cacheSegments.EdenCache;
import cache.cacheSegments.ProbationCache;
import cache.cacheSegments.ProtectedCache;
import cache.countMinSketch.FrequencySketch;
import lombok.Data;


@Data
public class MyCache {
    //最小缓存容量
    public static final int minCacheSize = 64;
    //最大缓存容量
    public static final int maxCacheSize = 1024;
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
    }
}
