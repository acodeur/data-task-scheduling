import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/Dashboard.vue'),
      meta: { title: '仪表盘', icon: 'Monitor' }
    },
    {
      path: '/tasks',
      name: 'TaskRepository',
      component: () => import('@/views/task/TaskRepository.vue'),
      meta: { title: '任务仓库', icon: 'Files' }
    },
    {
      path: '/tasks/:id',
      name: 'TaskDetail',
      component: () => import('@/views/task/TaskDetail.vue'),
      props: true,
      meta: { title: '任务详情', hidden: true }
    },
    {
      path: '/dags',
      name: 'DAGList',
      component: () => import('@/views/dag/DAGList.vue'),
      meta: { title: 'DAG工作流', icon: 'Share' }
    },
    {
      path: '/dags/:id/edit',
      name: 'DAGEditor',
      component: () => import('@/views/dag/DAGEditor.vue'),
      props: true,
      meta: { title: 'DAG编辑器', hidden: true }
    },
    {
      path: '/dags/new',
      name: 'NewDAG',
      component: () => import('@/views/dag/DAGEditor.vue'),
      props: { isNew: true },
      meta: { title: '新建DAG', hidden: true }
    },
    {
      path: '/scheduler',
      name: 'SchedulerMonitor',
      component: () => import('@/views/scheduler/SchedulerMonitor.vue'),
      meta: { title: '调度监控', icon: 'Timer' }
    },
    {
      path: '/nodes',
      name: 'NodeConfig',
      component: () => import('@/views/nodes/NodeConfig.vue'),
      meta: { title: '节点管理', icon: 'Setting' }
    },
    {
      path: '/jobs/history',
      name: 'JobHistory',
      component: () => import('@/views/jobs/JobHistory.vue'),
      meta: { title: '执行历史', hidden: true }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 数据任务调度系统`
  }
  next()
})

export default router
