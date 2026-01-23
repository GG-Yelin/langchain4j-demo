#!/bin/bash

# demo-core 启动脚本（已修复配置问题）

echo "=========================================="
echo "启动 demo-core (已修复 MCP 配置)"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 进入 demo-core 目录
cd "$(dirname "$0")/langchain4j-demo-core" || exit 1

echo "当前目录: $(pwd)"
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ 错误: 未安装 Java${NC}"
    exit 1
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo -e "${GREEN}✅ Java 版本: $java_version${NC}"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ 错误: 未安装 Maven${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Maven 已安装${NC}"
echo ""

# 检查 mcp-server JAR
MCP_SERVER_JAR="../mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar"
if [ ! -f "$MCP_SERVER_JAR" ]; then
    echo -e "${RED}❌ 错误: mcp-server JAR 不存在${NC}"
    echo "   路径: $MCP_SERVER_JAR"
    echo ""
    echo "   请先构建 mcp-server:"
    echo "   cd mcp-server && mvn clean package -DskipTests"
    exit 1
fi

echo -e "${GREEN}✅ mcp-server JAR 已就绪${NC}"
echo ""

# 读取配置
echo "读取配置..."
TRANSPORT_TYPE=$(grep -A 3 "transport:" src/main/resources/application.yml | grep "type:" | awk '{print $2}' | tr -d ' ')
echo "  MCP 传输模式: ${TRANSPORT_TYPE}"
echo ""

if [ "$TRANSPORT_TYPE" = "stdio" ]; then
    echo -e "${GREEN}✅ 使用 Stdio 模式${NC}"
    echo "   demo-core 会自动启动 mcp-server JAR"
    echo "   只需启动一个服务即可"
elif [ "$TRANSPORT_TYPE" = "http" ]; then
    echo -e "${YELLOW}⚠️  使用 HTTP 模式${NC}"
    echo "   需要先手动启动 mcp-server (端口 8081)"
    echo ""

    # 检查 mcp-server 是否运行
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ mcp-server 已在运行${NC}"
    else
        echo -e "${RED}❌ mcp-server 未运行${NC}"
        echo ""
        echo "请在另一个终端先启动 mcp-server:"
        echo "  cd mcp-server && mvn spring-boot:run"
        echo ""
        echo "或者修改配置使用 stdio 模式:"
        echo "  编辑 src/main/resources/application.yml"
        echo "  将 mcp.transport.type 改为 stdio"
        exit 1
    fi
fi

echo ""
echo "=========================================="
echo "正在启动 demo-core..."
echo "=========================================="
echo ""
echo "服务信息:"
echo "  - 端口: 8080"
echo "  - API 端点:"
echo "    GET  /api/mcp/tools      - 获取 MCP 工具列表"
echo "    POST /api/mcp/invoke     - 直接调用 MCP 工具"
echo "    POST /api/mcp/chat       - AI 聊天（自动调用工具）"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

# 启动服务
mvn spring-boot:run
