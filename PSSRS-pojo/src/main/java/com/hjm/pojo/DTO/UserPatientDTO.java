package com.hjm.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserPatientDTO {
    private Long id;
    private String openid;
    private String nickname;
    private String phone;
    private String avatarUrl;
}
