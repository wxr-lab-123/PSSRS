package com.hjm.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {
    private Long expireTime;
    private Object data;
}
