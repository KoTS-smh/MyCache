package cache.cacheSegments;

import cache.CacheNode;
import cache.exceptions.ExceptionCode;
import cache.exceptions.MyCacheException;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class EdenCache implements Cache,DegradableCache,UpgradableCache{
    private ProtectedCache protectedCache;
    private ProbationCache probationCache;
    private final ConcurrentHashMap<String, CacheNode> edenCache;
    private int size;
    private CacheNode head;
    private CacheNode tail;

    public EdenCache(int size){
        this.size = size/100+1;
        this.edenCache = new ConcurrentHashMap<>(this.size);
    }

    public EdenCache(int size,ProbationCache probationCache,ProtectedCache protectedCache){
        this.size = size/100+1;
        this.probationCache = probationCache;
        this.protectedCache = protectedCache;
        this.edenCache = new ConcurrentHashMap<>(this.size);
    }

    @Override
    public void putCache(String key, CacheNode cacheNode) {
        if (edenCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.MULTIPLE_PUT_ERROR);
        }
        if (size == edenCache.size()){
            degrade();
        }
        if (head == null) {
            head = cacheNode;
            tail = cacheNode;
        } else {
            head.pre = cacheNode;
            cacheNode.next = head;
            head = cacheNode;
        }
        edenCache.put(key, cacheNode);
    }

    @Override
    public CacheNode get(String key) {
        if (!edenCache.containsKey(key)){
            return null;
        }
        CacheNode result = edenCache.get(key);
        upgrade(key);
        return result;
    }

    @Override
    public boolean contains(String key) {
        return edenCache.containsKey(key);
    }

    @Override
    public void remove(String key) {
        if (!edenCache.containsKey(key))
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        if (edenCache.size() == 1){
            head = null;
            tail = null;
            edenCache.remove(key);
            return;
        }
        if (tail.getKey().equals(key)) {
            tail = tail.pre;
            tail.next.pre = null;
            tail.next = null;
            edenCache.remove(key);
            return;
        }
        if (head.getKey().equals(key)){
            head = head.next;
            head.pre.next = null;
            head.pre = null;
            edenCache.remove(key);
            return;
        }
        CacheNode node = edenCache.get(key);
        node.pre.next = node.next;
        node.next.pre = node.pre;
        edenCache.remove(key);
    }

    @Override
    public void update(String key, Object value) {
        if (!edenCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        }
        edenCache.get(key).setCacheData(value);
    }

    @Override
    public void degrade() {
        probationCache.putCache(tail.getKey(),tail);
        remove(tail.getKey());
    }

    @Override
    public void upgrade(String key) {
        CacheNode node = edenCache.get(key);
        protectedCache.putCache(key,node);
        remove(key);
    }
}
