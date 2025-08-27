<template>
  <div class="dag-list">
    <!-- 页面标题和操作 -->
    <PageContent>
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <h2>工作流管理</h2>
            <span class="dag-count">共 {{ totalDAGs }} 个工作流</span>
          </div>
          <div class="header-right">
            <el-button
              type="primary"
              :icon="Plus"
              @click="$router.push('/dags/new')"
            >
              创建工作流
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <PageSearch
        v-model="searchForm"
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      />

      <!-- 状态筛选标签 -->
      <div class="status-tabs">
        <el-tag
          v-for="status in statusOptions"
          :key="status.value"
          :type="activeStatus === status.value ? 'primary' : 'info'"
          :effect="activeStatus === status.value ? 'dark' : 'light'"
          class="status-tag"
          @click="handleStatusChange(status.value)"
        >
          <el-icon v-if="status.icon"><component :is="status.icon" /></el-icon>
          {{ status.label }}
          <el-badge v-if="status.count" :value="status.count" :max="99" />
        </el-tag>
      </div>

      <!-- DAG列表 -->
      <div class="dag-table">
        <el-table
          :data="filteredDAGs"
          style="width: 100%"
          @row-click="handleRowClick"
        >
          <el-table-column prop="name" label="工作流名称" min-width="200">
            <template #default="{ row }">
              <div class="dag-name-cell">
                <div class="dag-status" :class="`status-${row.status}`">
                  <el-icon><component :is="getStatusIcon(row.status)" /></el-icon>
                </div>
                <div>
                  <div class="name">{{ row.name }}</div>
                  <div class="description">{{ row.description }}</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="schedule" label="调度配置" width="180">
            <template #default="{ row }">
              <div v-if="row.schedule.enabled" class="schedule-info">
                <el-icon><Timer /></el-icon>
                <span>{{ row.schedule.cron }}</span>
              </div>
              <span v-else class="no-schedule">未设置调度</span>
            </template>
          </el-table-column>

          <el-table-column prop="tasks" label="任务数量" width="100">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.taskCount }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="lastRun" label="最近运行" width="180">
            <template #default="{ row }">
              <div v-if="row.lastRun" class="last-run">
                <div class="run-time">{{ formatTime(row.lastRun.time) }}</div>
                <div class="run-result" :class="`result-${row.lastRun.status}`">
                  {{ getRunResultText(row.lastRun.status) }}
                </div>
              </div>
              <span v-else class="no-run">从未运行</span>
            </template>
          </el-table-column>

          <el-table-column prop="author" label="创建者" width="120" />

          <el-table-column prop="created" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.created) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                @click.stop="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="success"
                :disabled="row.status === 'running'"
                @click.stop="handleTrigger(row)"
              >
                立即运行
              </el-button>
              <el-dropdown
                trigger="click"
                @command="(cmd) => handleAction(cmd, row)"
                @click.stop
              >
                <el-button size="small" text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                    <el-dropdown-item command="export">导出</el-dropdown-item>
                    <el-dropdown-item command="history">运行历史</el-dropdown-item>
                    <el-dropdown-item
                      command="toggle"
                      :class="row.status === 'active' ? 'text-warning' : 'text-success'"
                    >
                      {{ row.status === 'active' ? '停用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided class="text-danger">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalDAGs"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </PageContent>

    <!-- 运行历史对话框 -->
    <el-dialog
      v-model="showHistory"
      title="运行历史"
      width="80%"
      top="5vh"
    >
      <div class="history-content">
        <!-- 运行历史表格 -->
        <el-table :data="runHistory" style="width: 100%">
          <el-table-column prop="id" label="运行ID" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="{ row }">
              {{ row.endTime ? formatTime(row.endTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="耗时" width="100">
            <template #default="{ row }">
              {{ row.duration ? formatDuration(row.duration) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getRunResultText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="trigger" label="触发方式" width="120" />
          <el-table-column prop="message" label="消息" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElButton, ElIcon, ElTable, ElTableColumn, ElTag, ElBadge,
  ElDropdown, ElDropdownMenu, ElDropdownItem, ElPagination,
  ElDialog
} from 'element-plus'
import {
  Plus, Timer, MoreFilled, VideoPlay, VideoPause, CircleCheck,
  CircleClose, Warning, Clock
} from '@element-plus/icons-vue'
import { PageContent, PageSearch } from '@/components/page'
import type { DAG, DAGFilter, RunStatus } from '@/types/dag'
import dayjs from 'dayjs'

const router = useRouter()

// 数据状态
const searchForm = ref<DAGFilter>({})
const activeStatus = ref<string>('all')
const currentPage = ref(1)
const pageSize = ref(20)
const showHistory = ref(false)
const selectedDAG = ref<DAG | null>(null)
const runHistory = ref([])

// 搜索字段配置
const searchFields = [
  {
    key: 'keyword',
    label: '关键词',
    type: 'input',
    placeholder: '搜索工作流名称、描述'
  },
  {
    key: 'author',
    label: '创建者',
    type: 'input'
  },
  {
    key: 'dateRange',
    label: '创建时间',
    type: 'daterange'
  }
]

// 状态选项
const statusOptions = ref([
  { value: 'all', label: '全部', icon: null, count: 0 },
  { value: 'active', label: '运行中', icon: VideoPlay, count: 0 },
  { value: 'paused', label: '已暂停', icon: VideoPause, count: 0 },
  { value: 'inactive', label: '未激活', icon: Clock, count: 0 }
])

// DAG数据
const dags = ref<DAG[]>([
  {
    id: '1',
    name: '数据处理流水线',
    description: '每日数据ETL处理工作流',
    status: 'active',
    schedule: {
      enabled: true,
      cron: '0 2 * * *'
    },
    taskCount: 5,
    lastRun: {
      time: new Date(Date.now() - 2 * 60 * 60 * 1000),
      status: 'success',
      duration: 120000
    },
    author: '张三',
    created: new Date('2024-01-10'),
    updated: new Date('2024-01-25')
  },
  {
    id: '2',
    name: '报表生成任务',
    description: '生成业务报表并发送邮件通知',
    status: 'active',
    schedule: {
      enabled: true,
      cron: '0 8 * * MON'
    },
    taskCount: 3,
    lastRun: {
      time: new Date(Date.now() - 12 * 60 * 60 * 1000),
      status: 'failed',
      duration: 45000
    },
    author: '李四',
    created: new Date('2024-01-05'),
    updated: new Date('2024-01-20')
  },
  {
    id: '3',
    name: '系统监控检查',
    description: '定期检查系统健康状态',
    status: 'paused',
    schedule: {
      enabled: false,
      cron: ''
    },
    taskCount: 8,
    lastRun: null,
    author: '王五',
    created: new Date('2024-01-01'),
    updated: new Date('2024-01-15')
  }
])

// 计算属性
const totalDAGs = computed(() => dags.value.length)
const filteredDAGs = computed(() => {
  let filtered = [...dags.value]

  // 状态筛选
  if (activeStatus.value !== 'all') {
    filtered = filtered.filter(dag => dag.status === activeStatus.value)
  }

  // 关键词搜索
  if (searchForm.value.keyword) {
    const keyword = searchForm.value.keyword.toLowerCase()
    filtered = filtered.filter(dag =>
      dag.name.toLowerCase().includes(keyword) ||
      dag.description?.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

// 方法
const getStatusIcon = (status: string) => {
  const icons = {
    active: VideoPlay,
    paused: VideoPause,
    running: Timer,
    inactive: Clock
  }
  return icons[status] || Clock
}

const getStatusType = (status: string) => {
  const types = {
    active: 'success',
    paused: 'warning',
    running: 'primary',
    inactive: 'info',
    success: 'success',
    failed: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    active: '运行中',
    paused: '已暂停',
    running: '执行中',
    inactive: '未激活'
  }
  return texts[status] || status
}

const getRunResultText = (status: string) => {
  const texts = {
    success: '成功',
    failed: '失败',
    running: '运行中',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const formatTime = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
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

const handleSearch = (form: DAGFilter) => {
  searchForm.value = { ...form }
}

const handleReset = () => {
  searchForm.value = {}
}

const handleStatusChange = (status: string) => {
  activeStatus.value = status
}

const handleRowClick = (row: DAG) => {
  router.push(`/dags/${row.id}/edit`)
}

const handleEdit = (dag: DAG) => {
  router.push(`/dags/${dag.id}/edit`)
}

const handleTrigger = (dag: DAG) => {
  console.log('立即运行DAG:', dag.name)
  // 这里应该调用API触发DAG运行
}

const handleAction = (command: string, dag: DAG) => {
  switch (command) {
    case 'duplicate':
      console.log('复制DAG:', dag.name)
      break
    case 'export':
      console.log('导出DAG:', dag.name)
      break
    case 'history':
      selectedDAG.value = dag
      showHistory.value = true
      loadRunHistory(dag.id)
      break
    case 'toggle':
      console.log('切换DAG状态:', dag.name)
      break
    case 'delete':
      console.log('删除DAG:', dag.name)
      break
  }
}

const loadRunHistory = async (dagId: string) => {
  // 模拟加载运行历史数据
  runHistory.value = [
    {
      id: 'run-001',
      startTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
      endTime: new Date(Date.now() - 2 * 60 * 60 * 1000 + 120000),
      duration: 120000,
      status: 'success',
      trigger: '定时触发',
      message: '执行成功'
    }
  ]
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 更新状态计数
const updateStatusCounts = () => {
  statusOptions.value.forEach(option => {
    if (option.value === 'all') {
      option.count = dags.value.length
    } else {
      option.count = dags.value.filter(dag => dag.status === option.value).length
    }
  })
}

onMounted(() => {
  updateStatusCounts()
})
</script>

<style lang="less" scoped>
.dag-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      h2 {
        margin: 0 0 4px 0;
        color: #303133;
        font-weight: 600;
      }

      .dag-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .status-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 20px;

    .status-tag {
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }

  .dag-table {
    .dag-name-cell {
      display: flex;
      align-items: center;
      gap: 12px;

      .dag-status {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: white;

        &.status-active {
          background: #67C23A;
        }

        &.status-paused {
          background: #E6A23C;
        }

        &.status-running {
          background: #409EFF;
        }

        &.status-inactive {
          background: #909399;
        }
      }

      .name {
        font-weight: 500;
        color: #303133;
        margin-bottom: 2px;
      }

      .description {
        font-size: 12px;
        color: #909399;
        line-height: 1.2;
      }
    }

    .schedule-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #606266;
    }

    .no-schedule {
      color: #C0C4CC;
      font-size: 13px;
    }

    .last-run {
      .run-time {
        font-size: 13px;
        color: #606266;
        margin-bottom: 2px;
      }

      .run-result {
        font-size: 12px;

        &.result-success {
          color: #67C23A;
        }

        &.result-failed {
          color: #F56C6C;
        }

        &.result-running {
          color: #409EFF;
        }
      }
    }

    .no-run {
      color: #C0C4CC;
      font-size: 13px;
    }

    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .history-content {
    max-height: 60vh;
    overflow-y: auto;
  }
}

:deep(.text-danger) {
  color: #F56C6C !important;
}

:deep(.text-warning) {
  color: #E6A23C !important;
}

:deep(.text-success) {
  color: #67C23A !important;
}
</style>
