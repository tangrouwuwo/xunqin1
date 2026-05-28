<template>
  <div class="container py-5">
    <!-- 社会价值文案 - 吸引用户成为志愿者 -->
    <div class="text-center mb-5">
      <div class="bg-light rounded-4 p-4 mb-4" style="border-left: 5px solid var(--primary-color);">
        <h5 class="text-primary fw-bold mb-3"><i class="fas fa-heart me-2"></i>您的参与，改变的不只是一个家庭</h5>
        <div class="row justify-content-center">
          <div class="col-md-10">
            <p class="text-secondary mb-2">在我国，每年有数以万计的家庭因各种原因与亲人失散。每一个失散家庭的背后，都是漫长的思念和无尽的等待。</p>
            <p class="text-secondary mb-2">成为我们的志愿者，您不再只是旁观者——您将成为连接亲情的桥梁，用行动点亮失散家庭团聚的希望。</p>
            <p class="text-secondary mb-0"><strong>每一次转发，都可能让一个孩子找到回家的路；每一次参与，都可能让一个家庭重获完整。</strong></p>
          </div>
        </div>
      </div>
      <div class="row g-3 mb-4 justify-content-center">
        <div class="col-md-3 col-6">
          <div class="border rounded-3 p-3 text-center h-100">
            <div class="text-primary fw-bold fs-3">500+</div>
            <small class="text-muted">已帮助家庭团聚</small>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="border rounded-3 p-3 text-center h-100">
            <div class="text-success fw-bold fs-3">2000+</div>
            <small class="text-muted">活跃志愿者在行动</small>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="border rounded-3 p-3 text-center h-100">
            <div class="text-warning fw-bold fs-3">98%</div>
            <small class="text-muted">志愿者满意度评价</small>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="border rounded-3 p-3 text-center h-100">
            <div class="text-info fw-bold fs-3">全国</div>
            <small class="text-muted">覆盖所有省市地区</small>
          </div>
        </div>
      </div>
      <h1 class="fw-bold mb-3">志愿者招募</h1>
      <p class="text-secondary lead">每一份力量都能带来希望，每一次行动都能改变命运</p>
      <router-link v-if="authStore.isLoggedIn" to="/dashboard" class="btn btn-primary rounded-pill px-4">
        <i class="fas fa-user-plus me-2"></i> 申请成为志愿者
      </router-link>
      <router-link v-else to="/register?role=VOLUNTEER" class="btn btn-primary rounded-pill px-4">
        <i class="fas fa-user-plus me-2"></i> 注册成为志愿者
      </router-link>
    </div>

    <div class="row g-4 mb-5">
      <div class="col-md-4" v-for="(benefit, idx) in benefits" :key="idx">
        <div class="card h-100 border-0 shadow text-center p-4">
          <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center mx-auto mb-3" style="width: 60px; height: 60px;">
            <i :class="benefit.icon" style="font-size: 1.5rem;"></i>
          </div>
          <h5 class="fw-bold mb-2">{{ benefit.title }}</h5>
          <p class="text-secondary mb-0">{{ benefit.desc }}</p>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-8 mx-auto">
        <h3 class="fw-bold mb-4">近期志愿活动</h3>
        <div v-for="activity in activities" :key="activity.id" class="card mb-3 border-0 shadow-sm">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h5 class="fw-bold mb-2">{{ activity.title }}</h5>
                <p class="text-secondary mb-3">{{ activity.description }}</p>
                <div class="d-flex flex-wrap gap-3 text-muted small">
                  <span><i class="fas fa-map-marker-alt text-primary me-1"></i> {{ activity.location }}</span>
                  <span><i class="far fa-calendar-alt text-primary me-1"></i> {{ activity.date }}</span>
                  <span><i class="fas fa-users text-primary me-1"></i> {{ activity.participants }}人已报名</span>
                </div>
              </div>
              <button class="btn btn-outline-primary btn-sm rounded-pill px-3 flex-shrink-0" @click="joinActivity(activity)">
                报名参加
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import api from '@/api'

const authStore = useAuthStore()

const benefits = [
  { icon: 'fas fa-hand-holding-heart', title: '参与线下寻亲', desc: '亲身参与线下寻亲活动，帮助失散家庭团聚' },
  { icon: 'fas fa-edit', title: '信息整理发布', desc: '帮助整理和发布寻亲信息，扩大传播范围' },
  { icon: 'fas fa-laptop-code', title: '专业技能支持', desc: '用你的专业技能为寻亲事业贡献力量' },
  { icon: 'fas fa-search', title: '线索核实', desc: '成为线索核实员，确保每条线索的真实性' },
  { icon: 'fas fa-globe', title: '网络推广', desc: '帮助推广寻亲信息，让更多人看到希望' },
  { icon: 'fas fa-hands-helping', title: '心理援助', desc: '为寻亲者提供心理支持和安慰' }
]

const activities = ref([
  { id: 1, title: '北京朝阳区线下寻亲活动', description: '组织志愿者在朝阳区开展线下寻亲宣传活动，发放寻亲传单，收集线索信息。', location: '北京市朝阳区', date: '2026年6月', participants: 15 },
  { id: 2, title: '广州天河区寻亲信息采集', description: '在广州天河区进行寻亲信息采集和DNA样本收集工作。', location: '广州市天河区', date: '2026年6月', participants: 22 },
  { id: 3, title: '线上寻亲信息推广行动', description: '通过社交媒体和网络平台推广寻亲信息，扩大信息覆盖面。', location: '线上', date: '2026年6月', participants: 35 }
])

async function joinActivity(activity) {
  if (!authStore.isLoggedIn) {
    alert('请先登录后再报名参加活动')
    return
  }
  try {
    const res = await api.post('/volunteer-activities/join', { activityId: activity.id })
    if (res.code === 200) {
      alert('报名成功！')
    } else {
      alert(res.message || '报名失败')
    }
  } catch (e) {
    alert('报名失败，请稍后重试')
  }
}

onMounted(() => {
})
</script>