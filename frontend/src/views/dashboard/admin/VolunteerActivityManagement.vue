<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">志愿活动管理</h2>
      <button class="btn btn-primary" @click="openCreate">
        <i class="fas fa-plus-circle me-2"></i>创建活动
      </button>
    </div>

    <div v-if="loading" class="text-center py-5"><div class="spinner-border text-primary"></div></div>

    <div v-else-if="list.length === 0" class="text-center text-muted py-5">
      <i class="fas fa-calendar-alt fa-3x mb-3"></i>
      <p>暂无活动，点击上方按钮创建</p>
    </div>

    <div v-else class="row">
      <div v-for="item in list" :key="item.id" class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 shadow-sm">
          <div class="position-relative" style="height: 180px; overflow: hidden;">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-img-top h-100 w-100"
                 style="object-fit: cover;" @error="e => e.target.style.display='none'">
            <div v-if="!item.coverImage" class="h-100 w-100 bg-light d-flex align-items-center justify-content-center">
              <i class="fas fa-image fa-3x text-muted"></i>
            </div>
            <span class="position-absolute top-0 end-0 badge m-2" :class="statusBadgeClass(item.status)">
              {{ statusText(item.status) }}
            </span>
            <span class="position-absolute top-0 start-0 badge bg-dark m-2">
              <i class="fas fa-users me-1"></i>{{ item.participantCount || 0 }}/{{ item.maxParticipants || '∞' }}
            </span>
          </div>
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ item.title }}</h5>
            <p class="card-text text-muted small flex-grow-1">
              {{ (item.description || '').substring(0, 80) }}{{ (item.description || '').length > 80 ? '...' : '' }}
            </p>
            <div class="text-muted small mb-2">
              <div v-if="item.location" class="mb-1"><i class="fas fa-map-marker-alt text-danger me-1"></i>{{ item.location }}</div>
              <div v-if="item.startTime" class="mb-1"><i class="far fa-calendar-alt text-primary me-1"></i>{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</div>
              <div><i class="fas fa-tag text-success me-1"></i>{{ item.typeName || item.type }}</div>
            </div>
            <div class="d-flex flex-wrap gap-1 mt-2">
              <button class="btn btn-sm btn-outline-info" title="查看详情" @click="viewDetail(item)">
                <i class="fas fa-eye"></i>
              </button>
              <button v-if="item.status === 0" class="btn btn-sm btn-outline-primary" title="编辑" @click="openEdit(item)">
                <i class="fas fa-edit"></i>
              </button>
              <button v-if="item.status === 0" class="btn btn-sm btn-outline-success" title="发布" @click="publishActivity(item)">
                <i class="fas fa-upload"></i>
              </button>
              <button v-if="item.status === 1" class="btn btn-sm btn-outline-success" title="开始活动" @click="startActivity(item)">
                <i class="fas fa-play"></i>
              </button>
              <button v-if="item.status === 2" class="btn btn-sm btn-outline-warning" title="结束活动" @click="endActivity(item)">
                <i class="fas fa-stop"></i>
              </button>
              <button v-if="item.status === 0 || item.status === 1" class="btn btn-sm btn-outline-danger" title="取消活动" @click="cancelActivity(item)">
                <i class="fas fa-times"></i>
              </button>
              <button class="btn btn-sm btn-outline-secondary" title="查看参与者" @click="viewParticipants(item)">
                <i class="fas fa-users"></i>
              </button>
              <button class="btn btn-sm btn-outline-info" title="查看报告" @click="viewReports(item)">
                <i class="fas fa-file-alt"></i>
              </button>
              <button class="btn btn-sm btn-outline-danger" title="删除活动（不可恢复）" @click="deleteActivity(item)">
                <i class="fas fa-trash-alt"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <nav v-if="totalPages > 1" class="mt-4">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: pageNum <= 1 }">
          <a class="page-link" href="#" @click.prevent="pageNum > 1 && changePage(pageNum - 1)">上一页</a>
        </li>
        <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: p === pageNum }">
          <a class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
        </li>
        <li class="page-item" :class="{ disabled: pageNum >= totalPages }">
          <a class="page-link" href="#" @click.prevent="pageNum < totalPages && changePage(pageNum + 1)">下一页</a>
        </li>
      </ul>
    </nav>

    <!-- 创建/编辑弹窗 -->
    <div v-if="showFormModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ isEditing ? '编辑活动' : '创建志愿活动' }}</h5>
            <button type="button" class="btn-close" @click="closeForm"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">活动标题 <span class="text-danger">*</span></label>
              <input v-model="form.title" class="form-control" placeholder="请输入活动标题" maxlength="200">
            </div>
            <div class="mb-3">
              <label class="form-label">活动类型 <span class="text-danger">*</span></label>
              <select v-model="form.type" class="form-select">
                <option value="">请选择活动类型</option>
                <option value="INFO_VERIFY">信息核实</option>
                <option value="CLUE_INVESTIGATE">线索调查</option>
                <option value="VOLUNTEER_RECRUIT">志愿者招募</option>
                <option value="COMMUNITY_PROMOTION">社区宣传</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">活动描述 <span class="text-danger">*</span></label>
              <textarea v-model="form.description" class="form-control" rows="3" placeholder="请简单描述活动内容"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">活动内容 <span class="text-danger">*</span></label>
              <textarea v-model="form.content" class="form-control" rows="5" placeholder="请详细描述活动内容、安排和要求等"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">活动地点</label>
              <input v-model="form.location" class="form-control" placeholder="如：北京市朝阳区">
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">开始时间 <span class="text-danger">*</span></label>
                <input v-model="form.startTime" type="datetime-local" class="form-control">
              </div>
              <div class="col-md-6">
                <label class="form-label">结束时间 <span class="text-danger">*</span></label>
                <input v-model="form.endTime" type="datetime-local" class="form-control">
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">最大参与人数</label>
              <input v-model="form.maxParticipants" type="number" class="form-control" min="1" placeholder="不填表示无限制">
            </div>
            <div class="mb-3">
              <label class="form-label">封面图片</label>
              <input type="file" class="form-control" accept="image/*" @change="onCoverChange">
              <div class="form-text">支持 JPG、PNG 格式</div>
              <div v-if="form.coverPreview" class="mt-2">
                <img :src="form.coverPreview" class="img-thumbnail" style="max-height: 150px;">
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeForm">取消</button>
            <button class="btn btn-primary" @click="save" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEditing ? '保存修改' : '创建活动' }}
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
            <div v-if="detailItem.coverImage" class="mb-4 text-center">
              <img :src="detailItem.coverImage" class="img-fluid rounded" style="max-height: 300px; object-fit: cover;">
            </div>
            <div class="mb-3">
              <span class="badge me-2" :class="statusBadgeClass(detailItem.status)">{{ statusText(detailItem.status) }}</span>
              <span class="badge bg-secondary">{{ detailItem.typeName || detailItem.type }}</span>
            </div>
            <div class="mb-3" v-if="detailItem.description">
              <h6 class="text-muted mb-2">活动描述</h6>
              <p style="white-space: pre-wrap;">{{ detailItem.description }}</p>
            </div>
            <div class="mb-3" v-if="detailItem.content">
              <h6 class="text-muted mb-2">活动内容</h6>
              <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailItem.content }}</p>
            </div>
            <div class="row text-muted small">
              <div class="col-md-6 mb-2" v-if="detailItem.location">
                <i class="fas fa-map-marker-alt text-danger me-1"></i>地点：{{ detailItem.location }}
              </div>
              <div class="col-md-6 mb-2" v-if="detailItem.startTime">
                <i class="far fa-calendar-alt text-primary me-1"></i>时间：{{ formatDate(detailItem.startTime) }} ~ {{ formatDate(detailItem.endTime) }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="fas fa-users text-success me-1"></i>参与人数：{{ detailItem.participantCount || 0 }}/{{ detailItem.maxParticipants || '无限制' }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="far fa-eye text-info me-1"></i>浏览次数：{{ detailItem.viewCount || 0 }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="far fa-clock text-secondary me-1"></i>创建时间：{{ detailItem.createTime }}
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
            <button v-if="detailItem.status === 0" class="btn btn-outline-primary" @click="openEdit(detailItem); showDetailModal = false">编辑</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 参与者弹窗 -->
    <ParticipantsModal v-if="showParticipants" :activity="selectedActivity" @close="showParticipants = false" />

    <!-- 报告弹窗 -->
    <ReportsModal v-if="showReports" :activity="selectedActivity" @close="showReports = false" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { volunteerActivityApi } from '@/api'
import ParticipantsModal from './ParticipantsModal.vue'
import ReportsModal from './ReportsModal.vue'

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

const showParticipants = ref(false)
const showReports = ref(false)
const selectedActivity = ref(null)

const form = reactive({
  title: '',
  type: '',
  description: '',
  content: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: '',
  coverFile: null,
  coverPreview: ''
})

function statusBadgeClass(s) {
  const map = { 0: 'bg-secondary', 1: 'bg-primary', 2: 'bg-success', 3: 'bg-dark', 4: 'bg-danger' }
  return map[s] || 'bg-secondary'
}

function statusText(s) {
  const map = { 0: '草稿', 1: '招募中', 2: '进行中', 3: '已结束', 4: '已取消' }
  return map[s] || '未知'
}

function formatDate(d) {
  if (!d) return '-'
  return d.replace('T', ' ').substring(0, 16)
}

function onCoverChange(e) {
  const file = e.target.files[0]
  if (file) {
    form.coverFile = file
    form.coverPreview = URL.createObjectURL(file)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await volunteerActivityApi().listAdmin({ pageNum: pageNum.value, pageSize })
    if (res.code === 200) {
      const page = res.data
      list.value = page.records || []
      totalPages.value = page.pages || 1
    }
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  pageNum.value = p
  loadData()
}

function openCreate() {
  isEditing.value = false
  editingId.value = null
  form.title = ''
  form.type = ''
  form.description = ''
  form.content = ''
  form.location = ''
  form.startTime = ''
  form.endTime = ''
  form.maxParticipants = ''
  form.coverFile = null
  form.coverPreview = ''
  showFormModal.value = true
}

function openEdit(item) {
  isEditing.value = true
  editingId.value = item.id
  form.title = item.title
  form.type = item.type
  form.description = item.description || ''
  form.content = item.content || ''
  form.location = item.location || ''
  form.startTime = item.startTime ? item.startTime.substring(0, 16) : ''
  form.endTime = item.endTime ? item.endTime.substring(0, 16) : ''
  form.maxParticipants = item.maxParticipants || ''
  form.coverFile = null
  form.coverPreview = item.coverImage || ''
  showFormModal.value = true
}

function closeForm() {
  showFormModal.value = false
  if (form.coverPreview && form.coverPreview.startsWith('blob:')) {
    URL.revokeObjectURL(form.coverPreview)
  }
}

async function save() {
  if (!form.title.trim()) { alert('请输入活动标题'); return }
  if (!form.type) { alert('请选择活动类型'); return }
  if (!form.description.trim()) { alert('请输入活动描述'); return }
  if (!form.content.trim()) { alert('请输入活动内容'); return }
  if (!form.startTime) { alert('请选择开始时间'); return }
  if (!form.endTime) { alert('请选择结束时间'); return }
  if (form.startTime >= form.endTime) { alert('结束时间必须晚于开始时间'); return }

  saving.value = true
  try {
    if (isEditing.value) {
      const data = {
        title: form.title.trim(),
        type: form.type,
        description: form.description.trim(),
        content: form.content.trim(),
        location: form.location.trim() || null,
        startTime: form.startTime + ':00',
        endTime: form.endTime + ':00',
        maxParticipants: form.maxParticipants ? parseInt(form.maxParticipants) : null
      }
      const res = await volunteerActivityApi().update(editingId.value, data)
      if (res.code === 200) {
        closeForm()
        loadData()
      } else {
        alert(res.message || '修改失败')
      }
    } else {
      const formData = new FormData()
      formData.append('title', form.title.trim())
      formData.append('type', form.type)
      formData.append('description', form.description.trim())
      formData.append('content', form.content.trim())
      if (form.location.trim()) formData.append('location', form.location.trim())
      formData.append('startTime', new Date(form.startTime).toISOString())
      formData.append('endTime', new Date(form.endTime).toISOString())
      if (form.maxParticipants) formData.append('maxParticipants', parseInt(form.maxParticipants))
      if (form.coverFile) formData.append('coverImage', form.coverFile)
      const res = await volunteerActivityApi().create(formData)
      if (res.code === 200) {
        closeForm()
        loadData()
      } else {
        alert(res.message || '创建失败')
      }
    }
  } catch (e) { alert(isEditing.value ? '修改失败' : '创建失败') }
  finally { saving.value = false }
}

async function publishActivity(item) {
  if (!confirm('确定要发布此活动吗？发布后志愿者可以报名参加。')) return
  try {
    const res = await volunteerActivityApi().publish(item.id)
    if (res.code === 200) loadData()
    else alert(res.message || '发布失败')
  } catch (e) { alert('发布失败') }
}

async function startActivity(item) {
  if (!confirm('确定要开始此活动吗？')) return
  try {
    const res = await volunteerActivityApi().start(item.id)
    if (res.code === 200) loadData()
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

async function endActivity(item) {
  if (!confirm('确定要结束此活动吗？')) return
  try {
    const res = await volunteerActivityApi().end(item.id)
    if (res.code === 200) loadData()
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

async function cancelActivity(item) {
  if (!confirm('确定要取消此活动吗？')) return
  try {
    const res = await volunteerActivityApi().cancel(item.id)
    if (res.code === 200) loadData()
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

function viewDetail(item) {
  detailItem.value = item
  showDetailModal.value = true
}

function viewParticipants(item) {
  selectedActivity.value = item
  showParticipants.value = true
}

function viewReports(item) {
  selectedActivity.value = item
  showReports.value = true
}

async function deleteActivity(item) {
  if (!confirm('确定要永久删除活动「' + item.title + '」吗？\n删除后无法恢复，相关的报名、报告、进度数据也会一并删除。')) return
  try {
    const res = await volunteerActivityApi().delete(item.id)
    if (res.code === 200) {
      alert('删除成功')
      loadData()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>