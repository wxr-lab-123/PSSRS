import { createRouter, createWebHistory } from 'vue-router'
import RoleSelect from '../views/RoleSelect.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import DoctorLayout from '../layouts/DoctorLayout.vue'

import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Patients from '../views/Patients.vue'
import Departments from '../views/Departments.vue'
import Doctors from '../views/Doctors.vue'
import Admins from '../views/Admins.vue'
import Schedules from '../views/Schedules.vue'
import Registrations from '../views/Registrations.vue'
import Orders from '../views/Orders.vue'
import Refunds from '../views/Refunds.vue'
import LeaveApprovals from '../views/LeaveApprovals.vue'
import Roles from '../views/Roles.vue'
import Permissions from '../views/Permissions.vue'
import Settings from '../views/Settings.vue'
import DoctorMySchedules from '../views/doctor/MySchedules.vue'
import DoctorMyRegistrations from '../views/doctor/MyRegistrations.vue'
import DoctorProfile from '../views/doctor/Profile.vue'
import DoctorNotifications from '../views/doctor/Notifications.vue'

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
      { path: 'patients', name: 'patients', component: Patients, meta: { title: '患者管理', roles: ['ADMIN'], permissions: ['patients:view'] } },
      { path: 'admins', name: 'admins', component: Admins, meta: { title: '管理员管理', roles: ['ADMIN'], permissions: ['admins:view'] } },
      { path: 'departments', name: 'departments', component: Departments, meta: { title: '科室管理', roles: ['ADMIN'], permissions: ['departments:view'] } },
      { path: 'doctors', name: 'doctors', component: Doctors, meta: { title: '医生管理', roles: ['ADMIN'], permissions: ['doctors:view'] } },
      { path: 'schedules', name: 'schedules', component: Schedules, meta: { title: '排班管理', roles: ['ADMIN'], permissions: ['schedules:view'] } },
      { path: 'registrations', name: 'registrations', component: Registrations, meta: { title: '挂号管理', roles: ['ADMIN'], permissions: ['registrations:view'] } },
      { path: 'leave-approvals', name: 'leaveApprovals', component: LeaveApprovals, meta: { title: '请假审批', roles: ['ADMIN'] } },
      { path: 'orders', name: 'orders', component: Orders, meta: { title: '订单管理', roles: ['ADMIN'], permissions: ['orders:view'] } },
      { path: 'refunds', name: 'refunds', component: Refunds, meta: { title: '退款审批', roles: ['ADMIN'] } },
      { path: 'roles', name: 'roles', component: Roles, meta: { title: '角色权限', roles: ['ADMIN'], permissions: ['roles:view'] } },
      { path: 'permissions', name: 'permissions', component: Permissions, meta: { title: '权限管理', roles: ['ADMIN'], permissions: ['permissions:view'] } },
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
      { path: 'notifications', name: 'doctor-notifications', component: DoctorNotifications, meta: { title: '消息通知', roles: ['DOCTOR'] } },
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
  const token = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  if (!token) {
    return next({ path: '/login', replace: true })
  }
  const rawUser = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_user') : null) || localStorage.getItem('auth_user')
  let roles = []
  let permissions = []
  try {
    const user = JSON.parse(rawUser || 'null')
    roles = user?.roles || []
    permissions = user?.permissions || []
  } catch {}
  const activeRole = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_active_role') : null) || localStorage.getItem('auth_active_role') || ''
  if (roles.length > 1 && !activeRole && to.path !== '/role-select') {
    return next('/role-select')
  }
  const needRoles = to.meta.roles
  if (needRoles && needRoles.length) {
    const ok = activeRole ? needRoles.includes(activeRole) : needRoles.some(r => roles.includes(r))
    if (!ok) {
      if (roles.includes('ADMIN')) return next('/admin')
      if (roles.includes('DOCTOR')) return next('/doctor')
      return next('/login')
    }
  }
  const needPerms = to.meta.permissions
  if (needPerms && needPerms.length) {
    const isAdmin = roles.includes('ADMIN')
    const hasAll = isAdmin || needPerms.every(p => permissions.includes(p))
    if (!hasAll) {
      return next('/admin')
    }
  }
  next()
})

export default router
