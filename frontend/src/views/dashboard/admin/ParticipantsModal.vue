<template>
  <div v-if="activity" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">参与者管理 - {{ activity.title }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <div class="btn-group btn-group-sm">
              <button class="btn" :class="filterStatus === null ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = null; loadParticipants()">全部</button>
              <button class="btn" :class="filterStatus === 0 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 0; loadParticipants()">待审核</button>
              <button class="btn" :class="filterStatus === 1 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 1; loadParticipants()">已通过</button>
              <button class="btn" :class="filterStatus === 2 ? 'btn-primary' : 'btn-outline-primary'" @click="filterStatus = 2; loadParticipants()">已拒绝</button>
            </div>
          </div>

          <div v-if="loading" class="text-center py-4"><div class="spinner-border text-primary"></div></div>
          <div v-else-if="participants.length === 0" class="text-center text-muted py-4">
            <i class="fas fa-users fa-2x mb-2"></i>
            <p>暂无参与者</p>
          </div>
          <div v-else>
            <div class="table-responsive">
              <table class="table table-hover align-middle">
                <thead class="table-light">
                  <tr>
                    <th>志愿者</th>
                    <th>联系方式</th>
                    <th>申请理由</th>
                    <th>状态</th>
                    <th>签到/工时</th>
                    <th>报告</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in participants" :key="p.id">
                    <td>
                      <div class="d-flex align-items-center">
                        <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2 flex-shrink-0" style="width: 36px; height: 36px; font-size: 14px;">
                          {{ (p.volunteerName || '?').charAt(0) }}
                        </div>
                        <div>
                          <div class="fw-semibold small">{{ p.volunteerName || '未知' }}</div>
                          <small class="text-muted">ID: {{ p.volunteerId }}</small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div v-if="p.volunteerPhone" class="small"><i class="fas fa-phone me-1"></i>{{ p.volunteerPhone }}</div>
                      <div v-else class="text-muted small">-</div>
                    </td>
                    <td>
                      <span class="small" :title="p.applyReason">{{ (p.applyReason || '-').substring(0, 20) }}{{ (p.applyReason || '').length > 20 ? '...' : '' }}</span>
                    </td>
                    <td>
                      <span class="badge" :class="statusBadgeClass(p.status)">{{ statusText(p.status) }}</span>
                    </td>
                    <td style="min-width: 120px;">
                      <div v-if="p.checkInTime" class="small">
                        <div><i class="fas fa-sign-in-alt text-success me-1"></i>{{ formatDateTime(p.checkInTime) }}</div>
                      </div>
                      <div v-if="p.checkOutTime" class="small">
                        <div><i class="fas fa-sign-out-alt text-warning me-1"></i>{{ formatDateTime(p.checkOutTime) }}</div>
                      </div>
                      <div v-if="p.workHours" class="small text-primary fw-bold">
                        <i class="fas fa-clock me-1"></i>{{ p.workHours }}小时
                      </div>
                      <div v-if="!p.checkInTime && !p.checkOutTime && !p.workHours" class="text-muted small">未签到</div>
                    </td>
                    <td>
                      <span v-if="p.isReported" class="badge bg-info"><i class="fas fa-file-alt me-1"></i>已提交</span>
                      <span v-else class="badge bg-secondary">未提交</span>
                    </td>
                    <td>
                      <div class="d-flex flex-wrap gap-1" style="min-width: 100px;">
                        <button v-if="p.status === 0" class="btn btn-sm btn-outline-success" title="通过" @click="approveParticipant(p.id)">
                          <i class="fas fa-check"></i>
                        </button>
                        <button v-if="p.status === 0" class="btn btn-sm btn-outline-danger" title="拒绝" @click="rejectParticipant(p.id)">
                          <i class="fas fa-times"></i>
                        </button>
                        <button v-if="p.status === 1 && !p.checkInTime" class="btn btn-sm btn-outline-primary" title="签到" @click="doCheckin(p)">
                          <i class="fas fa-sign-in-alt"></i>
                        </button>
                        <button v-if="p.status === 1 && p.checkInTime && !p.checkOutTime" class="btn btn-sm btn-outline-warning" title="签退" @click="doCheckout(p)">
                          <i class="fas fa-sign-out-alt"></i>
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

        <!-- 拒绝理由弹窗 -->
        <div v-if="showRejectModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.3);">
          <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content">
              <div class="modal-header">
                <h6 class="modal-title">拒绝参与者</h6>
                <button type="button" class="btn-close" @click="showRejectModal = false"></button>
              </div>
              <div class="modal-body">
                <div class="mb-3">
                  <label class="form-label">拒绝理由</label>
                  <textarea v-model="rejectReason" class="form-control" rows="3" placeholder="请输入拒绝理由..."></textarea>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-sm btn-secondary" @click="showRejectModal = false">取消</button>
                <button class="btn btn-sm btn-danger" @click="confirmReject">确认拒绝</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 签退弹窗(输入工时) -->
        <div v-if="showCheckoutModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.3);">
          <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content">
              <div class="modal-header">
                <h6 class="modal-title">签退 - {{ checkoutTarget?.volunteerName }}</h6>
                <button type="button" class="btn-close" @click="showCheckoutModal = false"></button>
              </div>
              <div class="modal-body">
                <div class="mb-3">
                  <label class="form-label">工作时长（小时）</label>
                  <input v-model="workHours" type="number" class="form-control" min="0.5" step="0.5" placeholder="输入工作时长...">
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-sm btn-secondary" @click="showCheckoutModal = false">取消</button>
                <button class="btn btn-sm btn-warning" @click="confirmCheckout">确认签退</button>
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

