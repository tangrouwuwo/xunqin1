<template>
  <div class="container py-5">
    <div class="text-center mb-5">
      <h1 class="fw-bold mb-3">社群交流</h1>
      <p class="text-secondary lead">加入我们的社群，与其他寻亲者、志愿者和线索提供者交流分享</p>
      <router-link v-if="authStore.isLoggedIn" to="/dashboard" class="btn btn-primary rounded-pill px-4">
        <i class="fas fa-plus-circle me-2"></i> 发布帖子
      </router-link>
    </div>

    <div class="row">
      <div class="col-lg-8 mx-auto">
        <div v-for="post in posts" :key="post.id" class="community-post">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <div class="d-flex align-items-center mb-1">
                <img v-if="post.avatar" :src="post.avatar" class="rounded-circle me-2"
                  style="width: 36px; height: 36px; object-fit: cover; flex-shrink: 0;"
                  :alt="post.username || '头像'" @error="onAvatarError">
                <div v-else class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2"
                  style="width: 36px; height: 36px; font-size: 14px; flex-shrink: 0;">
                  {{ (post.username || post.nickname || '?').charAt(0) }}
                </div>
                <div>
                  <h6 class="mb-0 fw-semibold" style="color: var(--primary-color);">{{ post.username || post.nickname || '匿名' }}</h6>
                  <small class="text-muted">{{ timeAgo(post.createdAt || post.createTime) }}</small>
                </div>
              </div>
            </div>
          </div>
          <h5 class="fw-bold mb-2">{{ post.title }}</h5>
          <p class="text-secondary mb-3">{{ post.content }}</p>
          <div class="d-flex gap-3 pt-2 border-top">
            <button class="btn btn-sm" :class="post.isLiked ? 'text-danger' : 'text-muted'" @click="toggleLike(post)">
              <i class="fas" :class="post.isLiked ? 'fa-thumbs-up' : 'far fa-thumbs-up'"></i>
              <span class="ms-1">{{ post.likes || post.likeCount || 0 }}</span>
            </button>
            <button class="btn btn-sm text-muted" @click="showComments(post)">
              <i class="far fa-comment me-1"></i> {{ post.commentCount || 0 }}
            </button>
          </div>
        </div>

        <div v-if="posts.length === 0" class="text-center py-5">
          <i class="fas fa-comments text-muted" style="font-size: 3rem;"></i>
          <p class="text-muted mt-3">暂无社群帖子</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { communityApi } from '@/api'
import { timeAgo } from '@/utils'

const router = useRouter()
const authStore = useAuthStore()
const posts = ref([])

async function loadPosts() {
  try {
    const res = await communityApi().listPosts()
    if (res.code === 200) {
      posts.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
      try {
        const likedRes = await communityApi().getMyLikedPosts()
        if (likedRes && likedRes.code === 200 && Array.isArray(likedRes.data)) {
          const likedIds = likedRes.data.map(id => Number(id))
          posts.value.forEach(p => { if (likedIds.includes(Number(p.id))) p.isLiked = true })
        }
      } catch (e) { /* 未登录忽略 */ }
    }
  } catch (e) {
    posts.value = []
  }
}

async function toggleLike(post) {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login' })
    return
  }
  try {
    if (post.isLiked) {
      const res = await communityApi().unlikePost(post.id)
      if (res.code === 200) {
        post.isLiked = false; post.likes = Math.max(0, (post.likes || post.likeCount || 0) - 1)
      } else if (res.message) {
        post.isLiked = false; post.likes = Math.max(0, (post.likes || post.likeCount || 0) - 1)
      }
    } else {
      const res = await communityApi().likePost(post.id)
      if (res.code === 200) {
        post.isLiked = true; post.likes = (post.likes || post.likeCount || 0) + 1
      } else if (res.message) {
        post.isLiked = true; post.likes = (post.likes || post.likeCount || 0) + 1
      }
    }
  } catch (e) { console.error('操作失败:', e) }
}

function showComments(post) {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login' })
    return
  }
  const roleRoutes = { ADMIN: 'AdminCommunity', SEEKER: 'SeekerCommunity', VOLUNTEER: 'VolunteerCommunity', CLUE_PROVIDER: 'ClueProviderCommunity' }
  const routeName = roleRoutes[authStore.role]
  if (routeName) {
    router.push({ name: routeName })
  } else {
    router.push('/dashboard')
  }
}

function onAvatarError(e) {
  e.target.style.display = 'none'
}

onMounted(() => {
  loadPosts()
})
</script>