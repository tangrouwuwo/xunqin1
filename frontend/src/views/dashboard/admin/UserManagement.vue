<template>
  <div>
    <h2 class="mb-4">用户管理</h2>
    <div class="card">
      <div class="card-body">
        <div class="d-flex justify-content-between mb-4">
          <h5>用户列表</h5>
          <button class="btn btn-primary" @click="showAddModal = true">
            <i class="fas fa-plus me-2"></i>添加用户
          </button>
        </div>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead class="table-light">
              <tr><th>ID</th><th>用户名</th><th>昵称</th><th>角色</th><th>邮箱</th><th>手机号</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="loading"><td colspan="8" class="text-center py-4"><div class="loader"></div></td></tr>
              <tr v-else-if="users.length === 0"><td colspan="8" class="text-center text-muted py-4">暂无用户数据</td></tr>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.nickname || '-' }}</td>
                <td><span class="role-badge" :class="'badge-' + getRoleClass(user.role)">{{ getRoleName(user.role) }}</span></td>
                <td>{{ user.email || '-' }}</td>
                <td>{{ user.phone || '-' }}</td>
                <td><span class="badge" :class="user.status === 1 ? 'bg-success' : 'bg-danger'">{{ user.status === 1 ? '正常' : '禁用' }}</span></td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-1" @click="editUser(user)"><i class="fas fa-edit"></i></button>
                  <button class="btn btn-sm btn-outline-warning me-1" @click="toggleStatus(user)" :disabled="user.role === 'ADMIN'">
                    <i class="fas" :class="user.status === 1 ? 'fa-ban' : 'fa-check'"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteUser(user)"><i class="fas fa-trash"></i></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <nav v-if="totalPages > 1">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage <= 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一页</a>
            </li>
            <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: p === currentPage }">
              <a class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">下一页</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- 添加用户模态框 -->
    <div v-if="showAddModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">添加用户</h5>
            <button type="button" class="btn-close" @click="showAddModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">用户名 <span class="text-danger">*</span></label>
              <input v-model="addForm.username" class="form-control" required>
            </div>
            <div class="mb-3">
              <label class="form-label">手机号</label>
              <input v-model="addForm.phone" class="form-control" maxlength="11">
              <small class="text-muted">手机号不能与已有用户重复</small>
            </div>
            <div class="mb-3">
              <label class="form-label">密码 <span class="text-danger">*</span></label>
              <input v-model="addForm.password" type="password" class="form-control" required>
            </div>
            <div class="mb-3">
              <label class="form-label">角色</label>
              <select v-model="addForm.role" class="form-control">
                <option value="SEEKER">寻亲者</option>
                <option value="VOLUNTEER">志愿者</option>
                <option value="CLUE_PROVIDER">线索提供者</option>
                <option value="ADMIN">管理员</option>
              </select>
            </div>
            <div v-if="addError" class="alert alert-danger">{{ addError }}</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
            <button class="btn btn-primary" @click="createUser" :disabled="addLoading">
              <span v-if="addLoading" class="spinner-border spinner-border-sm me-2"></span>
              保存
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="showEditModal" class="modal d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑用户 - {{ editForm.username }}</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">用户名</label>
              <input :value="editForm.username" class="form-control" disabled>
              <small class="text-muted">用户名不可修改</small>
            </div>
            <div class="mb-3">
              <label class="form-label">手机号</label>
              <input :value="editForm.phone" class="form-control" disabled>
              <small class="text-muted">手机号不可修改</small>
            </div>
            <div class="mb-3">
              <label class="form-label">昵称</label>
              <input v-model="editForm.nickname" class="form-control" placeholder="请输入昵称">
            </div>
            <div class="mb-3">
              <label class="form-label">邮箱</label>
              <input v-model="editForm.email" type="email" class="form-control" placeholder="请输入邮箱">
            </div>
            <div v-if="editError" class="alert alert-danger">{{ editError }}</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
            <button class="btn btn-primary" @click="saveEditUser" :disabled="editLoading">
              <span v-if="editLoading" class="spinner-border spinner-border-sm me-2"></span>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api'
import { getRoleName, getRoleClass } from '@/utils'

const users = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)

// 添加用户
const showAddModal = ref(false)
const addLoading = ref(false)
const addError = ref('')
const addForm = reactive({ username: '', phone: '', password: '', role: 'SEEKER' })

// 编辑用户
const showEditModal = ref(false)
const editLoading = ref(false)
const editError = ref('')
const editForm = reactive({ id: null, username: '', phone: '', nickname: '', email: '' })

async function loadUsers(page = 1) {
  loading.value = true
  try {
    const res = await adminApi().listUsers({ pageNum: page, pageSize: 10 })
    if (res.code === 200) {
      users.value = res.data.records || []
      totalPages.value = res.data.pages || 1
      currentPage.value = res.data.current || 1
    }
  } catch (e) { console.error('加载用户失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  if (p < 1 || p > totalPages.value) return
  loadUsers(p)
}

async function createUser() {
  addError.value = ''
  addLoading.value = true
  try {
    const res = await adminApi().createUser(addForm)
    if (res.code === 200) {
      showAddModal.value = false
      addForm.username = ''; addForm.phone = ''; addForm.password = ''; addForm.role = 'SEEKER'
      loadUsers()
    } else {
      addError.value = res.message || '添加失败'
    }
  } catch (e) {
    addError.value = '添加失败，请检查网络连接'
  } finally {
    addLoading.value = false
  }
}

function editUser(user) {
  editForm.id = user.id
  editForm.username = user.username || ''
  editForm.phone = user.phone || ''
  editForm.nickname = user.nickname || ''
  editForm.email = user.email || ''
  editError.value = ''
  showEditModal.value = true
}

async function saveEditUser() {
  editError.value = ''
  editLoading.value = true
  try {
    const res = await adminApi().updateUser(editForm.id, {
      nickname: editForm.nickname,
      email: editForm.email
    })
    if (res.code === 200) {
      showEditModal.value = false
      loadUsers()
    } else {
      editError.value = res.message || '更新失败'
    }
  } catch (e) {
    editError.value = '更新失败，请检查网络连接'
  } finally {
    editLoading.value = false
  }
}

async function toggleStatus(user) {
  if (user.role === 'ADMIN') {
    alert('不能禁用管理员账号')
    return
  }
  const newStatus = user.status === 1 ? 0 : 1
  try {
    const res = await adminApi().toggleUserStatus(user.id, newStatus)
    if (res.code === 200) {
      user.status = newStatus
    } else {
      alert(res.message || '操作失败')
    }
  } catch (e) { alert('操作失败') }
}

async function deleteUser(user) {
  if (user.role === 'ADMIN') {
    alert('不能删除管理员账号')
    return
  }
  if (!confirm(`确定要删除用户「${user.username}」吗？`)) return
  try {
    const res = await adminApi().deleteUser(user.id)
    if (res.code === 200) {
      users.value = users.value.filter(u => u.id !== user.id)
    }
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadUsers())
</script>