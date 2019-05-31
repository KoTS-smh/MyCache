package cache.cacheSegments;

import cache.CacheNode;
import cache.exceptions.ExceptionCode;
import cache.exceptions.MyCacheException;
import cache.countMinSketch.FrequencySketch;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class ProbationCache implements Cache,UpgradableCache{
    private ProtectedCache protectedCache;
    private ConcurrentHashMap<String, CacheNode> probationCache;
    private int size;
    private CacheNode head;
    private CacheNode tail;

    private FrequencySketch frequencySketch;

    public ProbationCache(int size,FrequencySketch frequencySketch){
        this.size = size-(int)(size*0.8)-size/100-1;
        this.probationCache = new ConcurrentHashMap<>(this.size);
        this.frequencySketch = frequencySketch;
    }

    public ProbationCache(int size,ProtectedCache protectedCache,FrequencySketch frequencySketch){
        this.size = size-(int)(size*0.8)-size/100-1;
        this.probationCache = new ConcurrentHashMap<>(this.size);
        this.protectedCache = protectedCache;
        this.frequencySketch = frequencySketch;
    }

    @Override
    public void putCache(String key, CacheNode cacheNode) {
        if (protectedCache.contains(key))
            throw new MyCacheException(ExceptionCode.MULTIPLE_PUT_ERROR);
        if (head == null){
            head = cacheNode;
            tail = cacheNode;
        }
        if (probationCache.size() == size){
            if (compete(key)){
                remove(tail.getKey());
                head.pre = cacheNode;
                cacheNode.next = head;
                head = cacheNode;
            }
        }
        probationCache.put(key, cacheNode);
    }

    @Override
    public CacheNode get(String key) {
        if (!probationCache.containsKey(key)){
            return null;
        }
        CacheNode result = probationCache.get(key);
        upgrade(key);
        return result;
    }

    @Override
    public boolean contains(String key) {
        return probationCache.containsKey(key);
    }

    @Override
    public void remove(String key) {
        if (!probationCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        }
        if (probationCache.size() == 1){
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
        CacheNode node = probationCache.get(key);
        node.pre.next = node.next;
        node.next.pre = node.pre;
        protectedCache.remove(key);
    }

    @Override
    public void update(String key, Object value) {
        if (!probationCache.containsKey(key)){
            throw new MyCacheException(ExceptionCode.CACHE_NOT_EXIST_ERROR);
        }
        probationCache.get(key).setCacheData(value);
    }

    @Override
    public void upgrade(String key) {
        CacheNode cacheNode = probationCache.get(key);
        protectedCache.putCache(key,cacheNode);
        remove(key);
    }

    //淘汰竞争，新进入缓刑队列的缓存会与队尾的缓存进行比较，获胜的能留下来
    private boolean compete(String key){
        int attacker = frequencySketch.frequency(key);
        int victim = frequencySketch.frequency(tail.getKey());
        if (attacker > victim){
            return true;
        }else if (attacker <= 5){// 根据原作者的意思，设置一个热度的阈值，能够提高命中率
            return false;
        }else return victim - attacker <= 2;
    }
}
