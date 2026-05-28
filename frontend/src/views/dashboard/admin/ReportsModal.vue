<template>
  <div v-if="activity" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">活动报告审核 - {{ activity.title }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <div class="btn-group btn-group-sm">
              <button class="btn" :class="filterStatus === null ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = null; loadReports()">全部</button>
              <button class="btn" :class="filterStatus === 0 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 0; loadReports()">待审核</button>
              <button class="btn" :class="filterStatus === 1 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 1; loadReports()">已通过</button>
              <button class="btn" :class="filterStatus === 2 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 2; loadReports()">已驳回</button>
            </div>
          </div>

          <div v-if="loading" class="text-center py-4"><div class="spinner-border text-primary"></div></div>
          <div v-else-if="reports.length === 0" class="text-center text-muted py-4">
            <i class="fas fa-file-alt fa-2x mb-2"></i>
            <p>暂无报告</p>
          </div>
          <div v-else>
            <div class="table-responsive">
              <table class="table table-hover align-middle">
                <thead class="table-light">
                  <tr>
                    <th>提交人</th>
                    <th>报告标题</th>
                    <th>状态</th>
                    <th>提交时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in reports" :key="r.id">
                    <td>
                      <div class="d-flex align-items-center">
                        <div class="rounded-circle bg-info text-white d-flex align-items-center justify-content-center me-2 flex-shrink-0" style="width: 32px; height: 32px; font-size: 12px;">
                          {{ (r.volunteerName || '?').charAt(0) }}
                        </div>
                        <span class="small">{{ r.volunteerName || '未知' }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="small fw-semibold">{{ r.title || '无标题' }}</span>
                    </td>
                    <td>
                      <span class="badge" :class="reportStatusBadge(r.status)">{{ reportStatusText(r.status) }}</span>
                    </td>
                    <td>
                      <span class="small text-muted">{{ formatDateTime(r.createTime) }}</span>
                    </td>
                    <td>
                      <div class="d-flex flex-wrap gap-1">
                        <button class="btn btn-sm btn-outline-info" title="查看详情" @click="viewReportDetail(r)">
                          <i class="fas fa-eye"></i> 详情
                        </button>
                        <button v-if="r.status === 0" class="btn btn-sm btn-outline-success" title="通过" @click="approveReport(r)">
                          <i class="fas fa-check"></i>
                        </button>
                        <button v-if="r.status === 0" class="btn btn-sm btn-outline-danger" title="驳回" @click="rejectReport(r)">
                          <i class="fas fa-times"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <nav v-if="totalPages > 1" class="mt-3">
              <ul class="pagination pagination-sm justify-content-center mb-0">
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
          </div>
        </div>

        <!-- 报告详情弹窗 -->
        <div v-if="showDetailModal && detailReport" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.3);">
          <div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">报告详情</h5>
                <button type="button" class="btn-close" @click="showDetailModal = false"></button>
              </div>
              <div class="modal-body">
                <div class="mb-3">
                  <span class="badge me-2" :class="reportStatusBadge(detailReport.status)">{{ reportStatusText(detailReport.status) }}</span>
                  <small class="text-muted">提交人：{{ detailReport.volunteerName || '未知' }}</small>
                  <small class="text-muted ms-3">提交时间：{{ formatDateTime(detailReport.createTime) }}</small>
                </div>

                <div class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">报告标题</h6>
                  <p class="fw-semibold">{{ detailReport.title || '无标题' }}</p>
                </div>

                <div class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">报告内容</h6>
                  <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailReport.content || '无' }}</p>
                </div>

                <div v-if="detailReport.workContent" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">工作内容</h6>
                  <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailReport.workContent }}</p>
                </div>

                <div v-if="detailReport.workResults" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">工作成果</h6>
                  <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailReport.workResults }}</p>
                </div>

                <div v-if="detailReport.problemsEncountered" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">遇到的问题</h6>
                  <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailReport.problemsEncountered }}</p>
                </div>

                <div v-if="detailReport.suggestions" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">建议与反馈</h6>
                  <p style="white-space: pre-wrap; line-height: 1.8;">{{ detailReport.suggestions }}</p>
                </div>

                <div v-if="detailReport.photos" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">照片</h6>
                  <div class="d-flex flex-wrap gap-2">
                    <div v-for="(photo, idx) in detailReport.photos.split(',')" :key="idx">
                      <img :src="photo.trim()" class="img-thumbnail" style="max-height: 120px; cursor: pointer;" @click="previewImage(photo.trim())">
                    </div>
                  </div>
                </div>

                <div v-if="detailReport.reviewRemark" class="mb-3">
                  <h6 class="text-muted border-bottom pb-2">审核意见</h6>
                  <p :class="detailReport.status === 1 ? 'text-success' : 'text-danger'">{{ detailReport.reviewRemark }}</p>
                  <small v-if="detailReport.reviewTime" class="text-muted">审核时间：{{ formatDateTime(detailReport.reviewTime) }}</small>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
                <button v-if="detailReport.status === 0" class="btn btn-success" @click="approveReport(detailReport); showDetailModal = false">审核通过</button>
                <button v-if="detailReport.status === 0" class="btn btn-danger" @click="rejectReport(detailReport); showDetailModal = false">驳回</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 图片预览 -->
        <div v-if="showImagePreview" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.7);" @click="showImagePreview = false">
          <div class="modal-dialog modal-dialog-centered modal-xl">
            <div class="modal-content bg-transparent border-0">
              <div class="text-end mb-2">
                <button class="btn btn-sm btn-light" @click="showImagePreview = false"><i class="fas fa-times"></i> 关闭</button>
              </div>
              <img :src="previewUrl" class="img-fluid" style="max-height: 80vh; object-fit: contain;">
            </div>
          </div>
        </div>

        <!-- 审核操作弹窗 -->
        <div v-if="showReviewModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.3);">
          <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content">
              <div class="modal-header">
                <h6 class="modal-title">{{ isApproving ? '通过报告' : '驳回报告' }}</h6>
                <button type="button" class="btn-close" @click="showReviewModal = false"></button>
              </div>
              <div class="modal-body">
                <div class="mb-3">
                  <label class="form-label">审核意见</label>
                  <textarea v-model="reviewRemark" class="form-control" rows="3" :placeholder="isApproving ? '可选：输入审核意见...' : '请输入驳回理由...'"></textarea>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-sm btn-secondary" @click="showReviewModal = false">取消</button>
                <button class="btn btn-sm" :class="isApproving ? 'btn-success' : 'btn-danger'" @click="confirmReview">
                  {{ isApproving ? '确认通过' : '确认驳回' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="$emit('close')">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { volunteerActivityApi } from '@/api'

const props = defineProps({ activity: Object })
const emit = defineEmits(['close'])

const reports = ref([])
const loading = ref(true)
const pageNum = ref(1)
const totalPages = ref(1)
const pageSize = 10
const filterStatus = ref(null)

const showDetailModal = ref(false)
const detailReport = ref(null)

const showImagePreview = ref(false)
const previewUrl = ref('')

const showReviewModal = ref(false)
const reviewTarget = ref(null)
const isApproving = ref(false)
const reviewRemark = ref('')

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
  return d.replace('T', ' ').substring(0, 19)
}

async function loadReports() {
  if (!props.activity) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize }
    if (filterStatus.value !== null) params.status = filterStatus.value
    const res = await volunteerActivityApi().getReports(props.activity.id, params)
    if (res.code === 200) {
      const data = res.data
      reports.value = Array.isArray(data) ? data : (data.records || [])
      totalPages.value = data.pages || 1
    }
  } catch (e) { console.error('加载报告失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  pageNum.value = p
  loadReports()
}

function viewReportDetail(r) {
  detailReport.value = r
  showDetailModal.value = true
}

function previewImage(url) {
  previewUrl.value = url
  showImagePreview.value = true
}

function approveReport(r) {
  reviewTarget.value = r
  isApproving.value = true
  reviewRemark.value = ''
  showReviewModal.value = true
}

function rejectReport(r) {
  reviewTarget.value = r
  isApproving.value = false
  reviewRemark.value = ''
  showReviewModal.value = true
}

async function confirmReview() {
  if (!reviewTarget.value) return
  try {
    let res
    if (isApproving.value) {
      res = await volunteerActivityApi().approveReport(reviewTarget.value.id, reviewRemark.value)
    } else {
      if (!reviewRemark.value.trim()) {
        alert('请输入驳回理由')
        return
      }
      res = await volunteerActivityApi().rejectReport(reviewTarget.value.id, reviewRemark.value)
    }
    if (res.code === 200) {
      showReviewModal.value = false
      showDetailModal.value = false
      loadReports()
    } else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

onMounted(() => loadReports())
watch(() => props.activity, () => { pageNum.value = 1; loadReports() })
</script>