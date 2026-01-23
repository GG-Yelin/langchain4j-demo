#!/bin/bash

# 启动 Demo Core

cd "$(dirname "$0")/langchain4j-demo-core" || exit 1

echo "=========================================="
echo "启动 LangChain4j Demo Core (端口 8080)"
echo "=========================================="

mvn spring-boot:run
