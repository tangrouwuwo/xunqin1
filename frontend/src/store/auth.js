import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api, { setAuthToken } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)
  const unreadCount = ref(0)

  // 清除 localStorage 中可能残留的 token
  localStorage.removeItem('authToken')
  localStorage.removeItem('token')

  const isLoggedIn = computed(() => !!token.value)
  const currentUser = computed(() => user.value)
  const role = computed(() => user.value?.role || '')
  const roleName = computed(() => {
    const names = { ADMIN: '管理员', SEEKER: '寻亲者', VOLUNTEER: '志愿者', CLUE_PROVIDER: '线索提供者' }
    return names[role.value] || role.value
  })
  const roleClass = computed(() => {
    const classes = { ADMIN: 'admin', SEEKER: 'seeker', VOLUNTEER: 'volunteer', CLUE_PROVIDER: 'clue' }
    return classes[role.value] || ''
  })

  async function login(username, password) {
    const res = await api.post('/auth/login', { username, password })
    if (res.code === 200) {
      token.value = res.data.token
      user.value = res.data.user
      setAuthToken(res.data.token)
    }
    return res
  }

  async function register(data) {
    const res = await api.post('/auth/register', data)
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    setAuthToken('')
  }

  function setUser(userData) {
    user.value = userData
  }

  return { token, user, unreadCount, isLoggedIn, currentUser, role, roleName, roleClass, login, register, logout, setUser }
})