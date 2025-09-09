<template>
  <div class="task-repository">
    <!-- 页面标题和操作 -->
    <PageContent :config="pageConfig" :model="pageModel">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <h2>任务仓库</h2>
            <span class="task-count">共 {{ totalTasks }} 个任务模板</span>
          </div>
          <div class="header-right">
            <el-button 
              type="primary" 
              :icon="Plus" 
              @click="showCreateDialog = true"
            >
              创建任务模板
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <PageSearch
        :config="searchConfig"
        v-model="searchForm"
        @search="handleSearch"
        @reset="handleReset"
      />

      <!-- 分类标签 -->
      <div class="category-tabs">
        <el-tag
          v-for="category in categories"
          :key="category.id"
          :type="activeCategory === category.id ? 'primary' : 'info'"
          :effect="activeCategory === category.id ? 'dark' : 'light'"
          class="category-tag"
          @click="handleCategoryChange(category.id)"
        >
          {{ category.name }}
        </el-tag>
      </div>

      <!-- 任务列表 -->
      <div class="task-list">
        <div class="list-toolbar">
          <div class="toolbar-left">
            <el-select v-model="viewMode" size="small">
              <el-option label="卡片视图" value="card" />
              <el-option label="列表视图" value="list" />
            </el-select>
            <el-select v-model="sortBy" size="small">
              <el-option label="最近更新" value="updated" />
              <el-option label="创建时间" value="created" />
              <el-option label="使用次数" value="usage" />
              <el-option label="评分" value="rating" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button-group>
              <el-button
                v-for="filter in quickFilters"
                :key="filter.key"
                :type="filter.active ? 'primary' : 'default'"
                size="small"
                @click="toggleQuickFilter(filter.key)"
              >
                {{ filter.label }}
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 卡片视图 -->
        <div v-if="viewMode === 'card'" class="card-view">
          <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="task-card"
            @click="handleTaskClick(task)"
          >
            <el-card>
              <template #header>
                <div class="card-header">
                  <div class="task-info">
                    <div class="task-icon" :class="`task-${task.type}`">
                      <el-icon><component :is="getTaskIcon(task.type)" /></el-icon>
                    </div>
                    <div class="task-title">
                      <h3>{{ task.name }}</h3>
                      <span class="task-type">{{ getTaskTypeName(task.type) }}</span>
                    </div>
                  </div>
                  <el-dropdown trigger="click" @command="(cmd) => handleTaskAction(cmd, task)">
                    <el-button text>
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                        <el-dropdown-item command="export">导出</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>

              <div class="card-content">
                <p class="task-description">{{ task.description }}</p>
                
                <div class="task-tags">
                  <el-tag
                    v-for="tag in task.tags.slice(0, 3)"
                    :key="tag"
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-tag v-if="task.tags.length > 3" size="small" type="info">
                    +{{ task.tags.length - 3 }}
                  </el-tag>
                </div>

                <div class="task-stats">
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ task.usage_count }}</span>
                  </div>
                  <div v-if="task.rating" class="stat-item">
                    <el-rate
                      v-model="task.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      size="small"
                    />
                  </div>
                </div>

                <div class="task-meta">
                  <span class="author">{{ task.author }}</span>
                  <span class="update-time">{{ formatTime(task.updated) }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="list-view">
          <el-table :data="filteredTasks" style="width: 100%">
            <el-table-column prop="name" label="任务名称" min-width="200">
              <template #default="{ row }">
                <div class="task-name-cell">
                  <div class="task-icon" :class="`task-${row.type}`">
                    <el-icon><component :is="getTaskIcon(row.type)" /></el-icon>
                  </div>
                  <div>
                    <div class="name">{{ row.name }}</div>
                    <div class="type">{{ getTaskTypeName(row.type) }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
            
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">
                {{ row.category.name }}
              </template>
            </el-table-column>
            
            <el-table-column prop="tags" label="标签" min-width="200">
              <template #default="{ row }">
                <el-tag
                  v-for="tag in row.tags.slice(0, 2)"
                  :key="tag"
                  size="small"
                  type="info"
                  style="margin-right: 4px;"
                >
                  {{ tag }}
                </el-tag>
                <el-tag v-if="row.tags.length > 2" size="small" type="info">
                  +{{ row.tags.length - 2 }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="usage_count" label="使用次数" width="100" />
            
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="{ row }">
                <el-rate
                  v-if="row.rating"
                  v-model="row.rating"
                  disabled
                  size="small"
                />
                <span v-else>-</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="author" label="作者" width="120" />
            
            <el-table-column prop="updated" label="更新时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.updated) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="handleTaskClick(row)">查看</el-button>
                <el-dropdown trigger="click" @command="(cmd) => handleTaskAction(cmd, row)">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                      <el-dropdown-item command="export">导出</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :total="totalTasks"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </PageContent>

    <!-- 创建/编辑任务对话框 -->
    <PageDialog
      :config="dialogConfig"
      v-model="showCreateDialog"
      @confirm="handleSaveTask"
    >
      <DynamicForm
        :iForm="taskFormConfig"
        v-model="taskForm"
      />
    </PageDialog>

    <!-- 任务详情对话框 -->
    <el-drawer
      v-model="showTaskDetail"
      :title="selectedTask?.name"
      size="50%"
    >
      <TaskDetail
        v-if="selectedTask"
        :task="selectedTask"
        @edit="handleEditTask"
        @duplicate="handleDuplicateTask"
        @delete="handleDeleteTask"
      />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElCard, ElIcon, ElButton, ElSelect, ElOption, ElButtonGroup,
  ElTag, ElDropdown, ElDropdownMenu, ElDropdownItem, ElTable,
  ElTableColumn, ElRate, ElPagination, ElDrawer
} from 'element-plus'
import {
  Plus, MoreFilled, View, DataBoard, Document, Connection,
  Upload, RefreshRight, Message, Tools
} from '@element-plus/icons-vue'
import { PageContent, PageSearch, PageDialog } from '@/components/page'
import { DynamicForm } from '@/components/form'
import TaskDetail from './TaskDetail.vue'
import type { TaskTemplate, TaskType, TaskCategory, TaskFilter } from '@/types/task'
import type { IPageContentConfig, IPageContentModel, IPageDialogConfig } from '@/components/page/type'
import type { IForm } from '@/components/form/type'
import dayjs from 'dayjs'

// 数据状态
const searchForm = ref<TaskFilter>({})
const showCreateDialog = ref(false)
const showTaskDetail = ref(false)
const selectedTask = ref<TaskTemplate | null>(null)
const editingTask = ref<TaskTemplate | null>(null)
const viewMode = ref<'card' | 'list'>('card')
const sortBy = ref('updated')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(12)

// 搜索字段配置
const searchFields = [
  {
    key: 'keyword',
    label: '关键词',
    type: 'input',
    placeholder: '搜索任务名称、描述、标签'
  },
  {
    key: 'type',
    label: '任务类型',
    type: 'select',
    multiple: true,
    options: [
      { label: 'SQL查询', value: 'sql' },
      { label: '脚本执行', value: 'script' },
      { label: 'HTTP请求', value: 'http' },
      { label: '文件传输', value: 'file_transfer' },
      { label: '数据同步', value: 'data_sync' },
      { label: '邮件发送', value: 'email' },
      { label: '自定义', value: 'custom' }
    ]
  },
  {
    key: 'author',
    label: '作者',
    type: 'input'
  },
  {
    key: 'dateRange',
    label: '更新时间',
    type: 'daterange'
  }
]

// 分类数据
const categories = ref<(TaskCategory & { id: string })[]>([
  { id: 'all', name: '全部', description: '所有任务模板' },
  { id: 'data', name: '数据处理', description: '数据相关任务', color: '#409EFF' },
  { id: 'system', name: '系统运维', description: '系统维护任务', color: '#67C23A' },
  { id: 'notification', name: '通知提醒', description: '消息通知任务', color: '#E6A23C' },
  { id: 'integration', name: '集成对接', description: '系统集成任务', color: '#F56C6C' }
])

// 快速筛选
const quickFilters = reactive([
  { key: 'my', label: '我的', active: false },
  { key: 'public', label: '公共', active: false },
  { key: 'recent', label: '最近使用', active: false },
  { key: 'popular', label: '热门', active: false }
])

// 任务数据
const tasks = ref<TaskTemplate[]>([
  {
    id: '1',
    name: '数据库备份任务',
    description: '自动备份MySQL数据库，支持增量和全量备份',
    type: 'sql',
    category: { id: 'system', name: '系统运维' },
    config: {},
    parameters: [],
    created: new Date('2024-01-15'),
    updated: new Date('2024-01-25'),
    version: 1,
    tags: ['数据库', '备份', 'MySQL'],
    isPublic: true,
    author: '张三',
    usage_count: 156,
    rating: 4.8
  },
  {
    id: '2',
    name: 'ETL数据清洗',
    description: '从源系统提取数据，进行清洗和转换，加载到目标系统',
    type: 'script',
    category: { id: 'data', name: '数据处理' },
    config: {},
    parameters: [],
    created: new Date('2024-01-10'),
    updated: new Date('2024-01-24'),
    version: 2,
    tags: ['ETL', '数据清洗', '数据转换'],
    isPublic: true,
    author: '李四',
    usage_count: 89,
    rating: 4.5
  }
])

// 计算属性
const totalTasks = computed(() => tasks.value.length)
const filteredTasks = computed(() => {
  let filtered = [...tasks.value]
  
  // 分类筛选
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(task => task.category.id === activeCategory.value)
  }
  
  // 关键词搜索
  if (searchForm.value.keyword) {
    const keyword = searchForm.value.keyword.toLowerCase()
    filtered = filtered.filter(task =>
      task.name.toLowerCase().includes(keyword) ||
      task.description?.toLowerCase().includes(keyword) ||
      task.tags.some(tag => tag.toLowerCase().includes(keyword))
    )
  }
  
  // 其他筛选条件...
  
  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'updated':
        return new Date(b.updated).getTime() - new Date(a.updated).getTime()
      case 'created':
        return new Date(b.created).getTime() - new Date(a.created).getTime()
      case 'usage':
        return b.usage_count - a.usage_count
      case 'rating':
        return (b.rating || 0) - (a.rating || 0)
      default:
        return 0
    }
  })
  
  return filtered
})

