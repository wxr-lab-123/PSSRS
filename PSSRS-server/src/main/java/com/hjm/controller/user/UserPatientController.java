package com.hjm.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjm.constant.RedisConstants;
import com.hjm.context.UserPatientContext;
import com.hjm.pojo.DTO.PatientRegisterDTO;
import com.hjm.pojo.Entity.Patient;
import com.hjm.pojo.Entity.UserPatient;
import com.hjm.result.Result;
import com.hjm.service.IPatientService;
import com.hjm.service.IUserPatientService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户患者相关接口控制器
 * 提供微信登录、用户信息修改、患者管理（增删改查）以及切换当前患者等功能
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserPatientController {

    private final IUserPatientService userPatientService;
    private final IPatientService patientService;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    /**
     * 微信登录接口
     * 根据前端传来的code调用微信服务进行登录认证，并返回结果
     *
     * @param body 请求体，必须包含"code"字段用于微信登录凭证校验
     * @return 登录结果，成功时返回token等信息
     */
    @PostMapping("/wechatLogin")
    public Result<String> wechatLogin(@RequestBody java.util.Map<String, String> body) {
        String code = body.get("code");
        return userPatientService.wechatLogin(code);
    }

    /**
     * 更新用户资料并同步到Redis缓存中
     * 支持更新昵称等基本信息，并将变更写入Redis以保持会话一致性
     *
     * @param body 包含待更新的信息，如nickname
     * @return 操作是否成功的统一响应结构
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody Map<String, Object> body) {
        Long uid = UserPatientContext.get().getId();
        if (uid == null) return Result.error("未登录");
        //String avatarUrl = body.get("avatarUrl") == null ? null : String.valueOf(body.get("avatarUrl"));
        String nickname = body.get("nickname") == null ? null : String.valueOf(body.get("nickname"));
        String phone = body.get("phone") == null ? null : String.valueOf(body.get("phone"));
        UserPatient up = new UserPatient();
        up.setId(uid);
        //if (avatarUrl != null) up.setAvatarUrl(avatarUrl);
        if (nickname != null && !nickname.isEmpty()) up.setNickname(nickname);
        if (phone != null && !phone.isEmpty()) up.setPhone(phone);
        userPatientService.updateById(up);
        // 同步更新Redis中的用户信息
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = null;
            if (attrs != null) {
                token = attrs.getRequest().getHeader("authorization");
                if (token == null || token.isBlank()) token = attrs.getRequest().getHeader("Authorization");
            }
            if (token != null && !token.isBlank()) {
                Map<String, String> map = new HashMap<>();
                //if (avatarUrl != null) map.put("u_avatar", avatarUrl);
                if (nickname != null && !nickname.isEmpty()) map.put("u_nickname", nickname);
                if (phone != null && !phone.isEmpty()) map.put("u_phone", phone);
                stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, map);
                stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
            }
        } catch (Exception ignored) {}
        return Result.success();
    }

    /**
     * 获取当前用户的患者列表
     * 查询所有属于该用户且未被删除的患者记录
     *
     * @return 当前用户关联的所有有效患者数据列表
     */
    @GetMapping("/patients")
    public Result<List<Patient>> listPatients() {
        Long uid = UserPatientContext.get().getId();
        List<Patient> list = patientService.list(new QueryWrapper<Patient>().eq("user_patient_id", uid).eq("is_deleted", 0));
        return Result.success(list);
    }

    /**
     * 添加一个新的患者信息
     * 将新患者与当前用户绑定，并设置默认状态为未删除
     *
     * @param p 新增患者的详细信息对象
     * @return 统一操作结果响应
     */
    @PostMapping("/patients")
    public Result addPatient(@RequestBody PatientRegisterDTO p) {
        return patientService.createPatient(p);
    }

    /**
     * 更新指定ID的患者信息
     * 使用路径变量确定要更新的具体患者，并应用请求体中的更改内容
     *
     * @param id 要更新的患者ID
     * @return 统一操作结果响应
     */
    @PutMapping("/patients/{id}")
    public Result updatePatient(@PathVariable Long id, @RequestBody PatientRegisterDTO pdto) {
        Patient p = new Patient();
        p.setId(id);
        if (pdto.getCode() == null || !pdto.getCode().equals(stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + pdto.getPhone()))) {
            return Result.error("验证码错误");
        }
        if (p.getIsDeleted() != null && p.getIsDeleted() == 1) return Result.error("已删除");
        if (pdto.getName() != null) p.setName(pdto.getName()); // Fixed method call to setName
        if (pdto.getPhone() != null) p.setPhone(pdto.getPhone());
        if (pdto.getRelation() != null) p.setRelation(pdto.getRelation());
        Long upId = UserPatientContext.get().getId();
        Patient p2 = patientService.getOne(new QueryWrapper<Patient>().eq("user_patient_id",upId )
                        .eq("relation", "本人")
                );
        if (p2 != null && pdto.getRelation().equals("本人")){
           return Result.error("已存在本人");
        }
        patientService.updateById(p);
        return Result.success();
    }

    /**
     * 删除指定ID的患者（逻辑删除）
     * 实际上是将对应患者的is_deleted字段标记为1，表示已删除
     *
     * @param id 需要删除的患者ID
     * @return 统一操作结果响应
     */
    @DeleteMapping("/patients/{id}")
    public Result deletePatient(@PathVariable Long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setIsDeleted(1);
        patientService.remove(new QueryWrapper<Patient>().eq("id", id));
        return Result.success();
    }

    /**
     * 切换当前活跃患者
     * 根据传入的患者ID查找对应的患者信息，并将其存储在Redis中作为当前活跃患者
     *
     * @param body 请求体，应包含"patientId"字段标识目标患者
     * @return 成功或失败的结果响应
     */
    @PostMapping("/switchPatient")
    public Result switchPatient(@RequestBody java.util.Map<String, Object> body) {
        Object pidObj = body.get("patientId");
        if (!(pidObj instanceof Number)) return Result.error("参数错误");
        Long pid = ((Number) pidObj).longValue();
        Patient p = patientService.getById(pid);
        if (p == null) return Result.error("患者不存在");

        // 构造需要保存至Redis的数据
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("id", p.getId() == null ? "" : String.valueOf(p.getId()));
        map.put("name", p.getName() == null ? "" : String.valueOf(p.getName()));
        map.put("phone", p.getPhone() == null ? "" : String.valueOf(p.getPhone()));

        // 获取当前请求上下文中的token并更新Redis
        org.springframework.web.context.request.ServletRequestAttributes attrs = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        String token = null;
        if (attrs != null) {
            token = attrs.getRequest().getHeader("authorization");
            if (token == null || token.isBlank()) token = attrs.getRequest().getHeader("Authorization");
        }
        if (token == null || token.isBlank()) return Result.error("未登录");

        stringRedisTemplate.opsForHash().putAll(com.hjm.constant.RedisConstants.LOGIN_USER_KEY + token, map);
        stringRedisTemplate.expire(com.hjm.constant.RedisConstants.LOGIN_USER_KEY + token, com.hjm.constant.RedisConstants.LOGIN_USER_TTL, java.util.concurrent.TimeUnit.MINUTES);
        return Result.success();
    }
}
