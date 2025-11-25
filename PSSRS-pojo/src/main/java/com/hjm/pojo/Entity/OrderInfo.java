package com.hjm.pojo.Entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 挂号系统订单表（先订单，再挂号）
 * </p>
 *
 * @author hjm
 * @since 2025-11-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_info")
@ApiModel(value="OrderInfo对象", description="挂号系统订单表（先订单，再挂号）")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单号（唯一）")
    private String orderNo;

    @ApiModelProperty(value = "患者ID")
    private Long patientId;

    @ApiModelProperty(value = "对应的号源ID")
    private Long scheduleId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付方式：0=未选择 1=微信 2=支付宝 3=银行卡")
    private Integer payWay;

    @ApiModelProperty(value = "订单状态：0=待支付 1=已支付 2=已取消 3=退款中 4=已退款")
    private Integer status;

    @ApiModelProperty(value = "订单创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "支付成功时间")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "取消时间")
    private LocalDateTime cancelTime;

    @ApiModelProperty(value = "退款完成时间")
    private LocalDateTime refundTime;

    @ApiModelProperty(value = "订单过期（超时未支付自动取消）时间")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "备注")
    private String remark;


}
