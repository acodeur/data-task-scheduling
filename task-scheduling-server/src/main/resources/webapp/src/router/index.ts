import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/Dashboard.vue'),
    },
    {
      path: '/dags',
      name: 'DAGList',
      component: () => import('@/views/dag/DAGList.vue'),
    },
    {
      path: '/dags/:id/edit',
      name: 'DAGEditor',
      component: () => import('@/views/dag/DAGEditor.vue'),
      props: true,
    },
    {
      path: '/dags/new',
      name: 'NewDAG',
      component: () => import('@/views/dag/DAGEditor.vue'),
      props: { isNew: true },
    },
  ],
})

export default router
