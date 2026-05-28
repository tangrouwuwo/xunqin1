<template>
  <div>
    <h2 class="mb-4">发布寻亲信息</h2>
    <div class="card">
      <div class="card-body">
        <div class="row">
          <div class="col-12 mb-3">
            <label class="form-label">标题 <span class="text-danger">*</span></label>
            <input v-model="form.title" class="form-control" placeholder="例如：寻找1998年失踪的女儿" required>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">姓名 <span class="text-danger">*</span></label>
            <input v-model="form.name" class="form-control" required>
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label">性别</label>
            <select v-model="form.gender" class="form-control">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label">失踪年龄 <span class="text-danger">*</span></label>
            <input v-model="form.ageAtMissing" type="number" class="form-control" required>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">失踪日期 <span class="text-danger">*</span></label>
            <input v-model="form.missingDate" type="date" class="form-control" required>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">失踪地点 <span class="text-danger">*</span></label>
            <div class="row g-2">
              <div class="col-md-5 mb-2">
                <select v-model="form.province" class="form-control" @change="form.city = ''">
                  <option value="">请选择省/直辖市</option>
                  <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
                </select>
              </div>
              <div class="col-md-4 mb-2">
                <select v-model="form.city" class="form-control">
                  <option value="">请选择市/区</option>
                  <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
                </select>
              </div>
              <div class="col-md-3 mb-2">
                <input v-model="form.detailLocation" class="form-control" placeholder="详细地址">
              </div>
            </div>
          </div>
          <div class="col-md-4 mb-3">
            <label class="form-label">身高(cm)</label>
            <input v-model="form.height" type="number" class="form-control">
          </div>
          <div class="col-md-4 mb-3">
            <label class="form-label">体重(kg)</label>
            <input v-model="form.weight" type="number" class="form-control">
          </div>
          <div class="col-md-4 mb-3">
            <label class="form-label">血型</label>
            <input v-model="form.bloodType" class="form-control">
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">体貌特征</label>
            <textarea v-model="form.appearance" class="form-control" rows="2" placeholder="五官特征、脸型、肤色等"></textarea>
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">衣着描述</label>
            <textarea v-model="form.clothing" class="form-control" rows="2" placeholder="失踪时穿着"></textarea>
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">特殊特征</label>
            <textarea v-model="form.specialFeatures" class="form-control" rows="2" placeholder="胎记、疤痕、纹身等"></textarea>
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">失踪原因</label>
            <textarea v-model="form.missingCause" class="form-control" rows="2" placeholder="失踪的可能原因"></textarea>
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">详细描述</label>
            <textarea v-model="form.description" class="form-control" rows="3" placeholder="更多关于失踪的详细信息"></textarea>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">联系人</label>
            <input v-model="form.contactName" class="form-control">
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">联系电话</label>
            <input v-model="form.contactPhone" class="form-control" maxlength="11">
          </div>
          <div class="col-12 mb-3">
            <label class="form-label">上传照片</label>
            <input ref="fileInput" type="file" accept="image/*" multiple class="form-control" @change="handleFileChange">
            <div v-if="previewImages.length > 0" class="mt-2 d-flex flex-wrap gap-2">
              <div v-for="(img, idx) in previewImages" :key="idx" class="position-relative" style="width: 100px; height: 100px;">
                <img :src="img" class="rounded border" style="width: 100%; height: 100%; object-fit: cover;">
                <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 rounded-circle" style="width: 22px; height: 22px; padding: 0; font-size: 12px; line-height: 22px;" @click="removeImage(idx)">&times;</button>
              </div>
            </div>
          </div>
        </div>
        <div class="text-center mt-3">
          <button class="btn btn-primary px-4" @click="submitForm" :disabled="submitting">
            <i class="fas fa-paper-plane me-2"></i>{{ submitting ? '提交中...' : '发布寻亲信息' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { missingPersonApi } from '@/api'
import { provinces, citiesByProvince, buildLocation } from '@/utils/chinaRegions'

const router = useRouter()
const submitting = ref(false)
const fileInput = ref(null)
const selectedFiles = ref([])
const previewImages = ref([])

const form = reactive({
  title: '', name: '', gender: '', ageAtMissing: '', missingDate: '',
  province: '', city: '', detailLocation: '', missingLocation: '',
  height: '', weight: '', bloodType: '', appearance: '', clothing: '',
  specialFeatures: '', missingCause: '', description: '', contactName: '', contactPhone: ''
})

const cities = computed(() => {
  return citiesByProvince[form.province] || []
})

function handleFileChange(e) {
  const files = Array.from(e.target.files || [])
  selectedFiles.value = files
  previewImages.value = []
  files.forEach(file => {
    const reader = new FileReader()
    reader.onload = (ev) => { previewImages.value.push(ev.target.result) }
    reader.readAsDataURL(file)
  })
}

function removeImage(idx) {
  selectedFiles.value.splice(idx, 1)
  previewImages.value.splice(idx, 1)
  if (fileInput.value) fileInput.value.value = ''
}

async function submitForm() {
  if (!form.title) { alert('请输入标题'); return }
  if (!form.name) { alert('请输入姓名'); return }
  if (!form.ageAtMissing) { alert('请输入失踪年龄'); return }
  if (!form.missingDate) { alert('请选择失踪日期'); return }
  if (!form.province) { alert('请选择失踪省份'); return }
  if (!form.city) { alert('请选择失踪市/区'); return }
  form.missingLocation = buildLocation(form.province, form.city, form.detailLocation)
  submitting.value = true
  try {
    if (selectedFiles.value.length > 0) {
      const fd = new FormData()
      fd.append('title', form.title)
      fd.append('name', form.name)
      if (form.gender) fd.append('gender', form.gender)
      fd.append('ageAtMissing', form.ageAtMissing)
      fd.append('missingDate', form.missingDate)
      fd.append('missingLocation', form.missingLocation)
      if (form.height) fd.append('height', form.height)
      if (form.weight) fd.append('weight', form.weight)
      if (form.bloodType) fd.append('bloodType', form.bloodType)
      if (form.appearance) fd.append('appearance', form.appearance)
      if (form.clothing) fd.append('clothing', form.clothing)
      if (form.specialFeatures) fd.append('specialFeatures', form.specialFeatures)
      if (form.missingCause) fd.append('missingCause', form.missingCause)
      if (form.description) fd.append('description', form.description)
      if (form.contactName) fd.append('contactName', form.contactName)
      if (form.contactPhone) fd.append('contactPhone', form.contactPhone)
      selectedFiles.value.forEach(f => fd.append('photos', f))
      const res = await missingPersonApi().createWithPhotos(fd)
      if (res.code === 200) {
        alert('发布成功，请等待审核')
        router.push('/dashboard/my-missing-persons')
      } else {
        alert(res.message || '发布失败')
      }
    } else {
      const res = await missingPersonApi().create({ ...form })
      if (res.code === 200) {
        alert('发布成功，请等待审核')
        router.push('/dashboard/my-missing-persons')
      } else {
        alert(res.message || '发布失败')
      }
    }
  } catch (e) { alert('发布失败') }
  finally { submitting.value = false }
}
</script>