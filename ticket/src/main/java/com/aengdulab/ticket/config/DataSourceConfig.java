package com.aengdulab.ticket.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    private static final String PRIMARY_DATASOURCE = "ticket.datasource.primary";
    private static final String LOCK_DATASOURCE = "ticket.datasource.lock";

    @ConfigurationProperties(prefix = PRIMARY_DATASOURCE)
    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @ConfigurationProperties(prefix = LOCK_DATASOURCE)
    @Bean
    public DataSource lockDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
}
