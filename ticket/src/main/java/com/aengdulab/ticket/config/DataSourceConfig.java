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

    @ConfigurationProperties(prefix = "ticket.datasource.primary")
    @Primary
    @Bean
    public DataSource primaryDataSource() {
        System.out.println("DataSourceConfig.primaryDataSource");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @ConfigurationProperties(prefix = "ticket.datasource.lock")
    @Bean
    public DataSource lockDataSource() {
        System.out.println("DataSourceConfig.lockDataSource");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
}
