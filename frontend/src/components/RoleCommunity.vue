<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">
        <i class="fas fa-comments text-primary me-2"></i>社群交流
        <small class="text-muted ms-2">- {{ roleName }}</small>
      </h2>
      <button v-if="!showCreateForm" class="btn btn-primary" @click="showCreateForm = true">
        <i class="fas fa-plus-circle me-2"></i>发布帖子
      </button>
      <button v-else class="btn btn-secondary" @click="cancelCreate">
        <i class="fas fa-times me-2"></i>取消发布
      </button>
    </div>

    <div v-if="showCreateForm" class="card mb-4">
      <div class="card-body">
        <h5 class="card-title mb-3"><i class="fas fa-pen-alt me-2"></i>发布新帖子</h5>
        <div class="mb-3">
          <input v-model="newPost.title" class="form-control" placeholder="帖子标题" maxlength="100" />
        </div>
        <div class="mb-3">
          <textarea v-model="newPost.content" class="form-control" rows="4" placeholder="写下你想分享的内容..." maxlength="2000"></textarea>
          <small class="text-muted">{{ newPost.content.length }}/2000</small>
        </div>
        <div class="text-end">
          <button class="btn btn-primary" :disabled="creatingPost || !newPost.title || !newPost.content" @click="createPost">
            <span v-if="creatingPost" class="spinner-border spinner-border-sm me-1"></span>
            <i v-else class="fas fa-paper-plane me-1"></i>发布
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary mb-3"></div>
      <p class="text-muted">加载中...</p>
    </div>

    <template v-else>
      <div v-if="posts.length === 0" class="text-center py-5">
        <i class="fas fa-comments text-muted" style="font-size: 3rem;"></i>
        <p class="text-muted mt-3">暂无帖子，快来发布第一条吧！</p>
      </div>

      <div v-for="post in posts" :key="post.id" class="card mb-3">
        <div class="card-body">
          <div class="d-flex align-items-center mb-3">
            <img v-if="post.avatar" :src="post.avatar" class="rounded-circle me-2"
              style="width: 40px; height: 40px; object-fit: cover; flex-shrink: 0;"
              :alt="post.username || '头像'" @error="onAvatarError">
            <div v-else class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2"
              style="width: 40px; height: 40px; font-size: 16px; flex-shrink: 0;">
              {{ (post.username || post.nickname || '?').charAt(0) }}
            </div>
            <div>
              <h6 class="mb-0 fw-semibold">{{ post.username || post.nickname || '匿名' }}</h6>
              <small class="text-muted">{{ timeAgo(post.createTime) }}</small>
            </div>
          </div>
          <h5 class="fw-bold mb-2">{{ post.title }}</h5>
          <p class="text-secondary mb-3" style="white-space: pre-wrap;">{{ post.content }}</p>
          <div class="d-flex gap-3 pt-2 border-top">
            <button class="btn btn-sm" :class="post.isLiked ? 'text-danger' : 'text-muted'" @click="toggleLike(post)">
              <i class="fas" :class="post.isLiked ? 'fa-thumbs-up' : 'far fa-thumbs-up'"></i>
              <span class="ms-1">{{ post.likeCount || 0 }}</span>
            </button>
            <button class="btn btn-sm text-muted" @click="toggleComments(post)">
              <i class="far fa-comment me-1"></i>{{ post.commentCount || 0 }}
            </button>
            <button v-if="Number(authStore.currentUser?.id) === Number(post.userId)" class="btn btn-sm text-danger ms-auto" @click="deletePost(post)">
              <i class="far fa-trash-alt"></i>
            </button>
          </div>

          <div v-if="activePostId === post.id" class="mt-3 pt-3 border-top">
            <div class="mb-3">
              <div class="input-group">
                <input v-model="commentText" class="form-control" placeholder="写下你的评论..."
                  @keydown.enter.prevent="addComment(post.id, null)" />
                <button class="btn btn-primary" @click="addComment(post.id, null)">
                  <i class="fas fa-paper-plane"></i>
                </button>
              </div>
            </div>
            <div v-if="commentsLoading" class="text-center py-2">
              <div class="spinner-border spinner-border-sm text-primary"></div>
            </div>
            <div v-else-if="comments.length === 0" class="text-muted small text-center py-2">
              暂无评论，来说两句吧
            </div>
            <div v-else class="comment-thread">
              <div v-for="comment in topLevelComments" :key="comment.id" class="comment-item mb-2">
                <div class="d-flex align-items-start">
                  <img v-if="comment.avatar" :src="comment.avatar" class="rounded-circle me-2"
                    style="width: 32px; height: 32px; object-fit: cover; flex-shrink: 0;"
                    :alt="comment.username || '头像'" @error="onAvatarError">
                  <div v-else class="rounded-circle bg-secondary text-white d-flex align-items-center justify-content-center me-2"
                    style="width: 32px; height: 32px; font-size: 13px; flex-shrink: 0;">
                    {{ (comment.username || comment.nickname || '?').charAt(0) }}
                  </div>
                  <div class="flex-grow-1">
                    <div class="bg-light rounded-3 p-2">
                      <div class="fw-bold small">{{ comment.username || comment.nickname || '匿名' }}</div>
                      <div class="small">{{ comment.content }}</div>
                    </div>
                    <div class="d-flex gap-2 mt-1 ms-1">
                      <button class="btn btn-sm p-0 border-0" :class="comment.isLiked ? 'text-danger' : 'text-muted'"
                        @click="toggleCommentLike(comment)">
                        <i class="fas" :class="comment.isLiked ? 'fa-thumbs-up' : 'far fa-thumbs-up'"></i>
                        <small class="ms-1">{{ comment.likeCount || 0 }}</small>
                      </button>
                      <button class="btn btn-sm p-0 border-0 text-muted" @click="openReply(comment)">
                        <small><i class="far fa-comment-dots me-1"></i>回复</small>
                      </button>
                      <button v-if="Number(authStore.currentUser?.id) === Number(comment.userId)" class="btn btn-sm p-0 border-0 text-danger ms-1" @click="deleteComment(comment)">
                        <small><i class="far fa-trash-alt"></i></small>
                      </button>
                    </div>
                    <div v-if="replyTargetId === comment.id" class="mt-1 mb-2">
                      <div class="input-group input-group-sm">
                        <input v-model="replyText" class="form-control" :placeholder="'回复 ' + (comment.username || comment.nickname || '匿名')"
                          @keydown.enter.prevent="addComment(comment.postId, comment.id)" />
                        <button class="btn btn-primary btn-sm" @click="addComment(comment.postId, comment.id)">
                          <i class="fas fa-paper-plane"></i>
                        </button>
                      </div>
                    </div>
                    <div v-if="getReplies(comment.id).length > 0" class="ms-3 mt-1">
                      <div v-for="reply in getReplies(comment.id)" :key="reply.id" class="comment-item mb-1">
                        <div class="d-flex align-items-start">
                          <img v-if="reply.avatar" :src="reply.avatar" class="rounded-circle me-2"
                            style="width: 28px; height: 28px; object-fit: cover; flex-shrink: 0;"
                            :alt="reply.username || '头像'" @error="onAvatarError">
                          <div v-else class="rounded-circle bg-info text-white d-flex align-items-center justify-content-center me-2"
                            style="width: 28px; height: 28px; font-size: 11px; flex-shrink: 0;">
                            {{ (reply.username || reply.nickname || '?').charAt(0) }}
                          </div>
                          <div class="flex-grow-1">
                            <div class="bg-light rounded-3 p-2">
                              <div class="fw-bold small">{{ reply.username || reply.nickname || '匿名' }}</div>
                              <div class="small">{{ reply.content }}</div>
                            </div>
                            <div class="d-flex gap-2 mt-1 ms-1">
                              <button class="btn btn-sm p-0 border-0" :class="reply.isLiked ? 'text-danger' : 'text-muted'"
                                @click="toggleCommentLike(reply)">
                                <i class="fas" :class="reply.isLiked ? 'fa-thumbs-up' : 'far fa-thumbs-up'"></i>
                                <small class="ms-1">{{ reply.likeCount || 0 }}</small>
                              </button>
                              <button class="btn btn-sm p-0 border-0 text-muted" @click="openReply(reply)">
                                <small><i class="far fa-comment-dots me-1"></i>回复</small>
                              </button>
                              <button v-if="Number(authStore.currentUser?.id) === Number(reply.userId)" class="btn btn-sm p-0 border-0 text-danger ms-1" @click="deleteComment(reply)">
                                <small><i class="far fa-trash-alt"></i></small>
                              </button>
                            </div>
                            <div v-if="replyTargetId === reply.id" class="mt-1 mb-2">
                              <div class="input-group input-group-sm">
                                <input v-model="replyText" class="form-control"
                                  :placeholder="'回复 ' + (reply.username || reply.nickname || '匿名')"
                                  @keydown.enter.prevent="addComment(reply.postId, reply.id)" />
                                <button class="btn btn-primary btn-sm" @click="addComment(reply.postId, reply.id)">
                                  <i class="fas fa-paper-plane"></i>
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { communityApi } from '@/api'
import { timeAgo } from '@/utils'

