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
          <el-dropdown trigger="click" popper-class="user-dropdown-popper">
            <div class="user-info">
              <span class="avatar-ring">
                <el-avatar
                  :size="34"
                  :src="userAvatar"
                >{{ userName?.charAt(0) }}</el-avatar>
              </span>
              <div class="user-meta">
                <span class="user-name">{{ userName }}</span>
                <span class="user-role">{{ roleLabel }}</span>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User" @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item :icon="SwitchButton" divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 多标签窗口栏 -->
      <div class="tabs-bar">
        <div
          v-for="tab in openTabs"
          :key="tab.path"
          class="tab-item"
          :class="{ active: tab.path === route.path }"
          @click="router.push(tab.path)"
        >
          <span class="tab-title">{{ tab.title }}</span>
          <el-icon class="tab-close" @click.stop="closeTab(tab)">
            <Close />
          </el-icon>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
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
  Close,
  SwitchButton,
} from '@element-plus/icons-vue'
import $API from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 响应式数据
const sidebarCollapsed = ref(false)
const userRole = ref(localStorage.getItem('role') || '')
const userName = ref(localStorage.getItem('nickname') || localStorage.getItem('username') || '')
const userAvatar = ref(localStorage.getItem('avatar') || '')

// 角色中文标签
const roleLabel = computed(
  () =>
    ({
      system_admin: '系统管理员',
      enterprise_admin: '企业管理员',
      user: '承包人',
    })[userRole.value] || '用户'
)

// 获取用户名
const fetchUserName = async () => {
  try {
    const res = await $API.getName()
    if (res.data?.data?.name) {
      userName.value = res.data.data.name
      localStorage.setItem('nickname', res.data.data.name)
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

const titleMap = {
  '/index/userManage': '用户管理',
  '/index/landInfo': '地块信息',
  '/index/landAllocationInfo': '地块分配情况',
  '/index/cropInfo': '农作物信息',
  '/index/plantingPlanInfo': '计划信息',
  '/index/materialInfo': '公司农资库存',
  '/index/contractorMaterialInfo': '承包人农资库存',
  '/index/equipmentInfo': '公司设备信息',
  '/index/contractorEquipmentInfo': '承包人设备信息',
  '/index/planAdjustInfo': '计划调整审批',
  '/index/materialApproval': '农资审批',
  '/index/equipmentApproval': '设备审批',
  '/index/plantingRecord': '种植记录',
  '/index/landMonitor': '农作物监控',
  '/index/landActivity': '农事活动记录',
  '/index/matureCropInfo': '成熟作物信息',
  '/index/agriculturalAssistant': '农业助手',
  '/index/contractorManage': '承包人管理',
  '/index/profile': '个人中心',
}

const pageTitle = computed(() => titleMap[route.path] || '农业管理系统')

// 多标签窗口：点击菜单自动打开对应窗口，点击标签切换，点击 x 关闭
const openTabs = ref([])

const closeTab = (tab) => {
  const index = openTabs.value.findIndex((item) => item.path === tab.path)
  if (index === -1) return
  openTabs.value.splice(index, 1)
  if (route.path !== tab.path) return
  const next = openTabs.value[index - 1] || openTabs.value[index]
  router.push(next ? next.path : '/index/landInfo')
}

watch(
  () => route.path,
  (path) => {
    if (path === '/index' || path === '/login') return
    if (!openTabs.value.some((item) => item.path === path)) {
      openTabs.value.push({ path, title: titleMap[path] || '农业管理系统' })
    }
  },
  { immediate: true }
)

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
    ;['token', 'id', 'username', 'nickname', 'avatar', 'role'].forEach((key) => localStorage.removeItem(key))
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
  const handleUserInfoUpdate = (event) => {
    const data = event.detail || {}
    userName.value = data.nickname || data.name || data.username || userName.value
    userAvatar.value = data.avatar || userAvatar.value
  }
  window.addEventListener('userInfoUpdated', handleUserInfoUpdate)

  // 清理事件监听
  onBeforeUnmount(() => {
    window.removeEventListener('userNameUpdated', handleUserNameUpdate)
    window.removeEventListener('userInfoUpdated', handleUserInfoUpdate)
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
  width: 0;
}

.sidebar.collapsed .logo,
.sidebar.collapsed .sidebar-menu {
  visibility: hidden;
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
  background: url('/bg_picture.jpg') center/cover no-repeat fixed;
}

/* 顶部导航栏 */
.top-nav {
  height: 50px;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(232, 232, 232, 0.6);
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
  gap: 10px;
  cursor: pointer;
  padding: 5px 14px 5px 6px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(64, 158, 255, 0.18);
  transition: all 0.3s ease;
  outline: none;
}

.user-info:hover,
.user-info:focus-visible {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.92);
  border-color: rgba(64, 158, 255, 0.45);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.25);
}

.avatar-ring {
  display: inline-flex;
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  transition: transform 0.3s ease;
}

.user-info:hover .avatar-ring {
  transform: scale(1.06);
}

.avatar-ring :deep(.el-avatar) {
  border: 2px solid #fff;
  background: #409eff;
  font-weight: 600;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: #909399;
}

.arrow-icon {
  color: #909399;
  font-size: 12px;
  transition: transform 0.3s ease;
}

.user-info:hover .arrow-icon {
  transform: translateY(2px);
}

/* 多标签窗口栏 */
.tabs-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(232, 232, 232, 0.6);
  flex-shrink: 0;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 10px 0 14px;
  border-radius: 15px;
  font-size: 13px;
  color: #606266;
  background: #f0f2f5;
  border: 1px solid transparent;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #409eff;
  background: #ecf5ff;
}

.tab-item.active {
  color: #fff;
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

.tab-close {
  font-size: 12px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  transition: all 0.2s;
}

.tab-close:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: scale(1.15);
}

/* 内容区域 */
.content-wrapper {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
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

<style>
/* 右上角用户下拉面板（teleport 到 body，需全局样式） */
.user-dropdown-popper .el-dropdown-menu {
  padding: 6px 0;
  border-radius: 12px;
  overflow: hidden;
}
.user-dropdown-popper .el-dropdown-menu__item {
  padding: 10px 16px;
  font-size: 13px;
  color: #303133;
  transition: all 0.2s ease;
}
.user-dropdown-popper .el-dropdown-menu__item:not(.is-disabled):hover {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1), rgba(102, 177, 255, 0.18));
  color: #409eff;
  padding-left: 20px;
}
.user-dropdown-popper .el-dropdown-menu__item--divided {
  border-top: 1px solid #f0f2f5;
  margin-top: 0;
}
.user-dropdown-popper .el-dropdown-menu__item--divided::before {
  display: none;
}
</style>
 
