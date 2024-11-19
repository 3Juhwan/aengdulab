package com.aengdulab.distributedmail;

import com.aengdulab.distributedmail.support.MailHogClient;
import com.aengdulab.distributedmail.support.TestMailClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public TestMailClient testMailClient() {
        return new MailHogClient();
    }
}
