package com.muyuanjin;

import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.enumerate.Gender;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author muyuanjin
 */
public class BenchmarkTest {
    @Test
    void benchmark() throws InterruptedException {
        int n = 5000; //线程数
        int m = 1000; //每个线程个数
        long start = System.nanoTime();
        CountDownLatch count = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                for (int j = 0; j < m; j++) {
                    CustomSerializationEnum.Type.NAME.getDeserializeObj(Gender.class, "男");
//                    CustomSerializationEnum.Type.NAME.getSerializedValue(Gender.MALE);
                }
                count.countDown();
            }).start();
        }
        count.await();
        long l = System.nanoTime() - start;

        System.out.println("运行次数  " + (long) n * m + "次");
        System.out.println("耗时  " + l / 1000000 + "ms");
        System.out.println("平均耗时  " + l / m / n + "ns");
    }
}
