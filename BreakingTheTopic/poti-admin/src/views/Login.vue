<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="deco-dots"></div>
    <div class="deco-blob blob-1"></div>
    <div class="deco-blob blob-2"></div>

    <!-- 居中登录卡片 -->
    <div class="login-card">
      <img class="card-logo" src="../static/log.png" alt="破题管理系统 Logo" />
      <h1 class="card-title">破题管理系统</h1>
      <p class="card-subtitle">{{ isLogin ? '后台管理登录' : '管理员注册' }}</p>

      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item v-if="isLogin" label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Key"
              class="captcha-input"
              @keyup.enter="handleSubmit"
            />
            <div class="captcha-box">
              <img
                v-if="captchaImg"
                :src="captchaImg"
                class="captcha-img"
                title="点击刷新验证码"
                @click="loadCaptcha"
              />
              <el-icon class="captcha-refresh" :size="18" @click="loadCaptcha"><Refresh /></el-icon>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="!isLogin" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="!isLogin" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称"
            size="large"
            :prefix-icon="UserFilled"
          />
        </el-form-item>
        <el-form-item v-if="!isLogin" prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱（选填）"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item v-if="!isLogin" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号（选填）"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item v-if="!isLogin" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="avatar-tip">点击上传头像（选填）</div>
        </el-form-item>

        <el-form-item v-if="isLogin">
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我（7天内免登录）</el-checkbox>
            <span class="forgot-link" @click="handleForgot">忘记密码？</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleSubmit"
            class="login-btn"
          >
            {{ loading ? (isLogin ? '登录中...' : '注册中...') : (isLogin ? '登 录' : '注 册') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="security-strip">
        <el-icon :size="14"><Lock /></el-icon>
        <span>为保障系统安全，请勿在公共设备上保存登录状态</span>
      </div>

      <div class="switch-link">
        <span @click="toggleMode">
          {{ isLogin ? '没有账号？立即注册' : '已有账号？立即登录' }}
        </span>
      </div>
    </div>

    <!-- 卡外版权 -->
    <p class="login-footer">
      <el-icon :size="12"><Lock /></el-icon>
      © 2026 破题管理系统 版权所有 | 京ICP备12345678号-1
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Message, Phone, Plus, Key, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const isLogin = ref(true)
const uploadUrl = ref('http://localhost:8088/admin/upload/avatar')

const captchaImg = ref('')
const captchaKey = ref('')

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

const loadCaptcha = async () => {
  try {
    const res = await api.get('/admin/captcha')
    captchaKey.value = res.data.captchaKey
    captchaImg.value = res.data.captchaImage
  } catch (e) {
    console.error('获取验证码失败', e)
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!isLogin.value) {
    if (!value) {
      callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(value)) {
      callback(new Error('请输入正确的邮箱格式'))
    } else {
      callback()
    }
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
  } else {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) {
      callback(new Error('请输入正确的手机号格式'))
    } else {
      callback()
    }
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

onMounted(() => {
  const savedUsername = localStorage.getItem('admin_remember_username')
  if (savedUsername) {
    form.username = savedUsername
    rememberMe.value = true
  }
  loadCaptcha()
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
  const savedUsername = localStorage.getItem('admin_remember_username')
  if (savedUsername && isLogin.value) {
    form.username = savedUsername
  }
}

const handleForgot = () => {
  ElMessage.info('请联系超级管理员重置密码')
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    form.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    loading.value = true
    try {
      if (isLogin.value) {
        await handleLogin()
      } else {
        await handleRegister()
      }
    } catch (error) {
      console.error('提交失败：', error)
    } finally {
      loading.value = false
    }
  })
}

const handleLogin = async () => {
  try {
    const res = await api.post('/admin/login', {
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })

    if (rememberMe.value) {
      localStorage.setItem('admin_remember_username', form.username)
    } else {
      localStorage.removeItem('admin_remember_username')
    }

    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 验证码一次性使用，失败后刷新
    form.captchaCode = ''
    loadCaptcha()
    throw e
  }
}

const handleRegister = async () => {
  const registerData = {
    username: form.username,
    password: form.password,
    nickname: form.nickname
  }

  if (form.email) registerData.email = form.email
  if (form.phone) registerData.phone = form.phone
  if (form.avatar) registerData.avatar = form.avatar

  await api.post('/admin/register', registerData)

  ElMessage.success('注册成功，请登录')
  isLogin.value = true
  formRef.value?.resetFields()
  const savedUsername = localStorage.getItem('admin_remember_username')
  if (savedUsername) {
    form.username = savedUsername
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #F0F6FF 0%, #E8F1FE 100%);
  position: relative;
  overflow: hidden;
  padding: 24px 16px;
}

/* ===== 背景装饰 ===== */
.deco-dots {
  position: absolute;
  top: 0;
  left: 0;
  width: 420px;
  height: 420px;
  background-image: radial-gradient(rgba(30, 64, 175, 0.14) 1.5px, transparent 1.5px);
  background-size: 20px 20px;
  -webkit-mask-image: radial-gradient(circle at top left, #000 0%, transparent 72%);
  mask-image: radial-gradient(circle at top left, #000 0%, transparent 72%);
  pointer-events: none;
}

.deco-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}

.blob-1 {
  width: 480px;
  height: 480px;
  background: rgba(14, 165, 233, 0.10);
  bottom: -160px;
  right: -120px;
}

.blob-2 {
  width: 380px;
  height: 380px;
  background: rgba(30, 64, 175, 0.08);
  top: -120px;
  right: 28%;
}

/* ===== 登录卡片 ===== */
.login-card {
  width: 100%;
  max-width: 460px;
  background: var(--color-card);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(30, 64, 175, 0.12), 0 4px 16px rgba(15, 23, 42, 0.05);
  padding: 44px 40px 32px;
  position: relative;
  z-index: 1;
}

.card-logo {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: block;
  margin: 0 auto 16px;
  box-shadow: 0 8px 20px rgba(30, 64, 175, 0.22);
}

.card-title {
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
  margin: 0;
  text-align: center;
  letter-spacing: 1px;
}

.card-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 8px 0 0 0;
  text-align: center;
}

.login-form {
  margin-top: 28px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

/* 顶部标签样式 */
.login-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  line-height: 1;
  margin-bottom: 8px;
  padding: 0 0 0 0;
}

/* 隐藏必填星号，与原型一致 */
.login-form :deep(.el-form-item.is-required .el-form-item__label::before),
.login-form :deep(.el-form-item.is-required .el-form-item__label::after) {
  display: none;
}

.login-form :deep(.el-input__wrapper) {
  height: 44px;
  padding: 0 15px;
  border-radius: var(--radius-input);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(30, 64, 175, 0.12);
}

/* ===== 验证码 ===== */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-box {
  display: flex;
  align-items: center;
  height: 44px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  overflow: hidden;
  flex-shrink: 0;
  background: #F8FAFC;
}

.captcha-img {
  height: 100%;
  width: 118px;
  display: block;
  cursor: pointer;
  border-right: 1px solid var(--color-border);
}

.captcha-refresh {
  padding: 0 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.captcha-refresh:hover {
  color: var(--color-primary);
}

/* ===== 记住我 / 忘记密码 ===== */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.forgot-link {
  font-size: 14px;
  color: var(--color-accent);
  cursor: pointer;
}

.forgot-link:hover {
  opacity: 0.8;
}

/* ===== 登录按钮 ===== */
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: var(--radius-btn);
  background-color: var(--color-primary);
  border: none;
}

.login-btn:hover:not(.is-loading) {
  background-color: var(--color-primary-hover);
}

/* ===== 安全提示条 ===== */
.security-strip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 12px 14px;
  background: #EFF6FF;
  border-radius: var(--radius-input);
  font-size: 12.5px;
  color: rgba(30, 64, 175, 0.85);
}

.security-strip .el-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

/* ===== 切换注册 ===== */
.switch-link {
  text-align: center;
  margin-top: 16px;
}

.switch-link span {
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.switch-link span:hover {
  color: var(--color-primary);
}

/* ===== 卡外版权 ===== */
.login-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 24px 0 0 0;
  font-size: 12px;
  color: var(--color-disabled);
  position: relative;
  z-index: 1;
}

/* ===== 头像上传（注册） ===== */
.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--color-disabled);
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.avatar-tip {
  text-align: center;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 8px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .login-card {
    padding: 36px 24px 24px;
  }

  .deco-dots,
  .deco-blob {
    display: none;
  }
}
</style>
