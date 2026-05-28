<template>
  <div>
    <h2 class="mb-4">我的任务</h2>

    <div class="alert alert-info">
      <i class="fas fa-info-circle me-2"></i>寻亲者发布的任务需要管理员审核通过后，才会出现在任务大厅供志愿者认领。
    </div>

    <div class="row mb-3">
      <div class="col-md-8 d-flex align-items-center">
        <button class="btn btn-primary me-3" @click="showCreateModal = true">
          <i class="fas fa-plus-circle me-2"></i>发布任务
        </button>
        <select v-model="searchType" class="form-control me-2" style="max-width:150px" @change="loadData">
          <option value="">全部类型</option>
          <option value="INVESTIGATE">实地调查</option>
          <option value="SEARCH">搜索寻人</option>
          <option value="FOLLOW_UP">跟进回访</option>
          <option value="ANALYZE">信息分析</option>
          <option value="OTHER">其他</option>
        </select>
        <input v-model="searchTitle" class="form-control me-2" style="max-width:200px" placeholder="搜索标题..." @keyup.enter="loadData">
        <button class="btn btn-outline-secondary" @click="loadData"><i class="fas fa-search"></i></button>
      </div>
      <div class="col-md-4 d-flex justify-content-end">
        <div class="btn-group">
          <button class="btn" :class="activeTab === 'all' ? 'btn-primary' : 'btn-outline-primary'" @click="activeTab = 'all'; loadData()">全部</button>
          <button class="btn" :class="activeTab === 'pending' ? 'btn-primary' : 'btn-outline-primary'" @click="activeTab = 'pending'; loadData()">待审核</button>
          <button class="btn" :class="activeTab === 'active' ? 'btn-primary' : 'btn-outline-primary'" @click="activeTab = 'active'; loadData()">进行中</button>
        </div>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>任务标题</th>
            <th>类型</th>
            <th>地点</th>
            <th>所需技能</th>
            <th>截止日期</th>
            <th>状态</th>
            <th>志愿者</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="9" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="9" class="text-center text-muted py-4">暂无任务</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.title }}</td>
            <td>{{ typeText(item.type) }}</td>
            <td>{{ item.location || '-' }}</td>
            <td>{{ item.requiredSkills || '-' }}</td>
            <td>{{ item.deadline ? formatDate(item.deadline) : '-' }}</td>
            <td>
              <span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span>
              <small v-if="item.reviewRemark && item.status === 5" class="d-block text-muted">{{ item.reviewRemark }}</small>
            </td>
            <td>{{ item.volunteerName || '未认领' }}</td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
              <button v-if="item.status === 0 || item.status === 4" class="btn btn-sm btn-outline-primary me-1" @click="handleEdit(item)"><i class="fas fa-edit"></i></button>
              <button class="btn btn-sm btn-outline-danger" @click="handleDelete(item)"><i class="fas fa-trash"></i></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreateModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editingItem ? '编辑任务' : '发布任务' }}</h5>
            <button type="button" class="btn-close" @click="showCreateModal = false; editingItem = null"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-warning" v-if="!editingItem">
              <i class="fas fa-clock me-2"></i>提交后需要等待管理员审核，审核通过后任务才能被志愿者认领。
            </div>
            <div class="mb-3">
              <label class="form-label">任务标题 <span class="text-danger">*</span></label>
              <input v-model="form.title" class="form-control" placeholder="请输入任务标题">
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">任务类型</label>
                <select v-model="form.type" class="form-control">
                  <option value="">请选择</option>
                  <option value="INVESTIGATE">实地调查</option>
                  <option value="SEARCH">搜索寻人</option>
                  <option value="FOLLOW_UP">跟进回访</option>
                  <option value="ANALYZE">信息分析</option>
                  <option value="OTHER">其他</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">优先级</label>
                <select v-model.number="form.priority" class="form-control">
                  <option :value="1">低</option>
                  <option :value="2">中</option>
                  <option :value="3">高</option>
                </select>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">任务描述</label>
              <textarea v-model="form.description" class="form-control" rows="3" placeholder="详细描述任务内容"></textarea>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">任务地点</label>
                <input v-model="form.location" class="form-control" placeholder="如：北京市朝阳区">
              </div>
              <div class="col-md-6">
                <label class="form-label">所需技能</label>
                <input v-model="form.requiredSkills" class="form-control" placeholder="如：寻亲经验, 数据分析">
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">截止日期</label>
              <input v-model="form.deadline" type="datetime-local" class="form-control">
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateModal = false; editingItem = null">取消</button>
            <button class="btn btn-primary" @click="submitTask" :disabled="!form.title">{{ editingItem ? '保存修改' : '提交任务' }}</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDetailModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">任务详情 - {{ detailItem.title }}</h5>
            <button type="button" class="btn-close" @click="showDetailModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>类型：</strong>{{ typeText(detailItem.type) }}</p>
                <p><strong>地点：</strong>{{ detailItem.location || '-' }}</p>
                <p><strong>所需技能：</strong>{{ detailItem.requiredSkills || '-' }}</p>
                <p><strong>截止日期：</strong>{{ detailItem.deadline ? formatDate(detailItem.deadline) : '-' }}</p>
              </div>
              <div class="col-md-6">
                <p><strong>状态：</strong><span class="status-badge" :class="'status-' + statusClass(detailItem.status)">{{ statusText(detailItem.status) }}</span></p>
                <p><strong>志愿者：</strong>{{ detailItem.volunteerName || '未认领' }}</p>
                <p><strong>优先级：</strong><span class="badge" :class="priorityBadge(detailItem.priority)">{{ priorityText(detailItem.priority) }}</span></p>
                <p v-if="detailItem.reviewRemark"><strong>审核意见：</strong>{{ detailItem.reviewRemark }}</p>
              </div>
            </div>
            <p><strong>描述：</strong>{{ detailItem.description || '-' }}</p>
            <p v-if="detailItem.result"><strong>完成结果：</strong>{{ detailItem.result }}</p>
            <h6 class="mt-3">任务日志</h6>
            <div v-if="logs.length === 0" class="text-muted">暂无日志</div>
            <ul v-else class="list-unstyled">
              <li v-for="log in logs" :key="log.id" class="mb-2 pb-2 border-bottom">
                <small class="text-muted">{{ formatDate(log.createTime) }}</small>
                <p class="mb-0">{{ log.content }}</p>
              </li>
            </ul>
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
import { ref, reactive, onMounted } from 'vue'
import { taskApi } from '@/api'

