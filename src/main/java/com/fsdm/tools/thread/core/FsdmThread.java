package com.fsdm.tools.thread.core;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by @author liushouyun on 2023/1/29 2:46 下午.
 */
@Slf4j
public class FsdmThread extends Thread {

    public FsdmThread(Runnable target, String name) {
        super(() -> {
            try {
                target.run();
            } catch (Exception e) {
                log.error("FsdmThread name:{} err", name, e);
                throw e;
            }
        }, name);
    }
}
