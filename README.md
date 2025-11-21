# 患者自助挂号系统（PSSRS1）
## 文档

- [需求说明书](docs/需求说明书.md)
PSSRS1（Patient Self-Service Registration System）是一套面向医院场景的自助挂号整体解决方案，覆盖患者端挂号/缴费/查询、管理员端科室与医生管理、排班管理等核心业务。项目采用 **Spring Boot + MyBatis-Plus** 构建后端服务，结合 **Vue 3 管理后台** 和 **微信小程序** 前端，辅以 Redis 缓存与 JWT 鉴权，支持快速部署与二次开发。

## 功能概览

| 功能域 | 主要能力 |
| --- | --- |
| 患者服务 | 手机验证码登陆、在线挂号、缴费、取号、挂号记录与缴费记录查询、个人信息管理等 |
| 管理员/医生 | 医护账号管理、科室维护、医生档案与轮班配置、挂号订单管理、角色与权限配置等 |
| 系统支撑 | JWT 登录鉴权、Redis 缓存、分布式锁重建缓存、统一异常处理与结果封装 |

更多患者端功能点参见微信小程序文档《患者自助挂号系统》@PSSRS-server/src/main/resources/static/vue-ghxthtgl/miniprogram-1/README.md#1-143。

## 技术栈

- **后端**：Spring Boot 3.2、MyBatis-Plus、Spring Data Redis、Lombok、Hutool、JWT @PSSRS1/pom.xml#1-94 @PSSRS-server/pom.xml#33-99 @PSSRS-common/pom.xml#18-52
- **数据库 & 缓存**：MySQL、Redis，提供排班及订单缓存策略 @PSSRS-server/src/main/resources/application.yml#1-44 @PSSRS-server/src/main/java/com/hjm/service/impl/DoctorScheduleServiceImpl.java#54-188
- **前端**：Vue 3 + Element Plus（管理后台）、微信小程序 Skyline 渲染引擎（患者端）@PSSRS-server/src/main/resources/static/vue-ghxthtgl/ghxt-htgl/package.json#1-24 @PSSRS-server/src/main/resources/static/vue-ghxthtgl/miniprogram-1/README.md#1-143
- **构建工具**：Maven 多模块、Vite、微信开发者工具

## 模块说明

```
PSSRS1/
├── PSSRS-server    # Spring Boot 后端服务（REST API、缓存、拦截器）
├── PSSRS-common    # 通用模块（常量、异常、上下文、工具封装）
├── PSSRS-pojo      # 领域模型（DTO/VO/Entity）
└── static frontends
    ├── ghxt-htgl/          # Vue 3 管理后台工程（Element Plus）
    └── miniprogram-1/      # 微信小程序工程
```

- **Server**：入口 `PssrsServerApplication`，提供 `/api/admin/**` 与 `/api/**` 业务接口 @PSSRS-server/src/main/java/com/hjm/PssrsServerApplication.java#1-15 @PSSRS-server/src/main/java/com/hjm/controller/admin/UserController.java#48-156 @PSSRS-server/src/main/java/com/hjm/controller/user/AppointmentOrderController.java#22-118。
- **Common**：封装常量、上下文（`PatientContext`）、结果对象和工具类，支撑多模块复用 @PSSRS-common/src/main/java/com/hjm/context/PatientContext.java#1-16。
- **Pojo**：统一维护 Entity/DTO/VO，便于 Service 与控制器解耦 @PSSRS-pojo/src/main/java/com/hjm/pojo/DTO/DoctorDTO.java#1-60。
- **前端工程**：
  - `ghxt-htgl` 为 Vue + Element Plus 管理端，提供排班、用户、科室管理 UI @PSSRS-server/src/main/resources/static/vue-ghxthtgl/ghxt-htgl/README.md#1-20。
  - `miniprogram-1` 为微信小程序，覆盖挂号、缴费、查询等功能，支持 Skyline 渲染与模拟数据 @PSSRS-server/src/main/resources/static/vue-ghxthtgl/miniprogram-1/README.md#1-143。

## 核心后端设计

- **认证鉴权**：
  - 管理端：`JwtTokenUserInterceptor` 校验 `/api/admin/**` JWT，结合 `WebMvcConfiguration` 注册 @PSSRS-server/src/main/java/com/hjm/interceptor/JwtTokenUserInterceptor.java#1-68 @PSSRS-server/src/main/java/com/hjm/config/WebMvcConfiguration.java#17-33。
  - 患者端：`LoginInterceptor` + `ReflsahInterceptor` 守护 `/api/user/**`，刷新 Redis 中的登录态 @PSSRS-server/src/main/java/com/hjm/config/WebMvcConfig.java#20-27。
- **业务分层**：控制器（`controller`）→ 服务（`service` / `impl`）→ 数据访问（`mapper` + XML），遵循单一职责与 DTO/VO 分离。
- **医生排班与缓存策略**：`DoctorScheduleServiceImpl` 通过 Redis 缓存排班详情、使用逻辑过期与分布式锁重建缓存 @PSSRS-server/src/main/java/com/hjm/service/impl/DoctorScheduleServiceImpl.java#61-188。
- **用户体系与 RBAC**：管理员与医生共享用户表，依据角色常量分配权限；`UserServiceImpl` 提供注册、列表、更新等操作 @PSSRS-server/src/main/java/com/hjm/service/impl/UserServiceImpl.java#72-260。
- **统一响应**：所有接口返回 `Result<T>` 包装体，配合 `PageResult` 实现分页数据封装 @PSSRS-server/src/main/java/com/hjm/controller/admin/DepartmentController.java#31-82。

