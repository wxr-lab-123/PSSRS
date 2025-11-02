package com.hjm.result;

import lombok.Data;

/**
 * 通用响应封装类
 * 适配格式：
 * {
 *   "code": 0,
 *   "msg": "success",
 *   "data": {...}
 * }
 */
@Data
public class Result<T> {

    /** 状态码：0 表示成功，其他表示错误 */
    private Integer code;

    /** 提示消息 */
    private String msg;

    /** 返回数据 */
    private T data;

    // ======= 静态构造方法 =======

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}
