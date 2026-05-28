<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0"><i class="fas fa-search text-primary me-2"></i>寻亲信息查询</h2>
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <button class="btn btn-outline-primary mb-3" @click="showProvinces = !showProvinces">
          <i class="fas fa-map-marker-alt me-2"></i>{{ showProvinces ? '收起地区选择' : '按地区寻亲' }}
        </button>

        <div v-if="showProvinces" class="fade-in">
          <div class="mb-2"><strong>选择省份：</strong></div>
          <div class="d-flex flex-wrap gap-2 mb-3">
            <button
              class="btn btn-sm"
              :class="!selectedProvince ? 'btn-primary' : 'btn-outline-secondary'"
              @click="clearProvince"
            >全部</button>
            <button
              v-for="p in provinces"
              :key="p"
              class="btn btn-sm"
              :class="selectedProvince === p ? 'btn-primary' : 'btn-outline-secondary'"
              @click="selectProvince(p)"
            >{{ p }}</button>
            <button v-if="selectedProvince" class="btn btn-sm btn-outline-danger" @click="clearProvince">
              <i class="fas fa-times me-1"></i>清除筛选
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary mb-3" role="status"><span class="visually-hidden">加载中...</span></div>
      <p class="text-muted">正在加载寻亲信息...</p>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="text-center py-5 text-muted">
        <i class="fas fa-inbox fa-3x mb-3" style="opacity: 0.3;"></i>
        <p>暂无寻亲信息</p>
      </div>

      <div v-else class="row g-4">
        <div v-for="item in list" :key="item.id" class="col-md-6 col-lg-4">
          <div class="card h-100 missing-person-card border-0 shadow-sm" style="cursor: pointer;" @click="goToDetail(item.id)">
            <div class="row g-0 h-100">
              <div class="col-4 d-flex align-items-stretch">
                <div class="photo-wrapper w-100 d-flex align-items-center justify-content-center">
                  <template v-if="item.photos">
                    <img :src="getFirstPhoto(item.photos)" alt="照片" class="card-img-photo">
                  </template>
                  <template v-else>
                    <div class="no-photo d-flex flex-column align-items-center justify-content-center text-muted">
                      <i class="fas fa-user fa-3x"></i>
                    </div>
                  </template>
                </div>
              </div>
              <div class="col-8">
                <div class="card-body p-3 d-flex flex-column h-100">
                  <h5 class="fw-bold mb-2 text-primary text-truncate" style="font-size: 1.2rem;">{{ item.title || item.name }}</h5>
                  <div class="mb-1" style="font-size: 0.95rem;">
                    <strong>{{ item.name }}</strong>
                    <span v-if="item.gender" class="text-muted ms-1">（{{ item.gender }}）</span>
                  </div>
                  <div class="mb-1 text-muted small"><i class="fas fa-calendar-alt me-1" style="width: 16px;"></i>{{ item.ageAtMissing || '-' }}岁</div>
                  <div class="mb-1 text-muted small"><i class="fas fa-clock me-1" style="width: 16px;"></i>{{ item.missingDate || '-' }}</div>
                  <div class="text-muted small text-truncate"><i class="fas fa-map-marker-alt me-1" style="width: 16px;"></i>{{ displayLocation(item.missingLocation) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <nav v-if="totalPages > 1" class="mt-4">
        <ul class="pagination justify-content-center mb-0">
          <li class="page-item" :class="{ disabled: currentPage <= 1 }">
            <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一页</a>
          </li>
          <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: p === currentPage }">
            <a class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
          </li>
          <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
            <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">下一页</a>
          </li>
        </ul>
      </nav>
    </template>

    <div class="text-center mt-3">
      <small class="text-muted" v-if="!loading && totalRecords > 0">共 {{ totalRecords }} 条寻亲信息</small>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { missingPersonApi } from '@/api'
import { provinces } from '@/utils/chinaRegions'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(0)
const totalRecords = ref(0)
const pageSize = 12

const showProvinces = ref(false)
const selectedProvince = ref('')

function getFirstPhoto(photos) {
  if (!photos) return ''
  return photos.split(',')[0]
}

function displayLocation(location) {
  if (!location) return '-'
  return location.replace(/\|/g, ' ')
}

function selectProvince(p) {
  selectedProvince.value = p
  currentPage.value = 1
  loadData()
}

function clearProvince() {
  selectedProvince.value = ''
  currentPage.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize
    }
    if (selectedProvince.value) params.province = selectedProvince.value

    const res = await missingPersonApi().searchAll(params)
    if (res.code === 200 && res.data) {
      list.value = res.data.records || []
      totalPages.value = res.data.pages || 0
      totalRecords.value = res.data.total || 0
    } else {
      list.value = []
    }
  } catch (e) {
    console.error('加载失败:', e)
    list.value = []
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToDetail(id) {
  if (id) router.push(`/missing-persons/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.photo-wrapper {
  overflow: hidden;
  border-radius: 0.5rem 0 0 0.5rem;
  min-height: 175px;
}

.card-img-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.no-photo {
  width: 100%;
  height: 100%;
  background-color: #e9ecef;
  min-height: 175px;
}

.no-photo i {
  opacity: 0.4;
}

.missing-person-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 0.5rem !important;
  overflow: hidden;
}

.missing-person-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.12) !important;
}
</style>