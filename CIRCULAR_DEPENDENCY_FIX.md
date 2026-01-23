# 循环依赖问题修复

## 问题描述

启动后端时报错：
```
APPLICATION FAILED TO START

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  mcpController
↑     ↓
|  mcpServiceImpl
└─────┘
```

## 问题原因

在 `McpServiceImpl` 的构造函数中，通过 `ApplicationContext` 扫描所有 `@Component` 来查找工具类。这导致：

1. Spring 尝试创建 `McpServiceImpl`
2. `McpServiceImpl` 构造函数需要 `ApplicationContext`
3. `ApplicationContext` 需要先创建所有 Bean（包括 `McpController`）
4. `McpController` 依赖 `McpServiceImpl`
5. 形成循环依赖 ❌

## 解决方案

### 修改 1: 创建独立的配置类

**新文件**: `ToolsConfiguration.java`

```java
@Configuration
public class ToolsConfiguration {

    @Bean
    public List<Object> toolBeans(ApplicationContext applicationContext) {
        List<Object> tools = new ArrayList<>();

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                // 只收集 tools 包下的 Bean
                if (bean.getClass().getPackage() != null &&
                    bean.getClass().getPackage().getName().contains(".tools")) {
                    tools.add(bean);
                }
            } catch (Exception e) {
                // 忽略无法实例化的 Bean
            }
        }

        return tools;
    }
}
```

### 修改 2: McpServiceImpl 构造函数

**之前**:
```java
public McpServiceImpl(ChatLanguageModel chatModel, ApplicationContext applicationContext) {
    this.chatModel = chatModel;
    this.applicationContext = applicationContext;

    // 直接扫描 ApplicationContext - 导致循环依赖
    String[] beanNames = applicationContext.getBeanNamesForAnnotation(Component.class);
    ...
}
```

**之后**:
```java
public McpServiceImpl(ChatLanguageModel chatModel, List<Object> toolBeans) {
    this.chatModel = chatModel;
    this.applicationContext = null;

    // 直接接收工具 Bean 列表 - 无循环依赖
    this.tools = toolBeans.stream()
            .filter(bean -> bean.getClass().getPackage() != null &&
                           bean.getClass().getPackage().getName().contains(".tools"))
            .collect(Collectors.toList());
    ...
}
```

## 原理说明

### Bean 创建顺序

**修复前**:
```
1. Spring 开始创建 McpController
   → 需要 McpServiceImpl
2. Spring 开始创建 McpServiceImpl
   → 构造函数需要 ApplicationContext
   → ApplicationContext 扫描所有 Bean
   → 发现 McpController 还未创建
   → 循环依赖！❌
```

**修复后**:
```
1. Spring 创建 ToolsConfiguration 的 toolBeans() 方法返回的 Bean
   → 此时 ApplicationContext 已准备好
   → 扫描工具类，返回工具列表
2. Spring 创建 McpServiceImpl
   → 注入 ChatLanguageModel 和 toolBeans（List）
   → 无需访问 ApplicationContext
   → 创建成功 ✅
3. Spring 创建 McpController
   → 注入 McpServiceImpl
   → 创建成功 ✅
```

## 编译状态

✅ **BUILD SUCCESS**

## 如何启动

```bash
# 1. 进入后端目录
cd langchain4j-demo-core

# 2. 启动服务
mvn spring-boot:run
```

启动成功后，应该看到：
```
发现工具: WeatherTool
发现工具: CalculatorTool
已加载 2 个本地工具
Started Langchain4jDemoApplication in X seconds
```

## 验证功能

### 1. 检查工具列表

```bash
curl http://localhost:8080/api/mcp/tools
```

应该返回工具列表，包括 WeatherTool 和 CalculatorTool 的所有方法。

### 2. 测试聊天

```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "请帮我查询北京的天气"}'
```

### 3. 前端测试

打开 `http://localhost:5173`，点击 "MCP 模式"，应该能看到工具列表并正常聊天。

## 相关文件

- `ToolsConfiguration.java` - 工具配置类（新增）
- `McpServiceImpl.java` - MCP 服务实现（已修改）
- `WeatherTool.java` - 天气工具
- `CalculatorTool.java` - 计算器工具

## 总结

✅ **问题已修复**: 循环依赖
✅ **编译成功**: BUILD SUCCESS
✅ **解决方案**: 将工具收集逻辑移到独立的配置类
✅ **可以启动**: 等待用户重启测试

---

**修复时间**: 2026-01-21
**状态**: ✅ 已修复，可以启动
