package com.aengdulab.distributedmail;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ServerPortProvider {

    private final Environment environment;

    public ServerPortProvider(Environment environment) {
        this.environment = environment;
    }

    public int getServerPort() {
        return Integer.parseInt(Objects.requireNonNull(environment.getProperty("local.server.port")));
    }
}
