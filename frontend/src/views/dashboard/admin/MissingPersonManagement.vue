<template>
  <div>
    <h2 class="mb-4">寻亲信息管理</h2>
    <div class="card">
      <div class="card-body">
        <div class="row mb-3">
          <div class="col-md-4">
            <select v-model="filterStatus" class="form-select" @change="loadData">
              <option value="">全部状态</option>
              <option value="0">待审核</option>
              <option value="1">已通过</option>
              <option value="2">已拒绝</option>
            </select>
          </div>
          <div class="col-md-4">
            <input v-model="filterName" class="form-control" placeholder="按姓名搜索" @keyup.enter="loadData">
          </div>
          <div class="col-md-4">
            <div class="input-group">
              <input v-model="filterUsername" class="form-control" placeholder="按发布者用户名搜索" @keyup.enter="loadData">
              <button class="btn btn-primary" @click="loadData"><i class="fas fa-search"></i></button>
            </div>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead class="table-light">
              <tr><th>ID</th><th>标题</th><th>姓名</th><th>性别</th><th>失踪年龄</th><th>失踪日期</th><th>发布者</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="loading"><td colspan="9" class="text-center py-4"><div class="loader"></div></td></tr>
              <tr v-else-if="list.length === 0"><td colspan="9" class="text-center text-muted py-4">暂无数据</td></tr>
              <tr v-for="item in list" :key="item.id">
                <td>{{ item.id }}</td>
                <td class="fw-bold">{{ item.title || item.name }}</td>
                <td>{{ item.name }}</td>
                <td>{{ item.gender || '-' }}</td>
                <td>{{ item.ageAtMissing || '-' }}</td>
                <td>{{ item.missingDate || '-' }}</td>
                <td>{{ item.seekerName || '-' }}</td>
                <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
                <td>
                  <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
                  <button v-if="item.status === 0" class="btn btn-sm btn-outline-success me-1" @click="approve(item)">通过</button>
                  <button v-if="item.status === 0" class="btn btn-sm btn-outline-danger me-1" @click="reject(item)">拒绝</button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteItem(item)"><i class="fas fa-trash"></i></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="totalPages > 1" class="d-flex justify-content-center mt-3">
          <nav>
            <ul class="pagination pagination-sm">
              <li class="page-item" :class="{ disabled: currentPage <= 1 }">
                <a class="page-link" @click.prevent="changePage(currentPage - 1)">上一页</a>
              </li>
              <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: p === currentPage }">
                <a class="page-link" @click.prevent="changePage(p)">{{ p }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
                <a class="page-link" @click.prevent="changePage(currentPage + 1)">下一页</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
    <div v-if="showDetail" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">寻亲详情</h5>
            <button type="button" class="btn-close" @click="showDetail = false"></button>
          </div>
          <div class="modal-body">
            <!-- 照片展示 -->
            <div v-if="photosList.length > 0" class="mb-4">
              <div class="row g-2">
                <div v-for="(photo, idx) in photosList" :key="idx" class="col-md-4">
                  <img :src="photo" class="img-fluid rounded" style="height: 200px; width: 100%; object-fit: cover;"
                       @error="e => e.target.style.display='none'">
                </div>
              </div>
            </div>

            <h4 class="fw-bold mb-3">{{ detailItem.title || detailItem.name }}</h4>

            <!-- 基本信息 -->
            <div class="row">
              <div class="col-md-6"><strong>姓名：</strong>{{ detailItem.name }}</div>
              <div class="col-md-6"><strong>性别：</strong>{{ detailItem.gender || '-' }}</div>
              <div class="col-md-6"><strong>发布者：</strong>{{ detailItem.seekerName || '-' }}</div>
              <div class="col-md-6"><strong>失踪年龄：</strong>{{ detailItem.ageAtMissing || '-' }}</div>
              <div class="col-md-6"><strong>身高：</strong>{{ detailItem.height || '-' }}cm</div>
              <div class="col-md-6"><strong>失踪日期：</strong>{{ detailItem.missingDate || '-' }}</div>
              <div class="col-md-6"><strong>失踪地点：</strong>{{ detailItem.missingLocation || '-' }}</div>
              <div class="col-12 mt-2"><strong>体貌特征：</strong>{{ detailItem.appearance || '-' }}</div>
              <div class="col-12"><strong>衣着描述：</strong>{{ detailItem.clothing || '-' }}</div>
              <div class="col-12"><strong>特殊特征：</strong>{{ detailItem.specialFeatures || '-' }}</div>
              <div class="col-12"><strong>失踪原因：</strong>{{ detailItem.missingCause || '-' }}</div>
              <div class="col-12"><strong>详细描述：</strong>{{ detailItem.description || '-' }}</div>
              <div class="col-md-6 mt-2"><strong>联系人：</strong>{{ detailItem.contactName || '-' }}</div>
              <div class="col-md-6 mt-2"><strong>联系电话：</strong>{{ detailItem.contactPhone || '-' }}</div>
            </div>

            <!-- 变更记录 -->
            <div v-if="changeLogs.length > 0" class="mt-4 pt-3 border-top">
              <div class="d-flex align-items-center mb-3">
                <h6 class="mb-0 me-2"><i class="fas fa-history text-info me-1"></i>修改历史</h6>
                <span class="badge bg-info rounded-pill">{{ changeLogs.length }} 项变更</span>
                <button class="btn btn-sm btn-link ms-auto" @click="showChangeLogs = !showChangeLogs">
                  {{ showChangeLogs ? '收起' : '展开' }}
                  <i class="fas" :class="showChangeLogs ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
                </button>
              </div>
              <div v-if="showChangeLogs" class="table-responsive">
                <table class="table table-sm table-bordered mb-0">
                  <thead class="table-light">
                    <tr><th style="width:15%">字段</th><th style="width:35%">原值</th><th style="width:35%">新值</th><th style="width:15%">修改时间</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="log in changeLogs" :key="log.id">
                      <td class="fw-medium">{{ log.fieldName }}</td>
                      <td class="text-muted" style="word-break:break-all">{{ log.oldValue || '(空)' }}</td>
                      <td class="text-danger" style="word-break:break-all">{{ log.newValue || '(空)' }}</td>
                      <td class="small">{{ formatTime(log.createTime) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetail = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api'

const list = ref([])
const loading = ref(true)
const showDetail = ref(false)
const detailItem = ref({})
const showChangeLogs = ref(true)
const changeLogs = ref([])
const filterStatus = ref('')
const filterName = ref('')
const filterUsername = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = ref(10)

function formatTime(t) {
  if (!t) return '-'
  return t.substring(0, 16).replace('T', ' ')
}

const photosList = computed(() => {
  if (!detailItem.value || !detailItem.value.photos) return []
  return detailItem.value.photos.split(',').filter(p => p && p.trim())
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
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value
    if (filterName.value) params.name = filterName.value
    if (filterUsername.value) params.username = filterUsername.value
    const res = await adminApi().listMissingPersons(params)
    if (res.code === 200) {
      list.value = res.data.records || []
      currentPage.value = res.data.current || 1
      totalPages.value = res.data.pages || 1
    }
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function changePage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
}

async function viewDetail(item) {
  detailItem.value = { ...item }
  showDetail.value = true
  showChangeLogs.value = true
  changeLogs.value = []
  try {
    const res = await adminApi().getChangeLogs(item.id)
    if (res.code === 200 && res.data) {
      changeLogs.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error('获取变更记录失败', e)
  }
}

async function approve(item) {
  const remark = prompt('审核备注（可选）：')
  try {
    const res = await adminApi().approveMissingPerson(item.id, remark)
    if (res.code === 200) { item.status = 1; await loadData() }
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

async function reject(item) {
  const remark = prompt('请输入拒绝原因：')
  if (!remark) return
  try {
    const res = await adminApi().rejectMissingPerson(item.id, remark)
    if (res.code === 200) { item.status = 2; await loadData() }
    else alert(res.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

async function deleteItem(item) {
  if (!confirm(`确定要删除「${item.name}」的寻亲信息吗？`)) return
  try {
    const res = await adminApi().deleteMissingPerson(item.id)
    if (res.code === 200) list.value = list.value.filter(i => i.id !== item.id)
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>