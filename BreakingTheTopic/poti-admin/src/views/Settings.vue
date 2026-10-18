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
              <span class="form-tip" style="margin-left: 10px">仅管理后台登录的图形验证码（不影响小程序短信验证码登录）</span>
            </el-form-item>

            <el-form-item label="竞赛功能">
              <el-switch v-model="siteForm.competition_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">关闭后小程序竞赛中心不可用（列表/详情/报名/答题/成绩均拦截）</span>
            </el-form-item>

            <el-form-item label="讨论区">
              <el-switch v-model="siteForm.discussion_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">关闭后题目讨论列表隐藏且禁止发表</span>
            </el-form-item>

            <el-form-item label="签到奖励">
              <el-switch v-model="siteForm.checkin_reward_enabled" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">真实生效：小程序签到成功即按下方档位自动发放会员时长，断签归零重新累计可循环再得</span>
            </el-form-item>

            <el-form-item label="连签奖励档位">
              <div class="reward-tiers">
                <div class="reward-tier" v-for="tier in rewardTiers" :key="tier.streak">
                  <span class="tier-label">连签 {{ tier.streak }} 天送</span>
                  <el-input-number v-model="siteForm['checkin_reward_days_' + tier.streak]" :min="0" :max="365" size="small" style="width: 100px" />
                  <span class="tier-unit">天会员</span>
                </div>
              </div>
              <div class="form-tip">保存后实时生效；填 0 表示该档位不发放；同一轮连签每档仅发一次，断签后重新累计可再次获得</div>
            </el-form-item>

            <el-form-item label="每日一题自动发布">
              <el-switch v-model="siteForm.daily_question_auto" active-value="true" inactive-value="false" />
              <span class="form-tip" style="margin-left: 10px">开启后按下方时间自动随机抽题发布（排除近 30 天已用题目）；关闭即停止自动发布，手动配置不受影响</span>
            </el-form-item>

            <el-form-item label="每日一题更新时间">
              <el-input v-model="siteForm.daily_question_time" placeholder="如 08:00" style="width: 140px" />
              <span class="form-tip" style="margin-left: 10px">自动发布开关开启时，每天到该时间点自动抽题发布</span>
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
          <el-alert type="success" :closable="false" show-icon
            title="真实生效：后台登录页「忘记密码」通过本邮箱发送验证码重置密码，修改保存后即时生效" style="margin-bottom: 12px" />
          <el-alert type="info" :closable="false" show-icon
            title="QQ邮箱填 smtp.qq.com（端口465）、163邮箱填 smtp.163.com（端口465）；用户名=邮箱地址；密码为授权码（邮箱设置中开启SMTP服务后生成，不是登录密码）。管理员账号需先在「管理员管理」中绑定邮箱" style="margin-bottom: 16px" />
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="siteForm.email_smtp_host" placeholder="如 smtp.qq.com" />
            </el-form-item>

            <el-form-item label="SMTP端口">
              <el-input-number v-model="siteForm.email_smtp_port" :min="1" :max="65535" />
            </el-form-item>

            <el-form-item label="SMTP用户名">
              <el-input v-model="siteForm.email_smtp_username" placeholder="发件邮箱地址" />
            </el-form-item>

            <el-form-item label="SMTP密码">
              <el-input v-model="siteForm.email_smtp_password" type="password" placeholder="邮箱授权码，非登录密码" show-password />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="短信设置" name="sms">
          <el-alert v-if="siteForm.sms_template_code" type="success" :closable="false" show-icon
            title="已接入 Spug 推弹（push.spug.cc）：验证码短信实时生效，修改模板ID后保存即可切换" style="margin-bottom: 16px" />
          <el-alert v-else type="warning" :closable="false" show-icon
            title="短信当前为开发模式（验证码打印到服务日志）：在 push.spug.cc 创建验证码模板后，将模板ID填入下方并保存即接入" style="margin-bottom: 16px" />
          <el-form :model="siteForm" label-width="120px">
            <el-form-item label="AccessKey">
              <el-input v-model="siteForm.sms_access_key" placeholder="传统短信服务商预留（阿里云等），Spug 无需填写" />
            </el-form-item>

            <el-form-item label="AccessSecret">
              <el-input v-model="siteForm.sms_access_secret" type="password" placeholder="传统短信服务商预留（阿里云等），Spug 无需填写" show-password />
            </el-form-item>

            <el-form-item label="短信签名">
              <el-input v-model="siteForm.sms_sign_name" placeholder="Spug 签名随模板生效，无需填写" />
            </el-form-item>

            <el-form-item label="短信模板ID">
              <el-input v-model="siteForm.sms_template_code" placeholder="粘贴 Spug「短信验证码」页的调用URL（或其最后一段编码）" />
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

// 连签奖励档位（对应配置键 checkin_reward_days_3/7/15/30）
const rewardTiers = [
  { streak: 3 },
  { streak: 7 },
  { streak: 15 },
  { streak: 30 }
]

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
  checkin_reward_days_3: 1,
  checkin_reward_days_7: 3,
  checkin_reward_days_15: 7,
  checkin_reward_days_30: 15,
  daily_question_auto: 'false',
  daily_question_time: '08:00',
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
        } else if (key === 'upload_max_size' || key === 'email_smtp_port' || key === 'default_page_size' || key.startsWith('checkin_reward_days_')) {
          siteForm[key] = parseInt(configs[key])
          if (isNaN(siteForm[key])) siteForm[key] = 0
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

.reward-tiers {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reward-tier {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tier-label {
  font-size: 13px;
  color: #606266;
  width: 90px;
}

.tier-unit {
  font-size: 13px;
  color: #606266;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}
</style>
