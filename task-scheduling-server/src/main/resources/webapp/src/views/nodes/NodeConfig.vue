<template>
  <div class="node-config">
    <!-- 页面标题和操作 -->
    <PageContent>
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <h2>服务器节点管理</h2>
            <span class="node-count">共 {{ totalNodes }} 个节点</span>
          </div>
          <div class="header-right">
            <el-button-group>
              <el-button type="success" :icon="RefreshRight" @click="refreshNodes">
                刷新状态
              </el-button>
              <el-button type="primary" :icon="Plus" @click="showAddDialog = true">
                添加节点
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 集群概览 -->
      <div class="cluster-overview">
        <el-card>
          <template #header>
            <span>集群概览</span>
          </template>
          <div class="overview-stats">
            <div class="stat-item">
              <div class="stat-icon online">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ clusterStats.onlineNodes }}</div>
                <div class="stat-label">在线节点</div>
              </div>
            </div>

            <div class="stat-item">
              <div class="stat-icon offline">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ clusterStats.offlineNodes }}</div>
                <div class="stat-label">离线节点</div>
              </div>
            </div>

            <div class="stat-item">
              <div class="stat-icon busy">
                <el-icon><Loading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ clusterStats.busyNodes }}</div>
                <div class="stat-label">繁忙节点</div>
              </div>
            </div>

            <div class="stat-item">
              <div class="stat-icon capacity">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ clusterStats.totalCapacity }}</div>
                <div class="stat-label">总计算能力</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 节点筛选 -->
      <div class="node-filters">
        <div class="filter-left">
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="维护中" value="maintenance" />
          </el-select>

          <el-select v-model="typeFilter" placeholder="节点类型" style="width: 140px;">
            <el-option label="全部类型" value="all" />
            <el-option label="计算节点" value="compute" />
            <el-option label="存储节点" value="storage" />
            <el-option label="混合节点" value="hybrid" />
          </el-select>

          <el-input
            v-model="searchKeyword"
            placeholder="搜索节点名称或IP"
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
            v-model="showHealthOnly"
            active-text="只显示健康节点"
          />
        </div>
      </div>

      <!-- 节点列表 -->
      <div class="nodes-grid">
        <div
          v-for="node in filteredNodes"
          :key="node.id"
          class="node-card"
          :class="{ 'node-offline': node.status === 'offline' }"
        >
          <el-card>
            <template #header>
              <div class="card-header">
                <div class="node-info">
                  <div class="node-status" :class="`status-${node.status}`">
                    <el-icon><component :is="getStatusIcon(node.status)" /></el-icon>
                  </div>
                  <div class="node-title">
                    <h4>{{ node.name }}</h4>
                    <span class="node-type">{{ getNodeTypeText(node.type) }}</span>
                  </div>
                </div>
                <el-dropdown trigger="click" @command="(cmd) => handleNodeAction(cmd, node)">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑配置</el-dropdown-item>
                      <el-dropdown-item command="logs">查看日志</el-dropdown-item>
                      <el-dropdown-item command="monitor">性能监控</el-dropdown-item>
                      <el-dropdown-item
                        command="toggle"
                        :class="node.status === 'online' ? 'text-warning' : 'text-success'"
                      >
                        {{ node.status === 'online' ? '下线节点' : '上线节点' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided class="text-danger">
                        移除节点
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>

            <div class="node-details">
              <div class="detail-item">
                <span class="label">IP地址:</span>
                <span class="value">{{ node.ip }}</span>
              </div>
              <div class="detail-item">
                <span class="label">端口:</span>
                <span class="value">{{ node.port }}</span>
              </div>
              <div class="detail-item">
                <span class="label">运行任务:</span>
                <span class="value">{{ node.runningTasks }} / {{ node.maxTasks }}</span>
              </div>
              <div class="detail-item">
                <span class="label">最后心跳:</span>
                <span class="value">{{ formatTime(node.lastHeartbeat) }}</span>
              </div>
            </div>

            <div class="resource-usage">
              <div class="usage-item">
                <div class="usage-header">
                  <span>CPU使用率</span>
                  <span>{{ node.resources.cpuUsage }}%</span>
                </div>
                <el-progress
                  :percentage="node.resources.cpuUsage"
                  :color="getUsageColor(node.resources.cpuUsage)"
                  :stroke-width="6"
                />
              </div>

              <div class="usage-item">
                <div class="usage-header">
                  <span>内存使用率</span>
                  <span>{{ node.resources.memoryUsage }}%</span>
                </div>
                <el-progress
                  :percentage="node.resources.memoryUsage"
                  :color="getUsageColor(node.resources.memoryUsage)"
                  :stroke-width="6"
                />
              </div>

              <div class="usage-item">
                <div class="usage-header">
                  <span>磁盘使用率</span>
                  <span>{{ node.resources.diskUsage }}%</span>
                </div>
                <el-progress
                  :percentage="node.resources.diskUsage"
                  :color="getUsageColor(node.resources.diskUsage)"
                  :stroke-width="6"
                />
              </div>
            </div>

            <div class="node-actions">
              <el-button size="small" @click="viewNodeDetails(node)">
                详细信息
              </el-button>
              <el-button
                size="small"
                type="primary"
                :disabled="node.status !== 'online'"
                @click="testConnection(node)"
              >
                连接测试
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </PageContent>

    <!-- 添加节点对话框 -->
    <PageDialog
      v-model="showAddDialog"
      title="添加服务器节点"
      width="600px"
      @confirm="handleAddNode"
    >
      <DynamicForm
        v-model="nodeForm"
        :fields="nodeFormFields"
        :rules="nodeFormRules"
      />
    </PageDialog>

    <!-- 节点详情对话框 -->
    <el-drawer
      v-model="showNodeDetails"
      :title="`节点详情 - ${selectedNode?.name}`"
      size="60%"
    >
      <div v-if="selectedNode" class="node-details-content">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="节点名称">{{ selectedNode.name }}</el-descriptions-item>
            <el-descriptions-item label="节点ID">{{ selectedNode.id }}</el-descriptions-item>
            <el-descriptions-item label="IP地址">{{ selectedNode.ip }}</el-descriptions-item>
            <el-descriptions-item label="端口">{{ selectedNode.port }}</el-descriptions-item>
            <el-descriptions-item label="节点类型">{{ getNodeTypeText(selectedNode.type) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedNode.status)">
                {{ getStatusText(selectedNode.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ selectedNode.system.os }}</el-descriptions-item>
            <el-descriptions-item label="架构">{{ selectedNode.system.arch }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ formatTime(selectedNode.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="最后心跳">{{ formatTime(selectedNode.lastHeartbeat) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 资源规格 -->
        <el-card class="detail-card">
          <template #header>
            <span>资源规格</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="CPU核心数">{{ selectedNode.system.cpuCores }}</el-descriptions-item>
            <el-descriptions-item label="CPU型号">{{ selectedNode.system.cpuModel }}</el-descriptions-item>
            <el-descriptions-item label="总内存">{{ formatBytes(selectedNode.system.totalMemory) }}</el-descriptions-item>
            <el-descriptions-item label="总磁盘">{{ formatBytes(selectedNode.system.totalDisk) }}</el-descriptions-item>
            <el-descriptions-item label="最大任务数">{{ selectedNode.maxTasks }}</el-descriptions-item>
            <el-descriptions-item label="当前任务数">{{ selectedNode.runningTasks }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 性能监控图表 -->
        <el-card class="detail-card">
          <template #header>
            <span>性能监控</span>
          </template>
          <div class="monitor-charts">
            <LineEchart
              :data="nodeMonitorData"
              :options="monitorChartOptions"
              height="300px"
            />
          </div>
        </el-card>

        <!-- 运行中的任务 -->
        <el-card class="detail-card">
          <template #header>
            <span>运行中的任务</span>
          </template>
          <el-table :data="nodeRunningTasks" style="width: 100%">
            <el-table-column prop="taskId" label="任务ID" width="120" />
            <el-table-column prop="dagName" label="工作流" width="150" />
            <el-table-column prop="taskName" label="任务名称" min-width="200" />
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="100">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :stroke-width="6" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-drawer>

    <!-- 性能监控对话框 -->
    <el-dialog
      v-model="showMonitor"
      :title="`性能监控 - ${monitorNode?.name}`"
      width="90%"
      top="5vh"
    >
      <div class="monitor-content">
        <div class="monitor-charts-grid">
          <el-card>
            <template #header>CPU使用率</template>
            <LineEchart :data="cpuMonitorData" height="200px" />
          </el-card>
          <el-card>
            <template #header>内存使用率</template>
            <LineEchart :data="memoryMonitorData" height="200px" />
          </el-card>
          <el-card>
            <template #header>网络I/O</template>
            <LineEchart :data="networkMonitorData" height="200px" />
          </el-card>
          <el-card>
            <template #header>磁盘I/O</template>
            <LineEchart :data="diskMonitorData" height="200px" />
          </el-card>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElCard, ElIcon, ElButton, ElButtonGroup, ElSelect, ElOption,
  ElInput, ElSwitch, ElProgress, ElDropdown, ElDropdownMenu,
  ElDropdownItem, ElDrawer, ElDescriptions, ElDescriptionsItem,
  ElTable, ElTableColumn, ElTag, ElDialog
} from 'element-plus'
import {
  Plus, RefreshRight, Search, CircleCheck, CircleClose,
  Loading, Monitor, MoreFilled, Connection, Warning
} from '@element-plus/icons-vue'
import { PageContent, PageDialog } from '@/components/page'
import { DynamicForm } from '@/components/form'
import { LineEchart } from '@/components/echarts'
import dayjs from 'dayjs'

// 数据状态
const showAddDialog = ref(false)
const showNodeDetails = ref(false)
const showMonitor = ref(false)
const selectedNode = ref(null)
const monitorNode = ref(null)
const statusFilter = ref('all')
const typeFilter = ref('all')
const searchKeyword = ref('')
const showHealthOnly = ref(false)

// 集群统计
const clusterStats = reactive({
  onlineNodes: 8,
  offlineNodes: 2,
  busyNodes: 3,
  totalCapacity: 156
})

// 节点数据
const nodes = ref([
  {
    id: 'node-001',
    name: 'worker-node-01',
    type: 'compute',
    status: 'online',
    ip: '192.168.1.101',
    port: 8080,
    runningTasks: 3,
    maxTasks: 10,
    lastHeartbeat: new Date(Date.now() - 30000),
    startTime: new Date(Date.now() - 24 * 60 * 60 * 1000),
    resources: {
      cpuUsage: 45,
      memoryUsage: 68,
      diskUsage: 32
    },
    system: {
      os: 'Ubuntu 20.04',
      arch: 'x86_64',
      cpuCores: 8,
      cpuModel: 'Intel Xeon E5-2680',
      totalMemory: 32 * 1024 * 1024 * 1024,
      totalDisk: 500 * 1024 * 1024 * 1024
    }
  },
  {
    id: 'node-002',
    name: 'worker-node-02',
    type: 'hybrid',
    status: 'online',
    ip: '192.168.1.102',
    port: 8080,
    runningTasks: 7,
    maxTasks: 12,
    lastHeartbeat: new Date(Date.now() - 15000),
    startTime: new Date(Date.now() - 12 * 60 * 60 * 1000),
    resources: {
      cpuUsage: 78,
      memoryUsage: 82,
      diskUsage: 45
    },
    system: {
      os: 'CentOS 7',
      arch: 'x86_64',
      cpuCores: 12,
      cpuModel: 'AMD EPYC 7742',
      totalMemory: 64 * 1024 * 1024 * 1024,
      totalDisk: 1024 * 1024 * 1024 * 1024
    }
  },
  {
    id: 'node-003',
    name: 'worker-node-03',
    type: 'storage',
    status: 'offline',
    ip: '192.168.1.103',
    port: 8080,
    runningTasks: 0,
    maxTasks: 8,
    lastHeartbeat: new Date(Date.now() - 5 * 60 * 1000),
    startTime: new Date(Date.now() - 48 * 60 * 60 * 1000),
    resources: {
      cpuUsage: 0,
      memoryUsage: 0,
      diskUsage: 67
    },
    system: {
      os: 'Ubuntu 22.04',
      arch: 'x86_64',
      cpuCores: 6,
      cpuModel: 'Intel Core i7-10700',
      totalMemory: 16 * 1024 * 1024 * 1024,
      totalDisk: 2048 * 1024 * 1024 * 1024
    }
  }
])

// 节点表单配置
const nodeForm = ref({
  name: '',
  ip: '',
  port: 8080,
  type: 'compute',
  maxTasks: 10,
  description: ''
})

const nodeFormFields = [
  {
    key: 'name',
    label: '节点名称',
    type: 'input',
    required: true,
    placeholder: '请输入节点名称'
  },
  {
    key: 'ip',
    label: 'IP地址',
    type: 'input',
    required: true,
    placeholder: '请输入IP地址'
  },
  {
    key: 'port',
    label: '端口',
    type: 'number',
    required: true,
    min: 1,
    max: 65535
  },
  {
    key: 'type',
    label: '节点类型',
    type: 'select',
    required: true,
    options: [
      { label: '计算节点', value: 'compute' },
      { label: '存储节点', value: 'storage' },
      { label: '混合节点', value: 'hybrid' }
    ]
  },
  {
    key: 'maxTasks',
    label: '最大任务数',
    type: 'number',
    required: true,
    min: 1,
    max: 100
  },
  {
    key: 'description',
    label: '描述',
    type: 'textarea',
    placeholder: '请输入节点描述'
  }
]

const nodeFormRules = {
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  ip: [{ required: true, message: '请输入IP地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  type: [{ required: true, message: '请选择节点类型', trigger: 'change' }]
}

// 监控数据
const nodeMonitorData = reactive({
  xAxis: ['12:00', '12:10', '12:20', '12:30', '12:40', '12:50', '13:00'],
  series: [
    {
      name: 'CPU使用率',
      data: [35, 42, 38, 45, 52, 48, 45],
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409EFF' }
    },
    {
      name: '内存使用率',
      data: [60, 65, 62, 68, 72, 70, 68],
      type: 'line',
      smooth: true,
      itemStyle: { color: '#67C23A' }
    }
  ]
})

const monitorChartOptions = {
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['CPU使用率', '内存使用率']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  }
}

const nodeRunningTasks = ref([
  {
    taskId: 'task-001',
    dagName: '数据处理流水线',
    taskName: '数据提取',
    startTime: new Date(Date.now() - 10 * 60 * 1000),
    progress: 65
  }
])

// 计算属性
const totalNodes = computed(() => nodes.value.length)

const filteredNodes = computed(() => {
  let filtered = [...nodes.value]

  if (statusFilter.value !== 'all') {
    filtered = filtered.filter(node => node.status === statusFilter.value)
  }

  if (typeFilter.value !== 'all') {
    filtered = filtered.filter(node => node.type === typeFilter.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(node =>
      node.name.toLowerCase().includes(keyword) ||
      node.ip.toLowerCase().includes(keyword)
    )
  }

  if (showHealthOnly.value) {
    filtered = filtered.filter(node =>
      node.status === 'online' &&
      node.resources.cpuUsage < 80 &&
      node.resources.memoryUsage < 80
    )
  }

  return filtered
})

// 方法
const getStatusIcon = (status: string) => {
  const icons = {
    online: CircleCheck,
    offline: CircleClose,
    maintenance: Warning
  }
  return icons[status] || Connection
}

const getStatusType = (status: string) => {
  const types = {
    online: 'success',
    offline: 'danger',
    maintenance: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    online: '在线',
    offline: '离线',
    maintenance: '维护中'
  }
  return texts[status] || status
}

const getNodeTypeText = (type: string) => {
  const texts = {
    compute: '计算节点',
    storage: '存储节点',
    hybrid: '混合节点'
  }
  return texts[type] || type
}

const getUsageColor = (usage: number) => {
  if (usage >= 80) return '#F56C6C'
  if (usage >= 60) return '#E6A23C'
  return '#67C23A'
}

const formatTime = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatBytes = (bytes: number) => {
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  if (bytes === 0) return '0 B'
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const refreshNodes = async () => {
  // 刷新节点状态
  console.log('刷新节点状态')
}

const handleNodeAction = (command: string, node: any) => {
  switch (command) {
    case 'edit':
      console.log('编辑节点:', node.name)
      break
    case 'logs':
      console.log('查看日志:', node.name)
      break
    case 'monitor':
      monitorNode.value = node
      showMonitor.value = true
      break
    case 'toggle':
      console.log('切换节点状态:', node.name)
      break
    case 'delete':
      console.log('删除节点:', node.name)
      break
  }
}

const viewNodeDetails = (node: any) => {
  selectedNode.value = node
  showNodeDetails.value = true
}

const testConnection = async (node: any) => {
  console.log('测试连接:', node.name)
  // 测试节点连接
}

const handleAddNode = () => {
  console.log('添加节点:', nodeForm.value)
  showAddDialog.value = false
  // 重置表单
  nodeForm.value = {
    name: '',
    ip: '',
    port: 8080,
    type: 'compute',
    maxTasks: 10,
    description: ''
  }
}

onMounted(() => {
  refreshNodes()
})
</script>

<style lang="less" scoped>
.node-config {
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

      .node-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .cluster-overview {
    margin-bottom: 20px;

    .overview-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: white;

          &.online {
            background: linear-gradient(45deg, #67C23A, #95D475);
          }

          &.offline {
            background: linear-gradient(45deg, #F56C6C, #F78989);
          }

          &.busy {
            background: linear-gradient(45deg, #E6A23C, #F0C78A);
          }

          &.capacity {
            background: linear-gradient(45deg, #409EFF, #66B1FF);
          }
        }

        .stat-info {
          .stat-number {
            font-size: 24px;
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

  .node-filters {
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

  .nodes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;

    .node-card {
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
      }

      &.node-offline {
        opacity: 0.7;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .node-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .node-status {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            color: white;

            &.status-online {
              background: #67C23A;
            }

            &.status-offline {
              background: #F56C6C;
            }

            &.status-maintenance {
              background: #E6A23C;
            }
          }

          .node-title {
            h4 {
              margin: 0 0 4px 0;
              font-size: 16px;
              font-weight: 600;
              color: #303133;
            }

            .node-type {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }

      .node-details {
        margin: 16px 0;

        .detail-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 13px;

          .label {
            color: #909399;
          }

          .value {
            color: #606266;
            font-weight: 500;
          }
        }
      }

      .resource-usage {
        margin: 16px 0;

        .usage-item {
          margin-bottom: 12px;

          .usage-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
            font-size: 12px;
            color: #606266;
          }
        }
      }

      .node-actions {
        display: flex;
        gap: 8px;
        justify-content: center;
        padding-top: 12px;
        border-top: 1px solid #EBEEF5;
      }
    }
  }

  .node-details-content {
    .detail-card {
      margin-bottom: 20px;
    }

    .monitor-charts {
      height: 300px;
    }
  }

  .monitor-content {
    .monitor-charts-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }
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
