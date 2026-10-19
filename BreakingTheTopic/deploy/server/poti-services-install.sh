#!/usr/bin/env bash
# poti 服务 systemd 注册脚本（4核4G 规格）
# 前置：jar 已上传至 /opt/poti/jars/；/opt/poti/poti.env 已按模板填写完毕
# 用法：bash poti-services-install.sh
set -euo pipefail

JAR_DIR=/opt/poti/jars
ENV_FILE=/opt/poti/poti.env
RUN_BASE=/opt/poti/run
JVM_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC"

# 服务名|端口（与各服务 application-dev.yml 的 server.port 一致）
SERVICES=(
  "gateway|8080"
  "auth|8100"
  "admin|8088"
  "user|8200"
  "question|8300"
  "practice|8400"
  "wrongbook|8500"
  "favorite|8600"
  "interview|8700"
  "agent|8900"
)

if [[ ! -f "$ENV_FILE" ]]; then
  echo "错误：缺少 $ENV_FILE，请先按 poti.env.template 填写"; exit 1
fi

for entry in "${SERVICES[@]}"; do
  name="${entry%%|*}"; port="${entry##*|}"
  jar=$(ls "$JAR_DIR/poti-${name}"-*.jar 2>/dev/null | head -n1 || true)
  if [[ -z "$jar" ]]; then
    echo "警告：未找到 poti-${name} 的 jar，跳过"; continue
  fi
  mkdir -p "$RUN_BASE/$name"

  cat > "/etc/systemd/system/poti-${name}.service" <<EOF
[Unit]
Description=poti ${name} service
After=network.target nacos.service mysql.service redis.service
Wants=nacos.service

[Service]
Type=simple
WorkingDirectory=${RUN_BASE}/${name}
EnvironmentFile=${ENV_FILE}
ExecStart=/usr/bin/java ${JVM_OPTS} -Dserver.port=${port} -jar ${jar}
Restart=on-failure
RestartSec=8
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
  echo "已生成 poti-${name}.service  (${jar})"
done

systemctl daemon-reload
for entry in "${SERVICES[@]}"; do
  name="${entry%%|*}"
  [[ -f "/etc/systemd/system/poti-${name}.service" ]] && systemctl enable --now "poti-${name}" || true
done

echo "等待服务启动（约 40 秒）后自检..."
sleep 40
fail=0
for entry in "${SERVICES[@]}"; do
  name="${entry%%|*}"; port="${entry##*|}"
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || true)
  echo "poti-${name} (${port}): HTTP ${code}"
  [[ "$code" == "200" ]] || fail=$((fail+1))
done
echo "完成。未通过数量：${fail}（刚启动可能有延迟，未通过的可等 30 秒重查：systemctl status poti-<服务名>）"