const list = ref([])
const loading = ref(true)
const activeTab = ref('all')
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const detailItem = ref({})
const logs = ref([])
const editingItem = ref(null)
const searchType = ref('')
const searchTitle = ref('')
const form = reactive({ title: '', description: '', type: '', priority: 2, location: '', requiredSkills: '', deadline: '' })

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function priorityBadge(p) {
  if (p === 3 || p === '3') return 'bg-danger'
  if (p === 2 || p === '2') return 'bg-warning text-dark'
  if (p === 1 || p === '1') return 'bg-info'
  return 'bg-secondary'
}

function priorityText(p) {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[p] || '未知'
}

function typeText(t) {
  const map = { INVESTIGATE: '实地调查', SEARCH: '搜索寻人', FOLLOW_UP: '跟进回访', ANALYZE: '信息分析', OTHER: '其他' }
  return map[t] || t || '其他'
}

function statusClass(s) {
  const map = { 0: 'pending', 1: 'info', 2: 'success', 3: 'rejected', 4: 'warning', 5: 'rejected' }
  return map[s] || 'pending'
}

function statusText(s) {
  const map = { 0: '待认领', 1: '进行中', 2: '已完成', 3: '已取消', 4: '待审核', 5: '已拒绝' }
  return map[s] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (activeTab.value === 'pending') params.status = 4
    else if (activeTab.value === 'active') params.status = 0
    if (searchType.value) params.type = searchType.value
    if (searchTitle.value) params.title = searchTitle.value
    const res = await taskApi().getMyPublished(params)
    if (res.code === 200) list.value = res.data.records || []
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

async function submitTask() {
  if (!form.title) { alert('请输入任务标题'); return }
  const data = {
    title: form.title,
    description: form.description,
    type: form.type || undefined,
    priority: form.priority,
    location: form.location || undefined,
    requiredSkills: form.requiredSkills || undefined,
    deadline: form.deadline || undefined
  }
  try {
    if (editingItem.value) {
      const res = await taskApi().update(editingItem.value.id, data)
      if (res.code === 200) {
        showCreateModal.value = false; editingItem.value = null; resetForm()
        loadData(); alert('修改成功')
      } else alert(res.message || '修改失败')
    } else {
      const res = await taskApi().create(data)
      if (res.code === 200) {
        showCreateModal.value = false; resetForm()
        loadData(); alert(res.message || '任务已提交')
      } else alert(res.message || '发布失败')
    }
  } catch (e) { alert('操作失败') }
}

function resetForm() {
  form.title = ''; form.description = ''; form.type = ''; form.priority = 2; form.location = ''; form.requiredSkills = ''; form.deadline = ''
}

async function viewDetail(item) {
  try {
    const res = await taskApi().getDetail(item.id)
    if (res.code === 200) detailItem.value = res.data
  } catch (e) { detailItem.value = item }
  showDetailModal.value = true
  try {
    const res = await taskApi().getLogs(item.id)
    if (res.code === 200) logs.value = res.data || []
  } catch (e) { logs.value = [] }
}

function handleEdit(item) {
  editingItem.value = item
  form.title = item.title
  form.description = item.description || ''
  form.type = item.type || ''
  form.priority = item.priority || 2
  form.location = item.location || ''
  form.requiredSkills = item.requiredSkills || ''
  form.deadline = item.deadline ? item.deadline.substring(0, 16) : ''
  showCreateModal.value = true
}

async function handleDelete(item) {
  if (!confirm(`确定要删除任务「${item.title}」吗？此操作不可恢复。`)) return
  try {
    const res = await taskApi().delete(item.id)
    if (res.code === 200) { loadData(); alert('任务已删除') }
    else alert(res.message || '删除失败')
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>