import { defineStore } from 'pinia'
import type { CommonState } from './login/types'

const useCommonStore = defineStore('common', {
  state: (): CommonState => ({
    roles: [],
    departments: [],
  }),
  
  actions: {
    loadAllRole() {
      // Mock data for now
      this.roles = [
        { id: '1', name: '管理员', code: 'admin' },
        { id: '2', name: '操作员', code: 'operator' },
      ]
    },
    
    loadAllDepartment() {
      // Mock data for now
      this.departments = [
        { id: '1', name: '技术部', code: 'tech' },
        { id: '2', name: '运维部', code: 'ops' },
      ]
    }
  }
})

export default useCommonStore