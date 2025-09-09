<template>
  <div class="job-history">
    <!-- 页面标题和操作 -->
    <PageContent :config="pageConfig" :model="pageModel">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <h2>执行历史</h2>
            <span class="history-count">共 {{ totalJobs }} 条执行记录</span>
          </div>
          <div class="header-right">
            <el-button-group>
              <el-button :icon="RefreshRight" @click="refreshData">
                刷新
              </el-button>
              <el-button :icon="Download" @click="exportHistory">
                导出
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <div class="filter-row">
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="运行中" value="running" />
            <el-option label="已取消" value="cancelled" />
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

          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px;"
          />

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

          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <!-- 执行历史表格 -->
      <div class="history-table">
        <el-table
          :data="filteredJobs"
          style="width: 100%"
          :row-class-name="getRowClassName"
          v-loading="loading"
        >
          <el-table-column prop="id" label="执行ID" width="140" fixed="left">
            <template #default="{ row }">
              <el-link type="primary" @click="viewJobDetail(row)">
                {{ row.id }}
              </el-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="dagName" label="工作流" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="goToDAG(row.dagId)">
                {{ row.dagName }}
              </el-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="startTime" label="开始时间" width="180" sortable>
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="endTime" label="结束时间" width="180" sortable>
            <template #default="{ row }">
              {{ row.endTime ? formatTime(row.endTime) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="duration" label="耗时" width="120" sortable>
            <template #default="{ row }">
              {{ row.duration ? formatDuration(row.duration) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="triggerType" label="触发方式" width="120" />
          
          <el-table-column prop="nodeId" label="执行节点" width="120" />
          
          <el-table-column prop="retryCount" label="重试次数" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.retryCount > 0" type="warning" size="small">
                {{ row.retryCount }}
              </el-tag>
              <span v-else>0</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewLogs(row)">
                日志
              </el-button>
              <el-dropdown
                trigger="click"
                @command="(cmd) => handleAction(cmd, row)"
              >
                <el-button size="small" text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="retry" :disabled="row.status === 'running'">
                      重新执行
                    </el-dropdown-item>
                    <el-dropdown-item command="clone">克隆配置</el-dropdown-item>
                    <el-dropdown-item command="export">导出详情</el-dropdown-item>
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
            :page-sizes="[20, 50, 100, 200]"
            :total="totalJobs"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </PageContent>

    <!-- 任务详情对话框 -->
    <el-drawer
      v-model="showJobDetail"
      :title="`任务详情 - ${selectedJob?.id}`"
      size="60%"
    >
      <div v-if="selectedJob" class="job-detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="执行ID">{{ selectedJob.id }}</el-descriptions-item>
            <el-descriptions-item label="工作流">{{ selectedJob.dagName }}</el-descriptions-item>
            <el-descriptions-item label="任务名称">{{ selectedJob.taskName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedJob.status)">
                {{ getStatusText(selectedJob.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatTime(selectedJob.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ selectedJob.endTime ? formatTime(selectedJob.endTime) : '未完成' }}
            </el-descriptions-item>
            <el-descriptions-item label="执行耗时">
              {{ selectedJob.duration ? formatDuration(selectedJob.duration) : '计算中' }}
            </el-descriptions-item>
            <el-descriptions-item label="触发方式">{{ selectedJob.triggerType }}</el-descriptions-item>
            <el-descriptions-item label="执行节点">{{ selectedJob.nodeId }}</el-descriptions-item>
            <el-descriptions-item label="重试次数">{{ selectedJob.retryCount }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 执行参数 -->
        <el-card class="detail-card">
          <template #header>
            <span>执行参数</span>
          </template>
          <el-table :data="selectedJob.parameters" style="width: 100%">
            <el-table-column prop="key" label="参数名" width="200" />
            <el-table-column prop="value" label="参数值" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="120" />
          </el-table>
        </el-card>

        <!-- 错误信息 -->
        <el-card v-if="selectedJob.status === 'failed'" class="detail-card">
          <template #header>
            <span>错误信息</span>
          </template>
          <div class="error-content">
            <pre>{{ selectedJob.errorMessage || '无详细错误信息' }}</pre>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button @click="viewLogs(selectedJob)">查看日志</el-button>
          <el-button type="primary" @click="retryJob(selectedJob)" :disabled="selectedJob.status === 'running'">
            重新执行
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 日志查看对话框 -->
    <el-drawer
      v-model="showLogs"
      :title="`执行日志 - ${logJob?.id}`"
      size="70%"
    >
      <div class="logs-content">
        <div class="logs-header">
          <div class="log-info">
            <span>任务: {{ logJob?.taskName }}</span>
            <span>状态: {{ logJob?.status }}</span>
            <span>节点: {{ logJob?.nodeId }}</span>
          </div>
          <el-button-group size="small">
            <el-button @click="refreshLogs">刷新</el-button>
            <el-button @click="downloadLogs">下载</el-button>
            <el-switch
              v-model="autoScrollLog"
              active-text="自动滚动"
              size="small"
            />
          </el-button-group>
        </div>
        
        <div class="logs-viewer" ref="logsViewer">
          <pre class="log-content">{{ jobLogs }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElTable, ElTableColumn, ElButton, ElButtonGroup, ElSelect,
  ElOption, ElDatePicker, ElInput, ElTag, ElLink, ElDropdown,
  ElDropdownMenu, ElDropdownItem, ElPagination, ElDrawer,
  ElCard, ElDescriptions, ElDescriptionsItem, ElSwitch
} from 'element-plus'
import {
  RefreshRight, Download, Search, MoreFilled
} from '@element-plus/icons-vue'
import { PageContent } from '@/components/page'
import type { IPageContentConfig, IPageContentModel } from '@/components/page/type'
import dayjs from 'dayjs'

const router = useRouter()

// 页面配置
const pageConfig: IPageContentConfig = {
  pageName: 'job-history',
  header: {
    title: '执行历史',
    btnTxt: '刷新'
  },
  columnList: [],
  pagination: {
    currentPage: 1,
    pageSize: 20
  }
}

const pageModel: IPageContentModel = {
  dataList: [],
  totalCount: 0
}

// 数据状态
const loading = ref(false)
const statusFilter = ref('all')
const dagFilter = ref('all')
const dateRange = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(50)
const showJobDetail = ref(false)
const showLogs = ref(false)
const selectedJob = ref(null)
const logJob = ref(null)
const jobLogs = ref('')
const autoScrollLog = ref(true)
const logsViewer = ref(null)

// DAG列表
const dagList = ref([
  { id: '1', name: '数据处理流水线' },
  { id: '2', name: '报表生成任务' },
  { id: '3', name: '系统监控检查' }
])

// 执行历史数据
const jobs = ref<any[]>([
  {
    id: 'job-20240127-001',
    dagId: '1',
    dagName: '数据处理流水线',
    taskName: '数据提取任务',
    status: 'success',
    startTime: new Date('2024-01-27 02:00:00'),
    endTime: new Date('2024-01-27 02:15:30'),
    duration: 930000,
    triggerType: '定时触发',
    nodeId: 'node-01',
    retryCount: 0,
    parameters: [
      { key: 'source_db', value: 'prod_db', type: 'string' },
      { key: 'batch_size', value: '1000', type: 'number' }
    ]
  },
  {
    id: 'job-20240127-002',
    dagId: '2',
    dagName: '报表生成任务',
    taskName: '数据聚合',
    status: 'failed',
    startTime: new Date('2024-01-27 08:00:00'),
    endTime: new Date('2024-01-27 08:05:15'),
    duration: 315000,
    triggerType: '手动触发',
    nodeId: 'node-02',
    retryCount: 2,
    errorMessage: 'Connection timeout to database server',
    parameters: [
      { key: 'report_date', value: '2024-01-26', type: 'date' },
      { key: 'format', value: 'excel', type: 'string' }
    ]
  },
  {
    id: 'job-20240127-003',
    dagId: '1',
    dagName: '数据处理流水线',
    taskName: '数据清洗',
    status: 'running',
    startTime: new Date('2024-01-27 10:30:00'),
    endTime: null,
    duration: null,
    triggerType: '定时触发',
    nodeId: 'node-03',
    retryCount: 0,
    parameters: [
      { key: 'clean_rules', value: 'standard', type: 'string' }
    ]
  }
])

// 计算属性
const totalJobs = computed(() => jobs.value.length)
const filteredJobs = computed(() => {
  let filtered = [...jobs.value]
  
  // 状态筛选
  if (statusFilter.value !== 'all') {
    filtered = filtered.filter(job => job.status === statusFilter.value)
  }
  
  // DAG筛选
  if (dagFilter.value !== 'all') {
    filtered = filtered.filter(job => job.dagId === dagFilter.value)
  }
  
  // 日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    filtered = filtered.filter(job => {
      const jobDate = dayjs(job.startTime)
      return jobDate.isAfter(startDate) && jobDate.isBefore(endDate)
    })
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(job =>
      job.taskName.toLowerCase().includes(keyword) ||
      job.dagName.toLowerCase().includes(keyword) ||
      job.id.toLowerCase().includes(keyword)
    )
  }
  
  // 按开始时间倒序排列
  filtered.sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime())
  
  return filtered
})

// 方法
const getStatusType = (status: string) => {
  const types = {
    success: 'success',
    failed: 'danger',
    running: 'primary',
    cancelled: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    success: '成功',
    failed: '失败',
    running: '运行中',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const getRowClassName = ({ row }) => {
  if (row.status === 'failed') return 'row-failed'
  if (row.status === 'running') return 'row-running'
  return ''
}

const formatTime = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatDuration = (ms: number) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}h ${minutes % 60}m ${seconds % 60}s`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`
  } else {
    return `${seconds}s`
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 刷新数据
  } finally {
    loading.value = false
  }
}

const exportHistory = () => {
  console.log('导出执行历史')
  // 导出逻辑
}

const handleSearch = () => {
  currentPage.value = 1
  // 搜索逻辑已在计算属性中实现
}

const resetFilters = () => {
  statusFilter.value = 'all'
  dagFilter.value = 'all'
  dateRange.value = []
  searchKeyword.value = ''
  currentPage.value = 1
}

const viewJobDetail = (job) => {
  selectedJob.value = job
  showJobDetail.value = true
}

const viewLogs = async (job) => {
  logJob.value = job
  showLogs.value = true
  await loadJobLogs(job.id)
}

const loadJobLogs = async (jobId: string) => {
  // 模拟加载日志
  jobLogs.value = `[${formatTime(new Date())}] 任务 ${jobId} 开始执行
[${formatTime(new Date(Date.now() - 120000))}] 初始化执行环境
[${formatTime(new Date(Date.now() - 90000))}] 加载任务配置
[${formatTime(new Date(Date.now() - 60000))}] 连接数据源
[${formatTime(new Date(Date.now() - 30000))}] 开始数据处理
[${formatTime(new Date())}] 任务执行完成`
  
  if (autoScrollLog.value) {
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (logsViewer.value) {
    logsViewer.value.scrollTop = logsViewer.value.scrollHeight
  }
}

const refreshLogs = () => {
  if (logJob.value) {
    loadJobLogs(logJob.value.id)
  }
}

const downloadLogs = () => {
  console.log('下载日志')
  // 下载日志逻辑
}

const goToDAG = (dagId: string) => {
  router.push(`/dags/${dagId}/edit`)
}

const handleAction = (command: string, job) => {
  switch (command) {
    case 'retry':
      retryJob(job)
      break
    case 'clone':
      console.log('克隆任务配置:', job.id)
      break
    case 'export':
      console.log('导出任务详情:', job.id)
      break
  }
}

const retryJob = (job) => {
  console.log('重新执行任务:', job.id)
  // 重新执行逻辑
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

onMounted(() => {
  refreshData()
})
</script>

<style lang="less" scoped>
.job-history {
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

      .history-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .filter-section {
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;

    .filter-row {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }
  }

  .history-table {
    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .job-detail-content {
    .detail-card {
      margin-bottom: 20px;
    }

    .error-content {
      background: #fef0f0;
      border: 1px solid #fcdede;
      border-radius: 4px;
      padding: 16px;

      pre {
        color: #f56c6c;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    }

    .detail-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      padding: 20px 0;
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
  .row-failed {
    background-color: #fef0f0;
  }

  .row-running {
    background-color: #f0f9ff;
  }
}
</style>