# Redis验证码实现

## 1. Maven依赖

在 `pom.xml` 中添加Redis依赖：

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redis连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

## 2. Redis配置

在 `application.yml` 中配置Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 如果有密码则填写
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

## 3. Redis配置类

```java
package com.hospital.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

## 4. 短信服务类

```java
package com.hospital.service;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SmsService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis key前缀
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    
    // 验证码有效期（分钟）
    private static final long CODE_EXPIRE_TIME = 5;
    
    // 发送限制时间（秒）
    private static final long SEND_LIMIT_TIME = 60;
    
    /**
     * 发送注册验证码
     */
    public void sendRegisterCode(String phone) {
        // 检查发送频率限制
        checkSendLimit(phone);
        
        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 存储到Redis，设置5分钟过期
        String key = SMS_CODE_PREFIX + "register:" + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 设置发送限制，60秒内不能重复发送
        String limitKey = SMS_LIMIT_PREFIX + phone;
        redisTemplate.opsForValue().set(limitKey, "1", SEND_LIMIT_TIME, TimeUnit.SECONDS);
        
        // TODO: 调用短信服务商API发送短信
        // 这里使用日志模拟
        System.out.println("发送注册验证码到手机：" + phone + "，验证码：" + code);
        
        // 实际项目中，这里应该调用阿里云、腾讯云等短信服务
        // 例如：smsClient.send(phone, code);
    }
    
    /**
     * 发送登录验证码
     */
    public void sendLoginCode(String phone) {
        checkSendLimit(phone);
        
        String code = RandomUtil.randomNumbers(6);
        String key = SMS_CODE_PREFIX + "login:" + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        String limitKey = SMS_LIMIT_PREFIX + phone;
        redisTemplate.opsForValue().set(limitKey, "1", SEND_LIMIT_TIME, TimeUnit.SECONDS);
        
        System.out.println("发送登录验证码到手机：" + phone + "，验证码：" + code);
    }
    
    /**
     * 验证注册验证码
     */
    public boolean verifyRegisterCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + "register:" + phone;
        String savedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (savedCode == null) {
            return false;
        }
        
        // 验证成功后删除验证码
        if (savedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
    /**
     * 验证登录验证码
     */
    public boolean verifyLoginCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + "login:" + phone;
        String savedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (savedCode == null) {
            return false;
        }
        
        if (savedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查发送频率限制
     */
    private void checkSendLimit(String phone) {
        String limitKey = SMS_LIMIT_PREFIX + phone;
        Object limit = redisTemplate.opsForValue().get(limitKey);
        
        if (limit != null) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }
    }
}
```

## 5. 短信控制器

```java
package com.hospital.controller;

import com.hospital.service.SmsService;
import com.hospital.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/sms")
public class SmsController {
    
    @Autowired
    private SmsService smsService;
    
    /**
     * 发送注册验证码
     * 使用 @RequestParam 接收参数
     */
    @PostMapping("/sendRegisterCode")
    public ResultVO<Void> sendRegisterCode(
            @RequestParam("phone") String phone, 
            HttpSession session) {
        try {
            smsService.sendRegisterCode(phone, session);
            return ResultVO.success("验证码已发送", null);
        } catch (Exception e) {
            return ResultVO.error(e.getMessage());
        }
    }
    
    /**
     * 发送登录验证码
     */
    @PostMapping("/sendLoginCode")
    public ResultVO<Void> sendLoginCode(
            @RequestParam("phone") String phone,
            HttpSession session) {
        try {
            smsService.sendLoginCode(phone, session);
            return ResultVO.success("验证码已发送", null);
        } catch (Exception e) {
            return ResultVO.error(e.getMessage());
        }
    }
}
```

**说明**: 
- 使用 `@RequestParam` 接收参数，而不是 `@RequestBody`
- 支持表单提交和URL参数两种方式
- 不需要单独的验证码校验接口，验证码在注册接口中自动校验

## 6. DTO类

```java
package com.hospital.dto;

import lombok.Data;

