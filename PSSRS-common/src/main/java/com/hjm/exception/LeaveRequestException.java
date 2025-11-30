package com.hjm.exception;

/**
 * 账号被锁定异常
 */
public class LeaveRequestException extends BaseException {

    public LeaveRequestException() {
    }

    public LeaveRequestException(String msg) {
        super(msg);
    }

}
