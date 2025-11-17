<script setup>
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
onBeforeMount(() => {
  const token = localStorage.getItem('auth_token')
  const isLoginPage = router.currentRoute.value.path === '/login'
  if (!token && !isLoginPage) {
    router.replace('/login')
  }
})
</script>

<template>
  <router-view />
  
</template>

<style scoped>
</style>
