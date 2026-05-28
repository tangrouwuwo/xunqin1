<template>
  <div>
    <h2 class="mb-4">线索管理</h2>
    <p class="text-muted">查看所有审核通过的线索并进行处理。</p>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr><th>ID</th><th>关联寻亲</th><th>线索内容</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="5" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="5" class="text-center text-muted py-4">暂无线索</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.missingPersonName || '-' }}</td>
            <td>{{ (item.content || '').substring(0, 50) }}{{ (item.content || '').length > 50 ? '...' : '' }}</td>
            <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewClue(item)"><i class="fas fa-eye"></i></button>
              <button v-if="item.status === 1" class="btn btn-sm btn-outline-success me-1" @click="markResolved(item)"><i class="fas fa-check"></i> 标记解决</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { clueApi } from '@/api'

const list = ref([])
const loading = ref(true)

function statusClass(s) {
  const map = { 0: 'pending', 1: 'success', 2: 'rejected' }
  return map[s] || 'pending'
}
function statusText(s) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[s] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await clueApi().listAll({ status: 1 })
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function viewClue(item) { alert(`线索详情\n关联寻亲人: ${item.missingPersonName || '-'}\n内容: ${item.content || '-'}\n状态: ${statusText(item.status)}`) }

async function markResolved(item) {
  if (!confirm('确定将此线索标记为已解决吗？')) return
  try {
    const res = await clueApi().handle(item.id, { status: 3, handleResult: '已核实解决', handleRemark: '志愿者确认线索有效' })
    if (res.code === 200) { loadData() }
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

onMounted(() => loadData())
</script>