@Data
public class SmsDTO {
    private String phone;
    private String code;
}
```

## 7. 修改注册服务

```java
package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private SmsService smsService;
    
    /**
     * 用户注册（带验证码验证）
     */
    public User register(RegisterDTO dto) {
        // 验证验证码
        boolean valid = smsService.verifyRegisterCode(dto.getPhone(), dto.getCode());
        if (!valid) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 检查手机号是否已注册
        if (existsByPhone(dto.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }
        
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setName(dto.getName());
        user.setIdCard(dto.getIdCard());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        
        this.save(user);
        return user;
    }
    
    /**
     * 检查手机号是否已存在
     */
    public boolean existsByPhone(String phone) {
        return this.lambdaQuery()
                .eq(User::getPhone, phone)
                .count() > 0;
    }
}
```

## 8. 修改RegisterDTO

```java
package com.hospital.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String phone;
    private String code;      // 新增验证码字段
    private String idCard;
    private String password;
}
```

## 9. Redis Key设计

### Key命名规范
```
sms:code:register:{phone}  - 注册验证码
sms:code:login:{phone}     - 登录验证码
sms:limit:{phone}          - 发送频率限制
```

### 过期时间
- 验证码：5分钟
- 发送限制：60秒

## 10. 测试验证码功能

### 使用Redis CLI测试

```bash
# 连接Redis
redis-cli

# 查看所有验证码
keys sms:code:*

# 查看特定手机号的验证码
get sms:code:register:13800138000

# 查看过期时间
ttl sms:code:register:13800138000

# 删除验证码
del sms:code:register:13800138000
```

### 使用Postman测试

1. **发送验证码**
```
POST http://localhost:8080/api/sms/sendRegisterCode
Content-Type: application/json

{
  "phone": "13800138000"
}
```

2. **查看Redis中的验证码**
```bash
redis-cli
get sms:code:register:13800138000
# 输出: "123456" (6位随机数字)
```

3. **注册（带验证码）**
```
POST http://localhost:8080/api/user/register
Content-Type: application/json

{
  "name": "张三",
  "phone": "13800138000",
  "code": "123456",  // 使用Redis中查到的验证码
  "idCard": "110101199001011234",
  "password": "123456"
}
```

**注意**: 
- 验证码在注册接口中自动校验
- 验证成功后验证码会自动删除
- 无需单独调用验证接口

## 11. 集成真实短信服务

### 阿里云短信服务示例

```java
@Service
public class AliyunSmsService {
    
    private static final String ACCESS_KEY_ID = "your_access_key_id";
    private static final String ACCESS_KEY_SECRET = "your_access_key_secret";
    private static final String SIGN_NAME = "your_sign_name";
    private static final String TEMPLATE_CODE = "your_template_code";
    
    public void sendSms(String phone, String code) {
        DefaultProfile profile = DefaultProfile.getProfile(
            "cn-hangzhou",
            ACCESS_KEY_ID,
            ACCESS_KEY_SECRET
        );
        
        IAcsClient client = new DefaultAcsClient(profile);
        
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phone);
        request.setSignName(SIGN_NAME);
        request.setTemplateCode(TEMPLATE_CODE);
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            if (!"OK".equals(response.getCode())) {
                throw new RuntimeException("短信发送失败：" + response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("短信发送异常", e);
        }
    }
}
```

## 12. 安全建议

1. **防止暴力破解**
   - 限制同一IP每天发送次数
   - 限制同一手机号每天发送次数
   - 验证码错误次数限制

2. **防止短信轰炸**
   - 图形验证码
   - 滑动验证
   - 行为验证

3. **验证码安全**
   - 使用随机数生成
   - 验证后立即删除
   - 设置合理的过期时间

4. **日志记录**
   - 记录发送日志
   - 记录验证日志
   - 异常告警

## 13. 常见问题

### Q1: Redis连接失败
**解决方案**:
- 检查Redis服务是否启动
- 检查防火墙设置
- 检查配置文件中的连接信息

### Q2: 验证码收不到
**解决方案**:
- 检查短信服务商配置
- 查看短信发送日志
- 检查手机号是否正确

### Q3: 验证码总是失效
**解决方案**:
- 检查Redis过期时间设置
- 检查服务器时间是否正确
- 查看Redis日志

---

**文档版本**: V1.0  
**更新日期**: 2024-11-03
