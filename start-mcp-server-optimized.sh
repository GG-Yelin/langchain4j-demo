#!/bin/bash

# MCP Server 启动脚本（优化版）
# 基于 Spring AI MCP Server WebMVC Starter

echo "=========================================="
echo "启动 MCP Server (Spring AI 官方实现)"
echo "=========================================="
echo ""

# 检查 Java 版本
check_java() {
    if ! command -v java &> /dev/null; then
        echo "❌ 错误: 未安装 Java"
        echo "   请安装 Java 17 或更高版本"
        exit 1
    fi

    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        echo "❌ 错误: Java 版本过低"
        echo "   当前版本: $java_version"
        echo "   需要版本: 17+"
        exit 1
    fi

    echo "✅ Java 版本检查通过: Java $java_version"
}

# 检查 Maven
check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo "❌ 错误: 未安装 Maven"
        exit 1
    fi

    echo "✅ Maven 检查通过"
}

# 进入 mcp-server 目录
cd "$(dirname "$0")/mcp-server" || exit 1

echo ""
echo "当前目录: $(pwd)"
echo ""

# 检查依赖
echo "检查环境..."
check_java
check_maven

echo ""
echo "=========================================="
echo "正在启动 MCP Server..."
echo "=========================================="
echo ""
echo "服务信息:"
echo "  - 端口: 8081"
echo "  - 协议: MCP (Model Context Protocol)"
echo "  - 传输: WebMVC (HTTP/SSE)"
echo "  - 模式: STATELESS"
echo ""
echo "可用工具:"
echo "  1. 计算器工具 (CalculatorTool)"
echo "     - calculator_add, calculator_subtract"
echo "     - calculator_multiply, calculator_divide"
echo "     - calculator_power, calculator_sqrt"
echo ""
echo "  2. 天气工具 (WeatherTool)"
echo "     - weather_get_current"
echo "     - weather_get_forecast"
echo ""
echo "=========================================="
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

# 启动服务
mvn spring-boot:run
