package com.hjm.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.hjm.pojo.DTO.PatientAdminSaveDTO;
import com.hjm.pojo.Entity.Patient;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.security.RequiresPermissions;
import com.hjm.service.IPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import static com.hjm.constant.PasswordConstant.DEFAULT_PASSWORD;

//起别名
@RestController("adminPatientController")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;

    @RequiresPermissions({"patients:view"})
    @RequestMapping("/patients")
    public PageResult list(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "gender", required = false) String gender
    ) {
        PageResult pageResult =  patientService.list(page, size, name, phone, gender);
        return pageResult;
    }
    @PostMapping("/patients")
    @RequiresPermissions({"patients:create"})
    public Result save(@RequestBody PatientAdminSaveDTO patientAdminSaveDTO) {
        Patient patient = BeanUtil.copyProperties(patientAdminSaveDTO, Patient.class);
        //md5加密
        patient.setPassword(DigestUtils.md5DigestAsHex(DEFAULT_PASSWORD.getBytes()));
        patientService.save(patient);
        return Result.success();
    }

    @RequiresPermissions({"patients:update"})
    @PutMapping("/patients/{id}")
    public Result update(@RequestBody PatientAdminSaveDTO patientAdminSaveDTO,
                         @PathVariable("id") Long id ) {
        Patient patient = BeanUtil.copyProperties(patientAdminSaveDTO, Patient.class);
        patient.setId(id);
        patientService.updateById(patient);
        return Result.success();
    }

}
