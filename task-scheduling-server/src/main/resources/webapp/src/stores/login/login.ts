import { defineStore } from 'pinia'
import type { LoginState } from './types'

const useLoginStore = defineStore('login', {
  state: (): LoginState => ({
    user: null,
    token: null,
    isAuthenticated: false,
  }),
  
  getters: {
    userInfo: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
  },
  
  actions: {
    setUser(user: any) {
      this.user = user
      this.isAuthenticated = !!user
    },
    
    setToken(token: string) {
      this.token = token
    },
    
    resetStateFromCache() {
      // Load from localStorage if available
      const cachedToken = localStorage.getItem('token')
      const cachedUser = localStorage.getItem('user')
      
      if (cachedToken) {
        this.token = cachedToken
      }
      
      if (cachedUser) {
        this.user = JSON.parse(cachedUser)
        this.isAuthenticated = true
      }
    },
    
    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})

export default useLoginStore