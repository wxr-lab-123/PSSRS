package com.hjm.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final String UPDATE_CODE_KEY = "update:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final String SCHEDULE_="cache:schedule:";
    public static final String SCHEDULE_DETAIL_KEY = "cache:scheduleDetail:";
    public static final Long SCHEDULE_TTL = 30L;

    public static final String ADMIM_PD_CODE = "pdc:code:";
    public static final Long ADMIM_PD_CODE_TTL = 2L;

    public static final String LOCK_SCH_KEY = "lock:schedule:";
    public static final Long LOCK_SCH_TTL = 10L;

    public static final String USER_PERMS_KEY = "perm:user:";
    public static final Long USER_PERMS_TTL = 60L;


    public static final long UPDATE_CODE_TTL = 2L;
}
