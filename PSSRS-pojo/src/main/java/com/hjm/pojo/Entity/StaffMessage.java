package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff_message")
public class StaffMessage {

    //主键
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    //接收者
    private Long receiverId;
    private String messageType;
    private String title;
    private String content;
    private String status;
    private Integer priority;
    private Integer pinned;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime readTime;

}
