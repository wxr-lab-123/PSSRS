package com.hjm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.hjm.pojo.DTO.CopyDSDTO;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.mapper.DoctorScheduleMapper;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 医生排班表 服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-11-05
 */
@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements IDoctorScheduleService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageResult listSchedules(DoctorScheduleDTO doctorScheduleDTO) {
        Page<DoctorSchedule> page = new Page<>(doctorScheduleDTO.getPage(), doctorScheduleDTO.getLimit());
        Page<DoctorScheduleVO> result = baseMapper.listSchedules(page, doctorScheduleDTO);
        PageResult pageResult = new PageResult(result.getTotal(), result.getRecords());
        return pageResult;
    }

    @Override
    public Result<DoctorScheduleVO> getXq(Long id) {
        DoctorScheduleVO result = baseMapper.getXq(id);
        return Result.success(result);
    }

    @Override
    public Result copy(CopyDSDTO copyDSDTO) {
        DoctorSchedule source = getById(copyDSDTO.getSource_schedule_id());
        if (source == null) {
            return Result.error("源排班不存在");
        }

        List<String> targetDates = copyDSDTO.getTarget_dates();
        List<DoctorSchedule> newSchedules = new ArrayList<>();

        for (String targetDate : targetDates) {
            LocalDate date = LocalDate.parse(targetDate);

            // 检查是否已存在同医生、同日期、同时间段的排班
            boolean exists = lambdaQuery()
                    .eq(DoctorSchedule::getDoctorId, source.getDoctorId())
                    .eq(DoctorSchedule::getScheduleDate, date)
                    .eq(DoctorSchedule::getTimeSlot, source.getTimeSlot())
                    .exists();
            if (exists) {
                // 跳过重复的
                continue;
            }

            // 创建新的对象副本
            DoctorSchedule newSchedule = new DoctorSchedule();
            BeanUtil.copyProperties(source, newSchedule);
            newSchedule.setId(null);
            newSchedule.setScheduleDate(date);

            newSchedules.add(newSchedule);
        }

        if (!newSchedules.isEmpty()) {
            saveBatch(newSchedules);
            return Result.success("成功复制 " + newSchedules.size() + " 条排班记录");
        } else {
            return Result.error("所有目标日期均已有排班，未复制任何记录");
        }
    }

    @Override
    public List<DoctorScheduleVO> listScheduleByDid(Long departmentId, String date) {

        if (stringRedisTemplate.hasKey("schedule:"+departmentId+":"+date)){
            String value = stringRedisTemplate.opsForValue().get("schedule:"+departmentId+":"+date);
            // 使用Hutool的JSON工具将字符串转为List<Map>
            List<Map> listMap = JSONUtil.toList(value, Map.class);
            return BeanUtil.copyToList(listMap, DoctorScheduleVO.class);
        }
        List<DoctorScheduleVO> result = baseMapper.listScheduleByDid(departmentId, date);
        //缓存数据
        stringRedisTemplate.opsForValue().set("schedule:"+departmentId+":"+date,result.toString());
        return result;
    }


}
