# 部署说明

## 项目配置已完成

### 1. 生产环境配置
- ✅ 配置了 `vite.config.js` 的生产环境构建设置
- ✅ 设置了 `base: './'` 支持相对路径部署
- ✅ 优化了构建配置，包括代码分割和压缩

### 2. Nginx配置
- ✅ 创建了 `nginx.conf` 配置文件
- ✅ 配置了API代理到后端8080端口
- ✅ 支持Vue Router的history模式
- ✅ 启用了gzip压缩和静态资源缓存

### 3. Docker配置
- ✅ 创建了 `Dockerfile` 用于构建Docker镜像
- ✅ 创建了 `docker-compose.yml` 用于容器编排

## 部署方式

### 方式一：Docker部署（推荐）

1. **构建并启动容器**
   ```bash
   docker-compose up -d --build
   ```

2. **访问应用**
   - 前端：http://localhost
   - 后端API：http://localhost:8080

### 方式二：传统Nginx部署

1. **构建生产版本**
   ```bash
   npm run build
   ```

2. **部署到Nginx服务器**
   ```bash
   # 复制dist目录到nginx的html目录
   cp -r dist/* /usr/share/nginx/html/
   
   # 复制nginx配置
   cp nginx.conf /etc/nginx/conf.d/default.conf
   
   # 重启nginx
   nginx -s reload
   ```

3. **确保后端服务运行在8080端口**

### 方式三：直接使用dist文件夹

1. **构建项目**
   ```bash
   npm run build
   ```

2. **将dist文件夹部署到任何静态文件服务器**
   - 确保服务器配置了API代理到 `http://localhost:8080`
   - 配置SPA路由支持（所有路由都指向index.html）

## 重要配置说明

### API代理配置
- 前端请求 `/api/*` 会自动代理到 `http://localhost:8080`
- 支持跨域请求
- 包含CORS头部设置

### 静态资源优化
- 启用gzip压缩
- 静态资源设置1年缓存
- 代码分割优化加载性能

### 注意事项
- 确保后端服务在8080端口正常运行
- 如果后端地址不是localhost:8080，请修改nginx.conf中的proxy_pass地址
- 生产环境建议使用HTTPS

## 测试部署

部署完成后，可以通过以下方式测试：

1. 访问 http://localhost 查看前端页面
2. 检查浏览器开发者工具的网络面板，确认API请求正确代理到8080端口
3. 测试页面刷新和路由跳转功能
