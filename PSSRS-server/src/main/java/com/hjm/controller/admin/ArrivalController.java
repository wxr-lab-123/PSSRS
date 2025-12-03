package com.hjm.controller.admin;

import com.hjm.result.Result;
import com.hjm.WebSocket.WebSocketServer;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ArrivalController {

    @Resource
    private WebSocketServer webSocketServer;

    @PostMapping("/patient-arrival")
    public Result<String> patientArrival(@RequestBody Map<String,Object> body) {
        String doctorId = String.valueOf(body.getOrDefault("doctorId",""));
        String patientName = String.valueOf(body.getOrDefault("patientName",""));
        String roomName = String.valueOf(body.getOrDefault("roomName",""));
        String payload = "{"+
                "\"type\":\"PATIENT_ARRIVAL\","+
                "\"doctorId\":\"" + doctorId + "\","+
                "\"patientName\":\"" + patientName + "\","+
                "\"roomName\":\"" + roomName + "\","+
                "\"timestamp\":" + System.currentTimeMillis() +
                "}";
        webSocketServer.sendMessage(doctorId, payload, "doctor");
        return Result.success("ok");
    }
}
