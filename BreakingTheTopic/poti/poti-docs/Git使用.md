## 执行初始化命令
--- 
git init


## 创建 .gitignore 文件（可选但推荐），忽略不需要提交的文件
---
cat > .gitignore << EOF
# IDEA
.idea/
*.iml
*.iws
.DS_Store

# Maven
target/
**/target/
logs/

# Docker volumes (数据目录)
mysql/data/
redis/data/
nacos/logs/
elasticsearch/data/

# 日志
*.log
EOF


## 将所有文件添加到暂存区
---
git add .


## 提交初始版本：
---
git commit -m "init: 项目初始化，完成微服务骨架搭建"


## 关联远程仓库（如果需要）：
---
git remote add origin https://github.com/yourname/poti.git
git branch -M main
git push -u origin main