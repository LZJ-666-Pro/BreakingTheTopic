@echo off
chcp 65001 >nul
echo ================================
echo Poti Microservice Deployment
echo (MySQL Local + Docker Services)
echo ================================
echo.

echo [Check] Verifying MySQL connection...
mysql -u root -p123456 -e "SELECT 1" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [Error] MySQL is not running or cannot connect!
    echo Please make sure MySQL is started.
    echo.
    echo Start MySQL: net start MySQL80
    echo.
    pause
    exit /b 1
)
echo [OK] MySQL connection successful

echo.
echo [1/4] Cleaning old build files...
call mvn clean

echo.
echo [2/4] Compiling and packaging all services...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed! Please check error messages.
    pause
    exit /b 1
)

echo.
echo [3/4] Building Docker images...
docker-compose -f docker-compose-hybrid.yml build

if %ERRORLEVEL% NEQ 0 (
    echo Docker image build failed!
    pause
    exit /b 1
)

echo.
echo [4/4] Starting all services...
docker-compose -f docker-compose-hybrid.yml up -d

if %ERRORLEVEL% NEQ 0 (
    echo Service startup failed!
    pause
    exit /b 1
)

echo.
echo ================================
echo Deployment Complete!
echo ================================
echo.
echo Deployment Mode: Hybrid
echo - MySQL: Local
echo - Other Services: Docker
echo.
echo Access URLs:
echo - Nacos: http://localhost:8848/nacos (nacos/nacos)
echo - API Docs: http://localhost:8200/swagger-ui.html
echo - Health: http://localhost:8080/actuator/health
echo.
echo View logs: docker-compose -f docker-compose-hybrid.yml logs -f
echo Stop services: docker-compose -f docker-compose-hybrid.yml down
echo.
pause
