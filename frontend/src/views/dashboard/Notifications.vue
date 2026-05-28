<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">消息通知 <span v-if="authStore.unreadCount > 0" class="badge bg-danger ms-2">{{ authStore.unreadCount }} 条未读</span></h2>
      <div>
        <button v-if="notifications.length > 0" class="btn btn-sm btn-outline-danger me-2" @click="deleteSelected">
          <i class="fas fa-trash me-1"></i>删除选中
        </button>
        <button v-if="authStore.unreadCount > 0" class="btn btn-sm btn-outline-primary" @click="markAllAsRead">
          <i class="fas fa-check-double me-1"></i>全部标为已读
        </button>
      </div>
    </div>
    <div v-if="notifications.length === 0" class="text-center py-5">
      <i class="fas fa-bell text-muted" style="font-size: 3rem;"></i>
      <p class="text-muted mt-3">暂无消息通知</p>
    </div>
    <div v-else class="list-group">
      <div v-for="notif in notifications" :key="notif.id"
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-start"
        :class="{ 'fw-bold': !notif.isRead }" style="cursor: pointer;">
        <div class="d-flex align-items-start flex-grow-1" @click="markAsRead(notif)">
          <div class="form-check me-3" @click.stop>
            <input type="checkbox" class="form-check-input" :value="notif.id" v-model="selectedIds">
          </div>
          <div>
            <div class="mb-1"><strong>{{ notif.title }}</strong></div>
            <div class="mb-1 text-muted small">{{ notif.content }}</div>
            <small class="text-muted">{{ formatDateTime(notif.createTime) }}</small>
          </div>
        </div>
        <div class="d-flex align-items-center ms-2">
          <span v-if="!notif.isRead" class="badge bg-danger rounded-pill me-2">新</span>
          <button class="btn btn-sm btn-outline-danger" @click.stop="deleteSingle(notif)" title="删除">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '@/api'
import { formatDateTime } from '@/utils'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const notifications = ref([])
const selectedIds = ref([])
let pollTimer = null

async function loadNotifications() {
  try {
    const res = await notificationApi().list()
    if (res.code === 200) {
      notifications.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
  } catch (e) {
    console.error('加载通知失败:', e)
  }
}

async function loadUnreadCount() {
  try {
    const res = await notificationApi().getUnreadCount()
    if (res.code === 200) authStore.unreadCount = res.data || 0
  } catch (e) { /* ignore */ }
}

async function markAsRead(notif) {
  if (notif.isRead) return
  try {
    const res = await notificationApi().markRead(notif.id)
    if (res.code === 200) {
      notif.isRead = true
      authStore.unreadCount = Math.max(0, authStore.unreadCount - 1)
    }
  } catch (e) { console.error('标记已读失败:', e) }
}

async function markAllAsRead() {
  try {
    const res = await notificationApi().markAllRead()
    if (res.code === 200) {
      notifications.value.forEach(n => { n.isRead = true })
      authStore.unreadCount = 0
    }
  } catch (e) { console.error('标记全部已读失败:', e) }
}

async function deleteSingle(notif) {
  if (!confirm(`确定删除通知「${notif.title}」吗？`)) return
  try {
    const res = await notificationApi().delete(notif.id)
    if (res.code === 200) {
      notifications.value = notifications.value.filter(n => n.id !== notif.id)
      if (!notif.isRead) authStore.unreadCount = Math.max(0, authStore.unreadCount - 1)
    } else alert(res.message || '删除失败')
  } catch (e) { alert('删除失败') }
}

async function deleteSelected() {
  if (selectedIds.value.length === 0) { alert('请先选择要删除的通知'); return }
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 条通知吗？`)) return
  let deleted = 0
  for (const id of selectedIds.value) {
    try {
      const notif = notifications.value.find(n => n.id === id)
      const res = await notificationApi().delete(id)
      if (res.code === 200) {
        if (notif && !notif.isRead) deleted++
        notifications.value = notifications.value.filter(n => n.id !== id)
      }
    } catch (e) { console.error('删除失败:', id) }
  }
  authStore.unreadCount = Math.max(0, authStore.unreadCount - deleted)
  selectedIds.value = []
  if (deleted > 0) alert(`成功删除 ${deleted} 条通知`)
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
  pollTimer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>