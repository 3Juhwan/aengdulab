package com.aengdulab.trenditem.supports;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeMeasure {

    public static <T> T measureTime(Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        T result = supplier.get();
        long endTime = System.currentTimeMillis();
        log.info("수행 시간 : {}ms", (endTime - startTime));

        return result;
    }
}
