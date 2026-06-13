# 银舞沐心 (wxyunwu)

> **让爷爷奶奶重新爱上自己**
>
> 界面像午后阳光一样温暖，操作像握手一样简单。专为长辈设计的舞蹈与健康伴侣小程序。

## 🌟 项目简介

银舞沐心是一款专为老年人设计的微信小程序，提供健康舞蹈课程、AI 健康语音伴侣、快捷紧急呼叫以及适老化的购物体验。项目采用原生微信小程序开发前端，配合 Java Spring Boot 提供稳定强大的后端 API 支持。

### 核心功能
- 💃 **广场舞与健康课程**：大字号大卡片，按难度分类的瀑布流视频课程。
- 🤖 **AI 健康伴侣**：支持超大语音按钮输入，智能解答健康疑问并提供陪伴。
- 🛒 **适老化商城**：精选适合长辈的健康商品与助老用品。
- 🏥 **紧急与健康监控**：健康数据看板与紧急联络人功能。

---

## 🛠️ 技术栈

### 前端 (Frontend & AdminFrontend)
- **平台**：微信小程序原生开发
- **样式**：采用 WXSS (使用 rpx 进行屏幕适配)
- **UI 设计**：遵循定制化适老设计规范（详见 `DESIGN_GUIDE.md`），使用大字号、柔和配色（暖阳橙、米白底）及棉花糖缓动动画。

### 后端 (Backend)
- **语言/框架**：Java 17+ / Spring Boot
- **构建工具**：Maven
- **数据存储**：本地 JSON 数据文件存储（基于 `data/` 目录）
- **AI 语音**：集成 Vosk 语音识别模型
- **部署**：Docker & Docker Compose

---

## 📂 目录结构

```text
├── frontend/               # 用户端微信小程序源码
├── adminFrontend/          # 管理端微信小程序源码
├── backend/                # Spring Boot 后端源码
│   ├── src/                # 后端 Java 源码及配置
│   ├── Dockerfile          # 后端容器构建文件
│   └── pom.xml             # Maven 依赖配置
├── data/                   # 本地数据目录（JSON数据及媒体文件）
├── maven/                  # Maven 包装器及依赖环境
├── mysql_init/             # MySQL 初始化脚本 (如果使用了 MySQL)
├── docker-compose.yml      # Docker 编排配置
├── DEPLOY.md               # 部署与环境搭建指南
└── DESIGN_GUIDE.md         # 适老化 UI/UX 设计规范
```

---

## 🚀 快速启动

### 1. 运行后端服务

**使用 Docker 部署（推荐）**

请确保机器上已安装 Docker 和 Docker Compose。

```bash
# 构建并启动后端容器
docker-compose up -d --build

# 查看实时日志
docker-compose logs -f
```

*详细部署指南和飞牛 NAS 配置请参考 [DEPLOY.md](DEPLOY.md)。*

**本地开发运行**

进入 `backend` 目录，使用 Maven 启动 Spring Boot 项目：

```bash
cd backend
./mvnw spring-boot:run
```
后端服务默认运行在 `http://localhost:8080`。

### 2. 运行微信小程序

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)。
2. 在微信开发者工具中选择 **导入项目**。
3. 目录选择仓库中的 `frontend/` 文件夹。
4. 填入你的 AppID（若无，可选择“测试号”）。
5. 在 `frontend/app.js` 或相关配置文件中，将 API 接口请求地址修改为你的后端运行地址（例如局域网 IP `http://192.168.x.x:8080/api`）。
6. 点击编译即可在模拟器中预览。

---

## 🎨 设计与开发规范

在进行二次开发前，请务必阅读 [DESIGN_GUIDE.md](DESIGN_GUIDE.md) 以了解我们的适老设计理念：
- **配色**：避免刺眼色彩，多用暖色（暖阳橙 #E07A5F，米白底 #FDF6EC）。
- **字体**：比普通 App 大 40% (32-36px)，高行距 (1.8)。
- **交互**：放大点击区域（不小于 88x88px），延长动画时间（400-600ms）给予充足的响应时间感知。
- **文案**：口语化、亲切，每句话尽量不超过 15 个字。

---

## 📄 许可证

本项目遵循相关开源协议，具体视文件内声明而定。
