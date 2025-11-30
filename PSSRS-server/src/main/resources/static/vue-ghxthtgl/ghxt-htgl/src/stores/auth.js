import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token') || '',
    user: (() => {
      try {
        const raw = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_user') : null) || localStorage.getItem('auth_user')
        return JSON.parse(raw || 'null')
      } catch { return null }
    })(),
    activeRole: ((typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_active_role') : null) || localStorage.getItem('auth_active_role')) || ''
  }),
  getters: {
    roles(state) { return state.user?.roles || [] },
    isAdmin() { return this.roles.includes('ADMIN') },
    isDoctor() { return this.roles.includes('DOCTOR') },
    permissions(state) { return state.user?.permissions || [] }
  },
  actions: {
    setToken(token) {
      this.token = token
      if (typeof sessionStorage !== 'undefined') {
        if (token) sessionStorage.setItem('auth_token', token)
        else sessionStorage.removeItem('auth_token')
      } else {
        if (token) localStorage.setItem('auth_token', token)
        else localStorage.removeItem('auth_token')
      }
    },
    setUser(user) {
      this.user = user
      const val = user ? JSON.stringify(user) : null
      if (typeof sessionStorage !== 'undefined') {
        if (val) sessionStorage.setItem('auth_user', val)
        else sessionStorage.removeItem('auth_user')
      } else {
        if (val) localStorage.setItem('auth_user', val)
        else localStorage.removeItem('auth_user')
      }
    },
    setActiveRole(role) {
      this.activeRole = role || ''
      if (typeof sessionStorage !== 'undefined') {
        if (this.activeRole) sessionStorage.setItem('auth_active_role', this.activeRole)
        else sessionStorage.removeItem('auth_active_role')
      } else {
        if (this.activeRole) localStorage.setItem('auth_active_role', this.activeRole)
        else localStorage.removeItem('auth_active_role')
      }
    },
    hasPerm(code) {
      if (!code) return true
      return this.isAdmin || this.permissions.includes(code)
    },
    logout() {
      this.setToken('')
      this.setUser(null)
      this.setActiveRole('')
    }
  }
})

