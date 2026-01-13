# LangChain4j Demo Frontend

基于 Vue 3 + Vite 的前端聊天界面，用于测试 LangChain4j 后端服务。

## 功能特性

- ✨ Vue 3 Composition API
- ⚡️ Vite 快速构建
- 🎨 现代化渐变紫色主题 UI
- 📱 响应式设计，支持移动端
- 🔄 支持 6 种聊天模式：
  - 简单聊天
  - 记忆聊天
  - 流式聊天
  - RAG 问答
  - 工具调用
  - MCP 工具

## 项目结构

```
langchain4j-demo-frontend/
├── index.html              # HTML 入口文件
├── package.json            # 项目依赖配置
├── vite.config.js          # Vite 配置文件
├── src/
│   ├── main.js             # 应用入口
│   ├── App.vue             # 根组件
│   ├── api/
│   │   └── chat.js         # API 接口封装
│   ├── components/
│   │   ├── Sidebar.vue     # 侧边栏组件
│   │   ├── MessageItem.vue # 消息组件
│   │   └── McpToolsModal.vue # MCP 工具模态框
│   ├── views/
│   │   └── ChatView.vue    # 聊天视图
│   └── assets/
│       └── css/
│           └── style.css   # 全局样式
└── README.md
```

## 快速开始

### 1. 安装依赖

```bash
cd langchain4j-demo-frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

前端服务将在 http://localhost:3000 启动。

### 3. 启动后端服务

确保后端服务运行在 http://localhost:8080。

```bash
cd ../langchain4j-demo-core
mvn spring-boot:run
```

### 4. 访问应用

打开浏览器访问 http://localhost:3000

## 可用命令

```bash
# 开发模式
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

## 配置说明

### API 代理配置

在 `vite.config.js` 中配置了 API 代理，所有 `/api` 开头的请求会被代理到后端服务：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 后端 API 接口

前端调用的后端接口包括：

- `POST /api/chat/simple` - 简单聊天
- `POST /api/chat/memory` - 记忆聊天
- `POST /api/chat/stream` - 流式聊天
- `POST /api/rag/query` - RAG 问答
- `POST /api/tool/chat` - 工具调用
- `POST /api/mcp/chat` - MCP 工具调用
- `GET /api/mcp/tools` - 获取 MCP 工具列表

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Axios** - HTTP 客户端
- **Fetch API** - 用于流式响应

## 开发说明

### 组件说明

#### App.vue
应用根组件，管理全局状态和聊天逻辑。

#### Sidebar.vue
侧边栏组件，包含：
- 聊天模式切换
- 参数设置（会话ID、Temperature、Max Tokens）
- 清空对话按钮
- 查看 MCP 工具按钮

#### ChatView.vue
聊天视图组件，包含：
- 消息列表显示
- 消息输入框
- 发送按钮

#### MessageItem.vue
单条消息组件，支持：
- 用户/助手消息样式区分
- 流式输出动画
- 元数据显示（Token 使用、工具调用等）

#### McpToolsModal.vue
MCP 工具列表模态框，显示可用的 MCP 工具信息。

### API 封装

所有后端 API 调用封装在 `src/api/chat.js` 中，便于维护和复用。

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 故障排查

### 前端无法连接后端

1. 确认后端服务运行在 http://localhost:8080
2. 检查浏览器控制台是否有 CORS 错误
3. 确认 vite.config.js 中的 proxy 配置正确

### 流式聊天不工作

1. 检查浏览器是否支持 Fetch API 和 Stream
2. 确认后端正确实现了 SSE (Server-Sent Events)

## License

MIT
