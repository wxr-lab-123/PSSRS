import { createRouter, createWebHistory } from 'vue-router'
import RoleSelect from '../views/RoleSelect.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import DoctorLayout from '../layouts/DoctorLayout.vue'

const Login = () => import('../views/Login.vue')
const Dashboard = () => import('../views/Dashboard.vue')
const Patients = () => import('../views/Patients.vue')
const Departments = () => import('../views/Departments.vue')
const Doctors = () => import('../views/Doctors.vue')
const Admins = () => import('../views/Admins.vue')
const Schedules = () => import('../views/Schedules.vue')
const Registrations = () => import('../views/Registrations.vue')
const Orders = () => import('../views/Orders.vue')
const Roles = () => import('../views/Roles.vue')
const Settings = () => import('../views/Settings.vue')
// 布局改为静态导入，避免生产环境分片加载失败
const DoctorMySchedules = () => import('../views/doctor/MySchedules.vue')
const DoctorMyRegistrations = () => import('../views/doctor/MyRegistrations.vue')
const DoctorProfile = () => import('../views/doctor/Profile.vue')

export const routes = [
  { path: '/login', name: 'login', component: Login, meta: { public: true, title: '登录' } },
  { path: '/role-select', component: RoleSelect },
  { path: '/', redirect: '/login' },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', name: 'dashboard', component: Dashboard, meta: { title: '仪表盘', roles: ['ADMIN'] } },
      { path: 'patients', name: 'patients', component: Patients, meta: { title: '患者管理', roles: ['ADMIN'] } },
      { path: 'admins', name: 'admins', component: Admins, meta: { title: '管理员管理', roles: ['ADMIN'] } },
      { path: 'departments', name: 'departments', component: Departments, meta: { title: '科室管理', roles: ['ADMIN'] } },
      { path: 'doctors', name: 'doctors', component: Doctors, meta: { title: '医生管理', roles: ['ADMIN'] } },
      { path: 'schedules', name: 'schedules', component: Schedules, meta: { title: '排班管理', roles: ['ADMIN'] } },
      { path: 'registrations', name: 'registrations', component: Registrations, meta: { title: '挂号管理', roles: ['ADMIN'] } },
      { path: 'orders', name: 'orders', component: Orders, meta: { title: '订单管理', roles: ['ADMIN'] } },
      { path: 'roles', name: 'roles', component: Roles, meta: { title: '角色权限', roles: ['ADMIN'] } },
      { path: 'settings', name: 'settings', component: Settings, meta: { title: '个人信息设置', roles: ['ADMIN'] } }
    ]
  },
  {
    path: '/doctor',
    component: DoctorLayout,
    meta: { roles: ['DOCTOR'] },
    children: [
      { path: '', name: 'doctor-schedules', component: DoctorMySchedules, meta: { title: '我的排班', roles: ['DOCTOR'] } },
      { path: 'registrations', name: 'doctor-registrations', component: DoctorMyRegistrations, meta: { title: '我的挂号', roles: ['DOCTOR'] } },
      { path: 'profile', name: 'doctor-profile', component: DoctorProfile, meta: { title: '个人信息', roles: ['DOCTOR'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()
  const token = localStorage.getItem('auth_token')
  if (!token) {
    console.log('未找到 token，跳转登录页')
    return next({ path: '/login', replace: true })
  }
  // 基于角色的简单校验
  const rawUser = localStorage.getItem('auth_user')
  let roles = []
  try { 
    const user = JSON.parse(rawUser || 'null')
    roles = user?.roles || []
    console.log('路由守卫 - 当前用户角色:', roles, '目标路由需要:', to.meta.roles)
  } catch (e) {
    console.warn('解析用户信息失败:', e)
  }
  const activeRole = localStorage.getItem('auth_active_role') || ''
  if (roles.length > 1 && !activeRole && to.path !== '/role-select') {
    return next('/role-select')
  }
  const needRoles = to.meta.roles
  if (needRoles && needRoles.length) {
    const ok = activeRole ? needRoles.includes(activeRole) : needRoles.some(r => roles.includes(r))
    if (!ok) {
      console.log('权限不足，跳转到对应角色首页')
      // 无权限：根据已有角色跳转首页
      if (roles.includes('ADMIN')) return next('/admin')
      if (roles.includes('DOCTOR')) return next('/doctor')
      return next('/login')
    }
  }
  next()
})

export default router


