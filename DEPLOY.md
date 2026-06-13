# 银舞沐心 - Docker 部署指南

## 前提条件

1. **飞牛 NAS** 已安装 Docker 和 docker-compose
2. **Docker** 版本 >= 20.10
3. **docker-compose** 版本 >= 2.0

## 部署步骤

### 方法一：使用部署脚本（推荐）

1. 将整个项目文件夹上传到飞牛 NAS 的共享目录
2. 在 SSH 或终端中进入项目目录
3. 运行部署脚本：
   ```bash
   ./部署到Docker.bat
   ```

### 方法二：手动部署

1. 上传项目到 NAS
2. 进入项目目录
3. 执行以下命令：
   ```bash
   # 构建并启动
   docker-compose up -d --build

   # 查看日志
   docker-compose logs -f

   # 停止服务
   docker-compose down
   ```

## 目录结构

```
├── backend/
│   ├── src/
│   ├── data/           # 数据文件（容器内映射到 /data）
│   ├── Dockerfile
│   └── pom.xml
├── data/               # 本地数据目录（视频、用户数据等）
├── docker-compose.yml
└── 部署到Docker.bat
```

## 数据持久化

- 视频文件存储在 `./data/media` 目录
- 用户数据（JSON）存储在 `./data` 目录
- 这些数据在容器重启后不会丢失

## 配置说明

### 修改端口

编辑 `docker-compose.yml`：

```yaml
ports:
  - "8080:8080"  # 修改前面的 8080 为你想要的端口
```

### 修改内存限制

编辑 `docker-compose.yml`：

```yaml
environment:
  - JAVA_OPTS=-Xms256m -Xmx1024m  # 根据需要调整
```

## 防火墙设置

如果飞牛 NAS 启用了防火墙，需要开放端口：

```bash
# Ubuntu/Debian
sudo ufw allow 8080/tcp

# CentOS
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

## 验证部署

访问 `http://你的NAS IP:8080/api/courses` 应该返回课程列表 JSON 数据。

## 常见问题

### 1. 容器启动失败

```bash
# 查看详细日志
docker-compose logs backend
```

### 2. 视频无法播放

确保 `./data/media` 目录有正确的读写权限：

```bash
chmod -R 777 ./data
```

### 3. 修改了代码后如何更新

```bash
docker-compose up -d --build
```

## 小程序配置

部署后，需要修改小程序的 `app.js` 中的 API 地址：

```javascript
// 将 localhost:8080 改成你的 NAS IP:端口
const API_BASE = 'http://192.168.x.x:8080/api';
```
