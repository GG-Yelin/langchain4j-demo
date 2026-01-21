# 简化版 RAG 功能使用指南

## 改进说明

已将 RAG 文档加载功能简化，现在**无需手动输入路径**，系统会自动从项目的 `resources/knowledge` 目录加载文档。

## 目录结构

```
langchain4j-demo-core/
└── src/main/resources/
    └── knowledge/
        └── 阿里巴巴Java开发规范（嵩山版）.pdf  ← 你的 PDF 文档
```

## 使用步骤

### 1. 准备文档

将你的文档放入 `src/main/resources/knowledge/` 目录：

```bash
cd langchain4j-demo-core/src/main/resources/knowledge/
# 将文档复制到这个目录
```

支持的文档格式：
- PDF (.pdf)
- 文本 (.txt)
- Markdown (.md)
- Word (.docx)

### 2. 启动服务

**启动后端：**
```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

**启动前端：**
```bash
cd langchain4j-demo-frontend
npm run dev
```

### 3. 加载文档

1. 打开前端页面：http://localhost:5173
2. 在左侧边栏找到"📚 加载文档到向量库"按钮（绿色）
3. 点击按钮，会弹出确认对话框
4. 点击"开始加载"按钮
5. 等待加载完成（会显示成功提示）

### 4. 使用 RAG 问答

1. 切换到"RAG 问答"模式
2. 开始提问

**示例问题（基于阿里巴巴Java开发规范）：**
```
问: Java 命名规范有哪些？
问: 什么是魔法值？
问: 如何正确使用集合？
问: 异常处理的规范是什么？
问: 什么是 OOP 规约？
```

## 界面变化

### 旧版（需要输入路径）
```
┌─────────────────────────────────┐
│ 加载文档到向量数据库         × │
├─────────────────────────────────┤
│ 文档路径                        │
│ ┌───────────────────────────┐   │
│ │ /path/to/documents        │   │  ← 需要手动输入
│ └───────────────────────────┘   │
│         [取消] [加载文档]       │
└─────────────────────────────────┘
```

### 新版（自动加载）
```
┌─────────────────────────────────┐
│ 加载文档到向量数据库         × │
├─────────────────────────────────┤
│ 📚 将从 resources/knowledge     │
│    目录加载文档到向量数据库     │
│                                 │
│ 📁 目录: src/.../knowledge/     │
│ 📄 支持格式: PDF, TXT, MD...    │  ← 自动加载，无需输入
│                                 │
│         [取消] [开始加载]       │
└─────────────────────────────────┘
```

## 后端 API 变化

### 旧版 API
```bash
POST /api/rag/load
Content-Type: application/json

