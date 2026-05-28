<template>
  <div>
    <h2 class="mb-4">我的寻亲信息</h2>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr><th>ID</th><th>标题</th><th>姓名</th><th>性别</th><th>失踪日期</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="7" class="text-center text-muted py-4">暂无寻亲信息</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td class="fw-bold">{{ item.title || item.name }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.gender || '-' }}</td>
            <td>{{ item.missingDate || '-' }}</td>
            <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
              <button class="btn btn-sm btn-outline-primary me-1" @click="editItem(item)"><i class="fas fa-edit"></i></button>
              <button class="btn btn-sm btn-outline-danger" @click="deleteItem(item)"><i class="fas fa-trash"></i></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDetail" class="modal d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">寻亲详情</h5>
            <button type="button" class="btn-close" @click="showDetail = false"></button>
          </div>
          <div class="modal-body">
            <h3 class="fw-bold mb-3">{{ detailItem.title || detailItem.name }}</h3>
            <div class="row">
              <div class="col-md-6"><strong>姓名：</strong>{{ detailItem.name }}</div>
              <div class="col-md-6"><strong>性别：</strong>{{ detailItem.gender || '-' }}</div>
              <div class="col-md-6"><strong>失踪年龄：</strong>{{ detailItem.ageAtMissing || '-' }}</div>
              <div class="col-md-6"><strong>身高：</strong>{{ detailItem.height || '-' }}cm</div>
              <div class="col-md-6"><strong>失踪日期：</strong>{{ detailItem.missingDate || '-' }}</div>
              <div class="col-md-6"><strong>失踪地点：</strong>{{ detailItem.missingLocation || '-' }}</div>
              <div class="col-12 mt-2"><strong>体貌特征：</strong>{{ detailItem.appearance || '-' }}</div>
              <div class="col-12"><strong>详细描述：</strong>{{ detailItem.description || '-' }}</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetail = false">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showEdit" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">修改寻亲信息</h5>
            <button type="button" class="btn-close" @click="showEdit = false"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-12 mb-3">
                <label class="form-label">标题 <span class="text-danger">*</span></label>
                <input v-model="editForm.title" class="form-control" required>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">姓名 <span class="text-danger">*</span></label>
                <input v-model="editForm.name" class="form-control" required>
              </div>
              <div class="col-md-3 mb-3">
                <label class="form-label">性别</label>
                <select v-model="editForm.gender" class="form-control">
                  <option value="">请选择</option>
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div class="col-md-3 mb-3">
                <label class="form-label">失踪年龄 <span class="text-danger">*</span></label>
                <input v-model="editForm.ageAtMissing" type="number" class="form-control" required>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">失踪日期 <span class="text-danger">*</span></label>
                <input v-model="editForm.missingDate" type="date" class="form-control" required>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">失踪地点 <span class="text-danger">*</span></label>
                <div class="row g-2">
                  <div class="col-md-5 mb-2">
                    <select v-model="editForm.province" class="form-control" @change="editForm.city = ''">
                      <option value="">请选择省/直辖市</option>
                      <option v-for="p in editProvinces" :key="p" :value="p">{{ p }}</option>
                    </select>
                  </div>
                  <div class="col-md-4 mb-2">
                    <select v-model="editForm.city" class="form-control">
                      <option value="">请选择市/区</option>
                      <option v-for="c in editCities" :key="c" :value="c">{{ c }}</option>
                    </select>
                  </div>
                  <div class="col-md-3 mb-2">
                    <input v-model="editForm.detailLocation" class="form-control" placeholder="详细地址">
                  </div>
                </div>
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">身高(cm)</label>
                <input v-model="editForm.height" type="number" class="form-control">
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">体重(kg)</label>
                <input v-model="editForm.weight" type="number" class="form-control">
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">血型</label>
                <input v-model="editForm.bloodType" class="form-control">
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">体貌特征</label>
                <textarea v-model="editForm.appearance" class="form-control" rows="2" placeholder="五官特征、脸型、肤色等"></textarea>
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">衣着描述</label>
                <textarea v-model="editForm.clothing" class="form-control" rows="2" placeholder="失踪时穿着"></textarea>
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">特殊特征</label>
                <textarea v-model="editForm.specialFeatures" class="form-control" rows="2" placeholder="胎记、疤痕、纹身等"></textarea>
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">失踪原因</label>
                <textarea v-model="editForm.missingCause" class="form-control" rows="2" placeholder="失踪的可能原因"></textarea>
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">详细描述</label>
                <textarea v-model="editForm.description" class="form-control" rows="3" placeholder="更多关于失踪的详细信息"></textarea>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">联系人</label>
                <input v-model="editForm.contactName" class="form-control">
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">联系电话</label>
                <input v-model="editForm.contactPhone" class="form-control" maxlength="11">
              </div>
              <div class="col-12 mb-3">
                <label class="form-label">上传照片</label>
                <input ref="editFileInput" type="file" accept="image/*" multiple class="form-control" @change="handleEditFileChange">
                <div v-if="editPreviewImages.length > 0" class="mt-2 d-flex flex-wrap gap-2">
                  <div v-for="(img, idx) in editPreviewImages" :key="idx" class="position-relative" style="width: 100px; height: 100px;">
                    <img :src="img" class="rounded border" style="width: 100%; height: 100%; object-fit: cover;">
                    <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 rounded-circle" style="width: 22px; height: 22px; padding: 0; font-size: 12px; line-height: 22px;" @click="removeEditImage(idx)">&times;</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showEdit = false">取消</button>
            <button class="btn btn-primary" @click="submitEdit" :disabled="submitting">
              <i class="fas fa-save me-2"></i>{{ submitting ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { missingPersonApi } from '@/api'
import { provinces as editProvinces, citiesByProvince as editCitiesByProvince, parseLocation, buildLocation } from '@/utils/chinaRegions'

const list = ref([])
const loading = ref(true)
const showDetail = ref(false)
const detailItem = ref({})
const showEdit = ref(false)
const submitting = ref(false)
const editFileInput = ref(null)
const editSelectedFiles = ref([])
const editPreviewImages = ref([])
const editForm = ref({
  title: '', name: '', gender: '', ageAtMissing: '', missingDate: '',
  province: '', city: '', detailLocation: '', missingLocation: '',
  height: '', weight: '', bloodType: '', appearance: '', clothing: '',
  specialFeatures: '', missingCause: '', description: '', contactName: '', contactPhone: ''
})
const editingId = ref(null)

const editCities = computed(() => {
  return editCitiesByProvince[editForm.value.province] || []
})

function statusClass(s) {
  if (s === 1) return 'approved'
  if (s === 2) return 'rejected'
  return 'pending'
}

function statusText(s) {
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return '审核中'
}

async function loadData() {
  loading.value = true
  try {
    const res = await missingPersonApi().getMyList()
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function viewDetail(item) {
  detailItem.value = item
  showDetail.value = true
}

function editItem(item) {
  editingId.value = item.id
  const parsed = parseLocation(item.missingLocation)
  editForm.value = {
    title: item.title || '',
    name: item.name || '',
    gender: item.gender || '',
    ageAtMissing: item.ageAtMissing || '',
    missingDate: item.missingDate || '',
    province: parsed.province,
    city: parsed.city,
    detailLocation: parsed.detail,
    missingLocation: item.missingLocation || '',
    height: item.height || '',
    weight: item.weight || '',
    bloodType: item.bloodType || '',
    appearance: item.appearance || '',
    clothing: item.clothing || '',
    specialFeatures: item.specialFeatures || '',
    missingCause: item.missingCause || '',
    description: item.description || '',
    contactName: item.contactName || '',
    contactPhone: item.contactPhone || ''
  }
  editSelectedFiles.value = []
  editPreviewImages.value = []
  if (editFileInput.value) editFileInput.value.value = ''
  showEdit.value = true
}

function handleEditFileChange(e) {
  const files = Array.from(e.target.files || [])
  editSelectedFiles.value = files
  editPreviewImages.value = []
  files.forEach(file => {
    const reader = new FileReader()
    reader.onload = (ev) => { editPreviewImages.value.push(ev.target.result) }
    reader.readAsDataURL(file)
  })
}

function removeEditImage(idx) {
  editSelectedFiles.value.splice(idx, 1)
  editPreviewImages.value.splice(idx, 1)
  if (editFileInput.value) editFileInput.value.value = ''
}

async function submitEdit() {
  if (!editForm.value.title) { alert('请输入标题'); return }
  if (!editForm.value.name) { alert('请输入姓名'); return }
  if (!editForm.value.ageAtMissing) { alert('请输入失踪年龄'); return }
  if (!editForm.value.missingDate) { alert('请选择失踪日期'); return }
  if (!editForm.value.province) { alert('请选择失踪省份'); return }
  if (!editForm.value.city) { alert('请选择失踪市/区'); return }
  editForm.value.missingLocation = buildLocation(editForm.value.province, editForm.value.city, editForm.value.detailLocation)
  submitting.value = true
  try {
    if (editSelectedFiles.value.length > 0) {
      const fd = new FormData()
      fd.append('title', editForm.value.title)
      fd.append('name', editForm.value.name)
      if (editForm.value.gender) fd.append('gender', editForm.value.gender)
      fd.append('ageAtMissing', editForm.value.ageAtMissing)
      fd.append('missingDate', editForm.value.missingDate)
      fd.append('missingLocation', editForm.value.missingLocation)
      if (editForm.value.height) fd.append('height', editForm.value.height)
      if (editForm.value.weight) fd.append('weight', editForm.value.weight)
      if (editForm.value.bloodType) fd.append('bloodType', editForm.value.bloodType)
      if (editForm.value.appearance) fd.append('appearance', editForm.value.appearance)
      if (editForm.value.clothing) fd.append('clothing', editForm.value.clothing)
      if (editForm.value.specialFeatures) fd.append('specialFeatures', editForm.value.specialFeatures)
      if (editForm.value.missingCause) fd.append('missingCause', editForm.value.missingCause)
      if (editForm.value.description) fd.append('description', editForm.value.description)
      if (editForm.value.contactName) fd.append('contactName', editForm.value.contactName)
      if (editForm.value.contactPhone) fd.append('contactPhone', editForm.value.contactPhone)
      editSelectedFiles.value.forEach(f => fd.append('photos', f))
      const res = await missingPersonApi().updateWithPhotos(editingId.value, fd)
      if (res.code === 200) {
        alert('修改成功，请等待审核')
        showEdit.value = false
        await loadData()
      } else {
        alert(res.message || '修改失败')
      }
    } else {
      const res = await missingPersonApi().update(editingId.value, { ...editForm.value })
      if (res.code === 200) {
        alert('修改成功，请等待审核')
        showEdit.value = false
        await loadData()
      } else {
        alert(res.message || '修改失败')
      }
    }
  } catch (e) { alert('修改失败') }
  finally { submitting.value = false }
}

async function deleteItem(item) {
  if (!confirm(`确定删除「${item.name}」的寻亲信息吗？`)) return
  try {
    const res = await missingPersonApi().delete(item.id)
    if (res.code === 200) list.value = list.value.filter(i => i.id !== item.id)
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>