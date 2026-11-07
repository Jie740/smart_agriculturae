# 智慧农业管理系统（Smart Agriculture）

一套面向农业生产全流程管理的前后端分离系统，涵盖土地管理、种植计划、农机/农资管理、作物监控、成熟收获，以及基于 RAG（检索增强生成）的 AI 农业助手。支持企业端、农户端与系统管理员多角色使用，并通过 Docker Compose 一键容器化部署。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts + Axios |
| 后端 | Spring Boot 3.3 + MyBatis-Plus + Spring Security + JWT + Redis |
| AI 能力 | LangChain4j + 阿里云 DashScope + pgvector 向量检索（RAG） |
| 数据库 | MySQL 8.0（业务数据）、PostgreSQL 16 + pgvector（AI 知识库/向量数据） |
| 对象存储 | MinIO（知识库文档等文件） |
| 部署 | Docker + Docker Compose + Nginx |
| 测试 | Jest + Vue Test Utils（前端单元测试） |

## 项目结构

```
graduation_work/
├── docker-compose.yml            # 一键编排：后端、前端、MySQL、PostgreSQL、Redis、MinIO
├── .env / .env.example           # 部署环境变量（数据库账号、MinIO 密钥、DashScope API Key 等）
├── .gitignore
├── rag_task.md                   # RAG 智能体模块的任务说明文档
│
├── data/                         # 容器初始化 SQL 脚本（挂载到 docker-entrypoint-initdb.d）
│   ├── mysql/
│   │   ├── 01-smart_agriculture.sql      # 业务库建库建表 + 初始数据
│   │   └── 02-rbac_init.sql              # RBAC 权限体系初始化
│   └── postgres/
│       ├── 00-pg_init.sql                # 启用 pgvector 扩展
│       ├── 01~05-ai_*.sql                # AI 知识库、RAG 文档/分块/检索相关表
│       └── 06~07-biz_*.sql               # 会话与消息记录表
│
├── graduation_work_springBoot/   # 后端服务（Spring Boot，端口 8080）
│   ├── pom.xml                   # Maven 依赖与构建配置
│   ├── Dockerfile                # 多阶段构建（Maven 编译 → JRE 运行）
│   ├── sql/                      # 数据库脚本原始版本（与 data/ 下对应）
│   └── src/main/
│       ├── java/com/clj/
│       │   ├── AgriculturalSystemApplication.java   # 启动类
│       │   ├── controller/       # 业务 REST 接口（土地、种植计划、农机、农资、作物、RBAC 等）
│       │   ├── service/impl/     # 业务逻辑层
│       │   ├── mapper/           # MyBatis-Plus 数据访问层
│       │   ├── domain/           # 实体、dto、vo
│       │   ├── ai/               # AI 模块：知识库、RAG 文档解析/分块/向量检索、对话、工具调用
│       │   ├── auth/             # 登录认证接口
│       │   ├── security/         # Spring Security 配置、JWT 过滤器、鉴权处理器
│       │   ├── sms/              # 短信验证码
│       │   ├── common/           # 全局配置、统一返回结果、异常、拦截器、枚举、工具类
│       │   └── utils/            # 通用工具
│       └── resources/
│           ├── application.yaml              # 主配置
│           ├── application-dev.yaml          # 开发环境配置
│           ├── application-prod.yaml         # 生产（容器）环境配置
│           └── com/clj/**/mapper/*.xml       # MyBatis XML 映射
│
└── graduation_work_vue/          # 前端应用（Vue 3 + Vite）
    ├── package.json
    ├── vite.config.js            # Vite 配置（开发代理等）
    ├── Dockerfile                # 多阶段构建（npm 打包 → Nginx 托管）
    ├── nginx.conf                # 容器内 Nginx：静态资源 + /api 反向代理到后端
    ├── index.html
    ├── tests/                    # Jest 单元测试
    └── src/
        ├── main.js / App.vue     # 入口
        ├── api/                  # Axios 封装与接口调用
        ├── router/               # 路由与角色守卫
        ├── store/                # Pinia 状态管理
        ├── utils/                # 工具函数（请求、Cookie 等）
        ├── components/           # 公共组件（含 rag/ AI 对话组件、icons/）
        ├── assets/               # 静态资源
        └── views/
            ├── common/           # 登录、首页等公共页面
            ├── user/             # 农户端：土地、种植计划、作物监控、农资/农机申请、个人信息等
            ├── enterprise-admin/ # 企业端：农机/农资审批、承包方管理、作物与收获管理等
            └── system-admin/     # 系统管理员：用户管理、承包方管理
```

