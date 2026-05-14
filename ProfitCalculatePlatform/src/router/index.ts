import { createRouter, createWebHistory } from 'vue-router'
import TaskManageView from '../views/TaskManageView.vue'
import SummaryView from '../views/SummaryView.vue'
import CostView from '../views/CostView.vue'
import PreviewView from '../views/PreviewView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/tasks' },
    { path: '/tasks', name: 'tasks', component: TaskManageView },
    { path: '/tasks/:taskId/preview', name: 'preview', component: PreviewView, props: true },
    { path: '/summary', name: 'summary', component: SummaryView },
    { path: '/costs', name: 'costs', component: CostView },
  ],
})

export default router
