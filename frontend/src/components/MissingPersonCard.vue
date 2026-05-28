<template>
  <div class="missing-person-card">
    <div class="card-img-wrapper">
      <img
        :src="imageSrc"
        class="card-img-top"
        :alt="person.name"
        @error="onImageError"
      >
    </div>
    <div class="card-body p-4">
      <h5 class="card-title fw-bold mb-1" style="font-size: 1.25rem;">{{ person.title || person.name }}</h5>
      <p class="text-muted small mb-2">被寻人：{{ person.name }}</p>
      <p class="card-text text-muted mb-3">
        {{ person.gender || '-' }} | {{ person.ageAtMissing || '-' }}岁
        <span v-if="person.missingDate"> | {{ formatDate(person.missingDate) }}</span>
      </p>
      <p class="card-text mb-3">
        {{ truncateText(person.description, 60) }}
      </p>
      <div class="d-flex justify-content-between align-items-center">
        <span class="status-badge" :class="'status-' + statusClass">{{ statusText }}</span>
        <button class="btn btn-sm btn-primary" @click="$emit('view-detail', person.id)">
          查看详情
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils'

const props = defineProps({
  person: { type: Object, required: true }
})

defineEmits(['view-detail'])

const defaultImages = [
  'images/OIP-C.webp',
  'images/20251125220048176407924862937.jpg',
  'images/20260118170341176872702158065.jpg',
  'images/OIP-C (1).webp'
]

const imageSrc = computed(() => {
  const photos = props.person.photos
  if (photos) {
    const first = photos.split(',')[0]
    if (first.startsWith('http')) return first
    return first
  }
  return defaultImages[Math.floor(Math.random() * defaultImages.length)]
})

const statusClass = computed(() => {
  const status = props.person.status
  if (status === 1) return 'approved'
  if (status === 2) return 'rejected'
  return 'pending'
})

const statusText = computed(() => {
  const status = props.person.status
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '审核中'
})

function onImageError(e) {
  e.target.src = defaultImages[Math.floor(Math.random() * defaultImages.length)]
}

function truncateText(text, maxLen) {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}
</script>