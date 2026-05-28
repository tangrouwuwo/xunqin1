<template>
  <div>
    <div class="mb-4">
      <h3>欢迎回来，{{ authStore.currentUser?.username }}！</h3>
      <p class="text-muted">您的角色：{{ authStore.roleName }}</p>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary mb-3" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
      <p class="text-muted">正在加载数据...</p>
    </div>

    <template v-else>
      <div v-if="error" class="alert alert-warning d-flex align-items-center mb-4">
        <i class="fas fa-exclamation-triangle me-2"></i>
        <span>{{ error }}，请检查后端 Spring Boot 是否已启动</span>
      </div>

      <div class="row g-4">
        <div v-for="card in dashboardCards" :key="card.title" class="col-md-4">
          <div class="dashboard-card p-4 text-center">
            <div class="card-icon"><i :class="card.icon"></i></div>
            <h5>{{ card.title }}</h5>
            <p class="text-muted">{{ card.desc }}</p>
            <button class="btn btn-sm btn-primary mt-2" @click="navigateTo(card.path)">
              <i class="fas fa-arrow-right me-1"></i> {{ card.action }}
            </button>
          </div>
        </div>
      </div>

      <div class="mt-4">
        <h4>最新寻亲信息</h4>
        <div v-if="missingPersons.length > 0" class="row g-4 fade-in">
          <div class="col-md-4" v-for="person in missingPersons" :key="person.id">
            <MissingPersonCard :person="person" @view-detail="showDetail" />
          </div>
        </div>
        <div v-else class="text-center py-5">
          <i class="fas fa-child text-muted" style="font-size: 3rem;"></i>
          <p class="text-muted mt-3">{{ error ? '数据加载失败，无法获取最新寻亲信息' : '暂无最新寻亲信息' }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { missingPersonApi } from '@/api'
import MissingPersonCard from '@/components/MissingPersonCard.vue'

const router = useRouter()
const authStore = useAuthStore()
const missingPersons = ref([])
const loading = ref(true)
const error = ref('')

const cardConfig = {
  ADMIN: [
    { icon: 'fas fa-users', title: '用户管理', desc: '管理所有用户账号', action: '查看', path: '/dashboard/users' },
    { icon: 'fas fa-child', title: '寻亲信息', desc: '管理寻亲信息', action: '查看', path: '/dashboard/missing-persons' },
    { icon: 'fas fa-lightbulb', title: '线索管理', desc: '管理所有线索', action: '查看', path: '/dashboard/clues' },
    { icon: 'fas fa-calendar-check', title: '成功案例', desc: '管理成功案例', action: '查看', path: '/dashboard/success-cases-manage' }
  ],
  SEEKER: [
    { icon: 'fas fa-plus-circle', title: '发布寻亲信息', desc: '发布新的寻亲信息', action: '发布', path: '/dashboard/create-missing-person' },
    { icon: 'fas fa-list', title: '我的寻亲信息', desc: '管理我的寻亲信息', action: '查看', path: '/dashboard/my-missing-persons' },
    { icon: 'fas fa-bell', title: '消息通知', desc: '查看系统通知', action: '查看', path: '/dashboard/notifications' }
  ],
  VOLUNTEER: [
    { icon: 'fas fa-lightbulb', title: '线索管理', desc: '管理分配的线索', action: '查看', path: '/dashboard/volunteer-clues' },
    { icon: 'fas fa-calendar-alt', title: '志愿活动', desc: '参与志愿活动', action: '查看', path: '/dashboard/volunteer-activities' },
    { icon: 'fas fa-tasks', title: '任务大厅', desc: '查看可接任务', action: '查看', path: '/dashboard/task-hall' }
  ],
  CLUE_PROVIDER: [
    { icon: 'fas fa-plus-circle', title: '提交线索', desc: '提交新的线索', action: '提交', path: '/dashboard/submit-clue' },
    { icon: 'fas fa-list', title: '我的线索', desc: '管理我的线索', action: '查看', path: '/dashboard/my-clues' },
    { icon: 'fas fa-bell', title: '线索状态', desc: '查看线索处理状态', action: '查看', path: '/dashboard/notifications' }
  ]
}

const dashboardCards = computed(() => cardConfig[authStore.role] || [])

function navigateTo(path) {
  router.push(path)
}

function showDetail(personId) {
  if (!personId) return
  router.push('/missing-persons/' + personId)
}

async function loadMissingPersons() {
  loading.value = true
  error.value = ''
  try {
    const res = await missingPersonApi().list({ pageNum: 1, pageSize: 6 })
    if (res && res.code === 200) {
      const records = res.data?.records || []
      missingPersons.value = records
      if (records.length === 0) {
        console.log('仪表盘: 暂无已审核通过的寻亲信息')
      }
    } else {
      console.warn('仪表盘: API返回异常', res)
      error.value = '数据加载异常'
    }
  } catch (e) {
    console.error('仪表盘加载寻亲信息失败，请确认后端服务是否正常运行:', e)
    console.error('错误详情:', e.message)
    missingPersons.value = []
    error.value = '无法连接服务器，请检查后端是否启动'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMissingPersons()
})
</script>