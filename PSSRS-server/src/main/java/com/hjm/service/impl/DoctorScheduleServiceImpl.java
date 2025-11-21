package com.hjm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.hjm.exception.DoctorScheduleException;
import com.hjm.pojo.DTO.BatchAddScheduleDTO;
import com.hjm.pojo.DTO.CopyDSDTO;
import com.hjm.pojo.DTO.DoctorScheduleDTO;
import com.hjm.pojo.DTO.TimeSlotDTO;
import com.hjm.pojo.Entity.DoctorSchedule;
import com.hjm.mapper.DoctorScheduleMapper;
import com.hjm.pojo.VO.DoctorScheduleVO;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IDoctorScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.utils.RedisData;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.hjm.constant.RedisConstants.*;

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

    //创建线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public PageResult listSchedules(DoctorScheduleDTO doctorScheduleDTO) {
        Page<DoctorSchedule> page = new Page<>(doctorScheduleDTO.getPage(), doctorScheduleDTO.getLimit());
        Page<DoctorScheduleVO> result = baseMapper.listSchedules(page, doctorScheduleDTO);
        PageResult pageResult = new PageResult(result.getTotal(), result.getRecords());
        return pageResult;
    }

    public Result<DoctorScheduleVO> getXq(Long id) throws InterruptedException {
        String key = SCHEDULE_DETAIL_KEY + id;
        // 1.查询Redis缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2.Redis无数据 → 返回空或查DB
        if (StrUtil.isBlank(json)) {
            // 说明缓存未预热，先从 DB 查一次
            DoctorScheduleVO vo = baseMapper.getXq(id);
            if (vo == null) return Result.error("排班不存在");

            saveSchedule2Redis(id, 30L);
            return Result.success(vo);
        }

        // 3.RedisData反序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        DoctorScheduleVO vo = data.toBean(DoctorScheduleVO.class);
        Long expireTime = redisData.getExpireTime();

        // 4.未过期 → 直接返回
        if (expireTime > System.currentTimeMillis()) {
            return Result.success(vo);
        }

        // 5.已过期 → 尝试获取锁做异步缓存重建
        String lockKey = LOCK_SCH_KEY + id;
        boolean locked = tryLock(lockKey);

        if (locked) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    saveSchedule2Redis(id, 30L);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unlock(lockKey);
                }
            });
        }

        // 6.返回旧数据（保障可用性）
        return Result.success(vo);
    }

    @Override
    public Result copy(CopyDSDTO copyDSDTO) {
        DoctorSchedule source = getById(copyDSDTO.getSource_schedule_id());
        if (source == null) {
            throw new DoctorScheduleException("源排班不存在");
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

        for (String targetDate : targetDates) {
            String key = SCHEDULE_ + targetDate + ":" + source.getDepartmentId();
            stringRedisTemplate.delete(key);
        }
        if (!newSchedules.isEmpty()) {
            saveBatch(newSchedules);
            return Result.success("成功复制 " + newSchedules.size() + " 条排班记录");
        } else {
            throw new DoctorScheduleException ("所有目标日期均已有排班，未复制任何记录");
        }
    }

    @Override
    public List<DoctorScheduleVO> listScheduleByDid(Long departmentId, String date) {
        String key = SCHEDULE_+date+":"+departmentId;

        if (stringRedisTemplate.hasKey(key)) {
            String value = stringRedisTemplate.opsForValue().get(key);
            // 使用 JSONUtil.parseArray 来确保正确解析
            return JSONUtil.toList(JSONUtil.parseArray(value), DoctorScheduleVO.class);
        }
        Boolean flag = false;
        LocalTime now = LocalTime.now();
        LocalDate localDate = LocalDate.parse(date);
        if (localDate.equals(LocalDate.now())) {
            flag = true;
        }
        List<DoctorScheduleVO> result = baseMapper.listScheduleByDid(departmentId, date,now,flag);

        long expireSeconds = Duration.between(
                LocalDateTime.now(),
                LocalDate.now().plusDays(1).atStartOfDay()
        ).getSeconds();
        // 使用 JSONUtil.toJsonStr 来正确序列化对象
        stringRedisTemplate.opsForValue().set(
                key,
                JSONUtil.toJsonStr(result),
                expireSeconds,
                TimeUnit.SECONDS
        );

        return result;
    }

    @Override
    @Transactional
    public Result batchAdd(BatchAddScheduleDTO dto) {
        List<String> dates = dto.getDates();
        List<TimeSlotDTO> times = dto.getTimeSlots();

        if (dates == null || dates.isEmpty()) {
            throw new DoctorScheduleException("日期不能为空");
        }
        if (times == null || times.isEmpty()) {
            throw new DoctorScheduleException("时段不能为空");
        }

        List<DoctorSchedule> doctorSchedules = new ArrayList<>();

        for (String date : dates) {
            LocalDate scheduleDate = LocalDate.parse(date);

            for (TimeSlotDTO time : times) {

                // 避免重复排班
                boolean exists = lambdaQuery()
                        .eq(DoctorSchedule::getDoctorId, dto.getDoctorId())
                        .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                        .eq(DoctorSchedule::getTimeSlot, time.getTimeSlot())
                        .exists();

                if (exists) {
                    continue; // 或者 throw new Exception
                }

                DoctorSchedule doctorSchedule = new DoctorSchedule();
                doctorSchedule.setScheduleDate(scheduleDate);
                doctorSchedule.setTimeSlot(time.getTimeSlot());
                doctorSchedule.setStartTime(time.getStartTime());
                doctorSchedule.setEndTime(time.getEndTime());
                doctorSchedule.setDoctorId(dto.getDoctorId());
                doctorSchedule.setDepartmentId(dto.getDepartmentId());
                doctorSchedule.setScheduleType(dto.getScheduleType());
                doctorSchedule.setMaxAppointments(dto.getMaxAppointments());
                doctorSchedule.setRoomNumber(dto.getRoomNumber());
                doctorSchedule.setStatus("AVAILABLE");

                doctorSchedules.add(doctorSchedule);
                String key = "SCHEDULE_" + date + ":" + dto.getDepartmentId();

                stringRedisTemplate.delete(key);
            }
        }

        saveBatch(doctorSchedules);
        return Result.success();
    }

    public void saveSchedule2Redis(Long id, Long expireSeconds) throws InterruptedException {
        DoctorScheduleVO result = baseMapper.getXq(id);
        RedisData redisData = new RedisData();
        redisData.setData(result);
        redisData.setExpireTime(System.currentTimeMillis() + expireSeconds * 1000);
        stringRedisTemplate.opsForValue().set(SCHEDULE_DETAIL_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    private Boolean tryLock(String key) {
        boolean flag  = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SCH_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
