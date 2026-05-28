<template>
  <div>
    <h2 class="mb-4">提交线索</h2>
    <div class="card">
      <div class="card-body">
        <div class="mb-3">
          <label class="form-label">关联寻亲信息 <span class="text-danger">*</span></label>
          <select v-model="form.missingPersonId" class="form-control">
            <option value="">请选择</option>
            <option v-for="p in missingPersons" :key="p.id" :value="p.id">{{ p.name }} (ID: {{ p.id }})</option>
          </select>
          <small class="text-muted">选择您要提供线索的寻亲人</small>
        </div>
        <div class="mb-3">
          <label class="form-label">线索类型</label>
          <select v-model="form.type" class="form-control">
            <option value="">请选择线索类型</option>
            <option value="SIGHTING">目击线索</option>
            <option value="INFORMATION">信息线索</option>
            <option value="DOCUMENT">文档线索</option>
            <option value="OTHER">其他</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label">线索描述 <span class="text-danger">*</span></label>
          <textarea v-model="form.content" class="form-control" rows="5" placeholder="请详细描述您掌握的线索信息，包括时间、地点、人物等关键信息..."></textarea>
        </div>
        <div class="mb-3">
          <div class="form-check">
            <input type="checkbox" class="form-check-input" id="anonymousCheck" v-model="form.isAnonymous">
            <label class="form-check-label" for="anonymousCheck">
              <i class="fas fa-user-secret me-1"></i>匿名提交
            </label>
            <small class="text-muted d-block">开启后，您的个人信息将对寻亲者保密</small>
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label">补充信息</label>
          <textarea v-model="form.additionalInfo" class="form-control" rows="3" placeholder="其他补充信息..."></textarea>
        </div>
        <div class="mb-3">
          <label class="form-label">线索来源</label>
          <input v-model="form.source" class="form-control" placeholder="如：亲眼所见、网络信息、他人提供等">
        </div>
        <div class="alert alert-info">
          <i class="fas fa-info-circle me-2"></i>线索提交后将进入审核流程，管理员审核通过后才会通知对应的寻亲者。
        </div>
        <div class="text-center">
          <button class="btn btn-primary px-4" @click="submitClue" :disabled="submitting">
            <i class="fas fa-paper-plane me-2"></i>{{ submitting ? '提交中...' : '提交线索' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { clueApi, missingPersonApi } from '@/api'

const missingPersons = ref([])
const submitting = ref(false)
const form = reactive({ missingPersonId: '', type: '', content: '', isAnonymous: false, additionalInfo: '', source: '' })

async function loadMissingPersons() {
  try {
    const res = await missingPersonApi().list({ status: 1 })
    if (res.code === 200) missingPersons.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载寻亲信息失败:', e) }
}

async function submitClue() {
  if (!form.missingPersonId) { alert('请选择关联的寻亲信息'); return }
  if (!form.content.trim()) { alert('请输入线索描述'); return }
  submitting.value = true
  try {
    const payload = {
      missingPersonId: form.missingPersonId,
      content: form.content,
      isAnonymous: form.isAnonymous ? 1 : 0
    }
    const res = await clueApi().create(payload)
    if (res.code === 200) {
      alert('线索提交成功！请等待管理员审核。')
      form.missingPersonId = ''; form.type = ''; form.content = ''; form.isAnonymous = false; form.additionalInfo = ''; form.source = ''
    } else {
      alert(res.message || '提交失败')
    }
  } catch (e) { alert('提交失败') }
  finally { submitting.value = false }
}

onMounted(() => loadMissingPersons())
</script>