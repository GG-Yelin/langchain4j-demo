# 聊天记忆管理

## 问题背景

在当前的实现中，记忆聊天使用 `ConcurrentHashMap<String, ChatMemory>` 在内存中存储每个会话的聊天历史。这会导致以下问题：

1. **内存泄漏**：会话数据永久存储，不会自动清理
2. **内存增长**：随着用户数量增加，内存占用无限增长
3. **无过期机制**：即使用户已经离开，session 数据仍然占用内存
4. **单实例限制**：数据存储在应用实例内存中，服务重启后丢失

## 解决方案

### 方案1：手动清理接口（已实现）

在 `ChatServiceImpl` 中添加了手动管理接口：

#### API 接口

```bash
# 1. 获取内存统计
GET /api/chat/memory/stats

# 响应示例
{
  "totalSessions": 15
}

# 2. 清除指定会话的记忆
DELETE /api/chat/memory/{sessionId}

# 响应示例
{
  "success": true,
  "message": "Memory cleared for session: user-123",
  "remainingSessions": 14
}

# 3. 清除所有会话的记忆
DELETE /api/chat/memory

# 响应示例
{
  "success": true,
  "message": "All memory cleared. Total sessions removed: 15",
  "remainingSessions": 0
}
```

#### 使用示例

```bash
# 查看当前会话数量
curl http://localhost:8080/api/chat/memory/stats

# 清除特定会话
curl -X DELETE http://localhost:8080/api/chat/memory/user-123

# 清除所有会话
curl -X DELETE http://localhost:8080/api/chat/memory
```

### 方案2：自动过期机制（已实现）

创建了 `ChatServiceWithExpirationImpl`，提供自动清理功能：

#### 特性

1. **会话过期时间**：默认 30 分钟无活动后过期（可配置）
2. **定时清理**：每 10 分钟自动清理过期会话
3. **自动更新**：每次访问会话时自动更新最后访问时间
4. **仍支持手动清理**：保留手动清理接口

#### 配置方式

默认情况下，使用的是 `ChatServiceImpl`（不带自动过期），因为它被标记为 `@Primary`。

##### 方法1：使用 @Qualifier 注解（推荐）

修改 `ChatController` 注入指定的实现：

```java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    // 默认：使用基础实现（手动清理）
    private final ChatService chatService;

    // 或者明确指定使用带过期的实现（自动清理）
    @Qualifier("chatServiceWithExpiration")
    private final ChatService chatService;

    // ...
}
```

##### 方法2：修改 @Primary 注解

如果想让自动过期成为默认实现，交换两个类的 `@Primary` 注解：

```java
// ChatServiceImpl.java
@Service
// @Primary  // 移除此注解
public class ChatServiceImpl implements ChatService { }

// ChatServiceWithExpirationImpl.java
@Service("chatServiceWithExpiration")
@Primary  // 添加此注解
public class ChatServiceWithExpirationImpl implements ChatService { }
```

##### 方法3：使用 Profile 配置

使用 Spring Profile 根据环境选择实现：

```java
@Service
@Primary
@Profile("!expiration")  // 非 expiration 环境使用
public class ChatServiceImpl implements ChatService { }

@Service("chatServiceWithExpiration")
@Primary
@Profile("expiration")   // expiration 环境使用
public class ChatServiceWithExpirationImpl implements ChatService { }
```

然后在 `application.yml` 中激活：

```yaml
spring:
  profiles:
    active: expiration  # 启用自动过期
```

#### 调整过期时间

修改 `ChatServiceWithExpirationImpl` 中的常量：

```java
// 会话过期时间：30分钟 → 修改为你需要的时间
private static final long SESSION_EXPIRATION_MS = 30 * 60 * 1000;

// 清理间隔：10分钟 → 修改 @Scheduled 注解
@Scheduled(fixedRate = 10 * 60 * 1000)
public void cleanupExpiredSessions() { }
```

### 方案3：使用外部缓存（推荐用于生产环境）

对于生产环境，建议使用专业的缓存方案：

#### 3.1 Redis 缓存

**优点**：
- 分布式缓存，多实例共享
- 支持自动过期（TTL）
- 持久化选项
- 高性能

**实现示例**：

```java
@Service
@RequiredArgsConstructor
public class RedisChatServiceImpl implements ChatService {

    private final RedisTemplate<String, ChatMemory> redisTemplate;

    // 会话过期时间：30分钟
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    @Override
    public ChatResponseVO chatWithMemory(ChatRequestVO requestVO) {
        String sessionId = requestVO.getSessionId();
        String key = "chat:session:" + sessionId;

        // 从 Redis 获取或创建记忆
        ChatMemory memory = redisTemplate.opsForValue().get(key);
        if (memory == null) {
            memory = MessageWindowChatMemory.withMaxMessages(10);
        }

        // ... 处理聊天 ...

        // 保存到 Redis 并设置过期时间
        redisTemplate.opsForValue().set(key, memory, SESSION_TTL);

        return response;
    }
}
```

