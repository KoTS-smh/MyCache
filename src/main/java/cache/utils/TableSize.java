package cache.utils;

import static cache.MyCache.maxCacheSize;
import static cache.MyCache.minCacheSize;

public class TableSize {

    public static int tableSizeFor(int size){
        if (size<=minCacheSize)
            return minCacheSize;
        if (size>=maxCacheSize)
            return maxCacheSize;
        int n = size-1;
        n|=n>>>1;
        n|=n>>>2;
        n|=n>>>4;
        n|=n>>>8;
        n|=n>>>16;
        return n+1;
    }
}
