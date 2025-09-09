// DAG Node Interface
export interface DAGNode {
  id: string
  name: string
  type: 'task' | 'condition' | 'parallel' | 'sequential'
  properties: Record<string, any>
  position: { x: number; y: number }
  dependencies: string[]
  status?: 'pending' | 'running' | 'success' | 'failed'
  metadata?: {
    description?: string
    timeout?: number
    retries?: number
    created: Date
    updated: Date
  }
}

// DAG Edge (Connection)
export interface DAGEdge {
  id: string
  source: string
  target: string
  condition?: string
}

// DAG Definition
export interface DAG {
  id: string
  name: string
  description?: string
  nodes: DAGNode[]
  edges: DAGEdge[]
  schedule?: {
    cron?: string
    enabled: boolean
  }
  status: 'draft' | 'active' | 'disabled' | 'paused'
  created: Date
  updated: Date
  version: number
  taskCount?: number
  lastRun?: Date | null
  author?: string
}

// Execution Result
export interface ExecutionResult {
  id: string
  dagId: string
  status: 'running' | 'success' | 'failed' | 'cancelled'
  startTime: Date
  endTime?: Date
  nodeResults: Record<string, {
    status: 'pending' | 'running' | 'success' | 'failed'
    startTime?: Date
    endTime?: Date
    output?: any
    error?: string
  }>
}

// Validation Result
export interface ValidationResult {
  isValid: boolean
  errors: Array<{
    type: 'cycle' | 'orphan' | 'invalid_dependency' | 'missing_property'
    message: string
    nodeId?: string
    edgeId?: string
  }>
}

// DAG Filter Types
export interface DAGFilter {
  status?: ('draft' | 'active' | 'disabled' | 'paused')[]
  name?: string
  keyword?: string
  author?: string
  dateRange?: {
    start: Date
    end: Date
  }
}

// Run Status Types
export type RunStatus = 'running' | 'pending' | 'success' | 'failed' | 'cancelled'