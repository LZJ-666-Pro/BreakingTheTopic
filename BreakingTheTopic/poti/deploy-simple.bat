@echo off
echo Step 1: Clean build files
cd poti
call mvn clean

echo.
echo Step 2: Package all services
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Build Docker images
cd ..
docker-compose -f docker-compose-hybrid.yml build
if %ERRORLEVEL% NEQ 0 (
    echo Docker build failed!
    pause
    exit /b 1
)

echo.
echo Step 4: Start all services
docker-compose -f docker-compose-hybrid.yml up -d
if %ERRORLEVEL% NEQ 0 (
    echo Start failed!
    pause
    exit /b 1
)

echo.
echo Deployment Complete!
echo.
echo Nacos: http://localhost:8848/nacos
echo API Docs: http://localhost:8200/swagger-ui.html
echo Health: http://localhost:8080/actuator/health
echo.
pause
