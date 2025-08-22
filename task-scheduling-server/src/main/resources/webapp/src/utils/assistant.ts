import { ZZ_TOKEN } from '@/global'
import { localCache } from './cache'
import type { Router } from 'vue-router'

class Assistant {
  private modules: any

  constructor() {
    this.modules = import.meta.glob(['@/views/**/*.vue'])
  }
  removeAllCache(): void {
    localCache.removeCache(ZZ_TOKEN)
    localCache.removeCache('userInfo')
    localCache.removeCache('userMenus')
  }

  loadRouteFromCache(router: Router): void {
    const token = localCache.getCache(ZZ_TOKEN)
    const userInfo = localCache.getCache('userInfo')
    const userMenus = localCache.getCache('userMenus')
    if (token && userInfo && userMenus) {
      for (const menu of userMenus) {
        let firstSubMenu = null
        for (const subMenu of menu.children) {
          // set the first submenu as the default parent's menu route's redirect
          if (!firstSubMenu && subMenu) {
            router.addRoute('Home', {
              path: menu.url,
              name: menu.name,
              redirect: subMenu.url,
            })
            firstSubMenu = subMenu
          }
          const componentName = this.getComponentNameFromUrlPath(subMenu.url)
          const componentPath = '/src/views/home' + `${subMenu.url}/${componentName}.vue`
          router.addRoute('Home', {
            path: subMenu.url,
            name: subMenu.name,
            component: this.modules[componentPath],
          })
        }
      }
    }
  }
  getComponentNameFromUrlPath(urlPath: string): string {
    const parts = urlPath.split('/')
    if (parts.length === 1) {
      return this.toPascalCase(parts[0])
    }
    if (parts.length >= 2) {
      return this.toPascalCase(parts[parts.length - 2]) + this.toPascalCase(parts[parts.length - 1])
    }
    return ''
  }
  toCamelCase(str: string): string {
    return str
      .replace(/(?:^\w|[A-Z]|\b\w)/g, (word, index) => {
        return index === 0 ? word.toLowerCase() : word.toUpperCase()
      })
      .replace(/\s+/g, '')
  }
  toPascalCase(str: string): string {
    return this.toCamelCase(str).replace(/^./, (match) => match.toUpperCase())
  }
}

const assistant = new Assistant()

export default assistant
