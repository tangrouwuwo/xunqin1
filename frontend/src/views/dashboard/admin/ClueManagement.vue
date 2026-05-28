<template>
  <div>
    <h2 class="mb-4">线索管理</h2>

    <div class="card mb-4">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-auto">
            <select v-model="filter.status" class="form-select" @change="loadData">
              <option value="">全部状态</option>
              <option value="0">待审核</option>
              <option value="1">已通过</option>
              <option value="2">已驳回</option>
            </select>
          </div>
          <div class="col">
            <input v-model="filter.content" class="form-control" placeholder="搜索线索内容..." @keyup.enter="loadData">
          </div>
          <div class="col-auto">
            <button class="btn btn-outline-primary" @click="loadData"><i class="fas fa-search"></i> 搜索</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-hover">
            <thead class="table-light">
              <tr><th>ID</th><th>关联寻亲</th><th>提交人</th><th>匿名</th><th>线索内容</th><th>状态</th><th>提交时间</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="loading"><td colspan="8" class="text-center py-4"><div class="loader"></div></td></tr>
              <tr v-else-if="list.length === 0"><td colspan="8" class="text-center text-muted py-4">暂无线索</td></tr>
              <tr v-for="item in list" :key="item.id" :class="{'table-warning': item.status === 0}">
                <td>{{ item.id }}</td>
                <td>{{ item.missingPersonName || '-' }}</td>
                <td>{{ item.submitterName || (item.providerId || '-') }}</td>
                <td><span v-if="item.isAnonymous == 1" class="text-muted"><i class="fas fa-user-secret"></i> 匿名</span><span v-else>否</span></td>
                <td>{{ (item.content || '').substring(0, 40) }}{{ (item.content || '').length > 40 ? '...' : '' }}</td>
                <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
                <td>{{ formatTime(item.createTime) }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
                  <button v-if="item.status === 0" class="btn btn-sm btn-outline-success me-1" @click="approveClue(item)"><i class="fas fa-check"></i></button>
                  <button v-if="item.status === 0" class="btn btn-sm btn-outline-warning me-1" @click="showReject(item)"><i class="fas fa-times"></i></button>
                  <button class="btn btn-sm btn-outline-primary me-1" @click="showEdit(item)"><i class="fas fa-edit"></i></button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteClue(item)"><i class="fas fa-trash"></i></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetail" class="modal fade show d-block" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">线索详情 #{{ detail.id }}</h5>
            <button type="button" class="btn-close" @click="showDetail = false"></button>
          </div>
          <div class="modal-body">
            <div class="row mb-3">
              <div class="col-md-4"><strong>关联寻亲者：</strong>{{ detail.missingPersonName || detail.missingPersonId || '-' }}</div>
              <div class="col-md-4"><strong>提交人：</strong>{{ detail.submitterName || (detail.isAnonymous == 1 ? '匿名' : detail.providerId || '匿名') }}</div>
              <div class="col-md-4"><strong>匿名提交：</strong>{{ detail.isAnonymous == 1 ? '是' : '否' }}</div>
            </div>
            <div class="mb-3">
              <strong class="fs-5 text-primary"><i class="fas fa-align-left me-2"></i>线索内容</strong>
              <div class="mt-2 p-4 bg-light rounded border-start border-primary border-4" style="white-space:pre-wrap; font-size:1.05rem; line-height:1.8; min-height:80px;">{{ detail.content || '暂无内容' }}</div>
            </div>
            <div class="row text-muted small">
              <div class="col-md-6">提交时间：{{ formatTime(detail.createTime) }}</div>
              <div class="col-md-6">审核时间：{{ formatTime(detail.handleTime) }}</div>
            </div>
            <div class="mb-3" v-if="detail.handleRemark">
              <strong>审核备注：</strong>
              <p class="mt-1 text-muted">{{ detail.handleRemark }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetail = false">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 驳回弹窗 -->
    <div v-if="showRejectModal" class="modal fade show d-block" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">驳回线索 #{{ rejectTarget?.id }}</h5>
            <button type="button" class="btn-close" @click="showRejectModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">驳回原因 <span class="text-danger">*</span></label>
              <textarea v-model="rejectReason" class="form-control" rows="3" placeholder="请输入驳回原因..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showRejectModal = false">取消</button>
            <button class="btn btn-warning" @click="confirmReject" :disabled="!rejectReason.trim()"><i class="fas fa-times me-1"></i>确认驳回</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditModal" class="modal fade show d-block" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑线索 #{{ editTarget?.id }}</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">线索内容 <span class="text-danger">*</span></label>
              <textarea v-model="editForm.content" class="form-control" rows="4"></textarea>
            </div>
            <div class="row mb-3">
              <div class="col-md-4">
                <label class="form-label">联系人姓名</label>
                <input v-model="editForm.contactName" class="form-control">
              </div>
              <div class="col-md-4">
                <label class="form-label">联系电话</label>
                <input v-model="editForm.contactPhone" class="form-control">
              </div>
              <div class="col-md-4">
                <label class="form-label">联系邮箱</label>
                <input v-model="editForm.contactEmail" class="form-control">
              </div>
            </div>
            <div class="form-check">
              <input type="checkbox" class="form-check-input" id="editAnon" v-model="editForm.isAnonymous">
              <label class="form-check-label" for="editAnon">匿名提交</label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
            <button class="btn btn-primary" @click="confirmEdit" :disabled="saving">
              <i class="fas fa-save me-1"></i>{{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { clueApi } from '@/api'

const list = ref([])
const loading = ref(true)
const filter = reactive({ status: '', content: '' })

// 详情
const showDetail = ref(false)
const detail = ref({})

// 驳回
const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')

// 编辑
const showEditModal = ref(false)
const editTarget = ref(null)
const editForm = reactive({ content: '', contactName: '', contactPhone: '', contactEmail: '', isAnonymous: false })
const saving = ref(false)

function statusClass(s) {
  const map = { 0: 'pending', 1: 'success', 2: 'rejected' }
  return map[s] || 'pending'
}

function statusText(s) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[s] || '未知'
}

function formatTime(t) {
  if (!t) return '-'
  return t.substring(0, 16).replace('T', ' ')
}

function viewDetail(item) {
  detail.value = { ...item }
  showDetail.value = true
  // 单独获取详情以确保字段完整
  clueApi().getById(item.id).then(res => {
    if (res.code === 200) {
      detail.value = res.data
    }
  }).catch(() => {})
}

function approveClue(item) {
  if (!confirm(`确定审核通过线索 #${item.id} 吗？`)) return
  clueApi().approve(item.id, {}).then(res => {
    if (res.code === 200) {
      alert('审核通过成功，寻亲者将收到通知')
      loadData()
    } else alert(res.message || '操作失败')
  }).catch(() => alert('操作失败'))
}

function showReject(item) {
  rejectTarget.value = item
  rejectReason.value = ''
  showRejectModal.value = true
}

function confirmReject() {
  if (!rejectReason.value.trim()) { alert('请输入驳回原因'); return }
  clueApi().reject(rejectTarget.value.id, { rejectReason: rejectReason.value }).then(res => {
    if (res.code === 200) {
      alert('已驳回')
      showRejectModal.value = false
      loadData()
    } else alert(res.message || '操作失败')
  }).catch(() => alert('操作失败'))
}

function showEdit(item) {
  editTarget.value = item
  editForm.content = item.content || ''
  editForm.contactName = item.contactName || ''
  editForm.contactPhone = item.contactPhone || ''
  editForm.contactEmail = item.contactEmail || ''
  editForm.isAnonymous = item.isAnonymous == 1
  showEditModal.value = true
}

async function confirmEdit() {
  if (!editForm.content.trim()) { alert('请输入线索内容'); return }
  saving.value = true
  try {
    const res = await clueApi().adminUpdate(editTarget.value.id, {
      missingPersonId: editTarget.value.missingPersonId,
      content: editForm.content,
      isAnonymous: editForm.isAnonymous ? 1 : 0,
      contactName: editForm.contactName,
      contactPhone: editForm.contactPhone,
      contactEmail: editForm.contactEmail
    })
    if (res.code === 200) {
      alert('修改成功')
      showEditModal.value = false
      loadData()
    } else alert(res.message || '修改失败')
  } catch (e) { alert('修改失败') }
  finally { saving.value = false }
}

function deleteClue(item) {
  if (!confirm(`确定删除线索 #${item.id} 吗？此操作不可恢复！`)) return
  clueApi().adminDelete(item.id).then(res => {
    if (res.code === 200) {
      alert('删除成功')
      loadData()
    } else alert(res.message || '删除失败')
  }).catch(() => alert('删除失败'))
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filter.status) params.status = filter.status
    if (filter.content) params.content = filter.content
    const res = await clueApi().listAll(params)
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

onMounted(() => loadData())
</script>