<template>
  <!-- 页面切换顶部加载进度条 -->
  <div class="page-progress" :class="{ done: !progress.visible && progress.percent === 100 }" v-show="progress.visible">
    <div class="page-progress-bar" :style="{ width: progress.percent + '%' }"></div>
  </div>
  <router-view />
</template>

<script setup>
import { progress } from './utils/pageProgress'
</script>

<style>
.page-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 9999;
  pointer-events: none;
}

.page-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.7);
  transition: width 0.2s ease-out;
}
</style>
