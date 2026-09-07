@echo off
echo ================================
echo 破题小程序 Docker 部署脚本
echo ================================
echo.

echo [1/4] 清理旧的构建文件...
call mvn clean

echo.
echo [2/4] 编译打包所有服务...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo [3/4] 构建Docker镜像...
docker-compose -f docker-compose-full.yml build

if %ERRORLEVEL% NEQ 0 (
    echo Docker镜像构建失败！
    pause
    exit /b 1
)

echo.
echo [4/4] 启动所有服务...
docker-compose -f docker-compose-full.yml up -d

if %ERRORLEVEL% NEQ 0 (
    echo 服务启动失败！
    pause
    exit /b 1
)

echo.
echo ================================
echo 部署完成！
echo ================================
echo.
echo 访问地址：
echo - Nacos控制台: http://localhost:8848/nacos (nacos/nacos)
echo - API文档: http://localhost:8200/swagger-ui.html
echo - 网关健康检查: http://localhost:8080/actuator/health
echo.
echo 查看日志: docker-compose -f docker-compose-full.yml logs -f
echo 停止服务: docker-compose -f docker-compose-full.yml down
echo.
pause
