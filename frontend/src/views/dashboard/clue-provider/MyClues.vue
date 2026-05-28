<template>
  <div>
    <h2 class="mb-4">我的线索</h2>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr><th>ID</th><th>关联寻亲</th><th>线索内容</th><th>匿名</th><th>状态</th><th>审核意见</th><th>提交时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="8" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="8" class="text-center text-muted py-4">暂无提交线索</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.missingPersonName || '-' }}</td>
            <td>{{ (item.content || '').substring(0, 50) }}{{ (item.content || '').length > 50 ? '...' : '' }}</td>
            <td><span v-if="item.isAnonymous == 1" class="text-muted"><i class="fas fa-user-secret"></i> 匿名</span><span v-else class="text-success"><i class="fas fa-user"></i> 实名</span></td>
            <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td>{{ item.handleRemark || '-' }}</td>
            <td>{{ formatTime(item.createTime) }}</td>
            <td>
              <button class="btn btn-sm btn-outline-primary me-1" @click="editClue(item)"><i class="fas fa-edit"></i></button>
              <button class="btn btn-sm btn-outline-danger" @click="deleteClue(item)"><i class="fas fa-trash"></i></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showEditModal" class="modal fade show d-block" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑线索</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">线索内容 <span class="text-danger">*</span></label>
              <textarea v-model="editForm.content" class="form-control" rows="4"></textarea>
            </div>
            <div class="mb-3">
              <div class="form-check">
                <input type="checkbox" class="form-check-input" id="editAnonymous" v-model="editForm.isAnonymous">
                <label class="form-check-label" for="editAnonymous">匿名提交</label>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">联系人姓名</label>
              <input v-model="editForm.contactName" class="form-control">
            </div>
            <div class="mb-3">
              <label class="form-label">联系电话</label>
              <input v-model="editForm.contactPhone" class="form-control">
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
            <button class="btn btn-primary" @click="saveEdit" :disabled="saving">
              <i class="fas fa-save me-1"></i>{{ saving ? '保存中...' : '保存' }}
            </button>
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
const showEditModal = ref(false)
const saving = ref(false)
const editForm = ref({ id: null, content: '', isAnonymous: false, contactName: '', contactPhone: '' })

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

function editClue(item) {
  editForm.value = {
    id: item.id,
    content: item.content || '',
    isAnonymous: item.isAnonymous == 1,
    contactName: item.contactName || '',
    contactPhone: item.contactPhone || ''
  }
  showEditModal.value = true
}

async function saveEdit() {
  if (!editForm.value.content.trim()) { alert('请输入线索内容'); return }
  saving.value = true
  try {
    const res = await clueApi().update(editForm.value.id, {
      missingPersonId: 0,
      content: editForm.value.content,
      isAnonymous: editForm.value.isAnonymous ? 1 : 0,
      contactName: editForm.value.contactName,
      contactPhone: editForm.value.contactPhone,
      contactEmail: ''
    })
    if (res.code === 200) {
      alert('修改成功')
      showEditModal.value = false
      loadData()
    } else {
      alert(res.message || '修改失败')
    }
  } catch (e) { alert('修改失败') }
  finally { saving.value = false }
}

async function deleteClue(item) {
  if (!confirm('确定要删除这条线索吗？')) return
  try {
    const res = await clueApi().delete(item.id)
    if (res.code === 200) {
      alert('删除成功')
      loadData()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) { alert('删除失败') }
}

async function loadData() {
  loading.value = true
  try {
    const res = await clueApi().getMyClues()
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

onMounted(() => loadData())
</script>