const participants = ref([])
const loading = ref(true)
const pageNum = ref(1)
const totalPages = ref(1)
const pageSize = 10
const filterStatus = ref(null)

const showRejectModal = ref(false)
const rejectTargetId = ref(null)
const rejectReason = ref('')

const showCheckoutModal = ref(false)
const checkoutTarget = ref(null)
const workHours = ref('')

function statusBadgeClass(s) {
  const map = { 0: 'bg-warning text-dark', 1: 'bg-success', 2: 'bg-danger' }
  return map[s] || 'bg-secondary'
}

function statusText(s) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[s] || '未知'
}

function formatDateTime(d) {
  if (!d) return '-'
  return d.replace('T', ' ').substring(0, 19)
}

async function loadParticipants() {
  if (!props.activity) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize }
    if (filterStatus.value !== null) params.status = filterStatus.value
    const res = await volunteerActivityApi().getParticipants(props.activity.id, params)
    if (res.code === 200) {
      const data = res.data
      participants.value = Array.isArray(data) ? data : (data.records || [])
      totalPages.value = data.pages || 1
    }
  } catch (e) { console.error('加载参与者失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  pageNum.value = p
  loadParticipants()
}

async function approveParticipant(id) {
  if (!confirm('确定通过该志愿者的参与申请吗？')) return
  try {
    const res = await volunteerActivityApi().approveParticipant(id)
    if (res.code === 200) loadParticipants()
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

function rejectParticipant(id) {
  rejectTargetId.value = id
  rejectReason.value = ''
  showRejectModal.value = true
}

async function confirmReject() {
  try {
    const res = await volunteerActivityApi().rejectParticipant(rejectTargetId.value, rejectReason.value || '管理员拒绝了申请')
    if (res.code === 200) {
      showRejectModal.value = false
      loadParticipants()
    } else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

async function doCheckin(p) {
  if (!confirm(`确认给 "${p.volunteerName}" 签到吗？`)) return
  try {
    const res = await volunteerActivityApi().checkin(p.id)
    if (res.code === 200) loadParticipants()
    else alert(res.message || '签到失败')
  } catch (e) { alert('签到失败') }
}

function doCheckout(p) {
  checkoutTarget.value = p
  workHours.value = ''
  showCheckoutModal.value = true
}

async function confirmCheckout() {
  if (!workHours.value || parseFloat(workHours.value) <= 0) {
    alert('请输入有效的工作时长')
    return
  }
  try {
    const res = await volunteerActivityApi().checkout(checkoutTarget.value.id, parseFloat(workHours.value))
    if (res.code === 200) {
      showCheckoutModal.value = false
      loadParticipants()
    } else alert(res.message || '签退失败')
  } catch (e) { alert('签退失败') }
}

onMounted(() => loadParticipants())
watch(() => props.activity, () => { pageNum.value = 1; loadParticipants() })
</script>