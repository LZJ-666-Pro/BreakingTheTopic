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
            autocomplete="off"
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
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item v-if="isLogin && captchaEnabled" label="验证码" prop="captchaCode">
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
              <svg class="captcha-refresh" viewBox="0 0 24 24" fill="none" @click="loadCaptcha">
                <path d="M21 12a9 9 0 1 1-2.64-6.36M21 3v6h-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
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
            autocomplete="new-password"
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
    </div>

    <!-- 忘记密码：邮箱验证码重置 -->
    <el-dialog v-model="fpDialog" width="400px" :show-close="true" :close-on-click-modal="false"
      append-to-body class="fp-dialog">
      <template #header>
        <div class="fp-header">
          <div class="fp-header-icon">
            <el-icon :size="20"><Lock /></el-icon>
          </div>
          <div>
            <h3 class="fp-title">找回密码</h3>
            <p class="fp-subtitle">通过账号绑定邮箱验证身份后重置</p>
          </div>
        </div>
        <div class="fp-steps">
          <div class="fp-step" :class="{ active: fpStep >= 1, done: fpStep > 1 }">
            <span class="fp-step-dot">{{ fpStep > 1 ? '✓' : '1' }}</span>
            <span class="fp-step-label">验证身份</span>
          </div>
          <span class="fp-step-line" :class="{ done: fpStep > 1 }"></span>
          <div class="fp-step" :class="{ active: fpStep >= 2 }">
            <span class="fp-step-dot">2</span>
            <span class="fp-step-label">设置新密码</span>
          </div>
        </div>
      </template>

      <el-form v-if="fpStep === 1" label-position="top" @submit.prevent="fpSendCode">
        <el-form-item label="管理员用户名">
          <el-input v-model="fpForm.username" placeholder="请输入账号绑定的用户名" size="large"
            :prefix-icon="User" @keyup.enter="fpSendCode" />
        </el-form-item>
        <p class="fp-tip">
          <el-icon :size="12" style="vertical-align: -2px"><InfoFilled /></el-icon>
          验证码将发送至该账号绑定的邮箱，10 分钟内有效
        </p>
      </el-form>

      <el-form v-else label-position="top" @submit.prevent="fpSubmit">
        <el-form-item label="邮箱验证码">
          <div class="captcha-row">
            <el-input v-model="fpForm.code" placeholder="6位验证码" size="large" :prefix-icon="Message"
              maxlength="6" @keyup.enter="fpSubmit" />
            <el-button type="primary" plain :disabled="fpCountdown > 0" :loading="fpSending"
              @click="fpSendCode" class="fp-send-btn">
              {{ fpCountdown > 0 ? fpCountdown + 's' : '重新发送' }}
            </el-button>
          </div>
          <p class="fp-tip">验证码已发送至 {{ fpEmailMasked }}，请查收（注意垃圾邮件箱）</p>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="fpForm.newPassword" type="password" placeholder="6-20位新密码" size="large"
            :prefix-icon="Lock" show-password autocomplete="new-password" @keyup.enter="fpSubmit" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="fp-footer">
          <el-button @click="fpDialog = false">取消</el-button>
          <el-button v-if="fpStep === 1" type="primary" :loading="fpSending" @click="fpSendCode">发送验证码</el-button>
          <el-button v-else type="primary" :loading="fpLoading" @click="fpSubmit">重置密码</el-button>
        </div>
      </template>
    </el-dialog>

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
import { User, Lock, UserFilled, Message, Phone, Plus, Key, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const isLogin = ref(true)
// 走 /api 代理（nginx / vite 转发到网关 → poti-admin），避免写死 localhost:8088
const uploadUrl = ref('/api/admin/upload/avatar')

const captchaImg = ref('')
const captchaKey = ref('')
// 后台「业务开关-登录验证码」：关闭时隐藏验证码输入（后端同步跳过校验）
const captchaEnabled = ref(true)

const loadCaptchaSwitch = async () => {
  try {
    const res = await api.get('/admin/config/value/login_captcha_enabled')
    const v = res.data?.configValue ?? res.data
    captchaEnabled.value = String(v) !== 'false'
  } catch (e) {
    captchaEnabled.value = true
  }
  if (captchaEnabled.value) {
    loadCaptcha()
  }
}

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
    { validator: (rule, value, callback) => {
        if (!captchaEnabled.value || (value && value.trim())) callback()
        else callback(new Error('请输入验证码'))
      }, trigger: 'blur' }
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
  loadCaptchaSwitch()
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

// ===== 忘记密码（邮箱验证码重置） =====
const fpDialog = ref(false)
const fpStep = ref(1)
const fpSending = ref(false)
const fpLoading = ref(false)
const fpEmailMasked = ref('')
const fpCountdown = ref(0)
let fpTimer = null
const fpForm = reactive({ username: '', code: '', newPassword: '' })

const handleForgot = () => {
  fpForm.username = ''
  fpForm.code = ''
  fpForm.newPassword = ''
  fpStep.value = 1
  fpEmailMasked.value = ''
  fpDialog.value = true
}

const fpStartCountdown = () => {
  fpCountdown.value = 60
  if (fpTimer) clearInterval(fpTimer)
  fpTimer = setInterval(() => {
    fpCountdown.value--
    if (fpCountdown.value <= 0) clearInterval(fpTimer)
  }, 1000)
}

const fpSendCode = async () => {
  if (!fpForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  fpSending.value = true
  try {
    const res = await api.post('/admin/password/forgot', { username: fpForm.username.trim() })
    fpEmailMasked.value = res.data?.emailMasked || ''
    fpStep.value = 2
    fpStartCountdown()
    ElMessage.success('验证码已发送')
  } catch (e) {
    // 拦截器已统一弹错，此处静默
  } finally {
    fpSending.value = false
  }
}

const fpSubmit = async () => {
  if (!fpForm.code.trim()) {
    ElMessage.warning('请输入邮箱验证码')
    return
  }
  if (fpForm.newPassword.length < 6 || fpForm.newPassword.length > 20) {
    ElMessage.warning('新密码长度需为6-20位')
    return
  }
  fpLoading.value = true
  try {
    await api.post('/admin/password/reset', {
      username: fpForm.username.trim(),
      code: fpForm.code.trim(),
      newPassword: fpForm.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    fpDialog.value = false
    form.username = fpForm.username
    form.password = ''
  } catch (e) {
    // 拦截器已统一弹错，此处静默
  } finally {
    fpLoading.value = false
  }
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
  border-radius: 20px;
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
  font-size: 28px;
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
  font-size: 15px;
  font-weight: 600;
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

.fp-send-btn {
  width: 110px;
  flex-shrink: 0;
}

.fp-tip {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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
  width: 18px;
  height: 18px;
  margin: 0 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  flex-shrink: 0;
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
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: 10px;
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

/* ===== 卡外版权（固定页底） ===== */
.login-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 0;
  font-size: 12px;
  color: var(--color-disabled);
  z-index: 1;
  pointer-events: none;
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

<!-- 忘记密码弹窗 append-to-body 渲染在组件外，需非 scoped 样式 -->
<style>
.fp-dialog {
  border-radius: 14px;
}

.fp-dialog .el-dialog__header {
  padding: 24px 24px 0;
}

.fp-dialog .el-dialog__body {
  padding: 20px 24px 8px;
}

.fp-dialog .el-dialog__footer {
  padding: 8px 24px 20px;
}

.fp-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fp-header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(30, 64, 175, 0.08);
  color: var(--color-primary, #1e40af);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.fp-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.3;
}

.fp-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: #909399;
}

.fp-steps {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 14px;
  background: #f7f8fa;
  border-radius: 8px;
}

.fp-step {
  display: flex;
  align-items: center;
  gap: 6px;
}

.fp-step-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #d8dbe2;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.25s;
}

.fp-step-label {
  font-size: 12px;
  color: #909399;
}

.fp-step.active .fp-step-dot {
  background: var(--color-primary, #1e40af);
}

.fp-step.active .fp-step-label {
  color: var(--color-primary, #1e40af);
  font-weight: 500;
}

.fp-step.done .fp-step-dot {
  background: #67c23a;
}

.fp-step-line {
  flex: 1;
  height: 1px;
  background: #d8dbe2;
}

.fp-step-line.done {
  background: #67c23a;
}

.fp-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
