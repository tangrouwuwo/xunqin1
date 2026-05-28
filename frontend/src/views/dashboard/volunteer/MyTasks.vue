<template>
  <div>
    <h2 class="mb-4">我的任务</h2>
    <p class="text-muted">查看您已认领的任务，更新进度或提交完成结果。</p>

    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>任务标题</th>
            <th>发布者</th>
            <th>类型</th>
            <th>地点</th>
            <th>截止日期</th>
            <th>优先级</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="9" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="9" class="text-center text-muted py-4">暂无任务</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.title }}</td>
            <td>{{ item.publisherName }}</td>
            <td>{{ typeText(item.type) }}</td>
            <td>{{ item.location || '-' }}</td>
            <td>{{ item.deadline ? formatDate(item.deadline) : '-' }}</td>
            <td><span class="badge" :class="priorityBadge(item.priority)">{{ priorityText(item.priority) }}</span></td>
            <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
              <button v-if="item.status === 1" class="btn btn-sm btn-outline-primary me-1" @click="showUpdateProgress(item)"><i class="fas fa-tasks"></i> 进度</button>
              <button v-if="item.status === 1" class="btn btn-sm btn-outline-success" @click="showComplete(item)"><i class="fas fa-check"></i> 完成</button>
            </td>
          </tr>
        </tbody>
      </table>
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
                <p><strong>发布者：</strong>{{ detailItem.publisherName }} ({{ detailItem.publisherRole }})</p>
                <p><strong>类型：</strong>{{ typeText(detailItem.type) }}</p>
                <p><strong>地点：</strong>{{ detailItem.location || '-' }}</p>
                <p><strong>所需技能：</strong>{{ detailItem.requiredSkills || '-' }}</p>
              </div>
              <div class="col-md-6">
                <p><strong>优先级：</strong><span class="badge" :class="priorityBadge(detailItem.priority)">{{ priorityText(detailItem.priority) }}</span></p>
                <p><strong>截止日期：</strong>{{ detailItem.deadline ? formatDate(detailItem.deadline) : '-' }}</p>
                <p><strong>状态：</strong><span class="status-badge" :class="'status-' + statusClass(detailItem.status)">{{ statusText(detailItem.status) }}</span></p>
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

    <div v-if="showProgressModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">更新进度 - {{ progressTask.title }}</h5>
            <button type="button" class="btn-close" @click="showProgressModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">进度说明</label>
              <textarea v-model="progressContent" class="form-control" rows="4" placeholder="描述当前完成的工作..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showProgressModal = false">取消</button>
            <button class="btn btn-primary" @click="submitProgress" :disabled="!progressContent">提交进度</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCompleteModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">完成任务 - {{ completeTask.title }}</h5>
            <button type="button" class="btn-close" @click="showCompleteModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">完成结果 <span class="text-danger">*</span></label>
              <textarea v-model="completeResult" class="form-control" rows="5" placeholder="请详细描述任务完成的情况、找到的线索、接触的人员等信息..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCompleteModal = false">取消</button>
            <button class="btn btn-success" @click="submitComplete" :disabled="!completeResult">提交完成结果</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { taskApi } from '@/api'

const list = ref([])
const loading = ref(true)
const showDetailModal = ref(false)
const showProgressModal = ref(false)
const showCompleteModal = ref(false)
const detailItem = ref({})
const logs = ref([])
const progressTask = ref({})
const progressContent = ref('')
const completeTask = ref({})
const completeResult = ref('')

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
    const res = await taskApi().getMyTasks()
    if (res.code === 200) list.value = res.data.records || []
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
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

function showUpdateProgress(item) {
  progressTask.value = item
  progressContent.value = ''
  showProgressModal.value = true
}

async function submitProgress() {
  try {
    const res = await taskApi().updateProgress(progressTask.value.id, {
      logContent: progressContent.value
    })
    if (res.code === 200) {
      showProgressModal.value = false
      alert('进度更新成功！')
    } else alert(res.message || '更新失败')
  } catch (e) { alert('更新失败') }
}

function showComplete(item) {
  completeTask.value = item
  completeResult.value = ''
  showCompleteModal.value = true
}

async function submitComplete() {
  try {
    const res = await taskApi().complete(completeTask.value.id, {
      result: completeResult.value
    })
    if (res.code === 200) {
      showCompleteModal.value = false
      loadData()
      alert('任务完成结果已提交！发布者将收到您的完成结果。')
    } else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

onMounted(() => loadData())
</script>