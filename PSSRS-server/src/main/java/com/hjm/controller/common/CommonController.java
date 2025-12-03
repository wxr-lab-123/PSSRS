package com.hjm.controller.common;

import com.hjm.constant.MessageConstant;
import com.hjm.result.Result;
import com.hjm.utils.AliOSSUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@Api(tags = "通用接口")
public class CommonController {
    @Autowired
    private AliOSSUtil ossUtil;
    @PostMapping("/api/admin/files/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("文件上传: {}  文件夹: {}", file.getOriginalFilename(), folder);

        try {
            // 上传逻辑（ossUtil 支持 folder 参数时一起传入）
            String url = ossUtil.upload(file); // 例如上传到 OSS 并返回完整 URL
            String path = folder != null ? folder + "/" + file.getOriginalFilename() : file.getOriginalFilename();

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("path", path);

            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    @PostMapping("/api/admin/notifications/call")
    public Result<String> notifyCall(@RequestBody Map<String, Object> body) {
        try {
            String orderNo = String.valueOf(body.getOrDefault("orderId", ""));
            if (orderNo == null || orderNo.isBlank()) {
                return Result.error("参数错误: 缺少订单号");
            }
            com.hjm.service.IAppointmentOrderService aos = com.hjm.utils.SpringUtils.getBean(com.hjm.service.IAppointmentOrderService.class);
            com.hjm.pojo.Entity.AppointmentOrder order = aos.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.hjm.pojo.Entity.AppointmentOrder>().eq("registration_no", orderNo));
            if (order == null) {
                return Result.error("订单不存在");
            }
            Long patientId = order.getPatientId();
            com.hjm.mapper.DoctorScheduleMapper dsm = com.hjm.utils.SpringUtils.getBean(com.hjm.mapper.DoctorScheduleMapper.class);
            com.hjm.pojo.VO.DoctorScheduleVO vo = dsm.getXq(order.getScheduleId());
            String dep = vo != null ? String.valueOf(vo.getDepartmentName()) : "";
            String doc = vo != null ? String.valueOf(vo.getDoctorName()) : "";
            String room = vo != null ? String.valueOf(vo.getRoomNumber()) : "";
            String reg = String.valueOf(order.getRegistrationNo());
            java.util.Map<String,Object> enriched = new java.util.HashMap<>(body);
            enriched.put("departmentName", dep);
            enriched.put("doctorName", doc);
            enriched.put("roomNumber", room);
            enriched.put("registrationNo", reg);
            String payload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(enriched);
            try {
                com.hjm.service.IPatientMessageService pms = com.hjm.utils.SpringUtils.getBean(com.hjm.service.IPatientMessageService.class);
                String type = String.valueOf(body.getOrDefault("type", "CALL_PATIENT"));
                String doctorIdStr = String.valueOf(body.getOrDefault("doctorId", "0"));
                Long doctorId = 0L;
                try { doctorId = Long.valueOf(doctorIdStr); } catch (Exception ignored) {}
                String content = String.join(", ", dep, doc, room, reg);
                pms.saveForPatients(java.util.List.of(patientId), type, content, doctorId);
            } catch (Exception ignored) {}
            com.hjm.WebSocket.WebSocketServer wss = com.hjm.utils.SpringUtils.getBean(com.hjm.WebSocket.WebSocketServer.class);
            wss.sendMessage(String.valueOf(patientId), payload, "patient");
            return Result.success("已推送");
        } catch (Exception e) {
            return Result.error("推送失败");
        }
    }

}
