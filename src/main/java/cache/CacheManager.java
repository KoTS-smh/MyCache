package cache;

import cache.Exceptions.ExceptionCode;
import cache.Exceptions.MyCacheException;
import cache.cacheSegments.EdenCache;
import cache.cacheSegments.ProbationCache;
import cache.cacheSegments.ProtectedCache;
import cache.countMinSketch.FrequencySketch;

public class CacheManager implements ICacheManager {

    private MyCache myCache;
    private EdenCache edenCache;
    private ProbationCache probationCache;
    private ProtectedCache protectedCache;
    private FrequencySketch frequencySketch;

    public CacheManager(MyCache myCache){
        this.myCache = myCache;
        this.edenCache = myCache.getEdenCache();
        this.probationCache = myCache.getProbationCache();
        this.protectedCache = myCache.getProtectedCache();
        this.frequencySketch = myCache.getFrequencySketch();
    }

    @Override
    public void putCache(String key, Object cacheData) {
        if (isHot(key)){
            CacheNode cacheNode = new CacheNode(key,cacheData,0,System.currentTimeMillis());
            edenCache.putCache(key,cacheNode);
        }
    }

    @Override
    public void putCache(String key, Object cacheData, long timeout) {
        if (isHot(key)){
            CacheNode cacheNode = new CacheNode(key,cacheData,timeout,System.currentTimeMillis());
            edenCache.putCache(key,cacheNode);
        }
    }

    @Override
    public Object getCache(String key) {
        Object data= edenCache.get(key);
        if (data == null)
            data = protectedCache.get(key);
        if (data == null)
            data = probationCache.get(key);
        if (data == null)
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        return data;
    }

    @Override
    public boolean contains(String key) {
        return edenCache.contains(key)||protectedCache.contains(key)||probationCache.contains(key);
    }

    //缓存的加入需要热度累积，只有热度比将要淘汰的高，才有加入的价值
    //如果缓存队列没满，则默认加入，但值至少是2，此过程是预热阶段
    private boolean isHot(String key){
        int adder_feq = frequencySketch.frequency(key);
        if (adder_feq < 2){
            return false;
        }
        if (probationCache.getProbationCache().size() == probationCache.getSize()){
            int victim_feq = frequencySketch.frequency(key);
            return adder_feq > victim_feq;
        }
        return true;
    }
}
