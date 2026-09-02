<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <h2 v-if="!sidebarCollapsed">现代农业公司管理系统</h2>
        <h2 v-else>AMS</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        router
      >
        <!-- 系统管理员菜单 -->
        <template v-if="userRole === 'system_admin'">
          <!-- 用户管理 -->
          <el-menu-item index="/index/userManage">
            <el-icon><User /></el-icon>
            <span v-if="!sidebarCollapsed">用户管理</span>
          </el-menu-item>

          <!-- 地块管理 -->
          <el-sub-menu index="index/landManage">
            <template #title>
              <el-icon><MapLocation /></el-icon>
              <span v-if="!sidebarCollapsed">地块管理</span>
            </template>
            <el-menu-item index="/index/landInfo">
              <el-icon><Location /></el-icon>
              <span v-if="!sidebarCollapsed">地块信息</span>
            </el-menu-item>
            <el-menu-item index="/index/landAllocationInfo">
              <el-icon><Location /></el-icon>
              <span v-if="!sidebarCollapsed">地块分配情况</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 种植计划管理 -->
          <el-sub-menu index="system-admin/plan">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span v-if="!sidebarCollapsed">计划管理</span>
            </template>
            <el-menu-item index="/index/cropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">农作物信息</span>
            </el-menu-item>
            <el-menu-item index="/index/plantingPlanInfo">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">计划信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 农资管理 -->
          <el-sub-menu index="system-admin/materials">
            <template #title>
              <el-icon><Box /></el-icon>
              <span v-if="!sidebarCollapsed">农资管理</span>
            </template>
            <el-menu-item index="/index/materialInfo">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="!sidebarCollapsed">公司农资库存</span>
            </el-menu-item>
            <el-menu-item index="/index/contractorMaterialInfo">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="!sidebarCollapsed">承包人农资库存</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 设备管理 -->
          <el-sub-menu index="index/equipment">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span v-if="!sidebarCollapsed">设备管理</span>
            </template>
            <el-menu-item index="/index/equipmentInfo">
              <el-icon><Tools /></el-icon>
              <span v-if="!sidebarCollapsed">公司设备信息</span>
            </el-menu-item>
            <el-menu-item index="/index/contractorEquipmentInfo">
              <el-icon><Tools /></el-icon>
              <span v-if="!sidebarCollapsed">承包人设备信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 审批管理 -->
          <el-sub-menu index="index/approval">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span v-if="!sidebarCollapsed">审批管理</span>
            </template>
            <el-menu-item index="/index/planAdjustInfo">
              <el-icon><Timer /></el-icon>
              <span v-if="!sidebarCollapsed">计划调整审批</span>
            </el-menu-item>
            <el-menu-item index="/index/materialApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">农资审批</span>
            </el-menu-item>
            <el-menu-item index="/index/equipmentApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">设备审批</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="index/planting">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span v-if="!sidebarCollapsed">种植管理</span>
            </template>
            <el-menu-item index="/index/plantingRecord">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">种植记录</span>
            </el-menu-item>
            <el-menu-item index="/index/landMonitor">
              <el-icon><Monitor /></el-icon>
              <span v-if="!sidebarCollapsed">农作物监控</span>
            </el-menu-item>
            <el-menu-item index="/index/landActivity">
              <el-icon><List /></el-icon>
              <span v-if="!sidebarCollapsed">农事活动记录</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 成熟作物管理 -->
          <el-sub-menu index="index/matureCrop">
            <template #title>
              <el-icon><Finished /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物管理</span>
            </template>
            <el-menu-item index="/index/matureCropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 农业助手 -->
          <el-menu-item index="/index/agriculturalAssistant">
            <el-icon><ChatDotRound /></el-icon>
            <span v-if="!sidebarCollapsed">农业助手</span>
          </el-menu-item>
        </template>

        <!-- 企业管理员菜单 -->
        <template v-else-if="userRole === 'enterprise_admin'">
          <!-- 承包人管理 -->
          <el-menu-item index="/index/contractorManage">
            <el-icon><User /></el-icon>
            <span v-if="!sidebarCollapsed">承包人管理</span>
          </el-menu-item>

          <!-- 地块管理 -->
          <el-sub-menu index="index/landManage">
            <template #title>
              <el-icon><MapLocation /></el-icon>
              <span v-if="!sidebarCollapsed">地块管理</span>
            </template>
            <el-menu-item index="/index/landInfo">
              <el-icon><Location /></el-icon>
              <span v-if="!sidebarCollapsed">地块信息</span>
            </el-menu-item>
            <el-menu-item index="/index/landAllocationInfo">
              <el-icon><Location /></el-icon>
              <span v-if="!sidebarCollapsed">地块分配情况</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 计划管理 -->
          <el-sub-menu index="system-admin/plan">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span v-if="!sidebarCollapsed">计划管理</span>
            </template>
            <el-menu-item index="/index/cropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">农作物信息</span>
            </el-menu-item>
            <el-menu-item index="/index/plantingPlanInfo">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">计划信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 农资管理 -->
          <el-sub-menu index="system-admin/materials">
            <template #title>
              <el-icon><Box /></el-icon>
              <span v-if="!sidebarCollapsed">农资管理</span>
            </template>
            <el-menu-item index="/index/materialInfo">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="!sidebarCollapsed">公司农资库存</span>
            </el-menu-item>
            <el-menu-item index="/index/contractorMaterialInfo">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="!sidebarCollapsed">承包人农资库存</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 设备管理 -->
          <el-sub-menu index="index/equipment">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span v-if="!sidebarCollapsed">设备管理</span>
            </template>
            <el-menu-item index="/index/equipmentInfo">
              <el-icon><Tools /></el-icon>
              <span v-if="!sidebarCollapsed">公司设备信息</span>
            </el-menu-item>
            <el-menu-item index="/index/contractorEquipmentInfo">
              <el-icon><Tools /></el-icon>
              <span v-if="!sidebarCollapsed">承包人设备信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 审批管理 -->
          <el-sub-menu index="index/approval">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span v-if="!sidebarCollapsed">审批管理</span>
            </template>
            <el-menu-item index="/index/planAdjustInfo">
              <el-icon><Timer /></el-icon>
              <span v-if="!sidebarCollapsed">计划调整审批</span>
            </el-menu-item>
            <el-menu-item index="/index/materialApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">农资审批</span>
            </el-menu-item>
            <el-menu-item index="/index/equipmentApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">设备审批</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="index/planting">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span v-if="!sidebarCollapsed">种植管理</span>
            </template>
            <el-menu-item index="/index/plantingRecord">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">种植记录</span>
            </el-menu-item>
            <el-menu-item index="/index/landMonitor">
              <el-icon><Monitor /></el-icon>
              <span v-if="!sidebarCollapsed">农作物监控</span>
            </el-menu-item>
            <el-menu-item index="/index/landActivity">
              <el-icon><List /></el-icon>
              <span v-if="!sidebarCollapsed">农事活动记录</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 成熟作物管理 -->
          <el-sub-menu index="index/matureCrop">
            <template #title>
              <el-icon><Finished /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物管理</span>
            </template>
            <el-menu-item index="/index/matureCropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 农业助手 -->
          <el-menu-item index="/index/agriculturalAssistant">
            <el-icon><ChatDotRound /></el-icon>
            <span v-if="!sidebarCollapsed">农业助手</span>
          </el-menu-item>
        </template>

        <!-- 承包人菜单 -->
        <template v-else-if="userRole === 'user'">
          <!-- 地块管理 -->
          <el-sub-menu index="index/landManage">
            <template #title>
              <el-icon><MapLocation /></el-icon>
              <span v-if="!sidebarCollapsed">地块管理</span>
            </template>
            <el-menu-item index="/index/landInfo">
              <el-icon><Location /></el-icon>
              <span v-if="!sidebarCollapsed">我的地块</span>
            </el-menu-item>
            <el-menu-item index="/index/landMonitor">
              <el-icon><Monitor /></el-icon>
              <span v-if="!sidebarCollapsed">农作物监控</span>
            </el-menu-item>
            <el-menu-item index="/index/plantingRecord">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">种植记录</span>
            </el-menu-item>
            <el-menu-item index="/index/landActivity">
              <el-icon><List /></el-icon>
              <span v-if="!sidebarCollapsed">农事活动记录</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 计划管理 -->
          <el-sub-menu index="system-admin/plan">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span v-if="!sidebarCollapsed">计划管理</span>
            </template>
            <!-- <el-menu-item index="/index/cropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">农作物信息</span>
            </el-menu-item> -->
            <el-menu-item index="/index/plantingPlanInfo">
              <el-icon><Document /></el-icon>
              <span v-if="!sidebarCollapsed">计划信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 农资管理 -->
          <el-sub-menu index="system-admin/materials">
            <template #title>
              <el-icon><Box /></el-icon>
              <span v-if="!sidebarCollapsed">农资管理</span>
            </template>
            <el-menu-item index="/index/contractorMaterialInfo">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="!sidebarCollapsed">我的农资库存</span>
            </el-menu-item>
            <el-menu-item index="/index/contractorEquipmentInfo">
              <el-icon><Tools /></el-icon>
              <span v-if="!sidebarCollapsed">我的设备信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 审批管理 -->
          <el-sub-menu index="index/approval">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span v-if="!sidebarCollapsed">申请管理</span>
            </template>
            <el-menu-item index="/index/planAdjustInfo">
              <el-icon><Timer /></el-icon>
              <span v-if="!sidebarCollapsed">计划调整申请</span>
            </el-menu-item>
            <el-menu-item index="/index/materialApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">农资申请</span>
            </el-menu-item>
            <el-menu-item index="/index/equipmentApproval">
              <el-icon><Notification /></el-icon>
              <span v-if="!sidebarCollapsed">设备申请</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 成熟作物管理 -->
          <!-- <el-sub-menu index="index/matureCrop">
            <template #title>
              <el-icon><Finished /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物管理</span>
            </template>
            <el-menu-item index="/index/matureCropInfo">
              <el-icon><Crop /></el-icon>
              <span v-if="!sidebarCollapsed">成熟作物信息</span>
            </el-menu-item>
          </el-sub-menu> -->

          <!-- 农业助手 -->
          <el-menu-item index="/index/agriculturalAssistant">
            <el-icon><ChatDotRound /></el-icon>
            <span v-if="!sidebarCollapsed">农业助手</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="nav-left">
          <el-button type="text" @click="toggleSidebar" class="menu-toggle">
            <el-icon><Menu /></el-icon>
          </el-button>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="nav-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar
                :size="32"
                src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
              ></el-avatar>
              <span>{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Setting,
  DataAnalysis,
  User,
  Menu,
  ArrowDown,
  OfficeBuilding,
  MapLocation,
  Calendar,
  Box,
  Cpu,
  Crop,
  ChatDotRound,
  Document,
  Location,
  Timer,
  List,
  Tools,
  ShoppingCart,
  Monitor,
  Finished,
  Notification,
} from '@element-plus/icons-vue'
import $API from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 响应式数据
const sidebarCollapsed = ref(false)
const userRole = ref(localStorage.getItem('role') || '')
const userName = ref('')

