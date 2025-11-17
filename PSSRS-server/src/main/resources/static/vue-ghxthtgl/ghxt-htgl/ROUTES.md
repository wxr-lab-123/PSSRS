# 路由配置说明

## 路由概览

本项目路由分为三个模块：
- **公共路由**：登录页（无需认证）
- **管理员路由**：`/admin/*`（需要 ADMIN 角色）
- **医生路由**：`/doctor/*`（需要 DOCTOR 角色）

---

## 一、公共路由

| 路径 | 路由名称 | 组件 | 说明 | 权限 |
|------|---------|------|------|------|
| `/login` | `login` | `Login.vue` | 登录页 | 公开 |

---

## 二、管理员路由（/admin）

所有管理员路由都在 `/admin` 路径下，需要 ADMIN 角色权限。

| 完整路径 | 路由名称 | 组件 | 页面标题 | API 接口前缀 |
|---------|---------|------|---------|------------|
| `/admin` | `dashboard` | `Dashboard.vue` | 仪表盘 | `/api/admin` |
| `/admin/patients` | `patients` | `Patients.vue` | 患者管理 | `/api/admin` |
| `/admin/admins` | `admins` | `Admins.vue` | 管理员管理 | `/api/admin` |
| `/admin/departments` | `departments` | `Departments.vue` | 科室管理 | `/api/admin` |
| `/admin/doctors` | `doctors` | `Doctors.vue` | 医生管理 | `/api/admin` |
| `/admin/schedules` | `schedules` | `Schedules.vue` | 排班管理 | `/api/admin` |
| `/admin/registrations` | `registrations` | `Registrations.vue` | 挂号管理 | `/api/admin` |
| `/admin/orders` | `orders` | `Orders.vue` | 订单管理 | `/api/admin` |
| `/admin/roles` | `roles` | `Roles.vue` | 角色权限 | `/api/admin` |
| `/admin/settings` | `settings` | `Settings.vue` | 系统设置 | `/api/admin` |

### 管理员模块 API 接口示例

#### 认证相关
- `POST /api/admin/auth/login` - 登录
- `POST /api/admin/auth/logout` - 登出

#### 患者管理
- `GET /api/admin/patients` - 获取患者列表（分页）
- `POST /api/admin/patients` - 新增患者
- `PUT /api/admin/patients/:id` - 更新患者
- `DELETE /api/admin/patients/:id` - 删除患者

#### 管理员管理
- `GET /api/admin/admins` - 获取管理员列表（分页）
- `POST /api/admin/admins` - 新增管理员
- `PUT /api/admin/admins/:id` - 更新管理员
- `DELETE /api/admin/admins/:id` - 删除管理员

#### 科室管理
- `GET /api/admin/departments` - 获取科室列表（分页）
- `GET /api/admin/departments?tree=true` - 获取科室树
- `POST /api/admin/departments` - 新增科室
- `PUT /api/admin/departments/:id` - 更新科室
- `DELETE /api/admin/departments/:id` - 删除科室

#### 医生管理
- `GET /api/admin/doctors` - 获取医生列表（分页）
- `POST /api/admin/doctors` - 新增医生
- `PUT /api/admin/doctors/:id` - 更新医生
- `DELETE /api/admin/doctors/:id` - 删除医生

---

## 三、医生路由（/doctor）

所有医生路由都在 `/doctor` 路径下，需要 DOCTOR 角色权限。

| 完整路径 | 路由名称 | 组件 | 页面标题 | API 接口前缀 |
|---------|---------|------|---------|------------|
| `/doctor` | `doctor-schedules` | `doctor/MySchedules.vue` | 我的排班 | `/api/admin` |
| `/doctor/registrations` | `doctor-registrations` | `doctor/MyRegistrations.vue` | 我的挂号 | `/api/admin` |
| `/doctor/profile` | `doctor-profile` | `doctor/Profile.vue` | 个人信息 | `/api/admin` |

---

## 四、路由守卫规则

### 认证检查
1. 如果访问非公开路由且无 token → 跳转到 `/login`
2. 如果访问需要特定角色的路由且用户无对应角色 → 跳转到角色对应首页
   - ADMIN 角色 → `/admin`
   - DOCTOR 角色 → `/doctor`

### 角色权限
- `meta.roles: ['ADMIN']` - 仅管理员可访问
- `meta.roles: ['DOCTOR']` - 仅医生可访问
- `meta.public: true` - 公开路由，无需认证

---

## 五、如何查看完整路由

### 方法 1：查看路由配置文件
文件位置：`src/router/index.js`

### 方法 2：在浏览器控制台
```javascript
// 查看所有路由
console.log(router.getRoutes())

// 查看当前路由
console.log(router.currentRoute.value)
```

### 方法 3：使用 Vue DevTools
在浏览器中安装 Vue DevTools 插件，可以直观查看路由状态。

---

## 六、路由跳转方式

### 在组件中使用
```vue
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 编程式导航
router.push('/admin/departments')
router.replace('/admin/patients')

// 使用路由名称
router.push({ name: 'departments' })
</script>

<template>
  <!-- 声明式导航 -->
  <router-link to="/admin/departments">科室管理</router-link>
</template>
```

### 在菜单中使用
```vue
<el-menu :default-active="$route.path" router>
  <el-menu-item index="/admin/departments">科室管理</el-menu-item>
</el-menu>
```

---

## 七、常见问题

### Q: 新增科室的路由是什么？
A: 
- **前端路由**：`/admin/departments`（科室管理页面）
- **API 路由**：`POST /api/admin/departments`

### Q: 如何添加新路由？
A: 在 `src/router/index.js` 的 `routes` 数组中添加新的路由配置。

### Q: 路由跳转失败怎么办？
A: 检查：
1. 路由路径是否正确
2. 是否已登录（有 token）
3. 用户角色是否有权限访问
4. 查看浏览器控制台的错误信息

