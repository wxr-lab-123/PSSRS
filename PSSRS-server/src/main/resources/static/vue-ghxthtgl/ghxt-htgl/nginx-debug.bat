@echo off
echo ========================================
echo Nginx 调试脚本
echo ========================================
echo.

echo 1. 检查nginx进程
tasklist | findstr nginx
echo.

echo 2. 检查80端口占用
netstat -ano | findstr :80
echo.

echo 3. 检查dist目录是否存在
if exist "E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist" (
    echo [OK] dist目录存在
    dir "E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist"
) else (
    echo [ERROR] dist目录不存在！
)
echo.

echo 4. 检查index.html是否存在
if exist "E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist\index.html" (
    echo [OK] index.html存在
) else (
    echo [ERROR] index.html不存在！
)
echo.

echo 5. 检查assets目录
if exist "E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist\assets" (
    echo [OK] assets目录存在
    dir "E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist\assets" | findstr ".js"
) else (
    echo [ERROR] assets目录不存在！
)
echo.

echo ========================================
echo 调试完成
echo ========================================
pause