// 获取用户名
const fetchUserName = async () => {
  try {
    const res = await $API.getName()
    if (res.data?.data?.name) {
      userName.value = res.data.data.name
    }
  } catch (error) {
    console.error('获取用户名失败:', error)
  }
}

// 计算属性
const activeMenu = computed(() => {
  const path = route.path
  return path
})

const pageTitle = computed(() => {
  const path = route.path
  const titleMap = {
    // 系统管理员
    '/index/userManage': '用户管理',
    '/index/landInfo': '地块信息',
    '/index/cropInfo': '农作物信息',
    '/index/agriculturalAssistant': '农业助手',
    // '/system-admin/plot/monitoring': '农作物监控',
    // '/system-admin/plot/activity': '农事活动记录',
    // '/system-admin/plan/info': '计划信息',
    // '/system-admin/plan/create': '计划制定',
    // '/system-admin/plan/approval': '计划调整审批',
    // '/system-admin/materials/info': '农资信息',
    // '/system-admin/materials/approval': '农资审批',
    // '/system-admin/equipment/info': '设备信息',
    // '/system-admin/equipment/approval': '设备审批',
    // '/system-admin/mature-crop/info': '成熟作物信息',
    // '/system-admin/mature-crop/processing': '加工记录',
    // '/system-admin/mature-crop/product': '产品信息',
    // '/system-admin/assistant': '农业助手',
    // 企业管理员
    // '/enterprise-admin/plot/info': '地块信息',
    // '/enterprise-admin/plot/monitoring': '农作物监控',
    // '/enterprise-admin/plot/activity': '农事活动记录',
    // '/enterprise-admin/plan/info': '计划信息',
    // '/enterprise-admin/plan/create': '计划制定',
    // '/enterprise-admin/plan/approval': '计划调整审批',
    // '/enterprise-admin/contractor/info': '承包人信息',
    // '/enterprise-admin/crop/info': '农作物信息',
    // '/enterprise-admin/materials/info': '农资信息',
    // '/enterprise-admin/materials/approval': '农资审批',
    // '/enterprise-admin/equipment/info': '设备信息',
    // '/enterprise-admin/equipment/approval': '设备审批',
    // '/enterprise-admin/mature-crop/info': '成熟作物信息',
    // '/enterprise-admin/mature-crop/processing': '加工记录',
    // '/enterprise-admin/mature-crop/product': '产品信息',
    // '/enterprise-admin/assistant': '农业助手',
    // 承包人
    // '/contractor/plot/info': '地块信息',
    // '/contractor/plot/monitoring': '农作物监控',
    // '/contractor/plot/activity': '农事活动记录',
    // '/contractor/plan/info': '计划信息',
    // '/contractor/plan/adjustment': '调整申请',
    // '/contractor/materials/info': '农资信息',
    // '/contractor/materials/application': '农资申请',
    // '/contractor/materials/equipment-info': '设备信息',
    // '/contractor/materials/equipment-application': '设备申请',
    // '/contractor/materials/equipment-repair': '设备报修',
    // '/contractor/assistant': '农业助手',
  }
  return titleMap[path] || '农业管理系统'
})

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const goToProfile = () => {
  router.push('/index/profile')
}

