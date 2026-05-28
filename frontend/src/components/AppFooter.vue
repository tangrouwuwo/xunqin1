<template>
  <footer class="bg-dark text-white py-5">
    <div class="container">
      <div class="row justify-content-center text-center">
        <div class="col-md-6">
          <div class="d-flex align-items-center justify-content-center mb-3">
            <i class="fas fa-home text-danger me-2" style="font-size: 1.8rem;"></i>
            <h4 class="fw-bold mb-0">寻亲网站</h4>
          </div>
          <p class="text-secondary mb-4">让爱回家，让亲情重聚</p>
          <div class="d-flex justify-content-center gap-3 mb-4">
            <a href="#" class="text-secondary" style="font-size: 1.5rem; transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">
              <i class="fab fa-weixin"></i>
            </a>
            <a href="#" class="text-secondary" style="font-size: 1.5rem; transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">
              <i class="fab fa-weibo"></i>
            </a>
            <a href="#" class="text-secondary" style="font-size: 1.5rem; transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">
              <i class="fab fa-qq"></i>
            </a>
          </div>
        </div>
      </div>
      <div class="border-top border-secondary pt-4 mt-2 text-center">
        <p class="text-secondary mb-2">&copy; 2026 寻亲网站 版权所有</p>
        <div>
          <a href="#" class="text-secondary text-decoration-none me-4" style="transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">隐私政策</a>
          <a href="#" class="text-secondary text-decoration-none me-4" style="transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">使用条款</a>
          <a href="#" class="text-secondary text-decoration-none" style="transition: color 0.3s ease;" @mouseenter="e => e.target.style.color = '#e94c3d'" @mouseleave="e => e.target.style.color = ''">联系我们</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>