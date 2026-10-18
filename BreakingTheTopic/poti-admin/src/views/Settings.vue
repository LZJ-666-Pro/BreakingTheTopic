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
          <el-alert type="info" :closable="false" show-icon
            title="保存后展示在小程序「联系我们-关于本站」（名称/描述/版权/备案号）" style="margin-bottom: 16px" />
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
            <el-alert type="info" :closable="false" show-icon
              title="生效于后台上传接口（管理员头像），保存后即时生效" style="margin-bottom: 16px" />
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
              <span class="form-tip" style="margin-left: 10px">竞赛模块未上线，暂不生效</span>
            </el-form-item>

            <el-form-item label="讨论区">
              <el-switch v-model="siteForm.discussion_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">关闭后题目讨论列表隐藏且禁止发表</span>
            </el-form-item>

            <el-form-item label="签到奖励">
              <el-switch v-model="siteForm.checkin_reward_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">奖励体系（积分/兑换）未上线，暂不生效</span>
            </el-form-item>

            <el-form-item label="每日一题更新时间">
              <el-input v-model="siteForm.daily_question_time" placeholder="如 00:00" style="width: 140px" />
              <span class="form-tip" style="margin-left: 10px">每日一题由后台手动设置，暂不生效</span>
            </el-form-item>

            <el-form-item label="默认分页条数">
              <el-input-number v-model="siteForm.default_page_size" :min="5" :max="100" />
              <span class="form-tip" style="margin-left: 10px">后台各列表页初始每页条数（个人手动选择优先）</span>
            </el-form-item>

            <el-form-item label="联系邮箱">
              <el-input v-model="siteForm.contact_email" placeholder="官方联系方式" style="width: 260px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="会员设置" name="vip">
          <el-alert type="success" :closable="false" show-icon
            title="以下价格保存后实时生效：小程序「会员中心」的套餐列表与开通金额均即时读取" style="margin-bottom: 16px" />
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="月卡价格(元)">
              <el-input-number v-model="siteForm.vip_price" :min="0.01" :max="9999" :precision="2" :step="1" />
              <span class="form-tip" style="margin-left: 10px">按月开通，对应配置键 vip_price</span>
            </el-form-item>

            <el-form-item label="季卡价格(元)">
              <el-input-number v-model="siteForm.vip_price_quarter" :min="0.01" :max="9999" :precision="2" :step="1" />
              <span class="form-tip" style="margin-left: 10px">按季开通（3个月），省钱标签按月卡价自动换算</span>
            </el-form-item>

            <el-form-item label="年卡价格(元)">
              <el-input-number v-model="siteForm.vip_price_year" :min="0.01" :max="9999" :precision="2" :step="1" />
              <span class="form-tip" style="margin-left: 10px">按年开通（12个月），省钱标签按月卡价自动换算</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮件设置" name="email">
          <el-alert type="warning" :closable="false" show-icon
            title="项目尚未接入邮件服务商，以下配置仅保存不生效" style="margin-bottom: 16px" />
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
          <el-alert type="warning" :closable="false" show-icon
            title="短信当前为开发模式（验证码打印到服务日志），接入服务商后以下配置才生效" style="margin-bottom: 16px" />
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
  vip_price: 1.99,
  vip_price_quarter: 4.99,
  vip_price_year: 9.99,
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
        if (key === 'vip_price' || key === 'vip_price_quarter' || key === 'vip_price_year') {
          // 价格为小数，用 parseFloat；空/非法时保留表单缺省值
          const parsed = parseFloat(configs[key])
          siteForm[key] = isNaN(parsed) ? siteForm[key] : parsed
        } else if (key === 'upload_max_size' || key === 'email_smtp_port' || key === 'default_page_size') {
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
