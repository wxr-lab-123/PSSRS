# Spring Boot 后端示例代码

## 项目结构

```
hospital-backend/
├── src/main/java/com/hospital/
│   ├── HospitalApplication.java          # 启动类
│   ├── config/                           # 配置类
│   │   ├── CorsConfig.java              # 跨域配置
│   │   └── WebSecurityConfig.java       # 安全配置
│   ├── controller/                       # 控制器
│   │   ├── UserController.java          # 用户控制器
│   │   ├── DepartmentController.java    # 科室控制器
│   │   ├── DoctorController.java        # 医生控制器
│   │   ├── RegistrationController.java  # 挂号控制器
│   │   ├── AppointmentController.java   # 预约控制器
│   │   └── QueryController.java         # 查询控制器
│   ├── entity/                          # 实体类
│   │   ├── User.java
│   │   ├── Department.java
│   │   ├── Doctor.java
│   │   ├── Registration.java
│   │   └── Appointment.java
│   ├── dto/                             # 数据传输对象
│   │   ├── LoginDTO.java
│   │   ├── RegisterDTO.java
│   │   └── RegistrationDTO.java
│   ├── vo/                              # 视图对象
│   │   └── ResultVO.java
│   ├── service/                         # 服务层
│   │   ├── UserService.java
│   │   ├── DepartmentService.java
│   │   ├── DoctorService.java
│   │   ├── RegistrationService.java
│   │   └── AppointmentService.java
│   ├── mapper/                          # MyBatis Mapper
│   │   ├── UserMapper.java
│   │   └── ...
│   └── util/                            # 工具类
│       ├── JwtUtil.java                 # JWT工具
│       └── PasswordUtil.java            # 密码工具
├── src/main/resources/
│   ├── application.yml                  # 配置文件
│   └── mapper/                          # MyBatis XML
└── pom.xml                              # Maven配置
```

## 1. Maven依赖 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.14</version>
    </parent>
    
    <groupId>com.hospital</groupId>
    <artifactId>hospital-backend</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>1.8</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.3</version>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>0.9.1</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Hutool工具类 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.20</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 2. 配置文件 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/hospital?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.hospital.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# JWT配置
jwt:
  secret: hospital-secret-key-2024
  expiration: 604800  # 7天，单位：秒
```

## 3. 统一响应对象

```java
package com.hospital.vo;

import lombok.Data;

@Data
public class ResultVO<T> {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> ResultVO<T> success(String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    public static <T> ResultVO<T> error(String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    public static <T> ResultVO<T> error(Integer code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

## 4. JWT工具类

```java
package com.hospital.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 生成Token
     */
    public String generateToken(Long userId, String phone) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("phone", phone)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 5. 用户实体类

```java
package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String name;
    private String idCard;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

## 6. 用户控制器

```java
package com.hospital.controller;

import com.hospital.dto.LoginDTO;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.User;
import com.hospital.service.UserService;
import com.hospital.util.JwtUtil;
import com.hospital.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO.getPhone(), loginDTO.getPassword());
        
        if (user == null) {
            return ResultVO.error("手机号或密码错误");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        
        // 返回用户信息和Token
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("phone", user.getPhone());
        data.put("name", user.getName());
        data.put("idCard", user.getIdCard());
        data.put("token", token);
        
        return ResultVO.success("登录成功", data);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResultVO<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        // 检查手机号是否已注册
        if (userService.existsByPhone(registerDTO.getPhone())) {
            return ResultVO.error("手机号已注册");
        }
        
        // 注册用户
        User user = userService.register(registerDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("phone", user.getPhone());
        data.put("name", user.getName());
        
        return ResultVO.success("注册成功", data);
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public ResultVO<User> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        User user = userService.getById(userId);
        // 不返回密码
        user.setPassword(null);
        
        return ResultVO.success(user);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResultVO<Void> logout() {
        // 可以在这里实现Token黑名单等逻辑
        return ResultVO.success("退出成功", null);
    }
}
```

## 7. 用户服务层

```java
package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    /**
     * 用户登录
     */
    public User login(String phone, String password) {
        User user = this.lambdaQuery()
                .eq(User::getPhone, phone)
                .one();
        
        if (user == null) {
            return null;
        }
        
        // 验证密码
        if (BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        
        return null;
    }
    
    /**
     * 用户注册
     */
    public User register(RegisterDTO dto) {
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setName(dto.getName());
        user.setIdCard(dto.getIdCard());
        // 密码加密
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

## 8. 挂号控制器

```java
package com.hospital.controller;

import com.hospital.dto.RegistrationDTO;
import com.hospital.entity.Registration;
import com.hospital.service.RegistrationService;
import com.hospital.util.JwtUtil;
import com.hospital.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建挂号
     */
    @PostMapping("/create")
    public ResultVO<Map<String, Object>> createRegistration(
            @RequestHeader("Authorization") String authorization,
            @RequestBody RegistrationDTO dto) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Map<String, Object> result = registrationService.createRegistration(userId, dto);
        
        return ResultVO.success("挂号成功", result);
    }
    
    /**
     * 获取挂号列表
     */
    @GetMapping("/list")
    public ResultVO<Map<String, Object>> getRegistrationList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Map<String, Object> result = registrationService.getRegistrationList(userId, page, pageSize, status);
        
        return ResultVO.success(result);
    }
    
    /**
     * 取消挂号
     */
    @PostMapping("/cancel")
    public ResultVO<Void> cancelRegistration(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Long> params) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long registrationId = params.get("id");
        
        registrationService.cancelRegistration(userId, registrationId);
        
        return ResultVO.success("取消成功", null);
    }
}
```

## 9. 跨域配置

```java
package com.hospital.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

