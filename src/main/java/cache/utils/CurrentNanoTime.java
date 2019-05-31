package cache.utils;

public enum CurrentNanoTime {
    INSTANCE;
    public long read(){
        return System.nanoTime();
    }
}
