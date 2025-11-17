@echo off
echo ========================================
echo Nginx配置修复脚本
echo ========================================
echo.

echo 您的nginx安装在: E:\desktop\nginx-1.20.2-cqwm
echo 配置文件位置: E:\desktop\nginx-1.20.2-cqwm\conf\nginx.conf
echo.

echo 请按照以下步骤操作:
echo.
echo 1. 打开文件: E:\desktop\nginx-1.20.2-cqwm\conf\nginx.conf
echo.
echo 2. 找到 server 块中的 root 这一行
echo.
echo 3. 将 root 改为:
echo    root   E:/desktop/qianduan/vue-ghxthtgl/ghxt-htgl/dist;
echo.
echo 4. 保存文件
echo.
echo 5. 重启nginx:
echo    cd E:\desktop\nginx-1.20.2-cqwm
echo    nginx -s stop
echo    start nginx
echo.
echo ========================================
echo.

echo 是否现在打开nginx配置文件? (Y/N)
set /p choice=

if /i "%choice%"=="Y" (
    notepad E:\desktop\nginx-1.20.2-cqwm\conf\nginx.conf
)

pause
