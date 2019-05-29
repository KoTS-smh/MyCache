package cache.countMinSketch;

import cache.utils.TableSize;

//这个类基本上和源码没什么区别
public final class FrequencySketch {

    // A mixture of seeds from FNV-1a, CityHash, and Murmur3,从caffeine源码里搬来的，实在搞不定四种hash方法
    private static final long[] SEED = new long[] {
            0xc3a5c85c97cb3127L, 0xb492b66fbe98f273L, 0x9ae16a3b2f90404fL, 0xcbf29ce484222325L};
    private long[] table;
    private int size;
    private int resetFlag;
    private int count;

    public FrequencySketch(int size){
        this.size = TableSize.tableSizeFor(size);
        this.table = new long[this.size];
        //源码中设的是table大小的10倍
        this.resetFlag = this.size*10;
        count = 0;
    }

    public int frequency(String key){
        int hash = spreadHash(key.hashCode());
        int start = (hash&3)<<2;
        int result = Integer.MAX_VALUE;
        for (int i=0;i<4;i++){
            int index = indexOfTable(hash,i);
            result = Math.min(result,(int)((table[index]>>>((start+i)<<2))&0xfL));
        }
        return result;
    }

    public void increase(String key){
        int hash = spreadHash(key.hashCode());
        int start = (hash&3)<< 2;
        int index0 = indexOfTable(hash,0);
        int index1 = indexOfTable(hash,1);
        int index2 = indexOfTable(hash,2);
        int index3 = indexOfTable(hash,3);
        boolean added = increaseAt(index0,start);
        added |= increaseAt(index1,start+1);
        added |= increaseAt(index2,start+2);
        added |= increaseAt(index3,start+3);
        if (added && (++count) == resetFlag){
            reset();
        }
    }

    private boolean increaseAt(int index,int start){
        int offset = start<<2;
        long mask = 0xfL<<offset;
        if ((table[index]&mask) != mask){
            table[index] += (1L<<offset);
            return true;
        }
        return false;
    }

    private int spreadHash(int hash){
        return hash^(hash>>>16);
    }

    private int indexOfTable(int hash,int i){
        long newHash = hash*SEED[i];
        //让高32位参与运算
        newHash += (newHash>>32);
        return ((int)newHash)&(size-1);
    }

    private void reset(){
        int left = 0;
        for(int i = 0;i<table.length;i++){
            left += Long.bitCount(table[i]&0x1111111111111111L);
            table[i]  = (table[i]>>>1)&0x7777777777777777L;
        }
        count = (count>>>1) - (left>>>2);
    }
}
