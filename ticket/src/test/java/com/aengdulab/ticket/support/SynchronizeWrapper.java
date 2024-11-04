package com.aengdulab.ticket.support;

public class SynchronizeWrapper {

    public static synchronized void execute(Runnable runnable) {
        runnable.run();
    }
}
