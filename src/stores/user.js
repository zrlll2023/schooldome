import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')

  async function login(credentials) {
    loading.value = true
    try {
      const res = await request.post('/api/auth/login', credentials)
      token.value = res.data.token
      userInfo.value = res.data.user
      localStorage.setItem('token', res.data.token)
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    try {
      const res = await request.post('/api/auth/register', data)
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await request.get('/api/user/info')
      userInfo.value = res.data
      return res.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      logout()
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  function initAuth() {
    if (token.value) {
      fetchUserInfo()
    }
  }

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    username,
    login,
    register,
    fetchUserInfo,
    logout,
    initAuth
  }
})
