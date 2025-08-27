<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="dashboard-header">
      <h2>任务调度监控台</h2>
      <span class="update-time">最后更新：{{ lastUpdateTime }}</span>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon active">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.activeDAGs }}</div>
            <div class="stat-label">活跃工作流</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon running">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.runningTasks }}</div>
            <div class="stat-label">运行中任务</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon success">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.todaySuccess }}</div>
            <div class="stat-label">今日成功</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon error">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.todayFailed }}</div>
            <div class="stat-label">今日失败</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 执行状态统计 -->
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>执行状态统计</span>
              <el-select v-model="statusPeriod" size="small" style="width: 120px">
                <el-option label="今日" value="today" />
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
              </el-select>
            </div>
          </template>
          <PieEchart
            :data="executionStatusData"
            :options="statusChartOptions"
            height="300px"
          />
        </el-card>

        <!-- 任务执行趋势 -->
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>任务执行趋势</span>
              <el-select v-model="trendPeriod" size="small" style="width: 120px">
                <el-option label="7天" value="7d" />
                <el-option label="30天" value="30d" />
                <el-option label="90天" value="90d" />
              </el-select>
            </div>
          </template>
          <LineEchart
            :data="executionTrendData"
            :options="trendChartOptions"
            height="300px"
          />
        </el-card>
      </div>

      <div class="chart-row">
        <!-- 资源使用情况 -->
        <el-card class="chart-card">
          <template #header>
            <span>服务器资源使用</span>
          </template>
          <BarEchart
            :data="resourceUsageData"
            :options="resourceChartOptions"
            height="280px"
          />
        </el-card>

        <!-- 热门任务排行 -->
        <el-card class="chart-card">
          <template #header>
            <span>热门任务排行</span>
          </template>
          <div class="popular-tasks">
            <div
              v-for="(task, index) in popularTasks"
              :key="task.id"
              class="task-rank-item"
            >
              <div class="rank-number" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <div class="task-info">
                <div class="task-name">{{ task.name }}</div>
                <div class="task-count">{{ task.executionCount }} 次执行</div>
              </div>
              <div class="success-rate" :class="getSuccessRateClass(task.successRate)">
                {{ (task.successRate * 100).toFixed(1) }}%
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最近活动 -->
    <el-card class="activity-card">
      <template #header>
        <span>最近活动</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="activity in recentActivities"
          :key="activity.id"
          :timestamp="formatTime(activity.timestamp)"
          :color="getActivityColor(activity.type)"
        >
          <div class="activity-content">
            <el-icon class="activity-icon">
              <component :is="getActivityIcon(activity.type)" />
            </el-icon>
            <span>{{ activity.message }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElCard, ElIcon, ElSelect, ElOption, ElTimeline, ElTimelineItem } from 'element-plus'
import {
  DataAnalysis,
  Timer,
  CircleCheck,
  CircleClose,
  VideoPlay,
  Warning,
  SuccessFilled
} from '@element-plus/icons-vue'
import { PieEchart, LineEchart, BarEchart } from '@/components/echarts'
import dayjs from 'dayjs'

// 数据状态
const lastUpdateTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
const statusPeriod = ref('today')
const trendPeriod = ref('7d')

// 仪表盘统计数据
const dashboardStats = reactive({
  activeDAGs: 12,
  runningTasks: 8,
  todaySuccess: 156,
  todayFailed: 3
})

// 执行状态数据
const executionStatusData = computed(() => [
  { name: '成功', value: dashboardStats.todaySuccess, itemStyle: { color: '#67C23A' } },
  { name: '失败', value: dashboardStats.todayFailed, itemStyle: { color: '#F56C6C' } },
  { name: '运行中', value: dashboardStats.runningTasks, itemStyle: { color: '#409EFF' } }
])

// 图表配置
const statusChartOptions = {
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  }
}

// 执行趋势数据
const executionTrendData = reactive({
  xAxis: ['01-21', '01-22', '01-23', '01-24', '01-25', '01-26', '01-27'],
  series: [
    {
      name: '成功',
      data: [120, 132, 101, 134, 90, 230, 210],
      type: 'line',
      itemStyle: { color: '#67C23A' }
    },
    {
      name: '失败',
      data: [2, 5, 3, 8, 2, 12, 6],
      type: 'line',
      itemStyle: { color: '#F56C6C' }
    }
  ]
})

const trendChartOptions = {
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['成功', '失败']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  }
}

// 资源使用数据
const resourceUsageData = reactive({
  xAxis: ['节点1', '节点2', '节点3', '节点4', '节点5'],
  series: [
    {
      name: 'CPU使用率',
      data: [65, 78, 45, 89, 56],
      type: 'bar',
      itemStyle: { color: '#409EFF' }
    },
    {
      name: '内存使用率',
      data: [72, 85, 52, 92, 68],
      type: 'bar',
      itemStyle: { color: '#67C23A' }
    }
  ]
})

