package com.aengdulab.distributedmail.service.support;

import com.aengdulab.distributedmail.domain.Subscribe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DistributedSupport {

    @Value("${server.id}")
    private int serverId;

    @Value("${server.count}")
    private int serverCount;

    public boolean isMine(Subscribe subscribe) {
        return subscribe.getId() % serverCount == serverId;
    }
}
