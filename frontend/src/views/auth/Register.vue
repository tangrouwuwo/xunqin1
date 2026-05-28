<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="text-center mb-4 fw-bold"><i class="fas fa-user-plus me-2"></i>注册</h1>
      <form @submit.prevent="handleRegister" autocomplete="off">
        <div style="position: absolute; left: -9999px" aria-hidden="true">
          <input type="text" name="fake_username" tabindex="-1" autocomplete="off">
          <input type="email" name="fake_email" tabindex="-1" autocomplete="off">
          <input type="password" name="fake_password" tabindex="-1" autocomplete="off">
        </div>
        <div class="mb-3">
          <label class="fw-semibold mb-2">用户名</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-user text-muted"></i></span>
            <input v-model="form.username" type="text" class="form-control" placeholder="请输入用户名" required autocomplete="off" :name="'reg_username_' + fieldSuffix" :id="'reg_username_' + fieldSuffix" :readonly="isUsernameReadonly" @focus="onUsernameFocus" @click="onUsernameFocus">
          </div>
        </div>
        <div class="mb-3">
          <label class="fw-semibold mb-2">手机号</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-phone text-muted"></i></span>
            <input v-model="form.phone" type="tel" class="form-control" placeholder="请输入11位手机号" required maxlength="11" autocomplete="off" :name="'reg_phone_' + fieldSuffix" :id="'reg_phone_' + fieldSuffix" :readonly="isPhoneReadonly" @focus="onPhoneFocus" @click="onPhoneFocus">
          </div>
        </div>
        <div class="mb-3">
          <label class="fw-semibold mb-2">密码</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-lock text-muted"></i></span>
            <input v-model="form.password" type="text" class="form-control" placeholder="请输入密码" required autocomplete="new-password" :name="'reg_password_' + fieldSuffix" :id="'reg_password_' + fieldSuffix" :readonly="isPasswordReadonly" @focus="onPasswordFocus" @click="onPasswordFocus">
          </div>
        </div>
        <div class="mb-3">
          <label class="fw-semibold mb-2">角色</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-user-tag text-muted"></i></span>
            <select v-model="form.role" class="form-control" required>
              <option value="SEEKER">寻亲者</option>
              <option value="VOLUNTEER">志愿者</option>
              <option value="CLUE_PROVIDER">线索提供者</option>
            </select>
          </div>
        </div>
        <button type="submit" class="btn btn-primary w-100 py-3 fw-semibold mt-2" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="fas fa-user-plus me-2"></i> 注册
        </button>
      </form>
      <div v-if="error" class="alert alert-danger mt-3 rounded-3">{{ error }}</div>
      <div v-if="success" class="alert alert-success mt-3 rounded-3">{{ success }}</div>
      <div class="text-center mt-3">
        <span class="text-muted">已有账号？</span>
        <router-link to="/login" class="text-primary fw-semibold">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const defaultRole = route.query.role === 'VOLUNTEER' ? 'VOLUNTEER' : 'SEEKER'
const form = reactive({ username: '', phone: '', password: '', role: defaultRole })
const error = ref('')
const success = ref('')
const loading = ref(false)
const fieldSuffix = ref('')

const isUsernameReadonly = ref(true)
const isPhoneReadonly = ref(true)
const isPasswordReadonly = ref(true)

function onUsernameFocus() {
  if (isUsernameReadonly.value) {
    isUsernameReadonly.value = false
    form.username = ''
  }
}

function onPhoneFocus() {
  if (isPhoneReadonly.value) {
    isPhoneReadonly.value = false
    form.phone = ''
  }
}

function onPasswordFocus() {
  if (isPasswordReadonly.value) {
    isPasswordReadonly.value = false
    form.password = ''
    setTimeout(() => {
      const input = document.getElementById('reg_password_' + fieldSuffix.value)
      if (input) input.type = 'password'
    }, 100)
  }
}

function resetForm() {
  form.username = ''
  form.phone = ''
  form.password = ''
  fieldSuffix.value = Date.now().toString(36) + Math.random().toString(36).substring(2, 6)
  isUsernameReadonly.value = true
  isPhoneReadonly.value = true
  isPasswordReadonly.value = true

  const passInput = document.getElementById('reg_password_' + fieldSuffix.value)
  if (passInput) {
    passInput.type = 'text'
    passInput.value = ''
  }

  const inputs = document.querySelectorAll('.login-card input')
  inputs.forEach(input => { input.value = '' })
}

onMounted(() => {
  resetForm()
  setTimeout(() => {
    const inputs = document.querySelectorAll('.login-card input')
    inputs.forEach(input => { input.value = '' })
    form.username = ''; form.phone = ''; form.password = ''
  }, 100)
  setTimeout(() => {
    form.username = ''; form.phone = ''; form.password = ''
  }, 300)
})

async function handleRegister() {
  error.value = ''
  success.value = ''
  loading.value = true

  if (form.phone.length !== 11) {
    error.value = '手机号长度必须为11位'
    loading.value = false
    return
  }

  try {
    const res = await authStore.register({ ...form })
    if (res.code === 200) {
      success.value = '注册成功！正在跳转到登录页面...'
      setTimeout(() => router.push('/login'), 1500)
    } else {
      error.value = res.message || '注册失败'
    }
  } catch (e) {
    error.value = '注册请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>