const resourceChartOptions = {
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['CPU使用率', '内存使用率']
  },
  xAxis: {
    type: 'category',
    axisLabel: {
      rotate: 45
    }
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  }
}

// 热门任务数据
const popularTasks = reactive([
  { id: '1', name: '数据同步任务', executionCount: 1250, successRate: 0.98 },
  { id: '2', name: '报表生成任务', executionCount: 980, successRate: 0.95 },
  { id: '3', name: '数据清洗任务', executionCount: 756, successRate: 0.92 },
  { id: '4', name: '邮件发送任务', executionCount: 642, successRate: 0.97 },
  { id: '5', name: '备份任务', executionCount: 543, successRate: 0.99 }
])

// 最近活动数据
const recentActivities = reactive([
  {
    id: '1',
    type: 'success',
    message: '数据同步任务执行成功',
    timestamp: new Date(Date.now() - 5 * 60 * 1000)
  },
  {
    id: '2',
    type: 'start',
    message: '报表生成任务开始执行',
    timestamp: new Date(Date.now() - 12 * 60 * 1000)
  },
  {
    id: '3',
    type: 'error',
    message: '数据清洗任务执行失败',
    timestamp: new Date(Date.now() - 25 * 60 * 1000)
  },
  {
    id: '4',
    type: 'success',
    message: 'DAG工作流部署成功',
    timestamp: new Date(Date.now() - 45 * 60 * 1000)
  }
])

// 工具方法
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return 'rank-other'
}

const getSuccessRateClass = (rate: number) => {
  if (rate >= 0.95) return 'success-high'
  if (rate >= 0.85) return 'success-medium'
  return 'success-low'
}

const getActivityColor = (type: string) => {
  const colors: Record<string, string> = {
    success: '#67C23A',
    error: '#F56C6C',
    start: '#409EFF',
    warning: '#E6A23C'
  }
  return colors[type] || '#909399'
}

const getActivityIcon = (type: string) => {
  const icons: Record<string, any> = {
    success: SuccessFilled,
    error: CircleClose,
    start: VideoPlay,
    warning: Warning
  }
  return icons[type] || Timer
}

const formatTime = (timestamp: Date) => {
  return dayjs(timestamp).format('MM-DD HH:mm')
}

// 定时刷新数据
let refreshTimer: NodeJS.Timeout | null = null

const refreshData = async () => {
  try {
    // 这里应该调用API获取最新数据
    // const stats = await api.getDashboardStats()
    // Object.assign(dashboardStats, stats)

    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } catch (error) {
    console.error('刷新数据失败:', error)
  }
}

onMounted(() => {
  refreshData()
  // 每30秒刷新一次数据
  refreshTimer = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="less" scoped>
.dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
      font-weight: 600;
    }

    .update-time {
      color: #909399;
      font-size: 14px;
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      .stat-item {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: white;

          &.active {
            background: linear-gradient(45deg, #409EFF, #66B1FF);
          }

          &.running {
            background: linear-gradient(45deg, #E6A23C, #F0C78A);
          }

          &.success {
            background: linear-gradient(45deg, #67C23A, #95D475);
          }

          &.error {
            background: linear-gradient(45deg, #F56C6C, #F78989);
          }
        }

        .stat-info {
          .stat-number {
            font-size: 32px;
            font-weight: 600;
            color: #303133;
            line-height: 1;
            margin-bottom: 4px;
          }

          .stat-label {
            color: #909399;
            font-size: 14px;
          }
        }
      }
    }
  }

  .charts-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin-bottom: 20px;

    .chart-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 20px;
    }

    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }

  .popular-tasks {
    .task-rank-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;

      &:last-child {
        border-bottom: none;
      }

      .rank-number {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 12px;
        color: white;

        &.rank-first {
          background: #FFD700;
        }

        &.rank-second {
          background: #C0C0C0;
        }

        &.rank-third {
          background: #CD7F32;
        }

        &.rank-other {
          background: #909399;
        }
      }

      .task-info {
        flex: 1;

        .task-name {
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }

        .task-count {
          font-size: 12px;
          color: #909399;
        }
      }

      .success-rate {
        font-weight: 600;
        font-size: 14px;

        &.success-high {
          color: #67C23A;
        }

        &.success-medium {
          color: #E6A23C;
        }

        &.success-low {
          color: #F56C6C;
        }
      }
    }
  }

  .activity-card {
    .activity-content {
      display: flex;
      align-items: center;
      gap: 8px;

      .activity-icon {
        font-size: 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;

    .stats-cards {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .chart-row {
      grid-template-columns: 1fr;
    }
  }
}
</style>
