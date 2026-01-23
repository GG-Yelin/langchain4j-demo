#!/bin/bash

# MCP Server 测试脚本
# 用于测试基于 Spring AI 实现的 MCP Server

echo "=========================================="
echo "MCP Server 测试脚本"
echo "=========================================="
echo ""

# 检查服务是否运行
check_server() {
    echo "1. 检查 MCP Server 是否运行..."
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo "   ✅ MCP Server 正在运行"
        return 0
    else
        echo "   ❌ MCP Server 未运行"
        echo "   请先启动 MCP Server: cd mcp-server && mvn spring-boot:run"
        return 1
    fi
}

# 测试工具列表
test_tools_list() {
    echo ""
    echo "2. 测试获取工具列表..."

    # MCP 协议的 tools/list 请求
    response=$(curl -s -X POST http://localhost:8081/mcp \
        -H "Content-Type: application/json" \
        -d '{
            "jsonrpc": "2.0",
            "id": 1,
            "method": "tools/list"
        }')

    if [ $? -eq 0 ]; then
        echo "   ✅ 工具列表请求成功"
        echo ""
        echo "   响应内容："
        echo "$response" | jq '.' || echo "$response"
    else
        echo "   ❌ 工具列表请求失败"
    fi
}

# 测试计算器工具
test_calculator() {
    echo ""
    echo "3. 测试计算器工具 (calculator_add)..."

    response=$(curl -s -X POST http://localhost:8081/mcp \
        -H "Content-Type: application/json" \
        -d '{
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/call",
            "params": {
                "name": "calculator_add",
                "arguments": {
                    "a": 10,
                    "b": 20
                }
            }
        }')

    if [ $? -eq 0 ]; then
        echo "   ✅ 计算器测试成功"
        echo ""
        echo "   响应内容："
        echo "$response" | jq '.' || echo "$response"
    else
        echo "   ❌ 计算器测试失败"
    fi
}

# 测试天气工具
test_weather() {
    echo ""
    echo "4. 测试天气工具 (weather_get_current)..."

    response=$(curl -s -X POST http://localhost:8081/mcp \
        -H "Content-Type: application/json" \
        -d '{
            "jsonrpc": "2.0",
            "id": 3,
            "method": "tools/call",
            "params": {
                "name": "weather_get_current",
                "arguments": {
                    "city": "北京"
                }
            }
        }')

    if [ $? -eq 0 ]; then
        echo "   ✅ 天气查询测试成功"
        echo ""
        echo "   响应内容："
        echo "$response" | jq '.' || echo "$response"
    else
        echo "   ❌ 天气查询测试失败"
    fi
}

# 主测试流程
main() {
    if ! command -v curl &> /dev/null; then
        echo "❌ 错误: 需要安装 curl"
        exit 1
    fi

    if ! command -v jq &> /dev/null; then
        echo "⚠️  警告: 未安装 jq，响应将以原始格式显示"
    fi

    check_server
    if [ $? -eq 0 ]; then
        test_tools_list
        test_calculator
        test_weather

        echo ""
        echo "=========================================="
        echo "测试完成!"
        echo "=========================================="
    fi
}

main
