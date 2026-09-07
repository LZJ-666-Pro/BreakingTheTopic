<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="头像">
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
        </el-form-item>
        
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-tag :type="form.role === 2 ? 'danger' : 'primary'">
            {{ form.role === 2 ? '超级管理员' : '普通管理员' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleUpdate" :loading="loading">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handlePasswordUpdate" :loading="passwordLoading">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)
const uploadUrl = ref('http://localhost:8088/admin/upload/avatar')

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  role: 1
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const res = await api.get('/admin/info')
    Object.assign(form, res.data)
  } catch (error) {
    ElMessage.error('加载用户信息失败')
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
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUpdate = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await api.post('/admin/update', {
        nickname: form.nickname,
        email: form.email,
        phone: form.phone,
        avatar: form.avatar
      })
      
      userStore.updateUserInfo({
        nickname: form.nickname,
        avatar: form.avatar
      })
      
      ElMessage.success('更新成功')
      
      setTimeout(() => {
        router.push('/dashboard')
      }, 1000)
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || '更新失败')
    } finally {
      loading.value = false
    }
  })
}

const handlePasswordUpdate = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    passwordLoading.value = true
    try {
      await api.post('/admin/password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      ElMessage.success('密码修改成功，请重新登录')
      passwordFormRef.value.resetFields()
      
      setTimeout(() => {
        userStore.logout()
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || '修改密码失败')
    } finally {
      passwordLoading.value = false
    }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.password-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.avatar-uploader {
  display: flex;
  align-items: center;
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
  border-color: #409eff;
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
</style>
