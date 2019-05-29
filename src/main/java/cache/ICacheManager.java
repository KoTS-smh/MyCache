package cache;

public interface ICacheManager {
    //存入缓存
    void putCache(String key,Object cacheData);

    void putCache(String key,Object cacheData,long timeout);

    //获得缓存
    Object getCache(String key);

    boolean contains(String key);

}
