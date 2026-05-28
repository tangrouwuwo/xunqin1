<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">成功案例管理</h2>
      <button class="btn btn-primary" @click="openCreate">
        <i class="fas fa-plus-circle me-2"></i>添加案例
      </button>
    </div>

    <div v-if="loading" class="text-center py-5"><div class="spinner-border text-primary"></div></div>

    <div v-else-if="list.length === 0" class="text-center text-muted py-5">
      <i class="fas fa-calendar-check fa-3x mb-3"></i>
      <p>暂无成功案例，点击上方按钮添加</p>
    </div>

    <div v-else class="row">
      <div v-for="item in list" :key="item.id" class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 shadow-sm">
          <div class="position-relative" style="height: 200px; overflow: hidden;">
            <img v-if="item.photos" :src="item.photos.split(',')[0]" class="card-img-top h-100 w-100"
                 style="object-fit: cover;" @error="e => e.target.style.display='none'">
            <div v-if="!item.photos" class="h-100 w-100 bg-light d-flex align-items-center justify-content-center">
              <i class="fas fa-image fa-3x text-muted"></i>
            </div>
            <span class="position-absolute top-0 end-0 badge bg-success m-2">已发布</span>
          </div>
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ item.title }}</h5>
            <p class="card-text text-muted small flex-grow-1">
              {{ (item.story || '').substring(0, 80) }}{{ (item.story || '').length > 80 ? '...' : '' }}
            </p>
            <div class="text-muted small mb-3">
              <div v-if="item.reunionLocation"><i class="fas fa-map-marker-alt text-primary me-1"></i>{{ item.reunionLocation }}</div>
              <div v-if="item.reunionDate"><i class="far fa-calendar-alt text-primary me-1"></i>{{ item.reunionDate }}</div>
            </div>
            <div class="d-flex justify-content-between">
              <button class="btn btn-sm btn-outline-primary" @click="viewDetail(item)">
                <i class="fas fa-eye me-1"></i>详情
              </button>
              <div>
                <button class="btn btn-sm btn-outline-secondary me-1" @click="openEdit(item)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteItem(item)">
                  <i class="fas fa-trash"></i>
                </button>
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

    <!-- 创建/编辑弹窗 -->
    <div v-if="showFormModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ isEditing ? '编辑成功案例' : '添加成功案例' }}</h5>
            <button type="button" class="btn-close" @click="closeForm"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">标题 <span class="text-danger">*</span></label>
              <input v-model="form.title" class="form-control" placeholder="请输入案例标题" maxlength="200">
            </div>
            <div class="mb-3">
              <label class="form-label">团聚故事 <span class="text-danger">*</span></label>
              <textarea v-model="form.story" class="form-control" rows="5" placeholder="请描述团聚的故事…"></textarea>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">团聚日期</label>
                <input v-model="form.reunionDate" type="date" class="form-control">
              </div>
              <div class="col-md-6">
                <label class="form-label">团聚地点</label>
                <input v-model="form.reunionLocation" class="form-control" placeholder="例如：北京市海淀区">
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">照片上传</label>
              <input type="file" class="form-control" accept="image/*" multiple @change="onPhotosChange">
              <div class="form-text">支持 JPG、PNG 格式，可多选</div>
              <div v-if="form.existingPhotos" class="mt-2">
                <small class="text-muted">当前已有 {{ form.existingPhotos.split(',').length }} 张图片</small>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeForm">取消</button>
            <button class="btn btn-primary" @click="save" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEditing ? '保存修改' : '发布案例' }}
            </button>
          </div>
        </div>
      </div>
    </div>

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
            <button class="btn btn-outline-primary" @click="openEdit(detailItem); showDetailModal = false">编辑</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { successCaseApi } from '@/api'

const list = ref([])
const loading = ref(true)
const pageNum = ref(1)
const totalPages = ref(1)
const pageSize = 12

const showFormModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const saving = ref(false)

const showDetailModal = ref(false)
const detailItem = ref(null)

const form = reactive({
  title: '',
  story: '',
  reunionDate: '',
  reunionLocation: '',
  newPhotos: null,
  existingPhotos: ''
})

const photosList = computed(() => {
  if (!detailItem.value || !detailItem.value.photos) return []
  return detailItem.value.photos.split(',').filter(p => p)
})

function onPhotosChange(e) {
  form.newPhotos = e.target.files
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

function resetForm() {
  form.title = ''
  form.story = ''
  form.reunionDate = ''
  form.reunionLocation = ''
  form.newPhotos = null
  form.existingPhotos = ''
  editingId.value = null
  isEditing.value = false
}

function openCreate() {
  resetForm()
  showFormModal.value = true
}

function openEdit(item) {
  resetForm()
  isEditing.value = true
  editingId.value = item.id
  form.title = item.title || ''
  form.story = item.story || ''
  form.reunionDate = item.reunionDate || ''
  form.reunionLocation = item.reunionLocation || ''
  form.existingPhotos = item.photos || ''
  showFormModal.value = true
}

function closeForm() {
  showFormModal.value = false
  resetForm()
}

async function save() {
  if (!form.title.trim()) { alert('请输入标题'); return }
  if (!form.story.trim()) { alert('请输入团聚故事'); return }

  saving.value = true
  try {
    const formData = new FormData()
    formData.append('title', form.title.trim())
    formData.append('content', form.story.trim())
    if (form.reunionDate) formData.append('reunionTime', form.reunionDate)
    if (form.reunionLocation) formData.append('reunionLocation', form.reunionLocation.trim())
    if (form.newPhotos && form.newPhotos.length > 0) {
      for (const file of form.newPhotos) {
        formData.append('photos', file)
      }
    }

    let res
    if (isEditing.value) {
      res = await successCaseApi().updateWithFiles(editingId.value, formData)
    } else {
      res = await successCaseApi().createWithFiles(formData)
    }

    if (res.code === 200) {
      closeForm()
      loadData()
    } else {
      alert(res.message || (isEditing.value ? '修改失败' : '添加失败'))
    }
  } catch (e) { alert('操作失败') }
  finally { saving.value = false }
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

async function deleteItem(item) {
  if (!confirm(`确定要永久删除案例「${item.title}」吗？\n删除后数据不可恢复！`)) return
  try {
    const res = await successCaseApi().delete(item.id)
    if (res.code === 200) {
      list.value = list.value.filter(i => i.id !== item.id)
    } else alert(res.message || '删除失败')
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>