## 数据库与脚本

- 默认数据库 `pssrs`，JDBC 配置见 `application.yml`（请在生产环境前修改默认账号密码）@PSSRS-server/src/main/resources/application.yml#7-44。
- `ghxt-htgl/database_schema.sql` 提供排班、系统设置、通知等表结构与初始化样例，可用于初始化数据库 @PSSRS-server/src/main/resources/static/vue-ghxthtgl/ghxt-htgl/database_schema.sql#1-99。

## 环境准备

| 组件 | 版本建议 |
| --- | --- |
| JDK | 17+ @PSSRS1/pom.xml#35-37 |
| Maven | 3.8+ |
| MySQL | 8.x |
| Redis | 6.x |
| Node.js | 18+（构建 Vue 管理端） |
| 微信开发者工具 | 最新版本（调试小程序） |

## 快速开始

1. **克隆仓库** 并在 MySQL 中创建 `pssrs` 数据库，导入 `database_schema.sql`。
2. **配置后端**：
   - 修改 `PSSRS-server/src/main/resources/application.yml` 中的数据库、Redis、OSS、JWT 配置。
   - 在根目录执行 `mvn clean install`，再进入 `PSSRS-server` 执行 `mvn spring-boot:run` 或打包运行。
3. **启动管理后台**（位于 `PSSRS-server/src/main/resources/static/vue-ghxthtgl/ghxt-htgl`）：
   - `npm install`
   - `npm run dev`
4. **调试小程序**（`miniprogram-1`）：
   - 使用微信开发者工具导入目录，按照 README 说明配置 Skyline 渲染与接口代理。

> **提示**：后端默认暴露 8080 端口（`port: 8080`），若需前后端联调，请确保跨域设置及接口地址正确 @PSSRS-server/src/main/resources/application.yml#1-12。

## 常用目录与文件

- `src/main/java/com/hjm/controller/admin`：管理员端接口（登录、医生/管理员管理、科室、排班等）。
- `src/main/java/com/hjm/controller/user`：患者端接口（挂号、缴费、个人信息等）。
- `src/main/java/com/hjm/service/impl`：核心业务实现，含缓存与事务处理。
- `src/main/java/com/hjm/config`：MyBatis-Plus、拦截器注册、Redis 配置。
- `src/main/resources/mapper`：MyBatis XML 映射文件。
- `static/vue-ghxthtgl`：前端源码（Vue 管理端 + 小程序）。
- `代码优化说明.md`：医生/管理员角色重构说明，可作为阅读服务层设计的参考 @代码优化说明.md#1-190。

## API 总览

- 管理端 `/api/admin/**`
  - 登录：`POST /api/admin/auth/login`
  - 医生管理：注册/查询/更新/删除、按科室筛选 @PSSRS-server/src/main/java/com/hjm/controller/admin/UserController.java#75-123
  - 管理员管理：注册、分页查询、更新、删除 @PSSRS-server/src/main/java/com/hjm/controller/admin/UserController.java#125-156
  - 排班管理：新增、分页查询、复制、批量添加、状态变更 @PSSRS-server/src/main/java/com/hjm/controller/admin/DoctorScheduleController.java#36-115
  - 科室管理：分页 CRUD、列表查询 @PSSRS-server/src/main/java/com/hjm/controller/admin/DepartmentController.java#31-82
- 患者端 `/api/**`
  - 账号：注册、登录、登出、短信验证码、密码重置 @PSSRS-server/src/main/java/com/hjm/controller/user/PatientController.java#36-115
  - 挂号 / 缴费：创建挂号单、支付、取号、取消、查询记录 @PSSRS-server/src/main/java/com/hjm/controller/user/AppointmentOrderController.java#30-115

更多接口示例与字段说明可参见服务层 DTO/VO 定义及 `mapper` SQL。

## 开发约定

- 统一使用 `Result<T>` 返回值与 `PageResult` 分页对象。
- 新增拦截器需在 `WebMvcConfiguration` 或 `WebMvcConfig` 中注册。
- 数据修改操作需关注缓存失效与 Redis Key 命名规范（`SCHEDULE_` 前缀等）。
- 医生/管理员注册默认使用 `PasswordConstant.DEFAULT_PASSWORD`（建议上线前修改）@PSSRS-server/src/main/java/com/hjm/service/impl/UserServiceImpl.java#108-180。

## 后续规划

- 对接医院 HIS 实时数据
- 集成微信/支付宝支付
- 完善消息通知与操作日志
- 增强小程序端安全与数据加密 @PSSRS-server/src/main/resources/static/vue-ghxthtgl/miniprogram-1/README.md#91-125

---

如需更多背景与代码优化信息，请参考根目录下的《代码优化说明.md》。
