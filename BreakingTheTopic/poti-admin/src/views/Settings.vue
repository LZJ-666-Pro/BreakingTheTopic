<template>
  <div class="settings-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
          <el-button type="primary" @click="handleSave" :loading="loading">
            保存设置
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="网站设置" name="site">
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="网站名称">
              <el-input v-model="siteForm.site_name" placeholder="请输入网站名称" />
            </el-form-item>
            
            <el-form-item label="网站描述">
              <el-input v-model="siteForm.site_description" type="textarea" :rows="3" placeholder="请输入网站描述" />
            </el-form-item>
            
            <el-form-item label="网站关键词">
              <el-input v-model="siteForm.site_keywords" placeholder="请输入网站关键词，用逗号分隔" />
            </el-form-item>
            
            <el-form-item label="ICP备案号">
              <el-input v-model="siteForm.site_icp" placeholder="请输入ICP备案号" />
            </el-form-item>
            
            <el-form-item label="版权信息">
              <el-input v-model="siteForm.site_copyright" placeholder="请输入版权信息" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="上传设置" name="upload">
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="文件大小限制">
              <el-input-number v-model="siteForm.upload_max_size" :min="1" :max="100" />
              <span style="margin-left: 10px">MB</span>
            </el-form-item>
            
            <el-form-item label="允许的文件类型">
              <el-input v-model="siteForm.upload_allow_types" placeholder="请输入允许的文件类型，用逗号分隔" />
              <div class="form-tip">文件扩展名，用逗号分隔，例如：jpg,jpeg,png,gif,pdf</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="业务开关" name="business">
          <el-form :model="siteForm" label-width="150px">
            <el-form-item label="开放注册">
              <el-switch v-model="siteForm.register_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">关闭后小程序端无法注册新用户</span>
            </el-form-item>

            <el-form-item label="登录验证码">
              <el-switch v-model="siteForm.login_captcha_enabled" active-value="true" inactive-value="false" />
            </el-form-item>

            <el-form-item label="竞赛功能">
              <el-switch v-model="siteForm.competition_enabled" active-value="true" inactive-value="false" />
            </el-form-item>

            <el-form-item label="讨论区">
              <el-switch v-model="siteForm.discussion_enabled" active-value="true" inactive-value="false" />
            </el-form-item>

            <el-form-item label="签到奖励">
              <el-switch v-model="siteForm.checkin_reward_enabled" active-value="true" inactive-value="false" />
            </el-form-item>

            <el-form-item label="每日一题更新时间">
              <el-input v-model="siteForm.daily_question_time" placeholder="如 00:00" style="width: 140px" />
            </el-form-item>

            <el-form-item label="默认分页条数">
              <el-input-number v-model="siteForm.default_page_size" :min="5" :max="100" />
            </el-form-item>

            <el-form-item label="会员价格(元/月)">
              <el-input-number v-model="siteForm.vip_price" :min="0" :max="999" />
              <span class="form-tip" style="margin-left: 10px">0 表示免费</span>
            </el-form-item>

            <el-form-item label="联系邮箱">
              <el-input v-model="siteForm.contact_email" placeholder="官方联系方式" style="width: 260px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮件设置" name="email">
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="siteForm.email_smtp_host" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            
            <el-form-item label="SMTP端口">
              <el-input-number v-model="siteForm.email_smtp_port" :min="1" :max="65535" />
            </el-form-item>
            
            <el-form-item label="SMTP用户名">
              <el-input v-model="siteForm.email_smtp_username" placeholder="请输入SMTP用户名" />
            </el-form-item>
            
            <el-form-item label="SMTP密码">
              <el-input v-model="siteForm.email_smtp_password" type="password" placeholder="请输入SMTP密码" show-password />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="短信设置" name="sms">
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="AccessKey">
              <el-input v-model="siteForm.sms_access_key" placeholder="请输入短信服务AccessKey" />
            </el-form-item>
            
            <el-form-item label="AccessSecret">
              <el-input v-model="siteForm.sms_access_secret" type="password" placeholder="请输入短信服务AccessSecret" show-password />
            </el-form-item>
            
            <el-form-item label="短信签名">
              <el-input v-model="siteForm.sms_sign_name" placeholder="请输入短信签名" />
            </el-form-item>
            
            <el-form-item label="短信模板ID">
              <el-input v-model="siteForm.sms_template_code" placeholder="请输入短信模板ID" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const router = useRouter()
const activeTab = ref('site')
const loading = ref(false)

const siteForm = reactive({
  site_name: '',
  site_description: '',
  site_keywords: '',
  site_icp: '',
  site_copyright: '',
  upload_max_size: 10,
  upload_allow_types: '',
  email_smtp_host: '',
  email_smtp_port: 465,
  email_smtp_username: '',
  email_smtp_password: '',
  sms_access_key: '',
  sms_access_secret: '',
  sms_sign_name: '',
  sms_template_code: '',
  register_enabled: 'true',
  login_captcha_enabled: 'true',
  competition_enabled: 'false',
  discussion_enabled: 'true',
  checkin_reward_enabled: 'true',
  daily_question_time: '00:00',
  default_page_size: 10,
  vip_price: 0,
  contact_email: ''
})

onMounted(async () => {
  await loadConfigs()
})

const loadConfigs = async () => {
  try {
    const res = await api.get('/admin/config/all')
    const configs = res.data
    
    for (const key in configs) {
      if (siteForm.hasOwnProperty(key)) {
        if (key === 'upload_max_size' || key === 'email_smtp_port' || key === 'default_page_size' || key === 'vip_price') {
          siteForm[key] = parseInt(configs[key]) || siteForm[key]
        } else {
          siteForm[key] = configs[key]
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const handleSave = async () => {
  loading.value = true
  try {
    const configs = []
    for (const key in siteForm) {
      configs.push({
        configKey: key,
        configValue: String(siteForm[key])
      })
    }
    
    await api.post('/admin/config/batch-update', configs)
    ElMessage.success('保存成功')
    
    setTimeout(() => {
      router.push('/dashboard')
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}
</style>
