import type { DAG, DAGNode, DAGEdge, ExecutionResult, ValidationResult } from '@/types/dag'
import { httpClient } from './httpClient'

export const dagService = {
  // Create operations
  async createDAG(dag: Partial<DAG>): Promise<DAG> {
    return await httpClient.post('/dags', dag)
  },

  async createNode(dagId: string, node: Partial<DAGNode>): Promise<DAGNode> {
    return await httpClient.post(`/dags/${dagId}/nodes`, node)
  },

  async createEdge(dagId: string, edge: Partial<DAGEdge>): Promise<DAGEdge> {
    return await httpClient.post(`/dags/${dagId}/edges`, edge)
  },

  // Read operations
  async fetchDAGs(): Promise<DAG[]> {
    return await httpClient.get('/dags')
  },

  async fetchDAG(id: string): Promise<DAG> {
    return await httpClient.get(`/dags/${id}`)
  },

  async validateDAG(dag: DAG): Promise<ValidationResult> {
    return await httpClient.post(`/dags/${dag.id}/validate`, dag)
  },

  // Update operations
  async updateDAG(id: string, updates: Partial<DAG>): Promise<DAG> {
    return await httpClient.put(`/dags/${id}`, updates)
  },

  async updateNode(dagId: string, nodeId: string, updates: Partial<DAGNode>): Promise<DAGNode> {
    return await httpClient.put(`/dags/${dagId}/nodes/${nodeId}`, updates)
  },

  async updateNodePosition(dagId: string, nodeId: string, position: { x: number; y: number }): Promise<void> {
    return await httpClient.patch(`/dags/${dagId}/nodes/${nodeId}/position`, { position })
  },

  // Delete operations
  async deleteDAG(id: string): Promise<void> {
    return await httpClient.delete(`/dags/${id}`)
  },

  async deleteNode(dagId: string, nodeId: string): Promise<void> {
    return await httpClient.delete(`/dags/${dagId}/nodes/${nodeId}`)
  },

  async deleteEdge(dagId: string, edgeId: string): Promise<void> {
    return await httpClient.delete(`/dags/${dagId}/edges/${edgeId}`)
  },

  // Execution operations
  async executeDAG(id: string): Promise<ExecutionResult> {
    return await httpClient.post(`/dags/${id}/execute`)
  },

  async pauseDAG(id: string): Promise<void> {
    return await httpClient.post(`/dags/${id}/pause`)
  },

  async resumeDAG(id: string): Promise<void> {
    return await httpClient.post(`/dags/${id}/resume`)
  },
}