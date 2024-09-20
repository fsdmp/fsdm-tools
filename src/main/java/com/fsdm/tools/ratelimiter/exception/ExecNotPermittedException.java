package com.fsdm.tools.ratelimiter.exception;

/**
 * Created by @author fsdm on 2022/9/16 4:14 下午.
 */
public class ExecNotPermittedException extends RuntimeException {

    public ExecNotPermittedException(String message, Throwable cause) {
        super(message, cause, false, true);
    }

}
