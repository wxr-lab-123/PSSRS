package com.hjm.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.hjm.constant.RedisConstants.SCHEDULE_;

@Component
@Slf4j
public class RedisTask {

    @Resource
    private  StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanTodayRegisterCache() {
        stringRedisTemplate.delete(
                stringRedisTemplate.keys(SCHEDULE_+"*")
        );
    }
}