## 10. 启动类

```java
package com.hospital;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hospital.mapper")
public class HospitalApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}
```

## 11. 数据库初始化SQL

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital DEFAULT CHARACTER SET utf8mb4;

USE hospital;

-- 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 科室表
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` varchar(50) NOT NULL COMMENT '科室名称',
  `description` varchar(500) COMMENT '科室描述',
  `location` varchar(100) COMMENT '科室位置',
  `phone` varchar(20) COMMENT '联系电话',
  `work_time` varchar(100) COMMENT '工作时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生表
CREATE TABLE `doctor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `name` varchar(50) NOT NULL COMMENT '医生姓名',
  `title` varchar(50) COMMENT '职称',
  `specialty` varchar(500) COMMENT '擅长领域',
  `introduction` text COMMENT '医生简介',
  `department_id` bigint NOT NULL COMMENT '科室ID',
  `price` decimal(10,2) NOT NULL COMMENT '挂号费',
  `photo` varchar(255) COMMENT '照片URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 挂号表
CREATE TABLE `registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '挂号ID',
  `order_no` varchar(50) NOT NULL COMMENT '挂号单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `department_id` bigint NOT NULL COMMENT '科室ID',
  `registration_date` date NOT NULL COMMENT '挂号日期',
  `time_slot` varchar(20) NOT NULL COMMENT '时段',
  `queue_number` varchar(20) COMMENT '排队号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `patient_phone` varchar(11) NOT NULL COMMENT '患者电话',
  `price` decimal(10,2) NOT NULL COMMENT '挂号费',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_doctor` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号表';

-- 插入测试数据
INSERT INTO department (name, description, location) VALUES
('内科', '内科疾病诊疗', '门诊楼2楼'),
('外科', '外科疾病诊疗', '门诊楼3楼'),
('儿科', '儿童疾病诊疗', '门诊楼1楼');

INSERT INTO doctor (name, title, specialty, department_id, price) VALUES
('张医生', '主任医师', '心血管疾病、高血压、冠心病', 1, 50.00),
('李医生', '副主任医师', '消化系统疾病、胃炎、胃溃疡', 1, 30.00),
('王医生', '主治医师', '呼吸系统疾病、感冒、支气管炎', 1, 20.00);
```

## 12. 运行说明

### 1. 环境要求
- JDK 1.8+
- MySQL 5.7+
- Maven 3.6+

### 2. 启动步骤

```bash
# 1. 创建数据库并执行SQL
mysql -u root -p < init.sql

# 2. 修改配置文件
# 编辑 application.yml，修改数据库连接信息

# 3. 编译项目
mvn clean package

# 4. 运行项目
java -jar target/hospital-backend-1.0.0.jar

# 或使用IDE直接运行 HospitalApplication.main()
```

### 3. 测试接口

```bash
# 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","phone":"13800138000","idCard":"110101199001011234","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456"}'
```

---

**文档版本**: V1.0  
**更新日期**: 2024-11-03
