<template>
  <div class="user-management">
    <div class="header">
      <h1>系统管理员 - 用户管理</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入姓名"
        style="width: 300px"
        clearable
        @clear="handleReset"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <!-- <el-button-group class="mb-4">
        <el-button type="primary">查询</el-button>
        <el-button type="warning"> 重置 </el-button>
      </el-button-group> -->
      <el-button type="primary" @click="handleSearch"> 查询 </el-button>
      <el-button type="warning" @click="handleReset"> 重置 </el-button>
    </div>

    <div class="table-container">
      <el-table
        :data="filteredUsers"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column prop="userId" label="用户ID" width="120" align="center" />
        <el-table-column prop="username" label="用户名" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="120" align="center" />
        <el-table-column prop="role" label="用户角色" width="120" align="center">
          <template #default="scope">
            <span :class="roleClass(scope.row.role)">
              {{ roleText(scope.row.role) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="password" label="密码" width="120" align="center" />
        <el-table-column prop="phone" label="手机号" width="120" align="center">
          <template #default="scope">
            {{ scope.row.phone || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" align="center">
          <template #default="scope">
            {{ scope.row.email || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <span :class="statusClass(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" size="small" @click="toggleUserStatus(scope.row)">
              <el-icon v-if="scope.row.status === 1"><Lock /></el-icon>
              <el-icon v-else><Unlock /></el-icon>
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="confirmDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-count="Math.ceil(total / pageSize)"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="用户ID" v-if="formData.userId">
          <el-input v-model="formData.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="text" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option label="系统管理员" value="system_admin" />
            <el-option label="企业管理员" value="enterprise_admin" />
            <el-option label="承包人" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="冻结" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除用户 {{ deleteUserInfo.username }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteUser">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Edit, Delete, Search, Lock, Unlock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import $API from '@/api'

// 用户数据
const userList = ref([])
const total = ref(0)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref('添加用户')

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// 表单数据
const formData = ref({
  userId: '',
  username: '',
  password: '',
  name: '',
  role: '',
  status: '1',
  phone: '',
  email: '',
})

// 待删除用户
const deleteUserInfo = ref({})

// 加载状态
const loading = ref(false)

// 过滤后的用户列表
const filteredUsers = computed(() => {
  return userList.value
})

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    let response
    if (searchQuery.value) {
      response = await $API.searchUsersByPage(searchQuery.value, currentPage.value, pageSize.value)
    } else {
      response = await $API.getUsersByPage(currentPage.value, pageSize.value)
    }
    if (response.data && response.data.code === '200') {
      userList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加用户'
  formData.value = {
    userId: '',
    username: '',
    password: '',
    name: '',
    role: '',
    status: '1',
    phone: '',
    email: '',
  }
  dialogVisible.value = true
  // 清除表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const openEditDialog = (user) => {
  dialogTitle.value = '编辑用户'
  formData.value = {
    ...user,
    // 确保状态值为字符串，与下拉框选项匹配
    status: user.status.toString(),
  }
  dialogVisible.value = true
  // 清除表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 确认删除
const confirmDelete = (user) => {
  deleteUserInfo.value = user
  deleteDialogVisible.value = true
}

// 保存用户
const saveUser = async () => {
  // 验证表单
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const userData = {
          ...formData.value,
          // 将状态转换为数字
          status: Number(formData.value.status),
        }
        if (formData.value.userId) {
          // 编辑现有用户
          // 调用后端API更新用户
          await $API.updateUser(userData)
          ElMessage({
            message: '编辑用户成功',
            type: 'success',
          })
        } else {
          // 添加新用户
          // 调用后端API创建用户
          await $API.addUser(userData)
          ElMessage({
            message: '添加用户成功',
            type: 'success',
          })
        }

        // 重新加载用户列表
        await loadUsers()
        dialogVisible.value = false
      } catch (error) {
        console.error('保存用户失败:', error)
        ElMessage({
          message: '保存用户失败',
          type: 'error',
        })
      }
    }
  })
}

// 删除用户
const deleteUser = async () => {
  try {
    // 调用后端API删除用户
    await $API.deleteUser(deleteUserInfo.value.userId)
    // 重新加载用户列表
    await loadUsers()
    deleteDialogVisible.value = false
    ElMessage({
      message: '删除用户成功',
      type: 'success',
    })
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage({
      message: '删除用户失败',
      type: 'error',
    })
  }
}

// 搜索处理
const handleSearch = async () => {
  console.log('搜索关键词:', searchQuery.value)
  currentPage.value = 1
  await loadUsers()
}

// 重置搜索
const handleReset = async () => {
  searchQuery.value = ''
  currentPage.value = 1
  await loadUsers()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadUsers()
}

// 角色文本映射
const roleText = (role) => {
  const roleMap = {
    system_admin: '系统管理员',
    enterprise_admin: '企业管理员',
    user: '承包人',
  }
  return roleMap[role] || role
}

// 角色样式映射
const roleClass = (role) => {
  const classMap = {
    system_admin: 'role-system-admin',
    enterprise_admin: 'role-enterprise-admin',
    user: 'role-user',
  }
  return classMap[role] || ''
}

// 状态文本映射
const statusText = (status) => {
  return status === 1 ? '正常' : '冻结'
}

// 状态样式映射
const statusClass = (status) => {
  return status === 1 ? 'status-normal' : 'status-frozen'
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    // 调用后端API更新状态
    await $API.updateUserStatus(user.userId, newStatus)
    // 重新加载用户列表
    await loadUsers()
    ElMessage({
      message: '状态切换成功',
      type: 'success',
    })
  } catch (error) {
    console.error('切换用户状态失败:', error)
    ElMessage({
      message: '状态切换失败',
      type: 'error',
    })
  }
}

// 初始化加载
onMounted(() => {
  console.log('组件挂载，开始加载用户数据')
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-icon {
  color: #909399;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-table {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-button {
  margin-right: 8px;
}

.table-container {
  height: calc(100vh - 280px);
  overflow: auto;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 美化滚动条 */
.table-container::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.table-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  border: 2px solid #fff;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  border: 2px solid #f0f7ff;
}

.table-container::-webkit-scrollbar-corner {
  background: #f1f1f1;
}

/* Firefox 滚动条样式 */
.table-container {
  scrollbar-width: thin;
  scrollbar-color: #764ba2 #f1f1f1;
}

/* 角色样式 */
.role-system-admin {
  color: #409eff;
  font-weight: 600;
}

.role-enterprise-admin {
  color: #67c23a;
  font-weight: 600;
}

.role-user {
  color: #e6a23c;
  font-weight: 600;
}

/* 状态样式 */
.status-normal {
  color: #67c23a;
  font-weight: 600;
}

.status-frozen {
  color: #f56c6c;
  font-weight: 600;
}
</style>
