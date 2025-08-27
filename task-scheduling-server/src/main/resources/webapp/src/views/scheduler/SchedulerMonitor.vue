<template>
  <div class="scheduler-monitor">
    <!-- 页面标题和操作 -->
    <PageContent>
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <h2>调度器监控</h2>
            <span class="status-info">
              调度器状态:
              <el-tag :type="schedulerStatus === 'running' ? 'success' : 'danger'">
                {{ schedulerStatus === 'running' ? '运行中' : '已停止' }}
              </el-tag>
            </span>
          </div>
          <div class="header-right">
            <el-button-group>
              <el-button
                :type="schedulerStatus === 'running' ? 'warning' : 'success'"
                :icon="schedulerStatus === 'running' ? VideoPause : Play"
                @click="toggleScheduler"
              >
                {{ schedulerStatus === 'running' ? '停止调度器' : '启动调度器' }}
              </el-button>
              <el-button
                type="primary"
                :icon="RefreshRight"
                @click="refreshData"
                :loading="loading"
              >
                刷新
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 实时统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon running">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.runningJobs }}</div>
              <div class="stat-label">运行中任务</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.pendingJobs }}</div>
              <div class="stat-label">等待执行</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.successJobs }}</div>
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
              <div class="stat-number">{{ stats.failedJobs }}</div>
              <div class="stat-label">今日失败</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <div class="filter-left">
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="运行中" value="running" />
            <el-option label="等待中" value="pending" />
            <el-option label="已完成" value="completed" />
            <el-option label="已失败" value="failed" />
          </el-select>

          <el-select v-model="dagFilter" placeholder="工作流筛选" style="width: 200px;">
            <el-option label="全部工作流" value="all" />
            <el-option
              v-for="dag in dagList"
              :key="dag.id"
              :label="dag.name"
              :value="dag.id"
            />
          </el-select>

          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务名称"
            style="width: 200px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="filter-right">
          <el-switch
            v-model="autoRefresh"
            active-text="自动刷新"
            @change="handleAutoRefreshChange"
          />
        </div>
      </div>

      <!-- 运行中的任务表格 -->
      <div class="running-jobs-section">
        <h3>运行中的任务</h3>
        <el-table
          :data="filteredRunningJobs"
          style="width: 100%"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="id" label="任务ID" width="120" />

          <el-table-column prop="dagName" label="工作流" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="goToDAG(row.dagId)">
                {{ row.dagName }}
              </el-link>
            </template>
          </el-table-column>

          <el-table-column prop="taskName" label="任务名称" min-width="200" />

          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="progress" label="进度" width="150">
            <template #default="{ row }">
              <el-progress
                :percentage="row.progress || 0"
                :status="getProgressStatus(row.status)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>

          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>

          <el-table-column prop="duration" label="运行时长" width="120">
            <template #default="{ row }">
              {{ formatDuration(row.duration) }}
            </template>
          </el-table-column>

          <el-table-column prop="nodeId" label="执行节点" width="120" />

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewLogs(row)">
                查看日志
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="cancelJob(row)"
                :disabled="row.status !== 'running'"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 调度队列 -->
      <div class="scheduler-queue-section">
        <h3>调度队列</h3>
        <el-table :data="schedulerQueue" style="width: 100%">
          <el-table-column prop="dagName" label="工作流" min-width="150" />
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="{ row }">
              <el-tag
                :type="getPriorityType(row.priority)"
                size="small"
              >
                {{ row.priority }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scheduledTime" label="计划执行时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.scheduledTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="triggerType" label="触发类型" width="120" />
          <el-table-column prop="retryCount" label="重试次数" width="100" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeFromQueue(row)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 最近完成的任务 -->
      <div class="recent-jobs-section">
        <div class="section-header">
          <h3>最近完成的任务</h3>
          <el-button size="small" @click="viewAllHistory">查看全部</el-button>
        </div>

        <el-table :data="recentJobs" style="width: 100%">
          <el-table-column prop="dagName" label="工作流" min-width="150" />
          <el-table-column prop="taskName" label="任务名称" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="耗时" width="120">
            <template #default="{ row }">
              {{ formatDuration(row.duration) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewLogs(row)">
                日志
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </PageContent>

    <!-- 日志查看对话框 -->
    <el-drawer
      v-model="showLogs"
      :title="`任务日志 - ${selectedJob?.taskName}`"
      size="60%"
    >
      <div class="logs-content">
        <div class="logs-header">
          <div class="log-info">
            <span>任务ID: {{ selectedJob?.id }}</span>
            <span>状态: {{ selectedJob?.status }}</span>
            <span>节点: {{ selectedJob?.nodeId }}</span>
          </div>
          <el-button-group size="small">
            <el-button @click="refreshLogs">刷新日志</el-button>
            <el-button @click="downloadLogs">下载日志</el-button>
            <el-button @click="clearLogs">清空显示</el-button>
          </el-button-group>
        </div>

        <div class="logs-viewer">
          <pre class="log-content">{{ jobLogs }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard, ElIcon, ElButton, ElButtonGroup, ElSelect, ElOption,
  ElInput, ElSwitch, ElTable, ElTableColumn, ElTag, ElProgress,
  ElLink, ElDrawer
} from 'element-plus'
import {
  Timer, Clock, CircleCheck, CircleClose, VideoPlay, VideoPause,
  RefreshRight, Search
} from '@element-plus/icons-vue'
import { PageContent } from '@/components/page'
import dayjs from 'dayjs'

const router = useRouter()

// 数据状态
const loading = ref(false)
const schedulerStatus = ref<'running' | 'stopped'>('running')
const autoRefresh = ref(false)
const statusFilter = ref('all')
const dagFilter = ref('all')
const searchKeyword = ref('')
const showLogs = ref(false)
const selectedJob = ref(null)
const jobLogs = ref('')

// 统计数据
const stats = reactive({
  runningJobs: 8,
  pendingJobs: 15,
  successJobs: 142,
  failedJobs: 3
})

// DAG列表
const dagList = ref([
  { id: '1', name: '数据处理流水线' },
  { id: '2', name: '报表生成任务' },
  { id: '3', name: '系统监控检查' }
])

// 运行中的任务
const runningJobs = ref([
  {
    id: 'job-001',
    dagId: '1',
    dagName: '数据处理流水线',
    taskName: '数据提取任务',
    status: 'running',
    progress: 65,
    startTime: new Date(Date.now() - 10 * 60 * 1000),
    duration: 600000,
    nodeId: 'node-01'
  },
  {
    id: 'job-002',
    dagId: '2',
    dagName: '报表生成任务',
    taskName: '数据聚合',
    status: 'running',
    progress: 30,
    startTime: new Date(Date.now() - 5 * 60 * 1000),
    duration: 300000,
    nodeId: 'node-02'
  }
])

// 调度队列
const schedulerQueue = ref([
  {
    dagId: '1',
    dagName: '数据处理流水线',
    priority: 'high',
    scheduledTime: new Date(Date.now() + 30 * 60 * 1000),
    triggerType: '定时触发',
    retryCount: 0
  },
  {
    dagId: '3',
    dagName: '系统监控检查',
    priority: 'normal',
    scheduledTime: new Date(Date.now() + 60 * 60 * 1000),
    triggerType: '手动触发',
    retryCount: 1
  }
])

// 最近完成的任务
const recentJobs = ref([
  {
    id: 'job-100',
    dagName: '数据处理流水线',
    taskName: '数据清洗',
    status: 'success',
    startTime: new Date(Date.now() - 60 * 60 * 1000),
    endTime: new Date(Date.now() - 50 * 60 * 1000),
    duration: 600000,
    nodeId: 'node-01'
  },
  {
    id: 'job-101',
    dagName: '报表生成任务',
    taskName: '邮件发送',
    status: 'failed',
    startTime: new Date(Date.now() - 120 * 60 * 1000),
    endTime: new Date(Date.now() - 115 * 60 * 1000),
    duration: 300000,
    nodeId: 'node-03'
  }
])

// 计算属性
const filteredRunningJobs = computed(() => {
  let filtered = [...runningJobs.value]

  if (statusFilter.value !== 'all') {
    filtered = filtered.filter(job => job.status === statusFilter.value)
  }

  if (dagFilter.value !== 'all') {
    filtered = filtered.filter(job => job.dagId === dagFilter.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(job =>
      job.taskName.toLowerCase().includes(keyword) ||
      job.dagName.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

// 自动刷新定时器
let refreshTimer: NodeJS.Timeout | null = null

// 方法
const getStatusType = (status: string) => {
  const types = {
    running: 'primary',
    pending: 'info',
    success: 'success',
    failed: 'danger',
    cancelled: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    running: '运行中',
    pending: '等待中',
    success: '成功',
    failed: '失败',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const getProgressStatus = (status: string) => {
  if (status === 'failed') return 'exception'
  if (status === 'success') return 'success'
  return undefined
}

const getPriorityType = (priority: string) => {
  const types = {
    high: 'danger',
    normal: 'primary',
    low: 'info'
  }
  return types[priority] || 'info'
}

const getRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

const formatTime = (date: Date) => {
  return dayjs(date).format('MM-DD HH:mm:ss')
}

const formatDuration = (ms: number) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)

  if (hours > 0) {
    return `${hours}h ${minutes % 60}m`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`
  } else {
    return `${seconds}s`
  }
}

const toggleScheduler = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    schedulerStatus.value = schedulerStatus.value === 'running' ? 'stopped' : 'running'
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    // 模拟数据刷新
    await new Promise(resolve => setTimeout(resolve, 500))
    // 更新统计数据和任务列表
  } finally {
    loading.value = false
  }
}

const handleAutoRefreshChange = (enabled: boolean) => {
  if (enabled) {
    refreshTimer = setInterval(() => {
      refreshData()
    }, 30000) // 30秒刷新一次
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
}

const goToDAG = (dagId: string) => {
  router.push(`/dags/${dagId}/edit`)
}

const viewLogs = (job: any) => {
  selectedJob.value = job
  showLogs.value = true
  loadJobLogs(job.id)
}

const loadJobLogs = async (jobId: string) => {
  // 模拟加载日志
  jobLogs.value = `[${formatTime(new Date())}] 任务开始执行
[${formatTime(new Date(Date.now() - 60000))}] 初始化参数
[${formatTime(new Date(Date.now() - 30000))}] 连接数据源
[${formatTime(new Date())}] 执行中...`
}

const refreshLogs = () => {
  if (selectedJob.value) {
    loadJobLogs(selectedJob.value.id)
  }
}

const downloadLogs = () => {
  // 下载日志逻辑
  console.log('下载日志')
}

const clearLogs = () => {
  jobLogs.value = ''
}

const cancelJob = async (job: any) => {
  console.log('取消任务:', job.id)
  // 取消任务逻辑
}

const removeFromQueue = (item: any) => {
  const index = schedulerQueue.value.findIndex(q => q === item)
  if (index > -1) {
    schedulerQueue.value.splice(index, 1)
  }
}

const viewAllHistory = () => {
  router.push('/jobs/history')
}

onMounted(() => {
  refreshData()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="less" scoped>
.scheduler-monitor {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-weight: 600;
      }

      .status-info {
        color: #909399;
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 8px;
      }
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

          &.running {
            background: linear-gradient(45deg, #409EFF, #66B1FF);
          }

          &.pending {
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

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;

    .filter-left {
      display: flex;
      gap: 12px;
    }
  }

  .running-jobs-section,
  .scheduler-queue-section,
  .recent-jobs-section {
    margin-bottom: 30px;

    h3 {
      margin: 0 0 16px 0;
      color: #303133;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;

      &::before {
        content: '';
        width: 4px;
        height: 20px;
        background: #409EFF;
        border-radius: 2px;
      }
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
      }
    }
  }

  .logs-content {
    height: 100%;
    display: flex;
    flex-direction: column;

    .logs-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #EBEEF5;

      .log-info {
        display: flex;
        gap: 16px;
        font-size: 14px;
        color: #606266;
      }
    }

    .logs-viewer {
      flex: 1;
      overflow-y: auto;
      padding: 16px;

      .log-content {
        background: #1e1e1e;
        color: #d4d4d4;
        padding: 16px;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.5;
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    }
  }
}

:deep(.el-table) {
  .even-row {
    background-color: #fafafa;
  }

  .odd-row {
    background-color: #ffffff;
  }
}
</style>
