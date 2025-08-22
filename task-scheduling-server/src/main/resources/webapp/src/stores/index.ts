import { createPinia } from 'pinia'
import type { App } from 'vue'
import useLoginStore from './login/login'
import useCommonStrore from './common'

const pinia = createPinia()

function install(app: App<Element>) {
  app.use(pinia)
  // 重置登录状态，加载用户信息和菜单
  const loginStore = useLoginStore()
  loginStore.resetStateFromCache()
  // 加载公共数据，如角色、部门
  const commonStrore = useCommonStrore()
  commonStrore.loadAllRole()
  commonStrore.loadAllDepartment()
}

export default install
