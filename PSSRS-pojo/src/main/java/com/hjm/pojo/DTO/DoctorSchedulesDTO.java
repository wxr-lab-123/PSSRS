package com.hjm.pojo.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class DoctorSchedulesDTO {
    @JsonProperty("doctor_id")
    private Long doctorId;

    @JsonProperty("department_id")
    private Long departmentId;

    @JsonProperty("schedule_date")
    private String scheduleDate;

    @JsonProperty("time_slot")
    private String timeSlot;

    // If only time is sent like "08:30:39", prefer LocalTime:
    @JsonProperty("start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @JsonProperty("max_appointments")
    private Long maxAppointments;

    @JsonProperty("room_number")
    private Long roomNumber;

    private String notes;
}