// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 导入需要路由的组件（示例：首页、详情页）
// 定义路由规则
const routes = [
  {
    path: '/',
    redirect: '/index',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/index',
    name: 'Index',
    component: () => import('@/views/Index.vue'),
    children: [
      {
        path: 'userManage',
        name: 'UserManage',
        component: () => import('@/views/system-admin/UserManage.vue'),
      },
      {
        path: 'landInfo',
        name: 'LandInfo',
        component: () => import('@/views/common/LandInfo.vue'),
      },
      {
        path: 'landAllocationInfo',
        name: 'LandAllocationInfo',
        component: () => import('@/views/common/LandAllocationInfo.vue'),
      },
      {
        path: 'cropInfo',
        name: 'CropInfo',
        component: () => import('@/views/common/CropInfo.vue'),
      },
      {
        path: 'plantingPlanInfo',
        name: 'PlantingPlanInfo',
        component: () => import('@/views/common/PlantingPlanInfo.vue'),
      },
      {
        path: 'equipmentInfo',
        name: 'EquipmentInfo',
        component: () => import('@/views/common/EquipmentInfo.vue'),
      },
      {
        path: 'materialInfo',
        name: 'MaterialInfo',
        component: () => import('@/views/common/MaterialInfo.vue'),
      },
      {
        path: 'contractorMaterialInfo',
        name: 'ContractorMaterialInfo',
        component: () => import('@/views/common/ContractorMaterialInfo.vue'),
      },
      {
        path: 'planAdjustInfo',
        name: 'PlanAdjustInfo',
        component: () => import('@/views/common/PlanAdjustInfo.vue'),
      },
      {
        path: 'materialApproval',
        name: 'MaterialApproval',
        component: () => import('@/views/common/MaterialApproval.vue'),
      },
      {
        path: 'equipmentApproval',
        name: 'EquipmentApproval',
        component: () => import('@/views/common/EquipmentApproval.vue'),
      },
      {
        path: 'contractorEquipmentInfo',
        name: 'ContractorEquipmentInfo',
        component: () => import('@/views/common/ContractorEquipmentInfo.vue'),
      },
      {
        path: 'plantingRecord',
        name: 'PlantingRecord',
        component: () => import('@/views/common/PlantingRecord.vue'),
      },
      {
        path: 'matureCropInfo',
        name: 'MatureCropInfo',
        component: () => import('@/views/common/MatureCropInfo.vue'),
      },

      {
        path: 'contractorManage',
        name: 'ContractorManage',
        component: () => import('@/views/enterprise-admin/ContractorManage.vue'),
      },
      {
        path: 'landActivity',
        name: 'LandActivity',
        component: () => import('@/views/common/LandActivity.vue'),
      },
      {
        path: 'landMonitor',
        name: 'CropMonitor',
        component: () => import('@/views/common/CropMonitor.vue'),
      },
      {
        path: 'plantingPlanInfo',
        name: 'PlantingPlanInfo',
        component: () => import('@/views/common/PlantingPlanInfo.vue'),
      },
      {
        path: 'planAdjustment',
        name: 'PlanAdjustment',
        component: () => import('@/views/common/PlanAdjustInfo.vue'),
      },
      {
        path: 'agriculturalAssistant',
        name: 'AgriculturalAssistant',
        component: () => import('@/views/common/AgriculturalAssistant.vue'),
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/common/UserProfile.vue'),
      },
    ],
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes, // 注入路由规则
})

// 可选：路由守卫（如登录验证）
// router.beforeEach((to, from, next) => {
//   // 示例：判断是否登录，未登录跳转到登录页
//   const isLogin = localStorage.getItem('token')
//   if (to.name !== 'Login' && !isLogin) next({ name: 'Login' })
//   else next()
// })

// 导出路由实例
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    return token ? '/index' : true
  }

  if (!token) {
    return '/login'
  }

  return true
})

export default router
