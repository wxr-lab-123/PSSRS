package com.hjm.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final String SCHEDULE_="cache:schedule:";
    public static final String SCHEDULE_DETAIL_KEY = "cache:scheduleDetail:";
    public static final Long SCHEDULE_TTL = 30L;

    public static final String LOCK_SCH_KEY = "lock:schedule:";
    public static final Long LOCK_SCH_TTL = 10L;


}
