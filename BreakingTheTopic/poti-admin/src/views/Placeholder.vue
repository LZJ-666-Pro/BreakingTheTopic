<template>
  <div class="placeholder-page">
    <el-card shadow="never">
      <div class="placeholder-body">
        <div class="placeholder-icon">🚧</div>
        <div class="placeholder-title">{{ route.meta.title }} 功能开发中</div>
        <div class="placeholder-desc">该模块正在规划与开发中，敬请期待</div>
        <el-button type="primary" @click="router.push('/dashboard')">返回工作台</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.placeholder-page {
  padding: 0;
}

.placeholder-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0 60px;
}

.placeholder-icon {
  font-size: 56px;
  margin-bottom: 20px;
}

.placeholder-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.placeholder-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 28px;
}
</style>
