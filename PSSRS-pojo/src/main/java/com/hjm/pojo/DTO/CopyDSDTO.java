package com.hjm.pojo.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CopyDSDTO {
    private Long source_schedule_id;
    private List<String> target_dates;

}
