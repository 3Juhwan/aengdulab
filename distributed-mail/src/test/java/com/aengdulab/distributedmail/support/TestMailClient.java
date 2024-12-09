package com.aengdulab.distributedmail.support;

import java.util.List;

public interface TestMailClient {

    List<String> getMailReceivedSubscribes(long sentMailCount);

    void deleteAll();
}
