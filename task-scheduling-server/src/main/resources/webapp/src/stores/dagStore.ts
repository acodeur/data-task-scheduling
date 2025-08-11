import { defineStore } from 'pinia'
import type { DAG, DAGNode, DAGEdge, ExecutionResult, ValidationResult } from '@/types/dag'
import { dagService } from '@/services/dagService'

interface DAGState {
  dags: DAG[]
  currentDAG: DAG | null
  loading: boolean
  error: string | null
}

export const useDagStore = defineStore('dag', {
  state: (): DAGState => ({
    dags: [],
    currentDAG: null,
    loading: false,
    error: null,
  }),

  getters: {
    getDagById: (state) => (id: string) => state.dags.find(dag => dag.id === id),
    getNodeById: (state) => (nodeId: string) => 
      state.currentDAG?.nodes.find(node => node.id === nodeId),
    getEdgeById: (state) => (edgeId: string) => 
      state.currentDAG?.edges.find(edge => edge.id === edgeId),
  },

  actions: {
    // Create operations
    async createDAG(dag: Partial<DAG>): Promise<DAG> {
      this.loading = true
      try {
        const newDAG = await dagService.createDAG(dag)
        this.dags.push(newDAG)
        return newDAG
      } catch (error) {
        this.error = error as string
        throw error
      } finally {
        this.loading = false
      }
    },

    async createNode(dagId: string, node: Partial<DAGNode>): Promise<DAGNode> {
      const newNode = await dagService.createNode(dagId, node)
      if (this.currentDAG?.id === dagId) {
        this.currentDAG.nodes.push(newNode)
      }
      return newNode
    },

    async createEdge(dagId: string, edge: Partial<DAGEdge>): Promise<DAGEdge> {
      const newEdge = await dagService.createEdge(dagId, edge)
      if (this.currentDAG?.id === dagId) {
        this.currentDAG.edges.push(newEdge)
      }
      return newEdge
    },

    // Read operations
    async fetchDAGs(): Promise<DAG[]> {
      this.loading = true
      try {
        this.dags = await dagService.fetchDAGs()
        return this.dags
      } catch (error) {
        this.error = error as string
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchDAG(id: string): Promise<DAG> {
      this.loading = true
      try {
        this.currentDAG = await dagService.fetchDAG(id)
        return this.currentDAG
      } catch (error) {
        this.error = error as string
        throw error
      } finally {
        this.loading = false
      }
    },

    async validateDAG(dag: DAG): Promise<ValidationResult> {
      return await dagService.validateDAG(dag)
    },

    // Update operations
    async updateDAG(id: string, updates: Partial<DAG>): Promise<DAG> {
      const updatedDAG = await dagService.updateDAG(id, updates)
      const index = this.dags.findIndex(dag => dag.id === id)
      if (index !== -1) {
        this.dags[index] = updatedDAG
      }
      if (this.currentDAG?.id === id) {
        this.currentDAG = updatedDAG
      }
      return updatedDAG
    },

    async updateNode(dagId: string, nodeId: string, updates: Partial<DAGNode>): Promise<DAGNode> {
      const updatedNode = await dagService.updateNode(dagId, nodeId, updates)
      if (this.currentDAG?.id === dagId) {
        const nodeIndex = this.currentDAG.nodes.findIndex(n => n.id === nodeId)
        if (nodeIndex !== -1) {
          this.currentDAG.nodes[nodeIndex] = updatedNode
        }
      }
      return updatedNode
    },

    async updateNodePosition(dagId: string, nodeId: string, position: { x: number; y: number }): Promise<void> {
      await dagService.updateNodePosition(dagId, nodeId, position)
      if (this.currentDAG?.id === dagId) {
        const node = this.currentDAG.nodes.find(n => n.id === nodeId)
        if (node) {
          node.position = position
        }
      }
    },

    // Delete operations
    async deleteDAG(id: string): Promise<void> {
      await dagService.deleteDAG(id)
      this.dags = this.dags.filter(dag => dag.id !== id)
      if (this.currentDAG?.id === id) {
        this.currentDAG = null
      }
    },

    async deleteNode(dagId: string, nodeId: string): Promise<void> {
      await dagService.deleteNode(dagId, nodeId)
      if (this.currentDAG?.id === dagId) {
        this.currentDAG.nodes = this.currentDAG.nodes.filter(n => n.id !== nodeId)
        this.currentDAG.edges = this.currentDAG.edges.filter(e => 
          e.source !== nodeId && e.target !== nodeId
        )
      }
    },

    async deleteEdge(dagId: string, edgeId: string): Promise<void> {
      await dagService.deleteEdge(dagId, edgeId)
      if (this.currentDAG?.id === dagId) {
        this.currentDAG.edges = this.currentDAG.edges.filter(e => e.id !== edgeId)
      }
    },

    // Execution operations
    async executeDAG(id: string): Promise<ExecutionResult> {
      return await dagService.executeDAG(id)
    },

    async pauseDAG(id: string): Promise<void> {
      return await dagService.pauseDAG(id)
    },

    async resumeDAG(id: string): Promise<void> {
      return await dagService.resumeDAG(id)
    },

    // Local state management
    setCurrentDAG(dag: DAG | null) {
      this.currentDAG = dag
    },

    clearError() {
      this.error = null
    },
  },
})