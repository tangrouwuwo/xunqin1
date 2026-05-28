<template>
  <div>
    <h2 class="mb-4">任务大厅</h2>
    <p class="text-muted">浏览所有待认领的任务，选择适合您的任务进行认领。</p>

    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>任务标题</th>
            <th>描述</th>
            <th>类型</th>
            <th>发布者</th>
            <th>地点</th>
            <th>所需技能</th>
            <th>截止日期</th>
            <th>优先级</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="10" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="10" class="text-center text-muted py-4">暂无待认领的任务</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.title }}</td>
            <td>{{ (item.description || '').substring(0, 30) }}{{ (item.description || '').length > 30 ? '...' : '' }}</td>
            <td>{{ typeText(item.type) }}</td>
            <td>{{ item.publisherName }}</td>
            <td>{{ item.location || '-' }}</td>
            <td>{{ item.requiredSkills || '-' }}</td>
            <td>{{ item.deadline ? formatDate(item.deadline) : '-' }}</td>
            <td><span class="badge" :class="priorityBadge(item.priority)">{{ priorityText(item.priority) }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
              <button class="btn btn-sm btn-outline-success" @click="claimTask(item)"><i class="fas fa-hand-paper me-1"></i>认领</button>
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
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
            <button class="btn btn-success" @click="claimTask(detailItem)"><i class="fas fa-hand-paper me-1"></i>认领任务</button>
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
const detailItem = ref({})

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
    const res = await taskApi().getAvailable()
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
}

async function claimTask(item) {
  if (!confirm(`确定要认领任务「${item.title}」吗？`)) return
  try {
    const res = await taskApi().claim(item.id)
    if (res.code === 200) {
      alert('认领成功！任务已添加到您的任务列表。')
      loadData()
      showDetailModal.value = false
    } else alert(res.message || '认领失败')
  } catch (e) { alert('认领失败') }
}

onMounted(() => loadData())
</script>