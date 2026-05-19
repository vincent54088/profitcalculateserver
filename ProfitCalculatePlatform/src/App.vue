<template>
  <el-container style="height: 100vh">
    <el-header style="display: flex; align-items: center; border-bottom: 1px solid #eee">
      <strong style="margin-right: 24px">报表测算平台</strong>
      <el-menu
        mode="horizontal"
        class="app-top-menu"
        :default-active="active"
        router
        style="border: none; flex: 1"
      >
        <el-menu-item index="/tasks">任务管理</el-menu-item>
        <el-menu-item index="/summary">评测结果</el-menu-item>
        <el-menu-item index="/costs">成本录入</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = computed(() => {
  const p = route.path
  if (p.startsWith('/tasks')) return '/tasks'
  if (p.startsWith('/summary')) return '/summary'
  if (p.startsWith('/costs')) return '/costs'
  return p
})
</script>

<style scoped>
.app-top-menu :deep(.el-menu-item) {
  font-family: 'Microsoft YaHei', '微软雅黑', sans-serif;
  font-weight: 700;
  font-size: 15px;
}
</style>
