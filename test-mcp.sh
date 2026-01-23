#!/bin/bash

# MCP 功能测试脚本

BASE_URL="http://localhost:8080/api/mcp"

echo "======================================"
echo "MCP 功能测试"
echo "======================================"
echo ""

echo "测试 1: 获取可用工具列表"
echo "--------------------------------------"
curl -s -X GET "$BASE_URL/tools" | jq '.' || echo "请求失败或返回非JSON"
echo ""
echo ""

echo "测试 2: MCP 聊天（不使用工具）"
echo "--------------------------------------"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下你自己"
  }' | jq '.' || echo "请求失败或返回非JSON"
echo ""
echo ""

echo "测试 3: MCP 聊天（可能使用工具）"
echo "--------------------------------------"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请帮我查询今天的天气"
  }' | jq '.' || echo "请求失败或返回非JSON"
echo ""
echo ""

echo "测试 4: 直接调用工具"
echo "--------------------------------------"
curl -s -X POST "$BASE_URL/invoke" \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "get_weather",
    "parameters": {
      "city": "北京"
    }
  }' | jq '.' || echo "请求失败或返回非JSON"
echo ""
echo ""

echo "======================================"
echo "测试完成"
echo "======================================"
echo ""
echo "注意事项:"
echo "1. 确保后端服务已启动: cd langchain4j-demo-core && mvn spring-boot:run"
echo "2. 确保 MCP Server 已启动并在 http://localhost:8081/sse 运行"
echo "3. 如果工具列表为空，说明 MCP Server 未启动或未注册工具"
echo ""