// 页面配置
const pageConfig: IPageContentConfig = {
  pageName: 'task-repository',
  header: {
    title: '任务仓库',
    btnTxt: '创建任务模板'
  },
  columnList: [],
  pagination: {
    currentPage: 1,
    pageSize: 12
  }
}

const pageModel: IPageContentModel = {
  dataList: [],
  totalCount: 0
}

// 搜索配置
const searchConfig: IForm = {
  formItems: [
    {
      field: 'keyword',
      label: '关键词',
      component: 'input',
      componentProps: { placeholder: '搜索任务名称、描述、标签' }
    },
    {
      field: 'type',
      label: '任务类型',
      component: 'select',
      componentProps: { multiple: true },
      options: [
        { label: 'SQL查询', value: 'sql' },
        { label: '脚本执行', value: 'script' },
        { label: 'HTTP请求', value: 'http' },
        { label: '文件传输', value: 'file_transfer' },
        { label: '数据同步', value: 'data_sync' },
        { label: '邮件发送', value: 'email' },
        { label: '自定义', value: 'custom' }
      ]
    },
    {
      field: 'author',
      label: '作者',
      component: 'input'
    },
    {
      field: 'dateRange',
      label: '更新时间',
      component: 'datepicker',
      componentProps: { type: 'daterange' }
    }
  ],
  formProps: {}
}

