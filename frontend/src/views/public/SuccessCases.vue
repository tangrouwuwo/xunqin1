<template>
  <div class="container py-5">
    <div class="text-center mb-5">
      <h1 class="fw-bold mb-3">成功案例</h1>
      <p class="text-secondary lead">每一个成功案例都是希望的开始，每一次团聚都是爱的见证</p>
    </div>

    <div v-if="loading" class="text-center py-5"><div class="spinner-border text-primary"></div></div>

    <div v-else-if="list.length === 0" class="text-center py-5">
      <i class="fas fa-heart fa-3x text-muted mb-3"></i>
      <p class="text-muted">暂无成功案例</p>
    </div>

    <div v-else class="row g-4">
      <div v-for="(item, idx) in list" :key="item.id" class="col-md-6 col-lg-4">
        <div class="card shadow h-100 border-0" style="cursor: pointer;" @click="viewDetail(item)">
          <div class="position-relative overflow-hidden" style="height: 200px;">
            <img v-if="item.photos" :src="item.photos.split(',')[0]" class="w-100 h-100"
                 style="object-fit: cover;" @error="e => e.target.style.display='none'">
            <div v-if="!item.photos" class="h-100 w-100 d-flex align-items-center justify-content-center" :class="getBgClass(item)">
              <i class="fas fa-heart text-white" style="font-size: 4rem; opacity: 0.8;"></i>
            </div>
          </div>
          <div class="card-body p-4 d-flex flex-column">
            <h5 class="card-title fw-bold mb-3">{{ item.title }}</h5>
            <p class="card-text text-secondary mb-3 flex-grow-1">
              {{ (item.story || '').substring(0, 120) }}{{ (item.story || '').length > 120 ? '...' : '' }}
            </p>
            <div class="d-flex justify-content-between align-items-center mt-auto">
              <div class="text-muted small">
                <i class="fas fa-map-marker-alt text-primary me-1"></i> {{ item.reunionLocation || '未知地点' }}
                <span v-if="item.reunionDate" class="ms-2"><i class="far fa-calendar-alt me-1"></i>{{ item.reunionDate }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <nav v-if="totalPages > 1" class="mt-4">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: pageNum <= 1 }">
          <a class="page-link" @click.prevent="changePage(pageNum - 1)">上一页</a>
        </li>
        <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: p === pageNum }">
          <a class="page-link" @click.prevent="changePage(p)">{{ p }}</a>
        </li>
        <li class="page-item" :class="{ disabled: pageNum >= totalPages }">
          <a class="page-link" @click.prevent="changePage(pageNum + 1)">下一页</a>
        </li>
      </ul>
    </nav>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal && detailItem" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ detailItem.title }}</h5>
            <button type="button" class="btn-close" @click="showDetailModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="photosList.length > 0" class="mb-4">
              <div class="row g-2">
                <div v-for="(photo, idx) in photosList" :key="idx" class="col-md-4">
                  <img :src="photo" class="img-fluid rounded" style="height: 200px; width: 100%; object-fit: cover;">
                </div>
              </div>
            </div>
            <div class="mb-3">
              <h6 class="text-muted mb-2">团聚故事</h6>
              <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailItem.story }}</p>
            </div>
            <div class="row text-muted small">
              <div class="col-md-6 mb-2" v-if="detailItem.reunionLocation">
                <i class="fas fa-map-marker-alt text-primary me-1"></i>团聚地点：{{ detailItem.reunionLocation }}
              </div>
              <div class="col-md-6 mb-2" v-if="detailItem.reunionDate">
                <i class="far fa-calendar-alt text-primary me-1"></i>团聚日期：{{ detailItem.reunionDate }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="far fa-eye text-primary me-1"></i>浏览次数：{{ detailItem.viewCount || 0 }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="far fa-clock text-primary me-1"></i>发布时间：{{ detailItem.createTime }}
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { successCaseApi } from '@/api'

const list = ref([])
const loading = ref(true)
const pageNum = ref(1)
const totalPages = ref(1)
const pageSize = 12

const showDetailModal = ref(false)
const detailItem = ref(null)

const bgClasses = ['bg-danger', 'bg-primary', 'bg-purple', 'bg-success', 'bg-orange', 'bg-pink']

const photosList = computed(() => {
  if (!detailItem.value || !detailItem.value.photos) return []
  return detailItem.value.photos.split(',').filter(p => p)
})

function getBgClass(item) {
  const idx = item.id ? item.id % bgClasses.length : 0
  return bgClasses[idx]
}

async function loadData() {
  loading.value = true
  try {
    const res = await successCaseApi().list({ pageNum: pageNum.value, pageSize })
    if (res.code === 200) {
      const page = res.data
      list.value = page.records || []
      totalPages.value = page.pages || 1
    }
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  if (p < 1 || p > totalPages.value) return
  pageNum.value = p
  loadData()
}

async function viewDetail(item) {
  try {
    const res = await successCaseApi().getById(item.id)
    if (res.code === 200) {
      detailItem.value = res.data
      showDetailModal.value = true
    }
  } catch (e) { alert('获取详情失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.bg-purple { background: linear-gradient(135deg, #c084fc, #9333ea); }
.bg-orange { background: linear-gradient(135deg, #fb923c, #ea580c); }
.bg-pink { background: linear-gradient(135deg, #f472b6, #db2777); }
</style>