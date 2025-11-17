package com.hjm.handler;

import com.hjm.constant.MessageConstant;
import com.hjm.exception.BaseException;
import com.hjm.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（自定义异常）
     */
    @ExceptionHandler(BaseException.class)
    public Result<?> handleBaseException(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        // 业务异常 → 返回 400
        return Result.error(400, ex.getMessage());
    }
    /**
     * 数据库约束异常（如唯一键冲突）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<?> handleSQLIntegrityException(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库异常：{}", ex.getMessage());
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + " 已存在";
            return Result.error(400, msg);
        } else {
            return Result.error(500, "数据库异常");
        }
    }
    /**
     * 未捕获的其他系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOtherException(Exception ex) {
        log.error("系统异常：", ex);
        return Result.error(500, "系统内部错误");
    }
}
