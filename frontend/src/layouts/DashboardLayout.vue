<template>
  <div>
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
      <div class="container-fluid">
        <router-link class="navbar-brand d-flex align-items-center" to="/">
          <i class="fas fa-home text-primary me-2" style="font-size: 1.5rem;"></i>
          <span class="fw-bold" style="font-size: 1.25rem;">寻亲网站</span>
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#dashboardNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="dashboardNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/dashboard">
                <i class="fas fa-tachometer-alt"></i> 仪表盘
              </router-link>
            </li>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
                <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px;">
                  <i class="fas fa-user" style="font-size: 14px;"></i>
                </div>
                <span>{{ authStore.currentUser?.username }}</span>
                <span class="ms-2 role-badge" :class="'badge-' + authStore.roleClass">{{ authStore.roleName }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                <li>
                  <router-link class="dropdown-item" to="/dashboard/profile">
                    <i class="fas fa-user-circle me-2 text-primary"></i> 个人资料
                  </router-link>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <a class="dropdown-item" href="#" @click.prevent="handleLogout">
                    <i class="fas fa-sign-out-alt me-2 text-danger"></i> 退出登录
                  </a>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container-fluid">
      <div class="row">
        <div class="col-md-3 col-lg-2 sidebar p-3">
          <h5 class="mb-3 px-3">功能导航</h5>
          <ul class="nav flex-column" id="sidebarMenu">
            <li class="nav-item mb-1" v-for="item in menuItems" :key="item.path">
              <router-link
                class="nav-link"
                :to="item.path"
                :class="{ 'active': isActive(item.path) }"
              >
                <i :class="item.icon + ' me-2'"></i> {{ item.text }}
                <span v-if="item.badge && item.badge > 0" class="badge bg-danger rounded-circle ms-2" style="font-size: 11px;">{{ item.badge }}</span>
              </router-link>
            </li>
          </ul>
        </div>
        <div class="col-md-9 col-lg-10 page-content p-4">
          <router-view />
        </div>
      </div>
    </div>
    <AIChat />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import api from '@/api'
import AIChat from '@/components/AIChat.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const baseMenu = [
  { icon: 'fas fa-tachometer-alt', text: '仪表盘', path: '/dashboard' },
  { icon: 'fas fa-user-circle', text: '个人资料', path: '/dashboard/profile' }
]

const roleMenus = {
  ADMIN: [
    { icon: 'fas fa-comments', text: '社群交流', path: '/dashboard/admin-community' },
    { icon: 'fas fa-users', text: '用户管理', path: '/dashboard/users' },
    { icon: 'fas fa-child', text: '寻亲信息管理', path: '/dashboard/missing-persons' },
    { icon: 'fas fa-lightbulb', text: '线索管理', path: '/dashboard/clues' },
    { icon: 'fas fa-calendar-check', text: '成功案例管理', path: '/dashboard/success-cases-manage' },
    { icon: 'fas fa-calendar-alt', text: '志愿活动管理', path: '/dashboard/volunteer-activities-manage' },
    { icon: 'fas fa-tasks', text: '任务管理', path: '/dashboard/tasks' },
    { icon: 'fas fa-bell', text: '消息通知', path: '/dashboard/notifications', isNotification: true }
  ],
  SEEKER: [
    { icon: 'fas fa-comments', text: '社群交流', path: '/dashboard/seeker-community' },
    { icon: 'fas fa-plus-circle', text: '发布寻亲信息', path: '/dashboard/create-missing-person' },
    { icon: 'fas fa-list', text: '我的寻亲信息', path: '/dashboard/my-missing-persons' },
    { icon: 'fas fa-lightbulb', text: '相关线索', path: '/dashboard/seeker-clues' },
    { icon: 'fas fa-tasks', text: '任务管理', path: '/dashboard/seeker-tasks' },
    { icon: 'fas fa-bell', text: '消息通知', path: '/dashboard/notifications', isNotification: true }
  ],
  VOLUNTEER: [
    { icon: 'fas fa-comments', text: '社群交流', path: '/dashboard/volunteer-community' },
    { icon: 'fas fa-lightbulb', text: '线索管理', path: '/dashboard/volunteer-clues' },
    { icon: 'fas fa-calendar-alt', text: '志愿活动', path: '/dashboard/volunteer-activities' },
    { icon: 'fas fa-tasks', text: '任务大厅', path: '/dashboard/task-hall' },
    { icon: 'fas fa-list-alt', text: '我的任务', path: '/dashboard/my-tasks' },
    { icon: 'fas fa-bell', text: '消息通知', path: '/dashboard/notifications', isNotification: true }
  ],
  CLUE_PROVIDER: [
    { icon: 'fas fa-comments', text: '社群交流', path: '/dashboard/clue-provider-community' },
    { icon: 'fas fa-lightbulb', text: '线索管理', path: '/dashboard/my-clues' },
    { icon: 'fas fa-plus-circle', text: '提交线索', path: '/dashboard/submit-clue' },
    { icon: 'fas fa-bell', text: '消息通知', path: '/dashboard/notifications', isNotification: true }
  ]
}

const menuItems = computed(() => {
  const menus = roleMenus[authStore.role] || []
  return [...baseMenu, ...menus.map(m => ({
    ...m,
    badge: m.isNotification ? authStore.unreadCount : 0
  }))]
})

function isActive(path) {
  return route.path === path
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}

async function loadUnreadCount() {
  try {
    const res = await api.get('/notifications/unread-count')
    if (res.code === 200) {
      authStore.unreadCount = res.data || 0
    }
  } catch (e) {
    console.error('获取未读消息数失败:', e)
  }
}

onMounted(() => {
  if (!authStore.isLoggedIn) {
    router.replace('/login')
    return
  }
  loadUnreadCount()
})
</script>