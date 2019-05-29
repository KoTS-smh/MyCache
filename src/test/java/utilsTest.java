import cache.CacheNode;
import cache.countMinSketch.FrequencySketch;
import cache.utils.TableSize;
import lombok.Data;

import java.util.HashMap;

public class utilsTest {
    @Data
    private static class Test1{
        private int a;
        private Test2 test2;
    }
    @Data
    private static class Test2{
        private int b;
        private Test1 test1;
    }

    public static void main(String a[]){
        Test1 test1 = new Test1();
        Test2 test2 = new Test2();
        test1.setTest2(test2);
        test2.setTest1(test1);
        test1.a = 100;
        System.out.println(test1.test2.test1.a);
    }

}
