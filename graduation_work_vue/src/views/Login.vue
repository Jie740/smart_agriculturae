<template>
  <div class="login-page">
    <div class="bg-layer"></div>
    <div class="bg-mask"></div>

    <section class="hero">
      <div class="hero-copy">
        <p class="hero-eyebrow">Smart Agriculture Platform</p>
        <h1>智慧农业管理系统</h1>
        <p class="hero-slogan">数字化管控 · 可视化监测 · 规范化审批</p>
        <p class="hero-desc">
          统一管理土地、作物、农资物料、智能设备、生产人员、农事任务、预警监测与审批流程，打通田间采集、计划排产、库存流转、作业执行、报表溯源全链路，助力降本增效、科学种植。
        </p>
        <ul class="hero-features">
          <li><span class="dot"></span>土地与作物</li>
          <li><span class="dot"></span>农资与设备</li>
          <li><span class="dot"></span>农事与审批</li>
          <li><span class="dot"></span>预警与溯源</li>
        </ul>
      </div>
    </section>

    <section class="panel">
      <el-card class="login-card" shadow="never">
        <div class="card-mark">
          <el-icon :size="26"><Sunny /></el-icon>
        </div>
        <div class="title">欢迎登录</div>
        <p class="subtitle">登录后进入智慧农业管理平台</p>

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
            登 录
          </el-button>
        </el-form>

        <p class="card-tip">智慧农业 · 数字化生产管理平台</p>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Sunny, User } from '@element-plus/icons-vue'
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
    if (tokenData.id) localStorage.setItem('id', String(tokenData.id))
    localStorage.setItem('role', normalizeRole(tokenData.role))
    if (tokenData.username) {
      localStorage.setItem('username', tokenData.username)
    }
    if (tokenData.nickname || tokenData.name) {
      localStorage.setItem('nickname', tokenData.nickname || tokenData.name)
    }
    localStorage.setItem('avatar', tokenData.avatar || '')
    window.dispatchEvent(new CustomEvent('userInfoUpdated', { detail: tokenData }))

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
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(360px, 0.9fr);
  background: #0f172a;
  overflow: hidden;
}

.bg-layer {
  position: fixed;
  inset: 0;
  background: url('../assets/login-bg.jpg') center/cover no-repeat;
  opacity: 0.55;
  z-index: 0;
}

.bg-mask {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, rgba(3, 7, 18, 0.55), rgba(15, 23, 42, 0.75));
  z-index: 1;
}

.hero {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 620px;
  padding: 96px 64px;
  color: #fff;
}

.hero-eyebrow {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.32em;
  text-transform: uppercase;
  color: #86efac;
}

.hero-copy h1 {
  margin: 0 0 12px;
  font-size: 46px;
  line-height: 1.15;
  font-weight: 700;
  letter-spacing: 2px;
}

.hero-slogan {
  margin: 0 0 24px;
  font-size: 16px;
  letter-spacing: 4px;
  color: rgba(255, 255, 255, 0.88);
}

.hero-desc {
  margin: 0;
  font-size: 16px;
  line-height: 1.9;
  max-width: 32rem;
  text-align: justify;
  color: rgba(255, 255, 255, 0.75);
}

.hero-features {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 28px;
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
}

.hero-features li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.hero-features .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 0 3px rgba(74, 222, 128, 0.25);
}

.panel {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.login-card {
  width: min(100%, 420px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: 0 20px 50px rgba(2, 8, 20, 0.45);
}

.login-card :deep(.el-card__body) {
  padding: 40px 36px 32px;
}

.card-mark {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #16a34a, #4ade80);
  box-shadow: 0 8px 20px rgba(22, 163, 74, 0.4);
}

.title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #ffffff;
}

.subtitle {
  margin: 8px 0 28px;
  font-size: 13px;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.65);
}

.login-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  border-radius: 10px;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #4ade80 inset;
  background: rgba(255, 255, 255, 0.16);
}

.login-card :deep(.el-input__inner) {
  color: #fff;
}

.login-card :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.45);
}

.login-card :deep(.el-input__prefix),
.login-card :deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.7);
}

.login-button {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border: 0;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #16a34a, #22c55e);
  box-shadow: 0 10px 24px rgba(22, 163, 74, 0.4);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(22, 163, 74, 0.5);
}

.card-tip {
  margin: 24px 0 0;
  text-align: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.5);
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

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-slogan {
    font-size: 14px;
    letter-spacing: 2px;
  }

  .panel {
    min-height: calc(100vh - 260px);
  }
}
</style>
