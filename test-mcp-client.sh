#!/bin/bash

# MCP Client 测试脚本
# 测试 demo-core 通过 MCP 协议远程调用 mcp-server 的工具

echo "=========================================="
echo "MCP Client 测试脚本"
echo "测试 LangChain4j MCP Client 远程调用"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# demo-core 服务地址
DEMO_CORE_URL="http://localhost:8080"

# 检查 MCP Server 是否运行
check_mcp_server() {
    echo "1. 检查 MCP Server 是否运行 (端口 8081)..."
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "   ${GREEN}✅ MCP Server 正在运行${NC}"
        return 0
    else
        echo -e "   ${RED}❌ MCP Server 未运行${NC}"
        echo "   请先启动 MCP Server:"
        echo "   cd mcp-server && mvn spring-boot:run"
        echo ""
        return 1
    fi
}

# 检查 demo-core 是否运行
check_demo_core() {
    echo ""
    echo "2. 检查 demo-core 是否运行 (端口 8080)..."
    if curl -s ${DEMO_CORE_URL}/actuator/health > /dev/null 2>&1; then
        echo -e "   ${GREEN}✅ demo-core 正在运行${NC}"
        return 0
    else
        echo -e "   ${RED}❌ demo-core 未运行${NC}"
        echo "   请先启动 demo-core:"
        echo "   cd langchain4j-demo-core && mvn spring-boot:run"
        echo ""
        return 1
    fi
}

# 测试获取工具列表
test_list_tools() {
    echo ""
    echo "=========================================="
    echo "3. 测试获取 MCP 工具列表"
    echo "=========================================="

    response=$(curl -s -X GET ${DEMO_CORE_URL}/api/mcp/tools)

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 请求成功${NC}"
        echo ""
        echo "可用的 MCP 工具:"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ 请求失败${NC}"
    fi
}

# 测试调用计算器工具 - 加法
test_calculator_add() {
    echo ""
    echo "=========================================="
    echo "4. 测试直接调用 MCP 工具: calculator_add"
    echo "=========================================="
    echo "计算: 15 + 25"

    response=$(curl -s -X POST "${DEMO_CORE_URL}/api/mcp/invoke?toolName=calculator_add" \
        -H "Content-Type: application/json" \
        -d '{
            "a": 15,
            "b": 25
        }')

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 调用成功${NC}"
        echo ""
        echo "返回结果: $response"
    else
        echo -e "${RED}❌ 调用失败${NC}"
    fi
}

# 测试调用计算器工具 - 乘法
test_calculator_multiply() {
    echo ""
    echo "=========================================="
    echo "5. 测试直接调用 MCP 工具: calculator_multiply"
    echo "=========================================="
    echo "计算: 6 * 7"

    response=$(curl -s -X POST "${DEMO_CORE_URL}/api/mcp/invoke?toolName=calculator_multiply" \
        -H "Content-Type: application/json" \
        -d '{
            "a": 6,
            "b": 7
        }')

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 调用成功${NC}"
        echo ""
        echo "返回结果: $response"
    else
        echo -e "${RED}❌ 调用失败${NC}"
    fi
}

# 测试调用天气工具
test_weather() {
    echo ""
    echo "=========================================="
    echo "6. 测试直接调用 MCP 工具: weather_get_current"
    echo "=========================================="
    echo "查询城市: 北京"

    response=$(curl -s -X POST "${DEMO_CORE_URL}/api/mcp/invoke?toolName=weather_get_current" \
        -H "Content-Type: application/json" \
        -d '{
            "city": "北京"
        }')

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 调用成功${NC}"
        echo ""
        echo "返回结果: $response"
    else
        echo -e "${RED}❌ 调用失败${NC}"
    fi
}

# 测试 AI 聊天（自动调用工具）
test_chat_with_tools() {
    echo ""
    echo "=========================================="
    echo "7. 测试 AI 聊天（自动调用 MCP 工具）"
    echo "=========================================="
    echo "问题: 计算 100 加 200 等于多少"

    response=$(curl -s -X POST ${DEMO_CORE_URL}/api/mcp/chat \
        -H "Content-Type: application/json" \
        -d '{
            "message": "帮我计算 100 加 200 等于多少"
        }')

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 请求成功${NC}"
        echo ""
        echo "AI 回答:"
        echo "$response" | jq -r '.content' 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ 请求失败${NC}"
    fi
}

# 主测试流程
main() {
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}❌ 错误: 需要安装 curl${NC}"
        exit 1
    fi

    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}⚠️  警告: 未安装 jq，响应将以原始格式显示${NC}"
    fi

    # 检查服务状态
    check_mcp_server
    mcp_server_status=$?

    check_demo_core
    demo_core_status=$?

    if [ $mcp_server_status -ne 0 ] || [ $demo_core_status -ne 0 ]; then
        echo ""
        echo -e "${RED}请先启动所需的服务，然后重新运行此脚本${NC}"
        exit 1
    fi

    # 运行测试
    test_list_tools
    test_calculator_add
    test_calculator_multiply
    test_weather
    test_chat_with_tools

    echo ""
    echo "=========================================="
    echo -e "${GREEN}测试完成!${NC}"
    echo "=========================================="
    echo ""
    echo "测试说明:"
    echo "1. 工具列表: 通过 MCP 协议获取远程 MCP Server 提供的所有工具"
    echo "2. 直接调用: 通过 MCP 协议直接调用指定工具"
    echo "3. AI 聊天: AI 自动通过 MCP 协议调用工具并回答问题"
    echo ""
    echo "架构:"
    echo "  [demo-core:8080] --MCP Protocol--> [mcp-server:8081]"
    echo "       (Client)                         (Server)"
    echo ""
}

main
