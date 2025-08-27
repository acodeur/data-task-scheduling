// 任务类型
export type TaskType = 'sql' | 'script' | 'http' | 'file_transfer' | 'data_sync' | 'email' | 'custom'

// 任务状态
export type TaskStatus = 'active' | 'inactive' | 'draft'

// 任务分类
export interface TaskCategory {
  id: string
  name: string
  description?: string
  color?: string
}

// 任务模板
export interface TaskTemplate {
  id: string
  name: string
  description?: string
  type: TaskType
  category: TaskCategory
  config: Record<string, any>
  parameters: TaskParameter[]
  created: Date
  updated: Date
  version: number
  tags: string[]
  isPublic: boolean
  author: string
  usage_count: number
  rating?: number
}

// 任务参数定义
export interface TaskParameter {
  name: string
  type: 'string' | 'number' | 'boolean' | 'select' | 'multiselect' | 'file' | 'json'
  required: boolean
  defaultValue?: any
  description?: string
  validation?: {
    pattern?: string
    min?: number
    max?: number
    options?: Array<{ label: string; value: any }>
  }
}

// 任务执行配置
export interface TaskExecutionConfig {
  timeout?: number
  retries?: number
  retryDelay?: number
  failureHandling?: 'stop' | 'continue' | 'skip'
  resources?: {
    cpu?: number
    memory?: number
    disk?: number
  }
}

// 任务运行历史
export interface TaskExecution {
  id: string
  taskId: string
  dagId?: string
  status: 'pending' | 'running' | 'success' | 'failed' | 'cancelled'
  startTime: Date
  endTime?: Date
  duration?: number
  output?: any
  error?: string
  logs?: string[]
  metrics?: Record<string, any>
}

// 任务搜索过滤器
export interface TaskFilter {
  type?: TaskType[]
  category?: string[]
  tags?: string[]
  status?: TaskStatus[]
  author?: string
  keyword?: string
  dateRange?: {
    start: Date
    end: Date
  }
}
