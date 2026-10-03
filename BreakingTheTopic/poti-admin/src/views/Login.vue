<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>破题管理系统</h1>
        <p>{{ isLogin ? '后台管理登录' : '管理员注册' }}</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleSubmit">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="!isLogin" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
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
            <el-checkbox v-model="rememberMe">记住用户名</el-checkbox>
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
            {{ loading ? (isLogin ? '登录中...' : '注册中...') : (isLogin ? '登录' : '注册') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="switch-link">
        <span @click="toggleMode">
          {{ isLogin ? '没有账号？立即注册' : '已有账号？立即登录' }}
        </span>
      </div>

      <div class="login-footer">
        <p>© 2024 破题管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Message, Phone, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const isLogin = ref(true)
const uploadUrl = ref('http://localhost:8088/admin/upload/avatar')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

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
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
  const savedUsername = localStorage.getItem('admin_remember_username')
  if (savedUsername && isLogin.value) {
    form.username = savedUsername
  }
}

const handleAvatarSuccess = (response) => {
  console.log('上传响应：', response)
  if (response.code === 200) {
    form.avatar = response.data
    console.log('头像URL：', form.avatar)
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
      console.log('表单验证失败')
      return
    }

    loading.value = true
    try {
      console.log('开始提交，模式：', isLogin.value ? '登录' : '注册')
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
  console.log('发送登录请求')
  const res = await api.post('/admin/login', {
    username: form.username,
    password: form.password
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

  console.log('发送注册请求，完整数据：', registerData)
  console.log('form.avatar值：', form.avatar)

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
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.login-box {
  width: 420px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  background: #1890ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo svg {
  width: 28px;
  height: 28px;
  color: #fff;
}

.login-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-box :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-box :deep(.el-input__wrapper) {
  padding: 0 15px;
  height: 40px;
  border-radius: 4px;
}

.login-box :deep(.el-input__inner) {
  font-size: 14px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  border-radius: 4px;
}

.switch-link {
  text-align: center;
  margin-top: 16px;
}

.switch-link span {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
}

.switch-link span:hover {
  color: #40a9ff;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.login-footer p {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #1890ff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
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
  color: #999;
  margin-top: 8px;
}

@media (max-width: 480px) {
  .login-box {
    width: calc(100% - 40px);
    padding: 32px 24px;
  }
}
</style>
