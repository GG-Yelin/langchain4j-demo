#!/bin/bash

# Spring AI MCP Server 测试脚本

SERVER_URL="http://localhost:8081"

echo "=========================================="
echo "Spring AI MCP Server 测试脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4

    echo -e "${YELLOW}测试: $name${NC}"
    echo "请求: $method $url"

    if [ "$method" = "GET" ]; then
        response=$(curl -s "$url")
    else
        response=$(curl -s -X POST "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    echo "响应:"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
    echo "----------------------------------------"
    echo ""
}

# 1. 健康检查
test_endpoint "健康检查" "GET" "$SERVER_URL/mcp/health"

# 2. 服务信息
test_endpoint "服务信息" "GET" "$SERVER_URL/mcp/info"

# 3. 初始化
test_endpoint "初始化连接" "POST" "$SERVER_URL/mcp/initialize" '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {}
}'

# 4. 列出工具
test_endpoint "列出所有工具" "POST" "$SERVER_URL/mcp/tools/list" '{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}'

# 5. 测试计算器 - 加法
test_endpoint "计算器: 15 + 25" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "add",
    "arguments": {
      "a": 15,
      "b": 25
    }
  }
}'

# 6. 测试计算器 - 乘法
test_endpoint "计算器: 6 * 7" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/call",
  "params": {
    "name": "multiply",
    "arguments": {
      "a": 6,
      "b": 7
    }
  }
}'

# 7. 测试计算器 - 除法
test_endpoint "计算器: 100 / 5" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 5,
  "method": "tools/call",
  "params": {
    "name": "divide",
    "arguments": {
      "a": 100,
      "b": 5
    }
  }
}'

# 8. 测试计算器 - 幂运算
test_endpoint "计算器: 2^10" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 6,
  "method": "tools/call",
  "params": {
    "name": "power",
    "arguments": {
      "base": 2,
      "exponent": 10
    }
  }
}'

# 9. 测试计算器 - 平方根
test_endpoint "计算器: √144" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 7,
  "method": "tools/call",
  "params": {
    "name": "sqrt",
    "arguments": {
      "number": 144
    }
  }
}'

# 10. 测试天气 - 查询当前天气
test_endpoint "天气: 北京当前天气" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 8,
  "method": "tools/call",
  "params": {
    "name": "getWeather",
    "arguments": {
      "city": "北京"
    }
  }
}'

# 11. 测试天气 - 查询天气预报
test_endpoint "天气: 上海未来3天预报" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 9,
  "method": "tools/call",
  "params": {
    "name": "getWeatherForecast",
    "arguments": {
      "city": "上海",
      "days": 3
    }
  }
}'

# 12. 测试通用 JSON-RPC 端点
test_endpoint "JSON-RPC: 列出工具" "POST" "$SERVER_URL/mcp/jsonrpc" '{
  "jsonrpc": "2.0",
  "id": 10,
  "method": "tools/list",
  "params": {}
}'

# 13. 测试错误处理 - 不存在的工具
test_endpoint "错误处理: 调用不存在的工具" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 11,
  "method": "tools/call",
  "params": {
    "name": "nonexistent",
    "arguments": {}
  }
}'

# 14. 测试错误处理 - 除以零
test_endpoint "错误处理: 除以零" "POST" "$SERVER_URL/mcp/tools/call" '{
  "jsonrpc": "2.0",
  "id": 12,
  "method": "tools/call",
  "params": {
    "name": "divide",
    "arguments": {
      "a": 10,
      "b": 0
    }
  }
}'

echo ""
echo -e "${GREEN}=========================================="
echo "所有测试完成!"
echo -e "==========================================${NC}"
