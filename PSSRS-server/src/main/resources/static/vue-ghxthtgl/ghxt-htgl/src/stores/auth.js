import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('auth_token') || '',
    user: (() => {
      try { return JSON.parse(localStorage.getItem('auth_user') || 'null') } catch { return null }
    })(),
    activeRole: localStorage.getItem('auth_active_role') || ''
  }),
  getters: {
    roles(state) { return state.user?.roles || [] },
    isAdmin() { return this.roles.includes('ADMIN') },
    isDoctor() { return this.roles.includes('DOCTOR') }
  },
  actions: {
    setToken(token) {
      this.token = token
      if (token) localStorage.setItem('auth_token', token)
      else localStorage.removeItem('auth_token')
    },
    setUser(user) {
      this.user = user
      if (user) localStorage.setItem('auth_user', JSON.stringify(user))
      else localStorage.removeItem('auth_user')
    },
    setActiveRole(role) {
      this.activeRole = role || ''
      if (this.activeRole) localStorage.setItem('auth_active_role', this.activeRole)
      else localStorage.removeItem('auth_active_role')
    },
    logout() {
      this.setToken('')
      this.setUser(null)
      this.setActiveRole('')
    }
  }
})


