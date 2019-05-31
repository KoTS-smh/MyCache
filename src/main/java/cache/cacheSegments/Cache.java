package cache.cacheSegments;

import cache.CacheNode;

public interface Cache {

    /*
    缓存中不存在，调用这个方法
     */
    void putCache(String key,CacheNode node);

    /*
    缓存命中，获得对应value
     */
    CacheNode get(String key);

    /*
    缓存命中，返回true
     */
    boolean contains(String key);

    /*
    缓存过时，被淘汰时，调用这个方法
     */
    void remove(String key);

    /*
    数据更新时，调用这个方法，db、Nosql更新成功时调用
     */
    void update(String key,Object value);
}
