package com.hjm.pojo.DTO;

import lombok.Data;

@Data
public class MessageDTO {
    private String message;

    // 发送者 ID
    private String senderId;

    // 发送者角色（例如 doctor 或 admin）
    private String senderRole;

    // 接收者 ID
    private String recipientId;

    // 接收者角色（例如 doctor 或 admin）
    private String recipientRole;

    // 消息类型，例如文本、文件、通知等
    private String messageType;

    // 时间戳
    private long timestamp;

    // 消息的优先级，0为普通，1为重要等
    private int priority;
}
