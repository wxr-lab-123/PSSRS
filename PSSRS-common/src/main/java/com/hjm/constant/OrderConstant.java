package com.hjm.constant;

public class OrderConstant {
    public static final Integer TO_BE_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer DELIVERED = 2;
    public static final Integer TO_BE_CONFIRMED = 3;
    public static final Integer CONFIRMED = 4;
    public static final Long DEFAULT_EXPIRE_TIME = 15L;
    public static final String ORDER_DEFAULT_REMARK = "订单创建成功，请在15分钟内完成支付，逾期将自动取消。未支付不占用号源\n";
    public static final String ORDER_PAID_REMARK = "订单支付成功,请在我的挂号中从查看信息\n";

    public static final Integer QUEUE_STATUS_WAITING = 0;
    public static final Integer QUEUE_STATUS_BECALLED = 1;
    public static final Integer QUEUE_STATUS_SERVICING = 2;
    public static final Integer QUEUE_STATUS_SERVICED = 3;
}
