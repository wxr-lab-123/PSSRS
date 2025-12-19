package com.hjm.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("staff_message")
@Data
public class StaffMessage {
    @TableId
    private Long messageId;
    private Long receiverId;
    private Long patientId;
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
