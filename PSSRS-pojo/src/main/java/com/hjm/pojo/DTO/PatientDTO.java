package com.hjm.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
        /** 患者姓名 */
        private String name;

        /** 性别（M：男，F：女） */
        private String gender;

        /** 年龄 */
        private Integer age;

        /** 手机号 */
        private String phone;
        /** 身份证 */
        private String idCard;

}
