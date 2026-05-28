<template>
  <div>
    <h2 class="mb-4">个人资料</h2>
    <div class="card">
      <div class="card-body">
        <div class="row">
          <div class="col-md-4 text-center mb-4">
            <div class="position-relative d-inline-block">
              <img :src="avatarUrl" class="rounded-circle" width="150" height="150" alt="头像" style="object-fit: cover; border: 3px solid #e9ecef;">
              <div class="mt-3">
                <input type="file" ref="avatarInputRef" accept="image/*" style="display:none" @change="uploadAvatar">
                <button class="btn btn-sm btn-outline-primary" @click="selectAvatar">
                  <i class="fas fa-camera"></i> 更换头像
                </button>
              </div>
            </div>
            <h4 class="mt-3">{{ profile.username }}</h4>
            <p class="text-muted">{{ authStore.roleName }}</p>
          </div>
          <div class="col-md-8">
            <h5>基本信息</h5>
            <hr>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold">用户名</label>
                <input type="text" class="form-control" :value="profile.username" disabled>
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold">角色</label>
                <input type="text" class="form-control" :value="authStore.roleName" disabled>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold">邮箱</label>
                <input v-model="profile.email" type="email" class="form-control">
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold">手机号</label>
                <input v-model="profile.phone" type="tel" class="form-control">
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-12">
                <label class="form-label fw-semibold">昵称</label>
                <input v-model="profile.nickname" type="text" class="form-control">
              </div>
            </div>
            <button class="btn btn-primary mt-3" @click="updateProfile" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="fas fa-save me-2"></i> 保存修改
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card mt-4">
      <div class="card-body">
        <h5>修改密码</h5>
        <hr>
        <div class="row">
          <div class="col-md-4 mb-3">
            <label class="form-label fw-semibold">当前密码</label>
            <input v-model="passwordForm.oldPassword" type="password" class="form-control">
          </div>
          <div class="col-md-4 mb-3">
            <label class="form-label fw-semibold">新密码</label>
            <input v-model="passwordForm.newPassword" type="password" class="form-control">
          </div>
          <div class="col-md-4 mb-3">
            <label class="form-label fw-semibold">确认新密码</label>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-control">
          </div>
        </div>
        <button class="btn btn-primary" @click="changePassword" :disabled="changingPassword">
          <span v-if="changingPassword" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="fas fa-key me-2"></i> 修改密码
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { userApi } from '@/api'

const authStore = useAuthStore()
const avatarInputRef = ref(null)

const profile = reactive({
  username: '',
  email: '',
  phone: '',
  nickname: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)
const changingPassword = ref(false)
const avatarVersion = ref(Date.now())

const avatarUrl = computed(() => {
  const user = authStore.currentUser
  if (user?.avatar) {
    return user.avatar.includes('?') ? `${user.avatar}&_t=${avatarVersion.value}` : `${user.avatar}?_t=${avatarVersion.value}`
  }
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.id || 'user'}`
})

function selectAvatar() {
  avatarInputRef.value?.click()
}

async function loadProfile() {
  try {
    const res = await userApi().getProfile()
    if (res.code === 200 && res.data) {
      profile.username = res.data.username || ''
      profile.email = res.data.email || ''
      profile.phone = res.data.phone || ''
      profile.nickname = res.data.nickname || ''
      authStore.setUser(res.data)
    }
  } catch (e) {
    const user = authStore.currentUser
    if (user) {
      profile.username = user.username || ''
      profile.email = user.email || ''
      profile.phone = user.phone || ''
      profile.nickname = user.nickname || ''
    }
  }
}

async function updateProfile() {
  saving.value = true
  try {
    const res = await userApi().updateProfile({
      email: profile.email,
      phone: profile.phone,
      nickname: profile.nickname
    })
    if (res.code === 200) {
      alert('保存成功！')
    } else {
      alert(res.message || '保存失败')
    }
  } catch (e) {
    alert('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('两次输入的新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    const res = await userApi().changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      alert('密码修改成功！')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      alert(res.message || '密码修改失败')
    }
  } catch (e) {
    alert('密码修改失败，请稍后重试')
  } finally {
    changingPassword.value = false
  }
}

async function uploadAvatar(e) {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await userApi().uploadAvatar(formData)
    if (res.code === 200) {
      if (res.data) {
        const updatedUser = { ...authStore.currentUser, avatar: res.data }
        authStore.setUser(updatedUser)
        avatarVersion.value = Date.now()
      }
      const profileRes = await userApi().getProfile()
      if (profileRes.code === 200 && profileRes.data) {
        profile.username = profileRes.data.username || ''
        profile.email = profileRes.data.email || ''
        profile.phone = profileRes.data.phone || ''
        profile.nickname = profileRes.data.nickname || ''
      }
    } else {
      alert(res.message || '上传失败')
    }
  } catch (e) {
    alert('上传失败')
  }
}

onMounted(() => {
  loadProfile()
})
</script>