<template>
  <div class="container py-4">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary mb-3" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
      <p class="text-muted">正在加载寻亲信息...</p>
    </div>

    <div v-else-if="error" class="text-center py-5">
      <i class="fas fa-exclamation-triangle text-warning" style="font-size: 3rem;"></i>
      <p class="text-muted mt-3">{{ error }}</p>
      <button class="btn btn-primary mt-2" @click="router.back()">
        <i class="fas fa-arrow-left me-2"></i>返回
      </button>
    </div>

    <template v-else-if="person">
      <div class="mb-3">
        <button class="btn btn-outline-secondary btn-sm" @click="goBack">
          <i class="fas fa-arrow-left me-2"></i>返回
        </button>
      </div>

      <div class="card">
        <div class="card-body">
          <div class="row">
            <div class="col-md-5 text-center mb-4">
              <template v-if="photoList.length > 0">
                <img :src="photoList[currentPhotoIndex]" class="img-fluid rounded shadow-sm" :alt="person.name"
                  style="max-height: 400px; object-fit: cover;" @error="onPhotoError">
                <div v-if="photoList.length > 1" class="d-flex gap-2 mt-3 justify-content-center flex-wrap">
                  <img v-for="(photo, idx) in photoList" :key="idx" :src="photo"
                    class="rounded border" :class="{ 'border-primary': idx === currentPhotoIndex }"
                    style="width: 60px; height: 60px; object-fit: cover; cursor: pointer;"
                    @click="currentPhotoIndex = idx" @error="onPhotoError">
                </div>
              </template>
              <template v-else>
                <div class="no-photo-detail d-flex flex-column align-items-center justify-content-center text-muted mx-auto">
                  <i class="fas fa-user fa-6x"></i>
                </div>
              </template>
            </div>
            <div class="col-md-7">
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <h2 class="fw-bold mb-1">{{ person.title || person.name }}</h2>
                  <p class="text-muted mb-0">被寻人：{{ person.name }}</p>
                </div>
                <span class="badge" :class="statusBadgeClass" style="font-size: 0.9rem;">{{ statusText }}</span>
              </div>

              <table class="table table-borderless">
                <tbody>
                  <tr>
                    <td class="text-muted" style="width: 120px;">性别</td>
                    <td><strong>{{ person.gender || '-' }}</strong></td>
                  </tr>
                  <tr v-if="person.ageAtMissing">
                    <td class="text-muted">失踪时年龄</td>
                    <td><strong>{{ person.ageAtMissing }}岁</strong></td>
                  </tr>
                  <tr v-if="person.currentAge">
                    <td class="text-muted">现在年龄</td>
                    <td><strong>{{ person.currentAge }}岁</strong></td>
                  </tr>
                  <tr v-if="person.birthDate">
                    <td class="text-muted">出生日期</td>
                    <td><strong>{{ person.birthDate }}</strong></td>
                  </tr>
                  <tr v-if="person.missingDate">
                    <td class="text-muted">失踪日期</td>
                    <td><strong>{{ formatDate(person.missingDate) }}</strong></td>
                  </tr>
                  <tr v-if="person.missingLocation">
                    <td class="text-muted">失踪地点</td>
                    <td><strong>{{ person.missingLocation }}</strong></td>
                  </tr>
                  <tr v-if="person.height">
                    <td class="text-muted">身高</td>
                    <td><strong>{{ person.height }}cm</strong></td>
                  </tr>
                  <tr v-if="person.weight">
                    <td class="text-muted">体重</td>
                    <td><strong>{{ person.weight }}kg</strong></td>
                  </tr>
                  <tr v-if="person.appearance">
                    <td class="text-muted">体貌特征</td>
                    <td><strong>{{ person.appearance }}</strong></td>
                  </tr>
                  <tr v-if="person.clothing">
                    <td class="text-muted">衣着描述</td>
                    <td><strong>{{ person.clothing }}</strong></td>
                  </tr>
                  <tr v-if="person.specialFeatures">
                    <td class="text-muted">特殊特征</td>
                    <td><strong>{{ person.specialFeatures }}</strong></td>
                  </tr>
                  <tr v-if="person.missingCause">
                    <td class="text-muted">失踪原因</td>
                    <td><strong>{{ person.missingCause }}</strong></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="person.description" class="mt-3 pt-3 border-top">
            <h5 class="fw-bold">详细描述</h5>
            <p class="text-secondary" style="white-space: pre-wrap; line-height: 1.8;">{{ person.description }}</p>
          </div>

          <div class="mt-4 pt-3 border-top">
            <h5 class="fw-bold">联系方式</h5>
            <div class="row mt-3">
              <div v-if="person.contactName" class="col-md-4 mb-2">
                <small class="text-muted">联系人</small>
                <p class="fw-bold mb-0">{{ person.contactName }}</p>
              </div>
              <div v-if="person.contactPhone" class="col-md-4 mb-2">
                <small class="text-muted">联系电话</small>
                <p class="fw-bold mb-0">{{ person.contactPhone }}</p>
              </div>
              <div v-if="person.contactEmail" class="col-md-4 mb-2">
                <small class="text-muted">联系邮箱</small>
                <p class="fw-bold mb-0">{{ person.contactEmail }}</p>
              </div>
            </div>
            <div v-if="person.reward" class="mt-2">
              <small class="text-muted">悬赏金额</small>
              <p class="fw-bold text-danger mb-0">{{ person.reward }}</p>
            </div>
          </div>

          <div class="mt-3 pt-3 border-top text-muted small d-flex justify-content-between">
            <span>浏览 {{ person.viewCount || 0 }} 次</span>
            <span>线索 {{ person.clueCount || 0 }} 条</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { missingPersonApi } from '@/api'
import { formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()
const person = ref(null)
const loading = ref(true)
const error = ref('')
const currentPhotoIndex = ref(0)

const photoList = computed(() => {
  if (!person.value?.photos) return []
  const parts = person.value.photos.split(',').filter(p => p.trim())
  return parts.map(p => p.startsWith('http') ? p : p)
})

const statusBadgeClass = computed(() => {
  const status = person.value?.status
  if (status === 1) return 'bg-success'
  if (status === 2) return 'bg-danger'
  return 'bg-warning text-dark'
})

const statusText = computed(() => {
  const status = person.value?.status
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '审核中'
})

function onPhotoError(e) {
  const src = e.target.src
  e.target.style.display = 'none'
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    if (!id) {
      error.value = '缺少寻亲信息编号'
      return
    }
    const res = await missingPersonApi().getDetail(id)
    if (res && res.code === 200) {
      person.value = res.data
    } else {
      error.value = res?.message || '寻亲信息不存在'
    }
  } catch (e) {
    console.error('加载详情失败:', e)
    error.value = '无法连接服务器，请检查后端是否启动'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.no-photo-detail {
  width: 100%;
  max-width: 400px;
  height: 400px;
  background-color: #e9ecef;
  border-radius: 0.375rem;
}

.no-photo-detail i {
  opacity: 0.35;
}
</style>