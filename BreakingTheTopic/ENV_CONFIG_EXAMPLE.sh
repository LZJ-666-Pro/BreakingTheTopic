# 环境变量配置示例
# 复制此文件为 /etc/profile.d/poti.sh 并修改相应配置

# ==================== 数据库配置 ====================
# 数据库主机地址
export DB_HOST=localhost

# 数据库端口
export DB_PORT=3306

# 数据库用户名
export DB_USERNAME=poti_user

# 数据库密码（请修改为强密码）
export DB_PASSWORD=your_secure_password_here

# ==================== Redis配置 ====================
# Redis主机地址
export REDIS_HOST=localhost

# Redis端口
export REDIS_PORT=6379

# Redis密码（请修改为强密码）
export REDIS_PASSWORD=your_redis_password_here

# ==================== JWT配置 ====================
# JWT密钥（至少32个字符，请修改为随机字符串）
export JWT_SECRET=your_jwt_secret_key_at_least_32_characters_here

# ==================== 微信小程序配置 ====================
# 微信小程序AppID
export WX_APPID=wx920f3d57f4121af7

# 微信小程序Secret（请修改为实际的Secret）
export WX_SECRET=your_wechat_app_secret_here

# ==================== AI配置 ====================
# AI提供商
export AI_PROVIDER=zhipu

# AI API密钥
export AI_API_KEY=your_ai_api_key_here

# AI基础URL
export AI_BASE_URL=https://open.bigmodel.cn/api/paas/v4

# AI模型
export AI_MODEL=glm-4-flash

# ==================== Nacos配置 ====================
# Nacos主机地址
export NACOS_HOST=localhost

# ==================== 文件上传配置 ====================
# 文件上传URL
export UPLOAD_URL=https://your-domain.com/uploads

# 头像URL前缀
export AVATAR_URL_PREFIX=https://your-domain.com/avatars/

# ==================== 其他配置 ====================
# 生产环境标识
export SPRING_PROFILES_ACTIVE=prod
