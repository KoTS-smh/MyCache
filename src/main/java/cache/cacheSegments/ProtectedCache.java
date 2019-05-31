package cache.cacheSegments;

import cache.CacheNode;
import cache.exceptions.ExceptionCode;
import cache.exceptions.MyCacheException;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class ProtectedCache implements Cache,DegradableCache{

    private ConcurrentHashMap<String, CacheNode> protectedCache;
    private ProbationCache probationCache;
    private int size;
    private CacheNode head;
    private CacheNode tail;

    public ProtectedCache(int size){
        this.size = (int) (size*0.8);
        this.protectedCache = new ConcurrentHashMap<>(this.size);
    }

    public ProtectedCache(int size,ProbationCache probationCache){
        this.size = (int) (size*0.8);
        this.protectedCache = new ConcurrentHashMap<>(this.size);
        this.probationCache = probationCache;
    }

    @Override
    public void putCache(String key, CacheNode node) {
        if (protectedCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.MULTIPLE_PUT_ERROR);
        }
        if (size == protectedCache.size()){
            degrade();
        }
        if (head == null) {
            head = node;
            tail = node;
        } else {
            head.pre = node;
            node.next = head;
            head = node;
        }
        protectedCache.put(key,node);
    }

    @Override
    public CacheNode get(String key) {
        if (!protectedCache.containsKey(key)){
            return null;
        }
        CacheNode node = protectedCache.get(key);
        moveToHead(node);
        return node;
    }

    @Override
    public boolean contains(String key) {
        return protectedCache.containsKey(key);
    }

    @Override
    public void remove(String key) {
        if (!protectedCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        }
        if (protectedCache.size() == 1){
            head = null;
            tail = null;
            protectedCache.remove(key);
            return;
        }
        if (tail.getKey().equals(key)) {
            tail = tail.pre;
            tail.next.pre = null;
            tail.next = null;
            protectedCache.remove(key);
            return;
        }
        if (head.getKey().equals(key)){
            head = head.next;
            head.pre.next = null;
            head.pre = null;
            protectedCache.remove(key);
            return;
        }
        CacheNode node = protectedCache.get(key);
        node.pre.next = node.next;
        node.next.pre = node.pre;
        protectedCache.remove(key);
    }

    @Override
    public void update(String key, Object value) {
        if (!protectedCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        }
        protectedCache.get(key).setCacheData(value);
    }

    @Override
    public void degrade() {
        probationCache.putCache(tail.getKey(),tail);
        remove(tail.getKey());
    }

    private void moveToHead(CacheNode node){
        if (node.getKey().equals(head.getKey()))
            return;
        if (node.getKey().equals(tail.getKey())){
            tail = tail.pre;
            tail.next = null;
            node.pre = null;
            node.next = head;
            head.pre = node;
            head = node;
            return;
        }
        node.pre.next = node.next;
        node.next.pre = node.pre;
        node.pre = null;
        node.next = head;
        head.pre = node;
        head = node;
    }
}
