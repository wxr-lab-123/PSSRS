package com.hjm.WebSocket;

import com.hjm.pojo.DTO.MessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 医生和管理员都可以发送消息，接收方是根据角色来决定
    @MessageMapping("/send/message")
    public void sendMessage(MessageDTO messageDTO) {
        // 判断角色类型并发送到相应的目标地址
        if (messageDTO.getSenderRole().equals("doctor")) {
            // 发送给医生
            messagingTemplate.convertAndSend("/topic/doctor/" + messageDTO.getRecipientId(), messageDTO);
        } else if (messageDTO.getSenderRole().equals("admin")) {
            // 发送给管理员
            messagingTemplate.convertAndSend("/topic/admin/" + messageDTO.getRecipientId(), messageDTO);
        }
    }
}
