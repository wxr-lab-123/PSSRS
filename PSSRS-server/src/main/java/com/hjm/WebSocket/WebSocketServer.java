package com.hjm.WebSocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.CloseReason;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.hjm.utils.SpringUtils;
import com.hjm.mapper.AppointmentOrderMapper;

@Component
@ServerEndpoint("/ws")
public class WebSocketServer {

    // 会话存储，每个用户角色一个会话
    private static final Map<String, java.util.Set<Session>> doctorSessions = new ConcurrentHashMap<>();
    private static final Map<String, java.util.Set<Session>> adminSessions = new ConcurrentHashMap<>();
    private static final Map<String, java.util.Set<Session>> patientSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        String role = session.getRequestParameterMap().getOrDefault("role", java.util.List.of("")).get(0);
        String userId = session.getRequestParameterMap().getOrDefault("userId", java.util.List.of("")).get(0);
        String token = session.getRequestParameterMap().getOrDefault("token", java.util.List.of("")).get(0);

        System.out.println("建立连接: 用户ID = " + userId + ", 角色 = " + role);

        if (token == null || token.isBlank()) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "unauthorized")); } catch (Exception ignored) {}
            return;
        }

        if ("patient".equals(role) && (userId == null || userId.isBlank())) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "missing userId")); } catch (Exception ignored) {}
            return;
        }

        if ("doctor".equals(role)) {
            doctorSessions.computeIfAbsent(userId, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        } else if ("admin".equals(role)) {
            adminSessions.computeIfAbsent(userId, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        } else if ("patient".equals(role)) {
            patientSessions.computeIfAbsent(userId, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        }
    }

    // 关闭连接时清理会话
    @OnClose
    public void onClose(Session session) {
        String role = session.getRequestParameterMap().get("role").get(0);
        String userId = session.getRequestParameterMap().get("userId").get(0);

        // 日志输出连接关闭信息
        System.out.println("关闭连接: 用户ID = " + userId + ", 角色 = " + role);

        if ("doctor".equals(role)) {
            java.util.Set<Session> set = doctorSessions.get(userId);
            if (set != null) { set.remove(session); if (set.isEmpty()) doctorSessions.remove(userId); }
        } else if ("admin".equals(role)) {
            java.util.Set<Session> set = adminSessions.get(userId);
            if (set != null) { set.remove(session); if (set.isEmpty()) adminSessions.remove(userId); }
        } else if ("patient".equals(role)) {
            java.util.Set<Session> set = patientSessions.get(userId);
            if (set != null) { set.remove(session); if (set.isEmpty()) patientSessions.remove(userId); }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket连接异常: 用户ID = "
                + session.getRequestParameterMap().getOrDefault("userId", java.util.List.of("unknown")).get(0)
                + ", 角色 = "
                + session.getRequestParameterMap().getOrDefault("role", java.util.List.of("unknown")).get(0)
                + ", 错误 = " + error.getMessage());
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            if (message == null) return;
            if (message.length() > 8192) {
                try { session.close(new CloseReason(CloseReason.CloseCodes.TOO_BIG, "payload too large")); } catch (Exception ignored) {}
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode msg = mapper.readTree(message);
            String type = msg.path("type").asText("");
            if ("PING".equals(type)) {
                String pong = "{" + "\"type\":\"PONG\",\"ts\":" + System.currentTimeMillis() + "}";
                session.getBasicRemote().sendText(pong);
                return;
            }
            if ("TEST_ECHO".equals(type)) {
                String echo = "{" + "\"type\":\"ECHO\",\"ts\":" + System.currentTimeMillis() + "}";
                session.getBasicRemote().sendText(echo);
                return;
            }
            String role = session.getRequestParameterMap().getOrDefault("role", java.util.List.of("")).get(0);
            if ("LEAVE_REQUEST".equals(type)) {
                String doctorId = msg.path("doctorId").asText("");
                String adminId = resolveAdminId(doctorId);
                if (adminId != null) {
                    sendMessage(adminId, message, "admin");
                } else {
                    broadcastToAdmins(message);
                }
                return;
            }
            if ("LEAVE_STATUS_UPDATE".equals(type)) {
                String doctorId = msg.path("doctorId").asText("");
                if (doctorId != null && !doctorId.isBlank()) {
                    sendMessage(doctorId, message, "doctor");
                }
                String status = msg.path("status").asText("");
                String scheduleId = msg.path("scheduleId").asText("");
                String reason = msg.path("reason").asText("");
                String payload = "{" +
                        "\"type\":\"LEAVE_APPROVAL_RESULT\"," +
                        "\"scheduleId\":\"" + scheduleId + "\"," +
                        "\"doctorId\":\"" + doctorId + "\"," +
                        "\"status\":\"" + status + "\"," +
                        "\"reason\":\"" + reason + "\"," +
                        "\"timestamp\":" + System.currentTimeMillis() +
                        "}";
                sendToRelevantPatientsByScheduleId(scheduleId, payload);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送消息给指定的医生或管理员
    public void sendMessage(String userId, String message, String role) {
        java.util.Set<Session> set = null;
        if ("doctor".equals(role)) set = doctorSessions.get(userId);
        else if ("admin".equals(role)) set = adminSessions.get(userId);
        else if ("patient".equals(role)) set = patientSessions.get(userId);
        if (set == null || set.isEmpty()) { System.out.println("用户ID = " + userId + " 的会话未找到或已关闭！"); return; }
        for (Session s : set) {
            if (s != null && s.isOpen()) {
                try { s.getBasicRemote().sendText(message); } catch (Exception ignored) {}
            }
        }
        System.out.println("消息已发送给用户ID = " + userId + ", 角色 = " + role + ", 会话数 = " + set.size());
    }

    private String resolveAdminId(String doctorId) {
        for (String id : adminSessions.keySet()) return id;
        return null;
    }

    private void broadcastToAdmins(String message) {
        for (java.util.Set<Session> set : adminSessions.values()) {
            for (Session s : set) {
                if (s != null && s.isOpen()) {
                    try { s.getBasicRemote().sendText(message); } catch (Exception ignored) {}
                }
            }
        }
    }

    private void broadcastToPatients(String message) {
        for (java.util.Set<Session> set : patientSessions.values()) {
            for (Session s : set) {
                if (s != null && s.isOpen()) {
                    try { s.getBasicRemote().sendText(message); } catch (Exception ignored) {}
                }
            }
        }
    }

    public void sendToRelevantPatientsByScheduleId(String scheduleIdStr, String message) {
        if (scheduleIdStr == null || scheduleIdStr.isBlank()) return;
        try {
            Long scheduleId = Long.valueOf(scheduleIdStr);
            AppointmentOrderMapper mapper = SpringUtils.getBean(AppointmentOrderMapper.class);
            java.util.List<Long> patientIds = mapper.listPatientIdsByScheduleId(scheduleId);
            if (patientIds == null || patientIds.isEmpty()) return;
            for (Long pid : patientIds) {
                java.util.Set<Session> set = patientSessions.get(String.valueOf(pid));
                if (set == null || set.isEmpty()) continue;
                for (Session s : set) {
                    if (s != null && s.isOpen()) {
                        try { s.getBasicRemote().sendText(message); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public int getOnlineDoctorCount() {
        return doctorSessions.size();
    }
}
