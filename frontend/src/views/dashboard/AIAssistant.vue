<template>
  <div>
    <h2 class="mb-4">AI 助手</h2>
    <div class="card">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div>
            <h5 class="mb-0"><i class="fas fa-robot text-primary me-2"></i>智能寻亲助手</h5>
            <small class="text-muted">我可以帮您解答寻亲相关问题、分析线索、提供建议</small>
          </div>
          <button v-if="messages.length > 1" class="btn btn-sm btn-outline-secondary" @click="clearChat">
            <i class="fas fa-trash-alt me-1"></i>清空对话
          </button>
        </div>
        <div ref="chatBox" class="chat-box mb-3 p-3 bg-light rounded" style="min-height: 400px; max-height: 500px; overflow-y: auto;">
          <div v-if="messages.length === 0" class="text-center text-muted py-5">
            <i class="fas fa-comment-dots" style="font-size: 3rem;"></i>
            <p class="mt-3">您好！我是智能寻亲助手，请问有什么可以帮助您的？</p>
            <div class="d-flex justify-content-center gap-2 flex-wrap mt-3">
              <button class="btn btn-sm btn-outline-primary" @click="quickQuestion('如何发布寻亲信息？')">如何发布寻亲信息？</button>
              <button class="btn btn-sm btn-outline-primary" @click="quickQuestion('怎样提供线索？')">怎样提供线索？</button>
              <button class="btn btn-sm btn-outline-primary" @click="quickQuestion('成为志愿者的条件')">成为志愿者的条件</button>
              <button class="btn btn-sm btn-outline-primary" @click="quickQuestion('寻亲成功案例')">寻亲成功案例</button>
            </div>
          </div>
          <div v-for="(msg, idx) in messages" :key="idx" class="mb-3" :class="msg.role === 'user' ? 'text-end' : ''">
            <div class="d-inline-block p-3 rounded-3" :class="msg.role === 'user' ? 'bg-primary text-white' : 'bg-white border'"
              style="max-width: 80%; text-align: left;">
              <div class="fw-bold small mb-1">{{ msg.role === 'user' ? '我' : 'AI助手' }}</div>
              <div style="white-space: pre-wrap;">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="loading" class="text-center py-3">
            <div class="spinner-border text-primary spinner-border-sm me-2"></div>AI正在思考...
          </div>
        </div>
        <div class="input-group">
          <textarea v-model="input" class="form-control" rows="2" placeholder="请输入您的问题..." @keydown.enter.exact.prevent="sendMessage"
            :disabled="loading" style="resize: none;"></textarea>
          <button class="btn btn-primary" @click="sendMessage" :disabled="loading || !input.trim()">
            <i class="fas fa-paper-plane"></i>
          </button>
        </div>
        <small class="text-muted mt-2 d-block">按 Enter 发送，Shift+Enter 换行</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiApi } from '@/api'

const messages = ref([
  { role: 'assistant', content: '您好！我是智能寻亲助手，请问有什么可以帮助您的？' }
])
const input = ref('')
const loading = ref(false)
const chatBox = ref(null)

async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollToBottom()
  try {
    const res = await aiApi().ask({ question: text })
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data || '抱歉，我暂时无法回答这个问题。' })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，我遇到了问题，请稍后再试。' })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，连接失败，请检查网络后重试。' })
  }
  finally { loading.value = false }
  scrollToBottom()
}

function quickQuestion(q) {
  input.value = q
  sendMessage()
}

function clearChat() {
  messages.value = [{ role: 'assistant', content: '对话已清空，请问有什么可以帮助您的？' }]
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  })
}
</script>