const props = defineProps({
  roleName: { type: String, default: '' }
})

const authStore = useAuthStore()

const posts = ref([])
const loading = ref(true)
const showCreateForm = ref(false)
const creatingPost = ref(false)
const newPost = ref({ title: '', content: '' })

const activePostId = ref(null)
const comments = ref([])
const commentsLoading = ref(false)
const commentText = ref('')
const replyText = ref('')
const replyTargetId = ref(null)

const topLevelComments = computed(() => comments.value.filter(c => !c.parentId))

function getReplies(parentId) {
  return comments.value.filter(c => c.parentId === parentId)
}

async function loadPosts() {
  loading.value = true
  try {
    const res = await communityApi().listPosts({ pageNum: 1, pageSize: 20 })
    if (res && res.code === 200) {
      posts.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
      try {
        const likedRes = await communityApi().getMyLikedPosts()
        if (likedRes && likedRes.code === 200 && Array.isArray(likedRes.data)) {
          const likedIds = likedRes.data.map(id => Number(id))
          posts.value.forEach(p => { if (likedIds.includes(Number(p.id))) p.isLiked = true })
        }
      } catch (e) { /* 未登录或获取失败忽略 */ }
    } else {
      posts.value = []
    }
  } catch (e) {
    console.error('加载帖子失败:', e)
    posts.value = []
  } finally {
    loading.value = false
  }
}

