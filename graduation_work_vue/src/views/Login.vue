<template>
  <div class="login-page">
    <section class="hero">
      <div class="hero-mask"></div>
      <div class="hero-copy">
        <h1>Modern Agriculture Management</h1>
        <p>Unified control for land, crops, materials, equipment, and approvals.</p>
      </div>
    </section>

    <section class="panel">
      <el-card class="login-card" shadow="never">
        <div class="title">Login</div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="Username, email, or phone"
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="Password"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" class="login-button" :loading="loginLoading" @click="handleLogin">
            Login
          </el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import $API from '@/api'

const router = useRouter()
const loginFormRef = ref()
const loginLoading = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
})

const loginRules = {
  account: [{ required: true, message: 'Enter a username, email, or phone number', trigger: 'blur' }],
  password: [{ required: true, message: 'Enter a password', trigger: 'blur' }],
}

const normalizeRole = (role) => (role || '').toString().toLowerCase()

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loginLoading.value = true
  try {
    const res = await $API.login({
      account: loginForm.account,
      password: loginForm.password,
    })

    if (res.data?.code !== 200 && res.data?.code !== '200') {
      throw new Error(res.data?.message || res.data?.msg || 'Login failed')
    }

    const tokenData = res.data?.data || {}
    if (!tokenData.token) {
      throw new Error('Login response is missing a token')
    }

    localStorage.setItem('token', tokenData.token)
    localStorage.setItem('role', normalizeRole(tokenData.role))
    if (tokenData.username) {
      localStorage.setItem('username', tokenData.username)
    }

    ElMessage.success('Login successful')

    const role = normalizeRole(tokenData.role)
    if (role === 'system_admin') {
      router.push('/index/userManage')
    } else if (role === 'enterprise_admin') {
      router.push('/index/contractorManage')
    } else {
      router.push('/index/landInfo')
    }
  } catch (error) {
    ElMessage.error(error?.message || 'Login failed')
  } finally {
    loginLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(360px, 0.9fr);
  background: #0f172a;
}

.hero {
  position: relative;
  min-height: 100vh;
  background: url('../assets/login-bg.jpg') center/cover no-repeat;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(3, 7, 18, 0.45), rgba(15, 23, 42, 0.72));
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 620px;
  padding: 96px 64px;
  color: #fff;
}

.hero-copy h1 {
  margin: 0 0 16px;
  font-size: 44px;
  line-height: 1.1;
}

.hero-copy p {
  margin: 0;
  font-size: 18px;
  line-height: 1.8;
  max-width: 32rem;
}

.panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #f8fafc;
}

.login-card {
  width: min(100%, 420px);
  border: 0;
  border-radius: 8px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .hero {
    min-height: 260px;
  }

  .hero-copy {
    padding: 48px 24px;
  }

  .panel {
    min-height: calc(100vh - 260px);
  }
}
</style>
