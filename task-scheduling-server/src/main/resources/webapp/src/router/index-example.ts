import { ZZ_TOKEN } from '@/global'
import assistant from '@/utils/assistant'
import { localCache } from '@/utils/cache'
import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/home/Home.vue'),
      // redirect: '/home/index',
    },
    {
      path: '/main',
      name: 'Main',
      redirect: '/home',
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/Login.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/not-found/NotFound.vue'),
    },
  ],
})

// 根据获取菜单加载路由
assistant.loadRouteFromCache(router)

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localCache.getCache(ZZ_TOKEN)
  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' }) // 未登录时重定向到登录页
  } else {
    next() // 允许访问
  }
})

export default router