{
  "path": "/path/to/documents"
}
```

### 新版 API
```bash
POST /api/rag/load
# 无需参数
```

**响应：**
```json
{
  "success": true,
  "message": "已成功从 knowledge 目录加载文档"
}
```

## 代码变化总结

### 后端

1. **RagService.java**
   ```java
   // 旧方法
   void loadDocumentFromPath(String path);

   // 新方法
   void loadDocumentsFromKnowledge();
   ```

2. **RagServiceImpl.java**
   ```java
   // 自动从 classpath:knowledge 加载
   ClassLoader classLoader = getClass().getClassLoader();
   URL resourceUrl = classLoader.getResource("knowledge");
   String knowledgePath = resourceUrl.getPath();
   List<Document> documents = FileSystemDocumentLoader.loadDocuments(knowledgePath);
   ```

3. **RagController.java**
   ```java
   // 无需接收 path 参数
   @PostMapping("/load")
   public ResponseEntity<?> loadDocuments() {
       ragService.loadDocumentsFromKnowledge();
       // ...
   }
   ```

### 前端

1. **API 调用简化**
   ```javascript
   // 旧方式
   export const loadDocuments = async (path) => {
     const { data } = await api.post('/rag/load', { path: path })
     return data
   }

   // 新方式
   export const loadDocuments = async () => {
     const { data } = await api.post('/rag/load')
     return data
   }
   ```

2. **Modal 组件简化**
   - 移除了路径输入框
   - 显示固定的 knowledge 目录信息
   - 按钮文字从"加载文档"改为"开始加载"

## 优势

### ✅ 简化操作
- 用户无需记住或输入文档路径
- 减少出错可能性
- 提升用户体验

### ✅ 标准化管理
- 所有文档统一放在 knowledge 目录
- 便于版本控制
- 易于团队协作

### ✅ 自动同步
- 添加新文档到 knowledge 目录
- 点击加载按钮即可更新
- 无需修改代码或配置

## 更新文档的流程

1. **添加新文档**
   ```bash
   cp 新文档.pdf src/main/resources/knowledge/
   ```

2. **重新编译**（如果服务在运行）
   ```bash
   # 方式1: 重启服务
   # Ctrl+C 停止，然后 mvn spring-boot:run

   # 方式2: 使用 spring-boot-devtools 自动重载
   ```

3. **在前端重新加载**
   - 点击"📚 加载文档到向量库"
   - 点击"开始加载"

## 注意事项

### ⚠️ 文档位置
文档必须放在 `src/main/resources/knowledge/` 目录下，否则无法加载。

### ⚠️ 编译后文件
运行时实际读取的是 `target/classes/knowledge/` 下的文件，所以：
- 添加新文档后需要重新编译
- 或者在开发模式下会自动复制

### ⚠️ 大文件处理
- PDF 文件较大（如你的规范文档约 2MB）
- 加载时间可能需要 10-30 秒
- 请耐心等待加载完成

### ⚠️ 内存存储
- 向量数据存储在内存中
- 服务重启后需要重新加载
- 生产环境建议使用持久化向量数据库

## 测试

### 快速测试流程

1. **验证文档存在**
   ```bash
   ls -lh src/main/resources/knowledge/
   # 应该能看到: 阿里巴巴Java开发规范（嵩山版）.pdf
   ```

2. **启动服务**
   ```bash
   # 终端1: 后端
   cd langchain4j-demo-core
   mvn spring-boot:run

   # 终端2: 前端
   cd langchain4j-demo-frontend
   npm run dev
   ```

3. **加载文档**
   - 访问 http://localhost:5173
   - 点击"📚 加载文档到向量库"
   - 点击"开始加载"
   - 看到成功消息

4. **测试问答**
   - 切换到"RAG 问答"模式
   - 提问: "Java 命名有什么规范？"
   - 验证 AI 基于文档内容回答

## 故障排查

### 问题1: 找不到 knowledge 目录
**错误信息**: "Knowledge directory not found in classpath"

**解决方案**:
```bash
# 确保目录存在
mkdir -p src/main/resources/knowledge

# 重新编译
mvn clean compile
```

### 问题2: 没有找到文档
**错误信息**: "No documents found in knowledge directory"

**解决方案**:
```bash
# 检查文档是否存在
ls src/main/resources/knowledge/

# 如果为空，添加文档
cp /path/to/your/document.pdf src/main/resources/knowledge/

# 重新编译
mvn clean compile
```

### 问题3: 加载时间过长
**现象**: 加载超过 60 秒还没完成

**可能原因**:
- 文档太大
- Embedding API 响应慢
- 网络问题

**解决方案**:
```yaml
# 在 application.yml 中增加超时时间
langchain4j:
  open-ai:
    embedding-model:
      timeout: 120s  # 增加到 120 秒
```

### 问题4: PDF 解析失败
**错误信息**: "Failed to parse PDF document"

**解决方案**:
- 确保 PDF 不是扫描版（需要文字可选取）
- 检查 PDF 是否损坏
- 尝试转换为其他格式（如 TXT）

## 性能优化建议

### 1. 文档预处理
```bash
# 将大文档分割成小文件
# 例如: 将规范手册按章节分割
阿里巴巴Java开发规范-第1章.pdf
阿里巴巴Java开发规范-第2章.pdf
...
```

### 2. 调整分割参数
```java
// 在 RagServiceImpl 中调整
DocumentSplitter splitter = DocumentSplitters.recursive(
    1000,  // 增加片段大小
    200    // 增加重叠大小
);
```

### 3. 缓存机制
未来可以添加：
- 启动时自动加载
- 文档指纹检测（避免重复加载）
- 增量更新

## 总结

简化后的 RAG 功能：
- ✅ 无需输入路径
- ✅ 自动从 knowledge 目录加载
- ✅ 界面更简洁
- ✅ 操作更直观
- ✅ 维护更方便

只需三步即可使用：
1. 文档放入 knowledge 目录
2. 点击加载按钮
3. 开始 RAG 问答

现在可以基于你的"阿里巴巴Java开发规范"进行智能问答了！
