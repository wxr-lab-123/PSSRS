package com.hjm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjm.constant.RedisConstants;
import com.hjm.mapper.PatientMapper;
import com.hjm.mapper.UserPatientMapper;
import com.hjm.pojo.DTO.PatientDTO;
import com.hjm.pojo.Entity.Patient;
import com.hjm.pojo.Entity.UserPatient;
import com.hjm.result.Result;
import com.hjm.service.IUserPatientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserPatientServiceImpl extends ServiceImpl<UserPatientMapper, UserPatient> implements IUserPatientService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PatientMapper patientMapper;

    @Value("${pss.wechat.appid}")
    private String wechatAppid;
    @Value("${pss.wechat.secret}")
    private String wechatSecret;

    @Override
    public Result<String> wechatLogin(String code) {
        String openid = null;
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wechatAppid + "&secret=" + wechatSecret + "&js_code=" + code + "&grant_type=authorization_code";
            String body = HttpUtil.get(url);
            JSONObject obj = JSONUtil.parseObj(body);
            openid = obj.getStr("openid");
            if (StrUtil.isBlank(openid)) {
                String errmsg = obj.getStr("errmsg");
                Integer errcode = obj.getInt("errcode");
                return Result.error("微信登录失败" + (errmsg != null ? ("(" + errcode + ":" + errmsg + ")") : ""));
            }
        } catch (Exception e) {
            return Result.error("微信登录接口异常");
        }
        UserPatient up = getOne(new QueryWrapper<UserPatient>().eq("openid", openid));
        if (up == null) {
            up = new UserPatient();
            up.setOpenid(openid);
            //设置默认昵称,由随机字母和数字生成
            up.setNickname("微信用户" + UUID.randomUUID().toString().substring(0, 6));
            save(up);
        }
        Patient first = patientMapper.selectOne(new QueryWrapper<Patient>().eq("user_patient_id", up.getId()).eq("is_deleted", 0).last("limit 1"));
        String token = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        if (first != null) {
            PatientDTO pd = new PatientDTO();
            pd.setId(first.getId());
            pd.setName(first.getName());
            pd.setPhone(first.getPhone());
            map.putAll(BeanUtil.beanToMap(pd));
        } else {
            Map<String, Object> blank = new HashMap<>();
            blank.put("id", 0L);
            blank.put("name", "");
            blank.put("phone", "");
            map.putAll(blank);
        }
        map.put("upid", up.getId());
        map.put("u_openid", up.getOpenid());
        map.put("u_nickname", up.getNickname());
        map.put("u_phone", up.getPhone());
        Map<String, String> strMap = new HashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String k = String.valueOf(e.getKey());
            Object v = e.getValue();
            strMap.put(k, v == null ? "" : String.valueOf(v));
        }
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, strMap);
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.success(token);
    }
}
