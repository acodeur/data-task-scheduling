<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <h1 class="logo">Task Scheduler</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="$router.push('/dags/new')">
          <el-icon><Plus /></el-icon>
          New DAG
        </el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="250px" class="app-sidebar">
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/">
            <el-icon><Monitor /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="/dags">
            <el-icon><Share /></el-icon>
            <span>DAGs</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { Monitor, Share, Plus } from '@element-plus/icons-vue'
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.logo {
  margin: 0;
  color: #409eff;
  font-size: 20px;
  font-weight: 600;
}

.app-sidebar {
  background: #f5f5f5;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>