const logout = async () => {
  try {
    await $API.logout()
  } catch (error) {
    console.error('登出请求失败:', error)
  } finally {
    // 清除本地存储的token和role
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

// 生命周期
onMounted(() => {
  // 获取用户角色和用户名
  const storedRole = localStorage.getItem('role')
  if (storedRole) {
    userRole.value = storedRole
  }
  fetchUserName()

  // 监听用户名更新事件
  const handleUserNameUpdate = (event) => {
    userName.value = event.detail.userName
  }
  window.addEventListener('userNameUpdated', handleUserNameUpdate)

  // 清理事件监听
  onBeforeUnmount(() => {
    window.removeEventListener('userNameUpdated', handleUserNameUpdate)
  })
})
</script>

<style scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  height: 100%;
  background: linear-gradient(180deg, #001529 0%, #002140 100%);
  color: #fff;
  transition: width 0.3s;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

/* 自定义滚动条样式 */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
  border-radius: 3px;
  transition: background 0.3s;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #66b1ff 0%, #409eff 100%);
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  margin-bottom: 8px;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 80px);
  overflow-y: auto;
  overflow-x: hidden;
}

/* 菜单滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.02);
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(64, 158, 255, 0.5);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(64, 158, 255, 0.8);
}

/* 菜单项样式美化 */
.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: linear-gradient(
    90deg,
    rgba(64, 158, 255, 0.2) 0%,
    rgba(64, 158, 255, 0.1) 100%
  ) !important;
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  color: white !important;
}

.sidebar-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: linear-gradient(
    90deg,
    rgba(64, 158, 255, 0.2) 0%,
    rgba(64, 158, 255, 0.1) 100%
  ) !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 50px !important;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(64, 158, 255, 0.15) !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(
    90deg,
    rgba(64, 158, 255, 0.3) 0%,
    rgba(64, 158, 255, 0.15) 100%
  ) !important;
  color: #409eff !important;
}

/* 图标样式 */
.sidebar-menu :deep(.el-icon) {
  font-size: 18px;
  margin-right: 8px;
  transition: transform 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon),
.sidebar-menu :deep(.el-sub-menu__title:hover .el-icon) {
  transform: scale(1.1);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

/* 顶部导航栏 */
.top-nav {
  height: 50px;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  flex-shrink: 0;
}

.nav-left {
  display: flex;
  align-items: center;
}

.menu-toggle {
  margin-right: 16px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info span {
  margin: 0 8px;
}

/* 内容区域 */
.content-wrapper {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: #f0f2f5;
  min-height: 0;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 999;
  }

  .sidebar.collapsed {
    left: -240px;
  }
}
</style>