// 对话框配置
const dialogConfig: IPageDialogConfig = {
  header: {
    addTitle: '创建任务模板',
    editTitle: '编辑任务模板'
  },
  dialogProps: {
    width: '800px'
  },
  footer: {
    cancelTxt: '取消',
    addConfirmTxt: '创建',
    editConfirmTxt: '保存'
  },
  formConfig: {
    ui: {
      formItems: [],
      formProps: {}
    }
  }
}

// 表单配置
const taskFormConfig: IForm = {
  formItems: [
    {
      field: 'name',
      label: '任务名称',
      component: 'input',
      required: true
    },
    {
      field: 'description',
      label: '任务描述',
      component: 'input'
    }
  ],
  formProps: {}
}

// 任务表单配置
const taskForm = ref<Partial<TaskTemplate>>({})
const taskFormFields: any[] = [
  // 这里应该定义任务表单字段
]
const taskFormRules = {}

// 方法
const getTaskIcon = (type: TaskType) => {
  const icons: Record<TaskType, any> = {
    sql: DataBoard,
    script: Document,
    http: Connection,
    file_transfer: Upload,
    data_sync: RefreshRight,
    email: Message,
    custom: Tools
  }
  return icons[type] || Tools
}

const getTaskTypeName = (type: TaskType) => {
  const names: Record<TaskType, string> = {
    sql: 'SQL查询',
    script: '脚本执行',
    http: 'HTTP请求',
    file_transfer: '文件传输',
    data_sync: '数据同步',
    email: '邮件发送',
    custom: '自定义'
  }
  return names[type] || '未知'
}

