# 智能体配置平台（Agent Config Platform）

一个可视化的智能体搭建平台，包含三大核心模块：

1. **Agent 配置模块**：定义智能体名称、模型供应商、模型名、提示词模板。
2. **MCP 工具配置模块**：配置可被智能体调用的 MCP 工具服务。
3. **Skill 配置模块**：维护技能模板和执行说明。

并支持基于已配置内容 **一键创建 ReactAgent**（Spring AI Alibaba Agent Framework）。

后端技术栈：**Java + Spring Boot + Spring AI Alibaba**  
前端技术栈：**React + Ant Design + Vite**

---

## 运行方式

### 1) 配置 DashScope Key（示例）

```bash
export AI_DASHSCOPE_API_KEY=your_api_key_here
```

### 2) 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 3) 启动前端

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
- `POST /api/react-agents/create`：根据 Agent + Tool + Skill 配置一键创建 ReactAgent，并可选试运行一次。

---

## ReactAgent 执行拦截与 Hook

### 平台内置执行 Hooks（后端）
- `RuntimeParamInjectHook`：将 `runtimeParams` 注入到系统提示词，并将 `requestId` 前缀到用户消息中。
- `RiskControlHook`：对高风险请求进行阻断，防止危险指令进入模型执行。
- `AuditTrailHook`：输出 trace 级日志，记录构建与执行过程。

### Spring AI Alibaba 原生 Hooks/Interceptors（按请求开启）
可通过 `hookOptions` 动态开启：
- `maxModelCalls` -> `ModelCallLimitHook`
- `enableToolRetry` + `toolRetryTimes` -> `ToolRetryInterceptor`
- `enableContextEditing` -> `ContextEditingInterceptor`

### 请求示例

```json
{
  "agentId": "agent-uuid",
  "userMessage": "给我一份今日运营总结",
  "runtimeParams": {
    "requestId": "REQ-20260328-001",
    "tenant": "default-tenant"
  },
  "hookOptions": {
    "maxModelCalls": 6,
    "enableToolRetry": true,
    "toolRetryTimes": 2,
    "enableContextEditing": true
  }
}
```

---

## 参考文档

- https://github.com/alibaba/spring-ai-alibaba
- https://java2ai.com/en/docs/quick-start/
