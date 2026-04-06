@echo off
REM ========================================
REM GetJobs 配置加密工具
REM Windows 版本
REM ========================================

echo ========================================
echo   GetJobs 配置加密工具
echo ========================================
echo.

set ENCRYPTION_KEY=%JASYPT_ENCRYPTOR_PASSWORD%

if "%ENCRYPTION_KEY%"=="" (
    echo ⚠️  未设置环境变量 JASYPT_ENCRYPTOR_PASSWORD
    echo 💡 使用默认加密密钥
    set ENCRYPTION_KEY=get-jobs-encryption-key-2026
)

echo 🔑 加密密钥: %ENCRYPTION_KEY:~0,4%****%ENCRYPTION_KEY:~-4%
echo.

if "%~1"=="" (
    echo 用法: encrypt-config.bat "要加密的明文"
    echo.
    echo 示例:
    echo   encrypt-config.bat "my_database_password"
    echo   encrypt-config.bat "my_jwt_secret"
    echo.
    echo 设置自定义加密密钥:
    echo   set JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
    echo   encrypt-config.bat "my_database_password"
    echo.
    pause
    exit /b 0
)

set PLAINTEXT=%~1

echo 📝 明文: %PLAINTEXT%
echo.

cd /d "%~dp0backend"

echo 🔒 正在加密...
call mvn compile exec:java -Dexec.mainClass="com.getjobs.common.util.EncryptionTool" -Dexec.args="%PLAINTEXT%" -Djasypt.encryptor.password="%ENCRYPTION_KEY%" -q

echo.
echo ========================================
echo 💡 使用方法:
echo.
echo 1. 将输出的 ENC(...) 复制到 application.yaml
echo 2. 启动应用时设置加密密钥:
echo    set JASYPT_ENCRYPTOR_PASSWORD=%ENCRYPTION_KEY%
echo    java -jar get_jobs.jar
echo.
echo 或使用 JVM 参数:
echo    java -Djasypt.encryptor.password=%ENCRYPTION_KEY% -jar get_jobs.jar
echo ========================================
echo.

cd /d "%~dp0"
pause
