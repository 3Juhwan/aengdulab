package com.aengdulab.ticket.service.support;

public class SynchronizedServiceWrapper {

    public static void execute(Runnable runnable, Object key) {
        synchronized (key) {
            runnable.run();
        }
    }
}
