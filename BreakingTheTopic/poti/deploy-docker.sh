#!/bin/bash

echo "================================"
echo "破题小程序 Docker 部署脚本"
echo "================================"
echo

echo "[1/4] 清理旧的构建文件..."
mvn clean

echo
echo "[2/4] 编译打包所有服务..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    echo "编译失败！请检查错误信息。"
    exit 1
fi

echo
echo "[3/4] 构建Docker镜像..."
docker-compose -f docker-compose-full.yml build

if [ $? -ne 0 ]; then
    echo "Docker镜像构建失败！"
    exit 1
fi

echo
echo "[4/4] 启动所有服务..."
docker-compose -f docker-compose-full.yml up -d

if [ $? -ne 0 ]; then
    echo "服务启动失败！"
    exit 1
fi

echo
echo "================================"
echo "部署完成！"
echo "================================"
echo
echo "访问地址："
echo "- Nacos控制台: http://localhost:8848/nacos (nacos/nacos)"
echo "- API文档: http://localhost:8200/swagger-ui.html"
echo "- 网关健康检查: http://localhost:8080/actuator/health"
echo
echo "查看日志: docker-compose -f docker-compose-full.yml logs -f"
echo "停止服务: docker-compose -f docker-compose-full.yml down"
echo