const formatTime = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const handleSearch = (form: TaskFilter) => {
  searchForm.value = { ...form }
}

const handleReset = () => {
  searchForm.value = {}
}

const handleCategoryChange = (categoryId: string) => {
  activeCategory.value = categoryId
}

const toggleQuickFilter = (key: string) => {
  const filter = quickFilters.find(f => f.key === key)
  if (filter) {
    filter.active = !filter.active
  }
}

const handleTaskClick = (task: TaskTemplate) => {
  selectedTask.value = task
  showTaskDetail.value = true
}

const handleTaskAction = (command: string, task: TaskTemplate) => {
  switch (command) {
    case 'edit':
      handleEditTask(task)
      break
    case 'duplicate':
      handleDuplicateTask(task)
      break
    case 'export':
      handleExportTask(task)
      break
    case 'delete':
      handleDeleteTask(task)
      break
  }
}

const handleEditTask = (task: TaskTemplate) => {
  editingTask.value = task
  taskForm.value = { ...task }
  showCreateDialog.value = true
}

const handleDuplicateTask = (task: TaskTemplate) => {
  taskForm.value = {
    ...task,
    name: task.name + ' (副本)',
    id: undefined
  }
  editingTask.value = null
  showCreateDialog.value = true
}

const handleExportTask = (task: TaskTemplate) => {
  // 导出任务逻辑
  console.log('导出任务:', task.name)
}

const handleDeleteTask = (task: TaskTemplate) => {
  // 删除任务逻辑
  console.log('删除任务:', task.name)
}

const handleSaveTask = () => {
  // 保存任务逻辑
  console.log('保存任务:', taskForm.value)
  showCreateDialog.value = false
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

onMounted(() => {
  // 初始化数据
})
</script>

<style lang="less" scoped>
.task-repository {
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

      .task-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .category-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 20px;

    .category-tag {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }

  .task-list {
    .list-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;

      .toolbar-left {
        display: flex;
        gap: 8px;
      }
    }

    .card-view {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .task-card {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .task-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .task-icon {
              width: 40px;
              height: 40px;
              border-radius: 8px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 18px;
              color: white;

              &.task-sql {
                background: linear-gradient(45deg, #409EFF, #66B1FF);
              }

              &.task-script {
                background: linear-gradient(45deg, #67C23A, #95D475);
              }

              &.task-http {
                background: linear-gradient(45deg, #E6A23C, #F0C78A);
              }

              &.task-file_transfer {
                background: linear-gradient(45deg, #F56C6C, #F78989);
              }

              &.task-data_sync {
                background: linear-gradient(45deg, #909399, #B1B3B8);
              }

              &.task-email {
                background: linear-gradient(45deg, #C0C4CC, #D3D4D6);
              }

              &.task-custom {
                background: linear-gradient(45deg, #606266, #79838F);
              }
            }

            .task-title {
              h3 {
                margin: 0 0 4px 0;
                font-size: 16px;
                font-weight: 600;
                color: #303133;
              }

              .task-type {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }

        .card-content {
          .task-description {
            margin: 0 0 12px 0;
            color: #606266;
            font-size: 14px;
            line-height: 1.5;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
          }

          .task-tags {
            margin-bottom: 12px;
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
          }

          .task-stats {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .stat-item {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              color: #909399;
            }
          }

          .task-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: #C0C4CC;

            .author {
              font-weight: 500;
            }
          }
        }
      }
    }

    .list-view {
      margin-bottom: 20px;

      .task-name-cell {
        display: flex;
        align-items: center;
        gap: 8px;

        .task-icon {
          width: 32px;
          height: 32px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          color: white;

          &.task-sql {
            background: #409EFF;
          }

          &.task-script {
            background: #67C23A;
          }

          &.task-http {
            background: #E6A23C;
          }

          &.task-file_transfer {
            background: #F56C6C;
          }

          &.task-data_sync {
            background: #909399;
          }

          &.task-email {
            background: #C0C4CC;
          }

          &.task-custom {
            background: #606266;
          }
        }

        .name {
          font-weight: 500;
          color: #303133;
        }

        .type {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
}

@media (max-width: 768px) {
  .task-repository {
    .card-view {
      grid-template-columns: 1fr;
    }

    .list-toolbar {
      flex-direction: column;
      gap: 12px;
    }
  }
}
</style>