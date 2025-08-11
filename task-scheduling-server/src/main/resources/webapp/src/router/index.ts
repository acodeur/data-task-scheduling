import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
    },
    {
      path: '/dags',
      name: 'DAGList',
      component: () => import('@/views/DAGList.vue'),
    },
    {
      path: '/dags/:id/edit',
      name: 'DAGEditor',
      component: () => import('@/views/DAGEditor.vue'),
      props: true,
    },
    {
      path: '/dags/new',
      name: 'NewDAG',
      component: () => import('@/views/DAGEditor.vue'),
      props: { isNew: true },
    },
  ],
})

export default router