## 功能模块

- **土地管理**：地块信息、土地流转/分配、农事活动记录
- **种植管理**：种植计划、计划调整、种植记录、农事操作记录
- **作物管理**：作物信息、生长监控、成熟作物与加工记录、产品管理
- **农机管理**：设备信息、设备类型、领用申请与审批、维修申请、使用记录
- **农资管理**：物资信息、物资类型、库存记录、领用申请与审批
- **AI 农业助手（RAG）**：知识库与文档管理（PDF 等解析、分块、pgvector 向量化检索）、流式对话、会话/消息记录
- **权限体系**：基于 Spring Security + JWT 的 RBAC（用户、角色、权限），支持系统管理员 / 企业管理员 / 农户多角色视图
- **其他**：短信验证码登录、MinIO 文件存储、Swagger（springdoc-openapi）接口文档

## 快速开始

### 方式一：Docker Compose 一键部署（推荐）

1. 复制环境变量文件并按需修改（`AL_API_KEY` 为阿里云 DashScope 密钥，必填）：

   ```bash
   cp .env.example .env
   ```

2. 在项目根目录构建并启动全部服务：

   ```bash
   docker compose up -d --build
   ```

3. 访问系统：

   | 服务 | 地址 |
   | --- | --- |
   | 前端 | http://localhost （Nginx 80 端口，/api 反代到后端） |
   | 后端 | http://localhost:8080（容器内部通信，未在宿主机映射） |
   | MySQL | localhost:3306（宿主机映射） |
   | MinIO API / 控制台 | localhost:9000 / localhost:9001 |
   | PostgreSQL、Redis | 仅容器内部网络访问 |

   首次启动时，MySQL/PostgreSQL 会自动执行 `data/` 下的初始化 SQL。

### 方式二：本地开发

**后端**（JDK 17+、Maven、本地 MySQL/Redis 等）：

```bash
cd graduation_work_springBoot
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**前端**（Node 20.19+ 或 22.12+）：

```bash
cd graduation_work_vue
npm install
npm run dev        # 开发服务器，接口经 Vite 代理转发到后端
```

**前端测试**：

```bash
npm test           # Jest 单元测试并输出覆盖率
```

## 配置说明

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `MYSQL_DB` / `MYSQL_USER` / `MYSQL_PASSWORD` | 业务库连接信息 | smart_agriculture / root / 123456 |
| `PG_DB` / `PG_USER` / `PG_PASSWORD` | AI 向量库连接信息 | agriculture_ai / postgres / 123456 |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` / `MINIO_BUCKET_NAME` | MinIO 对象存储 | admin / minio_password_123 / agriculture |
| `MINIO_PUBLIC_BASE_URL` | MinIO 附件的公网访问域名（AI 图片识别需要，见下文说明） | http://img.ajie-ai.xyz |
| `AL_API_KEY` | 阿里云 DashScope API Key（AI 助手必需） | 无，必须设置 |

后端通过环境变量注入配置（见 `application-prod.yaml`），前端容器内由 `nginx.conf` 将 `/api` 请求代理至 `backend:8080`。

## AI 助手图片识别说明（重要）

AI 助手的图片识别调用的是阿里云 DashScope（Qwen-VL）云端 API，**模型侧需要能通过网络地址访问到图片本身**。而 MinIO 默认只监听内网地址（如 `http://minio:9000` / `http://192.168.x.x:9000`），公网 API 无法回源取图，直接传内网 URL 会导致图片识别失败。有两种解决方式：

### 方式 A：配置公网域名 + Cloudflare Tunnel（本项目采用）

将内网 MinIO 的 9000 端口通过隧道映射到一个公网域名，模型和浏览器都可通过该域名直接访问图片：

