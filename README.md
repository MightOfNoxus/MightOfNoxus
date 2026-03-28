# 智能体配置平台（Agent Config Platform）

一个可视化的智能体搭建平台，包含三大核心模块：

1. **Agent 配置模块**：定义智能体名称、模型供应商、模型名、提示词模板。
2. **MCP 工具配置模块**：配置可被智能体调用的 MCP 工具服务。
3. **Skill 配置模块**：维护技能模板和执行说明。

后端技术栈：**Java + Spring Boot + Spring AI Alibaba**  
前端技术栈：**React + Ant Design + Vite**

---

## 目录结构

```text
.
├── backend                     # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/noxus/agentplatform
└── frontend                    # React UI
    ├── package.json
    └── src
```

---

## 运行方式

### 1) 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 2) 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

---

## 核心 API

- `GET /api/dashboard/summary`：读取平台摘要统计。
- `GET /api/agents` / `POST /api/agents`：管理 Agent 配置。
- `GET /api/mcp-tools` / `POST /api/mcp-tools`：管理 MCP 工具配置。
- `GET /api/skills` / `POST /api/skills`：管理 Skill 配置。

---

## 下一步建议

- 接入持久化（MySQL/PostgreSQL + JPA/MyBatis）。
- 与 Spring AI Alibaba 具体模型服务（如 DashScope/Qwen）打通。
- 增加一键发布能力：将 Agent + Tools + Skills 打包为可运行实例。
- 新增身份权限、审计日志、多环境配置。
