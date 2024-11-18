package com.aengdulab.trenditem.config;

import com.aengdulab.trenditem.supports.Fixture;
import java.time.Clock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {

    @Bean
    @Primary
    public Clock fixedClock() {
        return Fixture.FIXED_CLOCK;
    }
}