1. 注册域名并安装 `cloudflared`，创建 Tunnel，将公网主机名（本项目为 `img.ajie-ai.xyz`）指向内网服务：

   ```bash
   cloudflared tunnel route dns <tunnel-id> img.ajie-ai.xyz
   cloudflared tunnel run <tunnel-id>   # 隧道源站配置为 http://minio:9000（或 localhost:9000）
   ```

2. 把域名告知后端，上传附件返回的 URL 会自动改用该域名拼接：

   ```yaml
   # application-prod.yaml（或 .env 中的 MINIO_PUBLIC_BASE_URL）
   minio:
     public-base-url: http://img.ajie-ai.xyz
   ```

   对应代码：[MinioServiceImpl.publicUrl()](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_springBoot/src/main/java/com/clj/ai/service/impl/MinioServiceImpl.java) 与 [MinioProperties](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_springBoot/src/main/java/com/clj/ai/config/MinioProperties.java)。留空 `public-base-url` 时回退为内网 endpoint，仅本地调试可用。

### 方式 B：改为 Base64 传图（无域名/内网部署的替代方案）

若无法配置公网域名，可修改对应文件，将"传图片 URL"改为"传 Base64 编码的图片数据"，让模型无需回源访问 MinIO 即可识别图片：

- **后端**：当前实现已在服务端把 MinIO 图片读成字节流并转为 Base64 data URL 后再发给模型（`data:image/png;base64,...`），关键代码见 [MinioServiceImpl.readImageAsDataUrl()](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_springBoot/src/main/java/com/clj/ai/service/impl/MinioServiceImpl.java) 和 [MessageUtil.buildMultimodalMessage()](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_springBoot/src/main/java/com/clj/ai/util/MessageUtil.java)。若想彻底绕开 MinIO 存储环节，可改为前端直接上传 Base64。
- **前端**：修改聊天页 [AgriculturalAssistant.vue](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_vue/src/views/common/AgriculturalAssistant.vue) 的附件上传逻辑——用 `FileReader.readAsDataURL()` 将图片读成 Base64，随聊天请求的 `imageUrls` 字段直接发给后端（接口封装见 [api/index.js](file:///c:/Users/ajie/Desktop/graduation_work/graduation_work_vue/src/api/index.js)），后端将 data URL 原样传入 `ImageContent.from(...)` 即可。

> 注意：Base64 会使请求体增大约 1/3，过大的图片可能被模型接口拒绝，建议上传前先压缩；同时图片不再入库到 MinIO，历史消息中的图片回显需要另行处理。

# 核心页面展示
<img width="2560" height="1336" alt="image" src="https://github.com/user-attachments/assets/bfeb21fc-0818-4a89-ad3e-05e24c147df9" />
<img width="2560" height="1344" alt="image" src="https://github.com/user-attachments/assets/b1c714db-36de-4a3b-bcc5-3d01ea5c62ff" />
<img width="2559" height="1327" alt="image" src="https://github.com/user-attachments/assets/62abdd52-5199-487b-b28a-a893dae73378" />
<img width="2558" height="1337" alt="image" src="https://github.com/user-attachments/assets/3679bbc4-5993-4032-8000-4d56e2bbe56d" />
<img width="2560" height="1363" alt="image" src="https://github.com/user-attachments/assets/5038e9ce-ca31-4c34-9729-752fec1dc51e" />
<img width="2559" height="1344" alt="image" src="https://github.com/user-attachments/assets/29dd4e31-3516-4e84-8e7a-fbce8c5c0ed4" />
<img width="2560" height="1339" alt="image" src="https://github.com/user-attachments/assets/30b859ba-e11d-4599-84cf-9870fff32a13" />
<img width="2554" height="1347" alt="image" src="https://github.com/user-attachments/assets/fe855061-f843-4b0a-8f94-a192e6624ce6" />
<img width="2553" height="1342" alt="image" src="https://github.com/user-attachments/assets/379b609e-a22a-4cce-8a0f-7419ce88598a" />
<img width="2560" height="1354" alt="image" src="https://github.com/user-attachments/assets/7ab23a1d-460d-48e4-94c9-a7932807fa50" />
