<template>
  <div>
    <!-- 英雄区 -->
    <section class="hero-section text-white py-5" style="background: linear-gradient(135deg, #e94c3d 0%, #c0392b 50%, #a93226 100%); background-size: cover; background-position: center;">
      <div class="container text-center py-5">
        <h1 class="display-4 fw-bold mb-4 fade-in visible">让爱回家，让亲情重聚</h1>
        <p class="lead mb-5 fade-in visible">我们致力于帮助失散家庭重新团聚，通过社会力量和科技手段，让每一个寻亲者都能找到回家的路。</p>
        <div class="d-flex justify-content-center gap-3 flex-wrap">
          <router-link to="/missing-persons-search" class="btn btn-light text-danger fw-bold px-5 py-3 rounded-3 shadow-lg" style="color: var(--primary-color) !important;">
            <i class="fas fa-search me-2"></i> 寻亲查询
          </router-link>
          <router-link to="/success-cases" class="btn btn-outline-light fw-bold px-5 py-3 rounded-3 border-2">
            <i class="fas fa-calendar-check me-2"></i> 成功案例
          </router-link>
          <router-link to="/community" class="btn btn-outline-light fw-bold px-5 py-3 rounded-3 border-2">
            <i class="fas fa-lightbulb me-2"></i> 提供线索
          </router-link>
        </div>
      </div>
    </section>

    <!-- 成功案例 -->
    <section class="py-5 bg-light">
      <div class="container">
        <div class="text-center mb-5">
          <h2 class="fw-bold mb-3">成功案例</h2>
          <p class="text-secondary">每一个成功案例都是希望的开始，每一次团聚都是爱的见证</p>
        </div>
        <div class="row g-4" id="successCases">
          <div class="col-md-6 col-lg-4" v-for="(item, idx) in successCases" :key="idx">
            <div class="card shadow h-100 border-0" :style="{ animationDelay: idx * 0.1 + 's' }">
              <div class="position-relative overflow-hidden" style="height: 200px;">
                <img :src="item.photo" class="w-100 h-100" style="object-fit: cover;" :alt="item.title" @error="e => e.target.style.display='none'">
                <div v-if="!item.photo" class="h-100 w-100 d-flex align-items-center justify-content-center bg-primary">
                  <i class="fas fa-heart text-white" style="font-size: 4rem; opacity: 0.8;"></i>
                </div>
                <div class="position-absolute top-0 start-0 px-3 py-2 rounded-bottom-end bg-primary text-white">
                  <i class="fas fa-star me-1"></i> 团聚时刻
                </div>
              </div>
              <div class="card-body p-4">
                <h5 class="card-title fw-bold mb-3" style="color: var(--primary-color);">{{ item.title }}</h5>
                <p class="card-text text-secondary mb-4">{{ (item.story || '').substring(0, 120) }}{{ (item.story || '').length > 120 ? '...' : '' }}</p>
                <div class="text-muted small">
                  <i class="fas fa-map-marker-alt text-primary me-1"></i> {{ item.reunionLocation || '未知地点' }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="text-center mt-4">
          <router-link to="/success-cases" class="btn btn-primary btn-lg rounded-pill px-5 py-3 fw-semibold">
            <i class="fas fa-th-list me-2"></i> 查看更多成功案例
          </router-link>
        </div>
      </div>
    </section>

    <!-- 志愿者鼓励 -->
    <section class="py-5 bg-white">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-md-6 order-2 order-md-1">
            <img src="https://images.unsplash.com/photo-1559027615-cd4628902d4a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" class="img-fluid rounded-3 shadow" alt="志愿者活动">
          </div>
          <div class="col-md-6 mt-4 mt-md-0 order-1 order-md-2">
            <h2 class="fw-bold mb-4">加入我们，成为志愿者</h2>
            <p class="text-secondary mb-4">每一份力量都能带来希望，每一次行动都能改变命运。作为志愿者，您可以：</p>
            <ul class="list-unstyled">
              <li v-for="(item, idx) in volunteerBenefits" :key="idx" class="d-flex align-items-center mb-3 fade-in">
                <div class="rounded-circle me-3 d-flex align-items-center justify-content-center flex-shrink-0" style="background: var(--primary-color); color: white; width: 40px; height: 40px;">
                  <i class="fas fa-check"></i>
                </div>
                <span>{{ item }}</span>
              </li>
            </ul>
            <router-link to="/volunteer" class="btn btn-primary rounded-pill px-5 py-3 fw-semibold mt-3">
              <i class="fas fa-user-plus me-2"></i> 立即加入
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 社群交流预览 -->
    <section class="py-5 bg-light">
      <div class="container">
        <h2 class="text-center mb-5 fw-bold">社群交流</h2>
        <div class="card shadow border-0">
          <div class="card-body p-4">
            <div class="mb-4">
              <h5 class="fw-bold mb-2">最新动态</h5>
              <p class="text-secondary">加入我们的社群，与其他寻亲者、志愿者和线索提供者交流分享。</p>
            </div>
            <div v-for="(post, idx) in communityPosts" :key="idx" class="list-group-item p-3 rounded-3 mb-3 bg-white">
              <div class="d-flex justify-content-between mb-2">
                <h6 class="mb-0 fw-semibold">{{ post.title }}</h6>
                <small class="text-muted">{{ post.time }}</small>
              </div>
              <p class="mb-2 text-secondary">{{ post.content }}</p>
              <div class="d-flex justify-content-between align-items-center">
                <div class="text-muted small">
                  <button class="btn btn-sm text-muted me-3" @click="post.likes++">
                    <i class="far fa-thumbs-up me-1"></i> {{ post.likes }}
                  </button>
                  <span class="me-3">
                    <i class="far fa-comment me-1"></i> {{ post.comments }}
                  </span>
                </div>
                <router-link to="/community" class="text-primary small fw-medium">查看详情</router-link>
              </div>
            </div>
            <div class="text-center mt-4">
              <router-link to="/community" class="btn btn-outline-primary rounded-pill px-5 py-3 fw-semibold">
                <i class="fas fa-comments me-2"></i> 进入社群
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

const successCases = [
  {
    title: '郭刚堂父子24年寻亲终团聚',
    story: '1997年，郭刚堂年仅2岁半的儿子郭振在家门口被拐走。从此，郭刚堂骑摩托车走遍全国寻子，行程超过40万公里，足迹遍布大江南北。2021年7月，在公安机关的不懈努力下，郭振被成功找到，父子相拥而泣的场景感动了无数国人。',
    reunionLocation: '河南·郑州',
    photo: 'https://images.unsplash.com/photo-1542037104857-4bb4b9fe2433?w=600&h=400&fit=crop'
  },
  {
    title: '孙海洋14年寻子路迎来团圆',
    story: '2007年，孙海洋4岁的儿子孙卓在深圳被拐走。孙海洋夫妇14年如一日坚持寻子，走遍大江南北。2021年12月，在公安部打拐专项行动中，孙卓被成功找回，失散14年的一家三口终于团聚。',
    reunionLocation: '广东·深圳',
    photo: 'https://images.unsplash.com/photo-1511895426328-dc8714191300?w=600&h=400&fit=crop'
  },
  {
    title: '申军良找回被拐15年的儿子',
    story: '2005年，申军良年仅1岁的儿子申聪在广州被抢走。申军良变卖全部家产坚持寻子，足迹遍布全国。2020年3月，广州警方成功抓获犯罪嫌疑人，找回已15岁的申聪，父子重逢的场面令人动容。',
    reunionLocation: '广东·广州',
    photo: 'https://images.unsplash.com/photo-1475503572774-15a45e5d60b9?w=600&h=400&fit=crop'
  },
  {
    title: '李静芝32年坚守找回被拐儿子',
    story: '1988年，李静芝年仅2岁的儿子毛寅在西安被拐走。32年间她走遍全国寻子，被称为"打拐妈妈"。2020年5月，在警方帮助下终于找到失散32年的儿子，她的坚持创造了寻亲奇迹。',
    reunionLocation: '陕西·西安',
    photo: 'https://images.unsplash.com/photo-1491013516836-7db643ee125a?w=600&h=400&fit=crop'
  },
  {
    title: '陈升宽跨越25年的寻亲之路',
    story: '1996年，陈升宽3岁的儿子陈俊哲在云南昆明走失。25年来陈升宽从未放弃寻找，走遍全国各地张贴寻人启事。2021年，在警方和志愿者的帮助下，终于与失散25年的儿子团聚。',
    reunionLocation: '云南·昆明',
    photo: 'https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=600&h=400&fit=crop'
  },
  {
    title: '王玉琼46年后与失散儿子重逢',
    story: '1975年，王玉琼出生仅3个月的儿子在四川成都被抱走。46年来她始终保留着儿子的襁褓和照片，坚信终有一天能找到。2021年，通过DNA比对技术，终于与失散46年的儿子重逢。',
    reunionLocation: '四川·成都',
    photo: 'https://images.unsplash.com/photo-1519689680058-324335c77eba?w=600&h=400&fit=crop'
  }
]

const volunteerBenefits = [
  '参与线下寻亲活动',
  '帮助整理和发布寻亲信息',
  '提供专业技能支持',
  '成为线索核实员'
]

const communityPosts = [
  { title: '寻找1995年出生的小明', content: '小明于2000年在上海走失，有知情者请与我联系。', time: '2小时前', likes: 12, comments: 5 },
  { title: '志愿者招募通知', content: '本周六将在北京市朝阳区组织线下寻亲活动，欢迎志愿者报名参加。', time: '昨天', likes: 25, comments: 8 },
  { title: '成功案例分享', content: '感谢平台的帮助，我终于找到了失散15年的妹妹，谢谢所有志愿者的付出！', time: '2天前', likes: 42, comments: 15 }
]

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) entry.target.classList.add('visible')
    })
  }, { threshold: 0.1 })
  document.querySelectorAll('.fade-in').forEach(el => observer.observe(el))
})
</script>

<style scoped>
.rounded-bottom-end { border-bottom-right-radius: 0.375rem; }
</style>