function cancelCreate() {
  showCreateForm.value = false
  newPost.value = { title: '', content: '' }
}

async function createPost() {
  if (!newPost.value.title || !newPost.value.content) return
  creatingPost.value = true
  try {
    const res = await communityApi().createPost({
      title: newPost.value.title,
      content: newPost.value.content
    })
    if (res && res.code === 200) {
      cancelCreate()
      loadPosts()
    } else {
      alert(res?.message || '发布失败')
    }
  } catch (e) {
    alert('发布失败，请稍后重试')
  } finally {
    creatingPost.value = false
  }
}

async function toggleLike(post) {
  try {
    if (post.isLiked) {
      const res = await communityApi().unlikePost(post.id)
      if (res && res.code === 200) {
        post.isLiked = false
        post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
      } else if (res && res.message) {
        post.isLiked = false
        post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
      }
    } else {
      const res = await communityApi().likePost(post.id)
      if (res && res.code === 200) {
        post.isLiked = true
        post.likeCount = (post.likeCount || 0) + 1
      } else if (res && res.message) {
        post.isLiked = true
        post.likeCount = (post.likeCount || 0) + 1
      }
    }
  } catch (e) { console.error('操作失败:', e) }
}

async function toggleComments(post) {
  if (activePostId.value === post.id) {
    activePostId.value = null
    comments.value = []
    return
  }
  activePostId.value = post.id
  replyTargetId.value = null
  await loadComments(post.id)
}

async function loadComments(postId) {
  commentsLoading.value = true
  comments.value = []
  try {
    const res = await communityApi().getComments(postId)
    if (res && res.code === 200) {
      const records = Array.isArray(res.data) ? res.data : (res.data.records || [])
      comments.value = records
      try {
        const likedRes = await communityApi().getMyLikedComments()
        if (likedRes && likedRes.code === 200 && Array.isArray(likedRes.data)) {
          const likedIds = likedRes.data.map(id => Number(id))
          comments.value.forEach(c => { if (likedIds.includes(Number(c.id))) c.isLiked = true })
        }
      } catch (e) { /* 忽略 */ }
    }
  } catch (e) {
    console.error('加载评论失败:', e)
  } finally {
    commentsLoading.value = false
  }
}

async function addComment(postId, parentId) {
  const text = parentId ? replyText.value : commentText.value
  if (!text.trim()) return
  try {
    const data = { content: text.trim() }
    if (parentId) data.parentId = parentId
    const res = await communityApi().addComment(postId, data)
    if (res && res.code === 200) {
      commentText.value = ''
      replyText.value = ''
      replyTargetId.value = null
      await loadComments(postId)
      const post = posts.value.find(p => p.id === postId)
      if (post) post.commentCount = (post.commentCount || 0) + 1
    } else {
      alert(res?.message || '评论失败')
    }
  } catch (e) {
    alert('评论失败，请稍后重试')
  }
}

function openReply(comment) {
  replyTargetId.value = replyTargetId.value === comment.id ? null : comment.id
  replyText.value = ''
}

function onAvatarError(e) {
  e.target.style.display = 'none'
}

async function toggleCommentLike(comment) {
  try {
    if (comment.isLiked) {
      const res = await communityApi().unlikeComment(comment.id)
      if (res && res.code === 200) {
        comment.isLiked = false
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
      } else if (res && res.message) {
        comment.isLiked = false
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
      }
    } else {
      const res = await communityApi().likeComment(comment.id)
      if (res && res.code === 200) {
        comment.isLiked = true
        comment.likeCount = (comment.likeCount || 0) + 1
      } else if (res && res.message) {
        comment.isLiked = true
        comment.likeCount = (comment.likeCount || 0) + 1
      }
    }
  } catch (e) { console.error('操作失败:', e) }
}

async function deletePost(post) {
  if (!confirm('确定要删除这篇帖子吗？')) return
  try {
    const res = await communityApi().deletePost(post.id)
    if (res && res.code === 200) {
      posts.value = posts.value.filter(p => p.id !== post.id)
    } else {
      alert(res?.message || '删除失败')
    }
  } catch (e) {
    alert('删除失败')
  }
}

async function deleteComment(comment) {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    const res = await communityApi().deleteComment(comment.id)
    if (res && res.code === 200) {
      comments.value = comments.value.filter(c => c.id !== comment.id)
      const post = posts.value.find(p => p.id === comment.postId)
      if (post) post.commentCount = Math.max(0, (post.commentCount || 0) - 1)
    } else {
      alert(res?.message || '删除失败')
    }
  } catch (e) {
    alert('删除失败')
  }
}

onMounted(() => {
  loadPosts()
})
</script>