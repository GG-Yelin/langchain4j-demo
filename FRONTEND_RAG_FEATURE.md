# 前端 RAG 文档加载功能说明

## 功能概述

在前端页面的侧边栏新增了"📚 加载文档到向量库"按钮，方便用户随时更新向量数据库中的文档，支持 RAG 功能的使用。

## 实现的功能

### 1. 新增 API 方法
**文件**: `src/api/chat.js`

```javascript
export const loadDocuments = async (path) => {
  const { data } = await api.post('/rag/load', {
    path: path
  })
  return data
}
```

### 2. 创建文档加载弹窗组件
**文件**: `src/components/RagDocumentModal.vue`

功能特性：
- 输入文档路径（支持文件或目录）
- 加载状态提示
- 成功/失败反馈
- 自动关闭（成功后2秒）
- 响应式设计

### 3. 侧边栏按钮
**文件**: `src/components/Sidebar.vue`

- 添加了醒目的"📚 加载文档到向量库"按钮
- 采用绿色渐变色，与其他按钮区分
- 点击后打开文档加载弹窗

### 4. 主应用集成
**文件**: `src/App.vue`

- 集成 RagDocumentModal 组件
- 文档加载成功后显示系统提示消息
- 消息样式优化（MessageItem.vue）

## 使用方法

### 启动前端

```bash
cd langchain4j-demo-frontend
npm install
npm run dev
```

### 使用流程

1. **打开应用**
   - 访问前端应用（通常是 http://localhost:5173）

2. **点击"加载文档"按钮**
   - 在左侧边栏找到"📚 加载文档到向量库"按钮
   - 点击按钮打开文档加载弹窗

3. **输入文档路径**
   - 输入文档路径，例如：
     - 绝对路径：`/Users/username/documents`
     - 相对路径：`./test-documents`
     - 单个文件：`/path/to/document.txt`

4. **加载文档**
   - 点击"加载文档"按钮
   - 等待加载完成（显示加载动画）
   - 成功后会显示成功消息并自动关闭

5. **使用 RAG 问答**
   - 切换到"RAG 问答"模式
   - 基于已加载的文档进行提问

## 界面截图说明

### 侧边栏按钮
- 位置：设置区域，在"清空对话"按钮下方
- 样式：绿色渐变背景，带有书籍图标
- 交互：悬停时有轻微上移和阴影效果

### 文档加载弹窗
- 标题：加载文档到向量数据库
- 输入框：文档路径输入（支持回车快捷键）
- 按钮：
  - 取消：关闭弹窗
  - 加载文档：执行加载操作
- 状态显示：
  - 加载中：显示旋转动画和提示文字
  - 成功：绿色背景提示
  - 失败：红色背景错误信息

### 系统消息
- 文档加载成功后，会在聊天区域显示一条绿色系统消息
- 消息内容：✅ 文档已成功加载到向量数据库，现在可以使用 RAG 问答模式进行提问了！
- 样式：居中显示，绿色渐变背景

## 技术实现细节

### 组件通信
```
Sidebar (emit: show-rag-load)
    ↓
App.vue (showRagLoadModal = true)
    ↓
RagDocumentModal (emit: documents-loaded)
    ↓
App.vue (handleDocumentsLoaded)
    ↓
显示系统消息
```

### 错误处理
- 路径为空时禁用加载按钮
- 网络错误捕获并显示错误消息
- 后端返回的错误信息友好展示

### 响应式设计
- 桌面端：弹窗居中，最大宽度 600px
- 移动端：全屏显示，按钮垂直排列

## 文件清单

新增/修改的文件：
1. ✅ `src/api/chat.js` - 添加 loadDocuments API 方法
2. ✅ `src/components/RagDocumentModal.vue` - 新建文档加载弹窗组件
3. ✅ `src/components/Sidebar.vue` - 添加加载文档按钮
4. ✅ `src/components/MessageItem.vue` - 支持系统消息样式
5. ✅ `src/App.vue` - 集成 RagDocumentModal

## 测试建议

### 功能测试
1. **正常流程**
   - 输入有效路径
   - 验证加载成功
   - 检查系统消息显示

2. **错误处理**
   - 输入不存在的路径
   - 验证错误消息显示
   - 检查用户友好的错误提示

3. **边界情况**
   - 空路径（按钮应禁用）
   - 加载中再次点击（按钮应禁用）
   - 网络超时

### UI 测试
1. **响应式**
   - 桌面端显示
   - 移动端显示
   - 平板显示

2. **交互**
   - 悬停效果
   - 点击反馈
   - 键盘快捷键（回车）

## 未来优化建议

1. **文档路径浏览器**
   - 添加文件/目录选择器
   - 避免手动输入路径

2. **批量操作**
   - 支持多个路径同时加载
   - 显示加载进度条

3. **文档管理**
   - 查看已加载的文档列表
   - 删除特定文档
   - 清空向量数据库

4. **预设路径**
   - 保存常用路径
   - 路径历史记录

5. **实时反馈**
   - 显示加载的文档数量
   - 显示向量化进度

## 配置说明

### 后端 API
确保后端 `/api/rag/load` 接口已实现：
```java
@PostMapping("/load")
public ResponseEntity<?> loadDocuments(@RequestBody Map<String, String> payload)
```

### 前端代理配置
如果前后端分离部署，需要在 `vite.config.js` 中配置代理：
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

## 常见问题

### Q: 为什么加载文档后在 RAG 模式下仍然找不到相关内容？
A: 可能的原因：
1. 文档路径不正确
2. 文档格式不支持
3. 相似度阈值设置过高
4. 向量数据库是内存存储，服务重启后会丢失

### Q: 支持哪些文档格式？
A: 取决于后端配置的文档解析器，通常支持：
- 纯文本 (.txt)
- PDF (.pdf)
- Word (.docx)
- Markdown (.md)

### Q: 如何持久化向量数据？
A: 需要修改后端配置，将 `InMemoryEmbeddingStore` 替换为持久化向量数据库（如 Pinecone、Weaviate 等）。

## 总结

该功能为用户提供了方便的文档管理界面，使得 RAG 功能更加易用。用户可以随时更新文档库，无需重启服务或手动调用 API。
