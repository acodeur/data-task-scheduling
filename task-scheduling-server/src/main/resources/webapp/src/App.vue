<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <h1 class="logo">
          <el-icon class="logo-icon"><DataBoard /></el-icon>
          数据任务调度系统
        </h1>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <el-tooltip content="系统通知" placement="bottom">
            <el-badge :value="notificationCount" :max="99" :hidden="notificationCount === 0">
              <el-button circle size="large">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </el-tooltip>
          
          <el-tooltip content="系统设置" placement="bottom">
            <el-button circle size="large">
              <el-icon><Setting /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-dropdown trigger="click">
            <div class="user-profile">
              <el-avatar :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">管理员</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-container>
      <el-aside width="250px" class="app-sidebar">
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
          :collapse="isCollapsed"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <el-menu-item :index="route.path" v-if="!route.meta?.hidden">
              <el-icon v-if="route.meta?.icon">
                <component :is="getIconComponent(route.meta.icon)" />
              </el-icon>
              <span>{{ route.meta?.title || route.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
        
        <div class="sidebar-footer">
          <el-button
            text
            size="small"
            @click="toggleCollapse"
            class="collapse-btn"
          >
            <el-icon>
              <component :is="isCollapsed ? 'Expand' : 'Fold'" />
            </el-icon>
            <span v-if="!isCollapsed">收起菜单</span>
          </el-button>
        </div>
      </el-aside>

      <el-main class="app-main">
        <!-- 面包屑导航 -->
        <div class="breadcrumb-container" v-if="showBreadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="item in breadcrumbItems"
              :key="item.path"
              :to="item.path ? { path: item.path } : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <!-- 页面内容 -->
        <div class="page-container">
          <router-view />
        </div>
      </el-main>
    </el-container>

    <!-- 系统状态栏 -->
    <div class="status-bar">
      <div class="status-left">
        <span class="status-item">
          <el-icon><Connection /></el-icon>
          调度器状态: 
          <el-tag :type="schedulerStatus === 'running' ? 'success' : 'danger'" size="small">
            {{ schedulerStatus === 'running' ? '运行中' : '已停止' }}
          </el-tag>
        </span>
        <span class="status-item">
          活跃节点: {{ activeNodes }}/{{ totalNodes }}
        </span>
      </div>
      <div class="status-right">
        <span class="status-item">
          最后更新: {{ lastUpdateTime }}
        </span>
      </div>
    </div>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElContainer, ElHeader, ElAside, ElMain, ElMenu, ElMenuItem,
  ElIcon, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElAvatar, ElBadge, ElTooltip, ElTag, ElBreadcrumb, ElBreadcrumbItem
} from 'element-plus'
import {
  DataBoard, Bell, Setting, User, ArrowDown, Connection,
  Monitor, Share, Timer, Files, Expand, Fold
} from '@element-plus/icons-vue'
import { useRouter as useVueRouter } from 'vue-router'
import dayjs from 'dayjs'

const route = useRoute()
const router = useVueRouter()

// 响应式数据
const isCollapsed = ref(false)
const notificationCount = ref(3)
const schedulerStatus = ref<'running' | 'stopped'>('running')
const activeNodes = ref(8)
const totalNodes = ref(10)
const lastUpdateTime = ref(dayjs().format('HH:mm:ss'))

// 定时更新状态栏时间
setInterval(() => {
  lastUpdateTime.value = dayjs().format('HH:mm:ss')
}, 1000)

// 菜单路由
const menuRoutes = computed(() => {
  return router.options.routes || []
})

// 图标组件映射
const iconComponents = {
  Monitor,
  Share,
  Timer,
  Files,
  Setting,
  DataBoard
}

// 面包屑导航
const showBreadcrumb = computed(() => {
  return route.path !== '/' && !route.meta?.hidden
})

const breadcrumbItems = computed(() => {
  const items = []
  const pathArray = route.path.split('/').filter(Boolean)
  let currentPath = ''
  
  pathArray.forEach((segment, index) => {
    currentPath += `/${segment}`
    const routeRecord = router.resolve(currentPath)
    
    if (routeRecord.meta?.title) {
      items.push({
        title: routeRecord.meta.title,
        path: index === pathArray.length - 1 ? undefined : currentPath
      })
    }
  })
  
  return items
})

// 方法
const getIconComponent = (iconName: string) => {
  return iconComponents[iconName] || Monitor
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 监听路由变化，更新页面标题
watch(
  () => route.meta?.title,
  (title) => {
    if (title) {
      document.title = `${title} - 数据任务调度系统`
    }
  },
  { immediate: true }
)
</script>

<style lang="less" scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 1000;

  .header-left {
    .logo {
      margin: 0;
      color: #409eff;
      font-size: 20px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;

      .logo-icon {
        font-size: 24px;
      }
    }
  }

  .header-right {
    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;

      .user-profile {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        .username {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }

        .dropdown-icon {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.app-sidebar {
  background: #f5f5f5;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;

  .sidebar-menu {
    border: none;
    background: transparent;
    flex: 1;
    
    :deep(.el-menu-item) {
      margin: 4px 8px;
      border-radius: 6px;
      
      &:hover {
        background-color: #e8f4fd;
        color: #409eff;
      }
      
      &.is-active {
        background-color: #409eff;
        color: white;
        
        &::before {
          display: none;
        }
      }
    }
  }

  .sidebar-footer {
    padding: 16px;
    border-top: 1px solid #e4e7ed;
    
    .collapse-btn {
      width: 100%;
      justify-content: flex-start;
      color: #606266;
      
      &:hover {
        color: #409eff;
        background-color: #e8f4fd;
      }
    }
  }
}

.app-main {
  background: #f0f2f5;
  padding: 0;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;

  .breadcrumb-container {
    padding: 12px 20px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    
    :deep(.el-breadcrumb) {
      font-size: 14px;
      
      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          color: #606266;
          
          &:hover {
            color: #409eff;
          }
        }
        
        &:last-child .el-breadcrumb__inner {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .page-container {
    flex: 1;
    overflow: auto;
    padding: 20px;
  }
}

.status-bar {
  height: 32px;
  background: #303133;
  color: #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  font-size: 12px;
  border-top: 1px solid #909399;

  .status-left,
  .status-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .status-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-container {
    .app-header {
      padding: 0 12px;
      
      .logo {
        font-size: 16px;
      }
      
      .header-actions {
        gap: 8px;
      }
    }
    
    .app-sidebar {
      width: 200px !important;
    }
    
    .app-main {
      .page-container {
        padding: 12px;
      }
    }
  }
}

// 深色模式支持
@media (prefers-color-scheme: dark) {
  .app-header {
    background: #1d1e1f;
    border-bottom-color: #414243;
    color: #e4e7ed;
  }
  
  .app-sidebar {
    background: #2d2f31;
    border-right-color: #414243;
  }
  
  .app-main {
    background: #18181c;
  }
}
</style>