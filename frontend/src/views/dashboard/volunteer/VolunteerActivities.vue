<template>
  <div>
    <h2 class="mb-4">志愿者活动</h2>

    <!-- 选项卡 -->
    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'hall' }" href="#" @click.prevent="activeTab = 'hall'; loadData()">
          <i class="fas fa-th-list me-1"></i>活动大厅
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'mine' }" href="#" @click.prevent="activeTab = 'mine'; loadMyActivities()">
          <i class="fas fa-user-check me-1"></i>我的活动
        </a>
      </li>
    </ul>

    <!-- ==================== 活动大厅 ==================== -->
    <div v-if="activeTab === 'hall'">
      <div class="row mb-3">
        <div class="col-md-4">
          <select v-model="hallFilter.status" class="form-select form-select-sm" @change="loadData()">
            <option value="">全部状态</option>
            <option value="1">招募中</option>
            <option value="2">进行中</option>
            <option value="3">已结束</option>
          </select>
        </div>
        <div class="col-md-4">
          <select v-model="hallFilter.type" class="form-select form-select-sm" @change="loadData()">
            <option value="">全部类型</option>
            <option value="INFO_VERIFY">信息核实</option>
            <option value="CLUE_INVESTIGATE">线索调查</option>
            <option value="VOLUNTEER_RECRUIT">志愿者招募</option>
            <option value="COMMUNITY_PROMOTION">社区宣传</option>
          </select>
        </div>
        <div class="col-md-4">
          <input v-model="hallFilter.keyword" class="form-control form-control-sm" placeholder="搜索活动标题..." @input="onSearchInput">
        </div>
      </div>

      <div v-if="hallLoading" class="text-center py-5"><div class="spinner-border text-primary"></div></div>
      <div v-else-if="hallList.length === 0" class="text-center text-muted py-5">
        <i class="fas fa-calendar-alt fa-3x mb-3"></i>
        <p>暂无活动</p>
      </div>
      <div v-else class="row">
        <div v-for="item in hallList" :key="item.id" class="col-md-6 col-lg-4 mb-4">
          <div class="card h-100 shadow-sm">
            <div class="position-relative" style="height: 160px; overflow: hidden;">
              <img v-if="item.coverImage" :src="item.coverImage" class="card-img-top h-100 w-100" style="object-fit: cover;" @error="e => e.target.style.display='none'">
              <div v-else class="h-100 w-100 bg-light d-flex align-items-center justify-content-center">
                <i class="fas fa-image fa-3x text-muted"></i>
              </div>
              <span class="position-absolute top-0 end-0 badge m-2" :class="hallStatusBadge(item.status)">{{ hallStatusText(item.status) }}</span>
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
                <div v-if="item.startTime" class="mb-1"><i class="far fa-calendar-alt text-primary me-1"></i>{{ formatDateTime(item.startTime) }} ~ {{ formatDateTime(item.endTime) }}</div>
                <div><i class="fas fa-tag text-success me-1"></i>{{ item.typeName || item.type }}</div>
              </div>
              <div class="d-flex flex-wrap gap-2 mt-2">
                <button class="btn btn-sm btn-outline-info flex-grow-1" @click="viewDetail(item)"><i class="fas fa-info-circle me-1"></i>详情</button>
                <button v-if="item.hasParticipated" class="btn btn-sm" :class="item.participantStatus === 1 ? 'btn-outline-success' : 'btn-outline-warning'" disabled>
                  <i v-if="item.participantStatus === 0" class="fas fa-clock me-1"></i>
                  <i v-else-if="item.participantStatus === 1" class="fas fa-check me-1"></i>
                  <i v-else class="fas fa-times me-1"></i>
                  {{ item.participantStatus === 0 ? '审核中' : item.participantStatus === 1 ? '已参与' : '已拒绝' }}
                </button>
                <button v-else-if="item.status === 1" class="btn btn-sm btn-primary" @click="openJoin(item)">
                  <i class="fas fa-plus-circle me-1"></i>报名参加
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <nav v-if="hallTotalPages > 1" class="mt-4">
        <ul class="pagination pagination-sm justify-content-center">
          <li class="page-item" :class="{ disabled: hallPageNum <= 1 }">
            <a class="page-link" href="#" @click.prevent="hallPageNum > 1 && changeHallPage(hallPageNum - 1)">上一页</a>
          </li>
          <li v-for="p in hallTotalPages" :key="p" class="page-item" :class="{ active: p === hallPageNum }">
            <a class="page-link" href="#" @click.prevent="changeHallPage(p)">{{ p }}</a>
          </li>
          <li class="page-item" :class="{ disabled: hallPageNum >= hallTotalPages }">
            <a class="page-link" href="#" @click.prevent="hallPageNum < hallTotalPages && changeHallPage(hallPageNum + 1)">下一页</a>
          </li>
        </ul>
      </nav>
    </div>

    <!-- ==================== 我的活动 ==================== -->
    <div v-if="activeTab === 'mine'">
      <div class="mb-3">
        <select v-model="mineFilter.status" class="form-select form-select-sm w-auto" @change="loadMyActivities()">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
        </select>
      </div>

      <div v-if="mineLoading" class="text-center py-5"><div class="spinner-border text-primary"></div></div>
      <div v-else-if="mineList.length === 0" class="text-center text-muted py-5">
        <i class="fas fa-user-check fa-3x mb-3"></i>
        <p>暂无参与的活动</p>
        <button class="btn btn-primary btn-sm" @click="activeTab = 'hall'">去活动大厅看看</button>
      </div>
      <div v-else>
        <div class="row">
          <div v-for="item in mineList" :key="item.id" class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100 shadow-sm">
              <div class="position-relative" style="height: 140px; overflow: hidden;">
                <img v-if="item.coverImage" :src="item.coverImage" class="card-img-top h-100 w-100" style="object-fit: cover;" @error="e => e.target.style.display='none'">
                <div v-else class="h-100 w-100 bg-light d-flex align-items-center justify-content-center">
                  <i class="fas fa-image fa-3x text-muted"></i>
                </div>
                <span class="position-absolute top-0 end-0 badge m-2" :class="hallStatusBadge(item.status)">{{ hallStatusText(item.status) }}</span>
              </div>
              <div class="card-body d-flex flex-column">
                <h5 class="card-title">{{ item.title }}</h5>
                <div class="mb-2">
                  <span class="badge me-1" :class="participantStatusBadge(item.participantStatus)">{{ participantStatusText(item.participantStatus) }}</span>
                  <span v-if="item.myWorkHours" class="badge bg-info"><i class="fas fa-clock me-1"></i>{{ item.myWorkHours }}h</span>
                </div>
                <div class="text-muted small mb-2">
                  <div v-if="item.location" class="mb-1"><i class="fas fa-map-marker-alt text-danger me-1"></i>{{ item.location }}</div>
                  <div v-if="item.startTime" class="mb-1"><i class="far fa-calendar-alt text-primary me-1"></i>{{ formatDateTime(item.startTime) }}</div>
                </div>
                <div class="d-flex flex-wrap gap-2 mt-auto">
                  <button class="btn btn-sm btn-outline-info" @click="viewDetail(item)"><i class="fas fa-eye me-1"></i>详情</button>
                  <button v-if="item.participantStatus === 1 && item.status === 2" class="btn btn-sm btn-outline-primary" @click="doCheckin(item)">
            <i class="fas fa-sign-in-alt me-1"></i>签到
          </button>
          <button v-if="item.participantStatus === 1 && (item.status === 2 || item.status === 3) && item.canReport" class="btn btn-sm btn-outline-success" @click="openSubmitReport(item)">
            <i class="fas fa-edit me-1"></i>提交报告
          </button>
          <button v-if="item.participantStatus === 1 && (item.status === 2 || item.status === 3) && !item.canReport" class="btn btn-sm btn-outline-secondary" @click="viewMyReport(item)">
            <i class="fas fa-file-alt me-1"></i>查看报告
          </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <nav v-if="mineTotalPages > 1" class="mt-4">
          <ul class="pagination pagination-sm justify-content-center">
            <li class="page-item" :class="{ disabled: minePageNum <= 1 }">
              <a class="page-link" href="#" @click.prevent="minePageNum > 1 && changeMinePage(minePageNum - 1)">上一页</a>
            </li>
            <li v-for="p in mineTotalPages" :key="p" class="page-item" :class="{ active: p === minePageNum }">
              <a class="page-link" href="#" @click.prevent="changeMinePage(p)">{{ p }}</a>
            </li>
            <li class="page-item" :class="{ disabled: minePageNum >= mineTotalPages }">
              <a class="page-link" href="#" @click.prevent="minePageNum < mineTotalPages && changeMinePage(minePageNum + 1)">下一页</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- ==================== 活动详情弹窗 ==================== -->
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
              <span class="badge me-2" :class="hallStatusBadge(detailItem.status)">{{ hallStatusText(detailItem.status) }}</span>
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
                <i class="far fa-calendar-alt text-primary me-1"></i>时间：{{ formatDateTime(detailItem.startTime) }} ~ {{ formatDateTime(detailItem.endTime) }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="fas fa-users text-success me-1"></i>参与人数：{{ detailItem.participantCount || 0 }}/{{ detailItem.maxParticipants || '无限制' }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="far fa-eye text-info me-1"></i>浏览次数：{{ detailItem.viewCount || 0 }}
              </div>
              <div class="col-md-6 mb-2">
                <i class="fas fa-user text-primary me-1"></i>发布者：{{ detailItem.publisherName || '未知' }}
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
            <button v-if="detailItem.status === 1 && !detailItem.hasParticipated" class="btn btn-primary" @click="openJoin(detailItem); showDetailModal = false">报名参加</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 报名弹窗 ==================== -->
    <div v-if="showJoinModal && joinTarget" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.4);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">报名活动</h5>
            <button type="button" class="btn-close" @click="showJoinModal = false"></button>
          </div>
          <div class="modal-body">
            <p class="fw-semibold">{{ joinTarget.title }}</p>
            <div class="mb-3">
              <label class="form-label">申请理由 <span class="text-danger">*</span></label>
              <textarea v-model="applyReason" class="form-control" rows="4" placeholder="请描述您为什么想参加这个活动，以及您的相关经验和技能..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showJoinModal = false">取消</button>
            <button class="btn btn-primary" @click="confirmJoin" :disabled="!applyReason.trim() || joining">
              <span v-if="joining" class="spinner-border spinner-border-sm me-1"></span>
              提交申请
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 提交报告弹窗 ==================== -->
    <div v-if="showReportModal && reportTarget" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">提交活动报告 - {{ reportTarget.title }}</h5>
            <button type="button" class="btn-close" @click="showReportModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">报告标题 <span class="text-danger">*</span></label>
              <input v-model="reportForm.title" class="form-control" placeholder="请输入报告标题" maxlength="200">
            </div>
            <div class="mb-3">
              <label class="form-label">报告内容 <span class="text-danger">*</span></label>
              <textarea v-model="reportForm.content" class="form-control" rows="5" placeholder="请详细描述您的活动参与情况和心得..."></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">工作成果</label>
              <textarea v-model="reportForm.workResults" class="form-control" rows="4" placeholder="请描述您取得的工作成果..."></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">遇到的问题</label>
              <textarea v-model="reportForm.problemsEncountered" class="form-control" rows="3" placeholder="请描述您在活动中遇到的问题..."></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">照片上传</label>
              <input type="file" class="form-control" accept="image/*" multiple @change="onReportPhotosChange">
              <div class="form-text">支持 JPG、PNG 格式，可多选</div>
              <div v-if="reportPhotoPreviews.length > 0" class="mt-2 d-flex flex-wrap gap-2">
                <div v-for="(preview, idx) in reportPhotoPreviews" :key="idx" class="position-relative">
                  <img :src="preview" class="img-thumbnail" style="max-height: 80px;">
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showReportModal = false">取消</button>
            <button class="btn btn-success" @click="submitReport" :disabled="!reportForm.title.trim() || !reportForm.content.trim() || reportSubmitting">
              <span v-if="reportSubmitting" class="spinner-border spinner-border-sm me-1"></span>
              提交报告
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 我的报告查看弹窗 ==================== -->
    <div v-if="showMyReportModal && myReport" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">我的活动报告</h5>
            <button type="button" class="btn-close" @click="showMyReportModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <span class="badge me-2" :class="reportStatusBadge(myReport.status)">{{ reportStatusText(myReport.status) }}</span>
              <small class="text-muted">提交时间：{{ formatDateTime(myReport.createTime) }}</small>
            </div>
            <div class="mb-3">
              <h6 class="text-muted border-bottom pb-2">报告标题</h6>
              <p class="fw-semibold">{{ myReport.title || '无标题' }}</p>
            </div>
            <div class="mb-3">
              <h6 class="text-muted border-bottom pb-2">报告内容</h6>
              <p style="white-space: pre-wrap; line-height: 1.8;">{{ myReport.content || '无' }}</p>
            </div>
            <div v-if="myReport.workResults" class="mb-3">
              <h6 class="text-muted border-bottom pb-2">工作成果</h6>
              <p style="white-space: pre-wrap;">{{ myReport.workResults }}</p>
            </div>
            <div v-if="myReport.problemsEncountered" class="mb-3">
              <h6 class="text-muted border-bottom pb-2">遇到的问题</h6>
              <p style="white-space: pre-wrap;">{{ myReport.problemsEncountered }}</p>
            </div>
            <div v-if="myReport.photos" class="mb-3">
              <h6 class="text-muted border-bottom pb-2">照片</h6>
              <div class="d-flex flex-wrap gap-2">
                <div v-for="(photo, idx) in myReport.photos.split(',')" :key="idx">
                  <img :src="photo.trim()" class="img-thumbnail" style="max-height: 100px; cursor: pointer;" @click="previewImage(photo.trim())">
                </div>
              </div>
            </div>
            <div v-if="myReport.reviewRemark" class="mb-3">
              <h6 class="text-muted border-bottom pb-2">审核意见</h6>
              <p :class="myReport.status === 1 ? 'text-success' : 'text-danger'">{{ myReport.reviewRemark }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showMyReportModal = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { volunteerActivityApi } from '@/api'

const activeTab = ref('hall')

// 活动大厅
const hallList = ref([])
const hallLoading = ref(true)
const hallPageNum = ref(1)
const hallTotalPages = ref(1)
const hallPageSize = 12
const hallFilter = reactive({ status: '', type: '', keyword: '' })
let searchTimer = null

// 我的活动
const mineList = ref([])
const mineLoading = ref(false)
const minePageNum = ref(1)
const mineTotalPages = ref(1)
const minePageSize = 12
const mineFilter = reactive({ status: '' })

// 详情弹窗
const showDetailModal = ref(false)
const detailItem = ref(null)

// 报名弹窗
const showJoinModal = ref(false)
const joinTarget = ref(null)
const applyReason = ref('')
const joining = ref(false)

// 报告提交弹窗
const showReportModal = ref(false)
const reportTarget = ref(null)
const reportSubmitting = ref(false)
const reportForm = reactive({
  title: '',
  content: '',
  workResults: '',
  problemsEncountered: ''
})
const reportPhotoFiles = ref([])
const reportPhotoPreviews = ref([])

// 我的报告查看
const showMyReportModal = ref(false)
const myReport = ref(null)

// 状态映射
function hallStatusBadge(s) {
  const map = { 0: 'bg-secondary', 1: 'bg-primary', 2: 'bg-success', 3: 'bg-dark', 4: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
function hallStatusText(s) {
  const map = { 0: '草稿', 1: '招募中', 2: '进行中', 3: '已结束', 4: '已取消' }
  return map[s] || '未知'
}
function participantStatusBadge(s) {
  const map = { 0: 'bg-warning text-dark', 1: 'bg-success', 2: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
function participantStatusText(s) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[s] || '未知'
}
function reportStatusBadge(s) {
  const map = { 0: 'bg-warning text-dark', 1: 'bg-success', 2: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
function reportStatusText(s) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[s] || '未知'
}
function formatDateTime(d) {
  if (!d) return '-'
  return d.replace('T', ' ').substring(0, 16)
}

function previewImage(url) {
  window.open(url, '_blank')
}

// 活动大厅
async function loadData() {
  hallLoading.value = true
  try {
    const params = { pageNum: hallPageNum.value, pageSize: hallPageSize }
    if (hallFilter.status) params.status = parseInt(hallFilter.status)
    if (hallFilter.type) params.type = hallFilter.type
    if (hallFilter.keyword) params.keyword = hallFilter.keyword
    const res = await volunteerActivityApi().list(params)
    if (res.code === 200) {
      const data = res.data
      hallList.value = Array.isArray(data) ? data : (data.records || [])
      hallTotalPages.value = data.pages || 1
    }
  } catch (e) { console.error('加载活动失败:', e) }
  finally { hallLoading.value = false }
}

function changeHallPage(p) {
  hallPageNum.value = p
  loadData()
}

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    hallPageNum.value = 1
    loadData()
  }, 500)
}

// 我的活动
async function loadMyActivities() {
  mineLoading.value = true
  try {
    const params = { pageNum: minePageNum.value, pageSize: minePageSize }
    if (mineFilter.status !== '') params.status = parseInt(mineFilter.status)
    const res = await volunteerActivityApi().getMyActivities(params)
    if (res.code === 200) {
      const data = res.data
      mineList.value = Array.isArray(data) ? data : (data.records || [])
      mineTotalPages.value = data.pages || 1
    } else {
      console.error('加载我的活动失败:', res.message)
    }
  } catch (e) {
    console.error('加载我的活动失败:', e)
  }
  finally { mineLoading.value = false }
}

function changeMinePage(p) {
  minePageNum.value = p
  loadMyActivities()
}

// 活动详情
function viewDetail(item) {
  detailItem.value = item
  showDetailModal.value = true
}

// 报名
function openJoin(item) {
  joinTarget.value = item
  applyReason.value = ''
  showJoinModal.value = true
}

async function confirmJoin() {
  if (!applyReason.value.trim()) {
    alert('请输入申请理由')
    return
  }
  joining.value = true
  try {
    const res = await volunteerActivityApi().join(joinTarget.value.id, { applyReason: applyReason.value })
    if (res.code === 200) {
      showJoinModal.value = false
      alert('报名成功！请等待管理员审核。')
      loadData()
    } else alert(res.message || '报名失败')
  } catch (e) { alert('报名失败') }
  finally { joining.value = false }
}

// 签到
async function doCheckin(item) {
  if (!confirm(`确认在活动"${item.title}"中签到吗？`)) return
  try {
    const res = await volunteerActivityApi().checkinSelf(item.id)
    if (res.code === 200) {
      alert('签到成功！')
      loadMyActivities()
    } else alert(res.message || '签到失败')
  } catch (e) { alert('签到失败') }
}

// 照片上传处理
function onReportPhotosChange(e) {
  const files = Array.from(e.target.files || [])
  reportPhotoFiles.value = files
  reportPhotoPreviews.value = files.map(f => URL.createObjectURL(f))
}

// 提交报告
function openSubmitReport(item) {
  reportTarget.value = item
  reportForm.title = ''
  reportForm.content = ''
  reportForm.workResults = ''
  reportForm.problemsEncountered = ''
  reportPhotoFiles.value = []
  reportPhotoPreviews.value = []
  showReportModal.value = true
}

async function submitReport() {
  if (!reportForm.title.trim()) { alert('请输入报告标题'); return }
  if (!reportForm.content.trim()) { alert('请输入报告内容'); return }
  reportSubmitting.value = true
  try {
    const formData = new FormData()
    formData.append('title', reportForm.title.trim())
    formData.append('content', reportForm.content.trim())
    if (reportForm.workResults.trim()) formData.append('workResults', reportForm.workResults.trim())
    if (reportForm.problemsEncountered.trim()) formData.append('problemsEncountered', reportForm.problemsEncountered.trim())
    reportPhotoFiles.value.forEach(f => formData.append('photos', f))
    const res = await volunteerActivityApi().submitReport(reportTarget.value.id, formData)
    if (res.code === 200) {
      showReportModal.value = false
      alert('报告提交成功！请等待管理员审核。')
      loadMyActivities()
    } else alert(res.message || '提交失败')
  } catch (e) {
    console.error('提交报告失败:', e)
    const msg = e?.response?.data?.message || e?.message || '提交失败'
    alert('提交失败: ' + msg)
  }
  finally { reportSubmitting.value = false }
}

// 查看我的报告
async function viewMyReport(item) {
  try {
    const res = await volunteerActivityApi().getMyReport(item.id)
    if (res.code === 200 && res.data) {
      myReport.value = res.data
      showMyReportModal.value = true
    } else {
      alert('暂无报告信息')
    }
  } catch (e) { alert('加载报告失败') }
}

onMounted(() => {
  loadData()
  loadMyActivities()
})
</script>