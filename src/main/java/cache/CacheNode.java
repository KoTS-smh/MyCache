package cache;

import lombok.Data;

@Data
public class CacheNode {
    //key
    private String key;
    //缓存对象
    private Object cacheData;
    //数据失效时间
    private long timeout;
    //最后更新时间
    private long lastRefreshTime;
    //后一个缓存
    public CacheNode next;
    //前一个缓存
    public CacheNode pre;

    public CacheNode(String key,Object data, long timeout, long lastRefreshTime){
        this.key = key;
        this.cacheData = data;
        this.timeout = timeout;
        this.lastRefreshTime = lastRefreshTime;
    }
}
