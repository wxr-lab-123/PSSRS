package com.hjm.pojo.DTO;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BatchAddScheduleDTO {

        private List<String> dates;

        @JsonProperty("time_slots")
        private List<TimeSlotDTO> timeSlots;

        @JsonProperty("doctor_id")
        private Long doctorId;

        @JsonProperty("department_id")
        private Long departmentId;

        @JsonProperty("schedule_type")
        private String scheduleType;

        @JsonProperty("max_appointments")
        private Integer maxAppointments;

        @JsonProperty("room_number")
        private String roomNumber;
}



