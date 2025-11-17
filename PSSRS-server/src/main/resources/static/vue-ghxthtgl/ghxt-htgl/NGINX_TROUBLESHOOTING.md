# Nginx 500错误排查指南

## 问题：GET http://localhost/ 500 (Internal Server Error)

### 快速修复步骤

#### 1. 检查nginx配置文件路径格式（Windows）

**错误示例：**
```nginx
root E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist;  # ❌ 反斜杠会导致错误
```

**正确示例：**
```nginx
root E:/desktop/qianduan/vue-ghxthtgl/ghxt-htgl/dist;  # ✅ 使用正斜杠
```

#### 2. 完整的nginx配置（使用项目根目录的 nginx-windows.conf）

找到您的nginx配置文件：
- 通常位置：`nginx安装目录/conf/nginx.conf`
- 或：`C:/nginx/conf/nginx.conf`

替换为项目中的 `nginx-windows.conf` 内容，或者只修改 `server` 块部分。

#### 3. 测试配置并重启nginx

```bash
# 进入nginx安装目录
cd C:/nginx  # 或您的nginx安装路径

# 测试配置文件语法
nginx -t

# 如果测试通过，重启nginx
nginx -s reload

# 如果reload不生效，停止后重启
nginx -s stop
start nginx
```

#### 4. 查看nginx错误日志

```bash
# 错误日志通常在
C:/nginx/logs/error.log
```

打开错误日志查看具体错误信息。

### 常见500错误原因

#### 原因1：路径格式错误（最常见）
**症状：** 500错误
**解决：** 将所有反斜杠 `\` 改为正斜杠 `/`

#### 原因2：路径不存在
**症状：** 500错误或403错误
**解决：** 
```bash
# 确认dist目录存在
cd E:/desktop/qianduan/vue-ghxthtgl/ghxt-htgl
dir dist
```

#### 原因3：缺少mime.types
**症状：** 500错误，静态资源无法加载
**解决：** 确保nginx.conf中包含：
```nginx
http {
    include       mime.types;
    default_type  application/octet-stream;
    ...
}
```

#### 原因4：try_files配置错误
**症状：** 500错误
**解决：** 确保配置为：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 简化配置（最小可用版本）

如果上述方法都不行，使用这个最简配置：

```nginx
worker_processes  1;
events {
    worker_connections  1024;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    
    server {
        listen       80;
        server_name  localhost;
        
        # 修改为您的实际路径
        root   E:/desktop/qianduan/vue-ghxthtgl/ghxt-htgl/dist;
        index  index.html;
        
        location / {
            try_files $uri $uri/ /index.html;
        }
    }
}
```

### 验证步骤

1. **确认dist目录存在且包含文件**
   ```bash
   dir E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist
   dir E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl\dist\assets
   ```

2. **测试nginx配置**
   ```bash
   nginx -t
   ```

3. **查看nginx进程**
   ```bash
   tasklist | findstr nginx
   ```

4. **重启nginx**
   ```bash
   nginx -s stop
   start nginx
   ```

5. **访问测试**
   - 打开浏览器访问：http://localhost
   - 查看浏览器控制台
   - 查看nginx错误日志

### 如果还是不行

1. **完全停止nginx**
   ```bash
   taskkill /F /IM nginx.exe
   ```

2. **使用绝对路径启动**
   ```bash
   cd C:/nginx
   start nginx
   ```

3. **检查端口占用**
   ```bash
   netstat -ano | findstr :80
   ```

4. **尝试使用其他端口**
   ```nginx
   listen 8080;  # 改为8080端口
   ```
   然后访问：http://localhost:8080

### 替代方案：使用Vite预览

如果nginx配置困难，可以直接使用Vite的预览服务器：

```bash
cd E:\desktop\qianduan\vue-ghxthtgl\ghxt-htgl
npm run preview
```

这会在 http://localhost:4173 启动一个生产环境预览服务器。
