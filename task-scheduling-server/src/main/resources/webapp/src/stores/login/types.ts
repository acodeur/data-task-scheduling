export interface LoginState {
  user: any | null
  token: string | null
  isAuthenticated: boolean
}

export interface CommonState {
  roles: any[]
  departments: any[]
}