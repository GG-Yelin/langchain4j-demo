#!/bin/bash

# 启动 MCP Server

cd "$(dirname "$0")/mcp-server" || exit 1

echo "=========================================="
echo "启动 MCP Server (端口 8081)"
echo "=========================================="

mvn spring-boot:run
