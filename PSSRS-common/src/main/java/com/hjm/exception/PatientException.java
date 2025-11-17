package com.hjm.exception;

/**
 * 账号被锁定异常
 */
public class PatientException extends BaseException {

    public PatientException() {
    }

    public PatientException(String msg) {
        super(msg);
    }

}
