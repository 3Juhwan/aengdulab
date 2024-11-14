package com.aengdulab.trenditem.supports;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeMeasure {

    private static final Logger log = LoggerFactory.getLogger(TimeMeasure.class);

    public static <T> T measureTime(Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        T result = supplier.get();
        long endTime = System.currentTimeMillis();
        log.info("수행 시간 : {}ms", (endTime - startTime));

        return result;
    }
}
