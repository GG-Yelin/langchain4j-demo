#!/bin/bash

# 远程 MCP 测试脚本

echo "============================================"
echo "远程 MCP Server 测试脚本"
echo "============================================"

MCP_SERVER_URL="http://localhost:8081"
DEMO_CORE_URL="http://localhost:8080"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 等待服务启动
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=0

    echo -e "\n${YELLOW}等待 $name 启动...${NC}"
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $name 已启动${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 1
    done
    echo -e "\n${RED}✗ $name 启动超时${NC}"
    return 1
}

# 测试 MCP Server 健康检查
test_mcp_health() {
    echo -e "\n${YELLOW}[1] 测试 MCP Server 健康检查${NC}"
    response=$(curl -s "${MCP_SERVER_URL}/mcp/health")
    echo "响应: $response"
    if echo "$response" | grep -q '"status":"UP"'; then
        echo -e "${GREEN}✓ MCP Server 健康检查通过${NC}"
        return 0
    else
        echo -e "${RED}✗ MCP Server 健康检查失败${NC}"
        return 1
    fi
}

# 测试列出工具
test_list_tools() {
    echo -e "\n${YELLOW}[2] 测试列出 MCP 工具${NC}"
    response=$(curl -s "${MCP_SERVER_URL}/mcp/tools")
    echo "响应: $response"
    if echo "$response" | grep -q '"name"'; then
        echo -e "${GREEN}✓ 成功获取工具列表${NC}"
        return 0
    else
        echo -e "${RED}✗ 获取工具列表失败${NC}"
        return 1
    fi
}

# 测试直接调用工具 - 计算器
test_calculator() {
    echo -e "\n${YELLOW}[3] 测试直接调用计算器工具 (12 + 34)${NC}"
    response=$(curl -s -X POST "${MCP_SERVER_URL}/mcp/execute" \
        -H "Content-Type: application/json" \
        -d '{"toolName":"add","arguments":{"a":12,"b":34}}')
    echo "响应: $response"
    if echo "$response" | grep -q '"success":true' && echo "$response" | grep -q '46'; then
        echo -e "${GREEN}✓ 计算器工具调用成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 计算器工具调用失败${NC}"
        return 1
    fi
}

# 测试直接调用工具 - 天气
test_weather() {
    echo -e "\n${YELLOW}[4] 测试直接调用天气工具 (北京)${NC}"
    response=$(curl -s -X POST "${MCP_SERVER_URL}/mcp/execute" \
        -H "Content-Type: application/json" \
        -d '{"toolName":"getWeather","arguments":{"city":"北京"}}')
    echo "响应: $response"
    if echo "$response" | grep -q '"success":true' && echo "$response" | grep -q '北京'; then
        echo -e "${GREEN}✓ 天气工具调用成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 天气工具调用失败${NC}"
        return 1
    fi
}

# 测试通过 Demo Core 调用 MCP
test_demo_core_mcp() {
    echo -e "\n${YELLOW}[5] 测试通过 Demo Core 调用 MCP (AI + 远程工具)${NC}"
    echo "问题: 北京今天天气怎么样？"
    response=$(curl -s -X POST "${DEMO_CORE_URL}/api/mcp/chat" \
        -H "Content-Type: application/json" \
        -d '{"message":"北京今天天气怎么样？"}')
    echo "响应: $response"
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ Demo Core MCP 调用成功${NC}"
        return 0
    else
        echo -e "${RED}✗ Demo Core MCP 调用失败${NC}"
        return 1
    fi
}

# 测试通过 Demo Core 调用计算器
test_demo_core_calculator() {
    echo -e "\n${YELLOW}[6] 测试通过 Demo Core 调用 MCP 计算器${NC}"
    echo "问题: 帮我计算 123 + 456"
    response=$(curl -s -X POST "${DEMO_CORE_URL}/api/mcp/chat" \
        -H "Content-Type: application/json" \
        -d '{"message":"帮我计算 123 + 456"}')
    echo "响应: $response"
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ Demo Core 计算器调用成功${NC}"
        return 0
    else
        echo -e "${RED}✗ Demo Core 计算器调用失败${NC}"
        return 1
    fi
}

# 主测试流程
main() {
    # 检查服务是否已启动
    echo -e "\n${YELLOW}提示: 请确保已启动以下服务:${NC}"
    echo "1. MCP Server (端口 8081): cd mcp-server && mvn spring-boot:run"
    echo "2. Demo Core (端口 8080): cd langchain4j-demo-core && mvn spring-boot:run"
    echo ""
    read -p "按回车键开始测试..."

    # 等待服务启动
    wait_for_service "${MCP_SERVER_URL}/mcp/health" "MCP Server" || exit 1
    wait_for_service "${DEMO_CORE_URL}/actuator/health" "Demo Core" || exit 1

    # 运行测试
    passed=0
    total=0

    tests=(
        test_mcp_health
        test_list_tools
        test_calculator
        test_weather
        test_demo_core_mcp
        test_demo_core_calculator
    )

    for test in "${tests[@]}"; do
        total=$((total + 1))
        if $test; then
            passed=$((passed + 1))
        fi
        sleep 1
    done

    # 显示测试结果
    echo -e "\n============================================"
    echo -e "${YELLOW}测试结果总结${NC}"
    echo -e "============================================"
    echo -e "通过: ${GREEN}$passed${NC} / $total"

    if [ $passed -eq $total ]; then
        echo -e "${GREEN}✓ 所有测试通过！${NC}"
        exit 0
    else
        echo -e "${RED}✗ 部分测试失败${NC}"
        exit 1
    fi
}

# 运行主函数
main
