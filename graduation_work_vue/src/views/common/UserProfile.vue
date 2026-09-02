<template>
  <div class="user-profile">
    <div class="profile-header">
      <div class="header-content">
        <div class="avatar-container">
          <el-avatar :size="80" :src="avatarUrl" class="user-avatar">
            {{ userInfo.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-ring"></div>
        </div>
        <div class="header-text">
          <h1>个人中心</h1>
          <p class="welcome-text">欢迎回来，{{ userInfo.name || '用户' }}</p>
        </div>
      </div>
    </div>

    <div class="profile-content" v-loading="loading">
      <el-card class="info-card" v-if="!isEditing">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="startEdit" class="edit-btn">
                <el-icon><Edit /></el-icon>
                修改信息
              </el-button>
              <el-button type="warning" @click="openPasswordDialog" class="password-btn">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-button>
            </div>
          </div>
        </template>
        <el-descriptions :column="2" border class="info-descriptions">
          <el-descriptions-item label="用户ID">
            <el-tag type="info" effect="plain">{{ userInfo.userId }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">
            <span class="highlight-value">{{ userInfo.name }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(userInfo.role)" effect="dark">
              {{ getRoleText(userInfo.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            <span :class="userInfo.phone ? 'highlight-value' : 'empty-value'">
              {{ userInfo.phone || '未填写' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            <span :class="userInfo.email ? 'highlight-value' : 'empty-value'">
              {{ userInfo.email || '未填写' }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="edit-card" v-else>
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><Edit /></el-icon>
              <span>修改信息</span>
            </div>
            <div class="header-actions">
              <el-button @click="cancelEdit" class="cancel-btn">取消</el-button>
              <el-button type="primary" @click="saveEdit" class="save-btn">保存</el-button>
            </div>
          </div>
        </template>
        <el-form :model="editForm" label-width="100px" class="edit-form">
          <el-form-item label="用户ID">
            <el-input v-model="editForm.userId" disabled>
              <template #prefix
                ><el-icon><Key /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" placeholder="请输入用户名">
              <template #prefix
                ><el-icon><User /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="editForm.name" placeholder="请输入姓名">
              <template #prefix
                ><el-icon><Postcard /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="角色">
            <el-input v-model="editForm.role" disabled>
              <template #prefix
                ><el-icon><UserFilled /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" placeholder="请输入手机号">
              <template #prefix
                ><el-icon><Phone /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱">
              <template #prefix
                ><el-icon><Message /></el-icon
              ></template>
            </el-input>
          </el-form-item>
        </el-form>
      </el-card>

      <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordFormRules"
          label-width="100px"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              show-password
            >
              <template #prefix
                ><el-icon><Lock /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            >
              <template #prefix
                ><el-icon><Key /></el-icon
              ></template>
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            >
              <template #prefix
                ><el-icon><Key /></el-icon
              ></template>
            </el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePassword">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Edit,
  Key,
  Postcard,
  UserFilled,
  Phone,
  Message,
  Lock,
} from '@element-plus/icons-vue'
import $API from '@/api'

const loading = ref(false)
const isEditing = ref(false)
const userInfo = ref({})
const avatarUrl = ref('')

const editForm = ref({
  userId: '',
  username: '',
  name: '',
  role: '',
  phone: '',
  email: '',
})

const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const passwordFormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const getRoleText = (role) => {
  const map = {
    user: '承包人',
    enterprise_admin: '企业管理员',
    system_admin: '系统管理员',
  }
  return map[role] || role
}

const getRoleTagType = (role) => {
  const map = {
    user: 'success',
    enterprise_admin: 'warning',
    system_admin: 'danger',
  }
  return map[role] || 'info'
}

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await $API.getUserInfo()
    if (res.data?.data) {
      userInfo.value = res.data.data
      editForm.value = { ...res.data.data }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  editForm.value = { ...userInfo.value }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveEdit = async () => {
  loading.value = true
  try {
    const updateData = {
      userId: editForm.value.userId,
      username: editForm.value.username,
      name: editForm.value.name,
      phone: editForm.value.phone,
      email: editForm.value.email,
    }
    const res = await $API.updateUser(updateData)
    if (res.data?.code === 500 || res.data?.code === '500') {
      throw new Error(res.data?.message || res.data?.msg || '修改失败')
    }
    ElMessage.success('修改成功')
    isEditing.value = false
    fetchUserInfo()

    // 重新获取用户姓名并更新
    try {
      const nameRes = await $API.getName()
      if (nameRes.data?.data?.name) {
        const newName = nameRes.data.data.name
        window.dispatchEvent(new CustomEvent('userNameUpdated', { detail: { userName: newName } }))
      }
    } catch (nameError) {
      console.error('获取姓名失败:', nameError)
    }
  } catch (error) {
    console.error('修改失败:', error)
    const errorMsg =
      error.response?.data?.message || error.response?.data?.msg || error.message || '修改失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  passwordDialogVisible.value = true
  nextTick(() => {
    passwordFormRef.value?.clearValidate()
  })
}

const savePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await $API.updatePassword({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword,
      })
      if (res.data.code === '200') {
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
      } else {
        ElMessage.error(res.data.message || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || '密码修改失败')
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-profile {
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  min-height: calc(100vh - 60px);
}

.profile-header {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  font-size: 32px;
  font-weight: 700;
  color: white;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border: 3px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.15);
    opacity: 0;
  }
}

.header-text h1 {
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-text {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.profile-content {
  max-width: 900px;
  margin: 0 auto;
}

.info-card,
.edit-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.info-card:hover,
.edit-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 18px 24px;
  border-bottom: none;
}

:deep(.el-card__body) {
  padding: 28px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.header-title .el-icon {
  font-size: 22px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.edit-btn,
.save-btn,
.cancel-btn,
.password-btn {
  border-radius: 20px;
  padding: 10px 20px;
}

:deep(.el-descriptions) {
  margin-top: 8px;
}

:deep(.el-descriptions__label) {
  background-color: #f0f2f5;
  color: #606266;
  font-weight: 600;
}

:deep(.el-descriptions-item__content) {
  color: #303133;
}

.highlight-value {
  color: #409eff;
  font-weight: 600;
}

.empty-value {
  color: #c0c4cc;
  font-style: italic;
}

:deep(.el-tag) {
  border-radius: 12px;
}

.edit-form {
  max-width: 500px;
  margin: 0 auto;
  padding-top: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 600;
  padding-left: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  padding: 4px 11px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-input__prefix .el-icon) {
  color: #909399;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
  cursor: not-allowed;
  border-radius: 8px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 20px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #7c8ff5 0%, #8a5db3 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

:deep(.el-button--default) {
  border-radius: 20px;
  transition: all 0.3s ease;
}

:deep(.el-button--default:hover) {
  transform: translateY(-1px);
}
</style>