#### 3.2 Caffeine 本地缓存

**优点**：
- 高性能本地缓存
- 自动过期和淘汰
- 内存占用可控
- 无需外部依赖

**实现示例**：

```java
@Service
public class CaffeineChatServiceImpl implements ChatService {

    private final Cache<String, ChatMemory> memoryCache;

    public CaffeineChatServiceImpl() {
        this.memoryCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))  // 30分钟无访问后过期
                .maximumSize(1000)                           // 最多1000个会话
                .recordStats()                                // 记录统计信息
                .build();
    }

    @Override
    public ChatResponseVO chatWithMemory(ChatRequestVO requestVO) {
        String sessionId = requestVO.getSessionId();

        ChatMemory memory = memoryCache.get(sessionId,
            key -> MessageWindowChatMemory.withMaxMessages(10));

        // ... 处理聊天 ...

        return response;
    }
}
```

## 监控建议

### 1. 添加监控端点

```java
@GetMapping("/actuator/chat-memory")
public Map<String, Object> getMemoryMetrics() {
    return Map.of(
        "totalSessions", chatService.getMemorySessionCount(),
        "estimatedMemoryMB", calculateMemoryUsage(),
        "oldestSessionAge", getOldestSessionAge()
    );
}
```

### 2. 日志记录

当前实现已包含详细日志：

```
INFO  - Creating new session: user-123
INFO  - Chat with memory - SessionId: user-123, Messages: 6, LastAccess: 2026-01-13T18:30:00Z
INFO  - Cleanup completed. Removed 5 expired sessions. Remaining: 10
INFO  - Manually cleared memory for session: user-123
```

### 3. 告警阈值

建议设置告警：

- 会话数量 > 10000：可能存在内存泄漏
- 清理效率低 < 10%：可能需要调整过期时间
- 内存占用 > 2GB：考虑增加清理频率或使用 Redis

## 最佳实践

### 开发环境
- 使用 `ChatServiceImpl`（方案1）
- 手动清理测试数据
- 快速迭代开发

### 测试环境
- 使用 `ChatServiceWithExpirationImpl`（方案2）
- 较短的过期时间（如 10 分钟）
- 验证自动清理逻辑

### 生产环境
- 使用 Redis（方案3.1）
- 合理的过期时间（如 30-60 分钟）
- 启用监控和告警
- 定期检查缓存命中率

## 配置参考

### application.yml

```yaml
# 聊天记忆配置
chat:
  memory:
    # 会话过期时间（分钟）
    session-ttl: 30

    # 最大会话数量
    max-sessions: 10000

    # 清理间隔（分钟）
    cleanup-interval: 10

    # 每个会话最大消息数
    max-messages-per-session: 10

# Redis 配置（如果使用方案3.1）
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 5000ms
```

## 迁移指南

从当前实现迁移到带过期的实现：

1. **备份数据**（如果需要）
   ```bash
   # 导出当前会话统计
   curl http://localhost:8080/api/chat/memory/stats > memory-backup.json
   ```

2. **更新配置**
   ```java
   // 修改 ChatController 注入
   @Qualifier("chatServiceWithExpiration")
   private final ChatService chatService;
   ```

3. **启动应用**
   - 自动清理会在 10 分钟后首次执行
   - 查看日志确认清理正常运行

4. **监控观察**
   - 监控会话数量变化
   - 检查清理日志
   - 验证用户体验无影响

## 常见问题

### Q: 会话被清理后，用户还能继续对话吗？
A: 可以。清理只是删除历史记忆，用户发送新消息时会自动创建新会话。只是失去了上下文。

### Q: 如何避免活跃用户的会话被清理？
A: 使用 `expireAfterAccess`（最后访问后过期）而不是 `expireAfterWrite`（创建后过期）。当前实现已经这样做了。

### Q: 清理过期会话会影响性能吗？
A: 不会。清理操作是异步的，使用定时任务执行，不影响用户请求。

### Q: 如何估算内存占用？
A: 粗略估算：每个会话约占用 10KB（10条消息 × 1KB/消息）。10000个会话约 100MB。

### Q: 是否需要持久化聊天历史？
A: 取决于业务需求。如果需要长期保存对话记录，建议：
- 将聊天历史保存到数据库
- 缓存只保留最近的对话
- 用户登录时从数据库加载历史
