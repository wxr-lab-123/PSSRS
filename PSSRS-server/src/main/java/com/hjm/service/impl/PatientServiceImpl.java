package com.hjm.service.impl;

import com.hjm.mapper.PatientMapper;
import com.hjm.pojo.Entity.Patient;
import com.hjm.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 患者用户表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

}
