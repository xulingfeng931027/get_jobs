@echo off
REM ========================================
REM GetJobs 项目构建脚本（含代码混淆）
REM Windows 版本
REM ========================================

echo ========================================
echo   GetJobs 项目构建（含混淆）
echo ========================================
echo.

set PROJECT_ROOT=%~dp0
set BUILD_TYPE=%1

REM 检查构建类型
if "%BUILD_TYPE%"=="" (
    echo 用法: build-obfuscated.bat [all^|backend^|front]
    echo.
    echo   all      - 构建前后端（默认）
    echo   backend  - 仅构建后端
    echo   front    - 仅构建前端
    echo.
    set BUILD_TYPE=all
)

echo [构建类型] %BUILD_TYPE%
echo [项目根目录] %PROJECT_ROOT%
echo.

REM ========================================
REM 构建后端
REM ========================================
if "%BUILD_TYPE%"=="all" goto BUILD_BACKEND
if "%BUILD_TYPE%"=="backend" goto BUILD_BACKEND
goto SKIP_BACKEND

:BUILD_BACKEND
echo ========================================
echo   1/2 构建后端（Java）
echo ========================================
echo.

cd /d "%PROJECT_ROOT%backend"

echo [1/3] 清理旧的构建...
call mvn clean

echo [2/3] 编译项目...
call mvn compile

echo [3/3] 打包并混淆...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 后端构建失败！
    exit /b 1
)

echo.
echo ✅ 后端构建完成！
echo 📦 Jar 文件: backend\target\get_jobs-0.0.1-SNAPSHOT.jar
echo    （已包含代码混淆）
echo.

:SKIP_BACKEND

REM ========================================
REM 构建前端
REM ========================================
if "%BUILD_TYPE%"=="all" goto BUILD_FRONT
if "%BUILD_TYPE%"=="front" goto BUILD_FRONT
goto SKIP_FRONT

:BUILD_FRONT
echo ========================================
echo   2/2 构建前端（Next.js）
echo ========================================
echo.

cd /d "%PROJECT_ROOT%front"

echo [1/2] 安装依赖...
call pnpm install

echo [2/2] 构建并混淆...
call pnpm run build:obfuscate

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 前端构建失败！
    exit /b 1
)

echo.
echo ✅ 前端构建完成！
echo 📦 混淆后的文件: front\out\
echo.

:SKIP_FRONT

echo ========================================
echo   构建总结
echo ========================================
echo.
echo ✅ 构建完成！
echo.
echo 📦 后端输出:
if exist "%PROJECT_ROOT%backend\target\get_jobs-0.0.1-SNAPSHOT.jar" (
    echo    - 后端 Jar: backend\target\get_jobs-0.0.1-SNAPSHOT.jar（含混淆）
)
echo.
echo 📦 前端输出:
if exist "%PROJECT_ROOT%front\out" (
    echo    - 混淆版本: front\out\
)
echo.
echo 💡 提示:
echo    - 运行后端: java -jar backend\target\get_jobs-0.0.1-SNAPSHOT.jar
echo    - 前端文件已静态导出到 front\out\ 目录
echo.

cd /d "%PROJECT_ROOT%"
pause
