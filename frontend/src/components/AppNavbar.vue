<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm sticky-top">
    <div class="container-fluid">
      <router-link class="navbar-brand d-flex align-items-center" to="/">
        <i class="fas fa-home text-primary me-2" style="font-size: 1.5rem;"></i>
        <span class="fw-bold" style="font-size: 1.25rem;">寻亲网站</span>
      </router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="mainNav">
        <ul class="navbar-nav ms-auto align-items-center">
          <li class="nav-item">
            <router-link class="nav-link" :class="{ 'active-true': $route.path === '/' }" to="/">
              <i class="fas fa-home me-1"></i> 首页
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :class="{ 'active-true': $route.path === '/missing-persons-search' }" to="/missing-persons-search">
              <i class="fas fa-search me-1"></i> 寻亲查询
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :class="{ 'active-true': $route.path === '/success-cases' }" to="/success-cases">
              <i class="fas fa-calendar-check me-1"></i> 成功案例
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :class="{ 'active-true': $route.path === '/community' }" to="/community">
              <i class="fas fa-comments me-1"></i> 社群交流
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :class="{ 'active-true': $route.path === '/volunteer' }" to="/volunteer">
              <i class="fas fa-hand-holding-heart me-1"></i> 志愿者招募
            </router-link>
          </li>
          <li class="nav-item dropdown" v-if="!authStore.isLoggedIn">
            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
              <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px;">
                <i class="fas fa-user" style="font-size: 14px;"></i>
              </div>
              <span>登录/注册</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end shadow border-0" style="border-radius: 8px;">
              <li>
                <router-link class="dropdown-item d-flex align-items-center" to="/login">
                  <i class="fas fa-sign-in-alt me-2 text-primary"></i> 登录
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item d-flex align-items-center" to="/register">
                  <i class="fas fa-user-plus me-2 text-primary"></i> 注册
                </router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item" v-else>
            <router-link class="nav-link d-flex align-items-center" to="/dashboard">
              <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px;">
                <i class="fas fa-user" style="font-size: 14px;"></i>
              </div>
              <span>{{ authStore.currentUser?.username }}</span>
              <span class="ms-2 role-badge" :class="'badge-' + authStore.roleClass">{{ authStore.roleName }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/store/auth'
const authStore = useAuthStore()
</script>