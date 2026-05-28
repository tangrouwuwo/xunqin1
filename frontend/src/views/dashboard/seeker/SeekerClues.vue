<template>
  <div>
    <h2 class="mb-4">线索消息</h2>
    <p class="text-muted">这里显示与您发布的寻亲信息相关的线索（审核通过后可见）。</p>

    <div class="mb-3">
      <select v-model="statusFilter" class="form-select w-auto" @change="loadData">
        <option value="">全部状态</option>
        <option value="1">已通过</option>
      </select>
    </div>

    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr><th>关联寻亲人</th><th>线索内容</th><th>是否匿名</th><th>提交时间</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="6" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="6" class="text-center text-muted py-4">暂无相关线索</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.missingPersonName || '-' }}</td>
            <td>{{ (item.content || '').substring(0, 80) }}{{ (item.content || '').length > 80 ? '...' : '' }}</td>
            <td><span v-if="item.isAnonymous == 1" class="text-muted"><i class="fas fa-user-secret"></i> 匿名</span><span v-else><i class="fas fa-user"></i> {{ item.contactName || '实名' }}</span></td>
            <td>{{ formatTime(item.createTime) }}</td>
            <td><span class="status-badge" :class="'status-' + (item.status === 1 ? 'success' : 'rejected')">{{ item.status === 1 ? '已通过' : '已驳回' }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info" @click="viewDetail(item)"><i class="fas fa-eye"></i> 查看</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDetailModal" class="modal fade show d-block" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">线索详情</h5>
            <button type="button" class="btn-close" @click="showDetailModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="row mb-3">
              <div class="col-md-6">
                <strong>关联寻亲人：</strong>{{ detail.missingPersonName || '-' }}
              </div>
              <div class="col-md-6">
                <strong>提交方式：</strong>
                <span v-if="detail.isAnonymous == 1" class="text-muted"><i class="fas fa-user-secret"></i> 匿名提交</span>
                <span v-else>实名提交</span>
              </div>
            </div>
            <div class="mb-3" v-if="detail.isAnonymous != 1">
              <div class="row">
                <div class="col-md-4"><strong>联系人：</strong>{{ detail.contactName || '-' }}</div>
                <div class="col-md-4"><strong>电话：</strong>{{ detail.contactPhone || '-' }}</div>
                <div class="col-md-4"><strong>邮箱：</strong>{{ detail.contactEmail || '-' }}</div>
              </div>
            </div>
            <div class="mb-3">
              <strong>线索内容：</strong>
              <p class="mt-2 p-3 bg-light rounded">{{ detail.content || '无内容' }}</p>
            </div>
            <div class="mb-3" v-if="detail.handleRemark">
              <strong>审核意见：</strong>
              <p class="mt-2 p-3 bg-light rounded text-muted">{{ detail.handleRemark }}</p>
            </div>
            <div class="row text-muted small">
              <div class="col-md-6">提交时间：{{ formatTime(detail.createTime) }}</div>
              <div class="col-md-6">审核时间：{{ formatTime(detail.handleTime) }}</div>
            </div>
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
import { ref, onMounted } from 'vue'
import { clueApi } from '@/api'

const list = ref([])
const loading = ref(true)
const statusFilter = ref('')
const showDetailModal = ref(false)
const detail = ref({})

function formatTime(t) {
  if (!t) return '-'
  return t.substring(0, 16).replace('T', ' ')
}

function viewDetail(item) {
  detail.value = item
  showDetailModal.value = true
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    const res = await clueApi().getRelatedClues(params)
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

onMounted(() => loadData())
</script>