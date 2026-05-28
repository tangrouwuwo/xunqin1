<template>
  <div>
    <AppNavbar />
    <router-view />
    <AIChat v-if="authStore.isLoggedIn" />
  </div>
</template>

<script setup>
import AppNavbar from '@/components/AppNavbar.vue'
import AIChat from '@/components/AIChat.vue'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
</script>