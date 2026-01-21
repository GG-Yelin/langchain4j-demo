#!/bin/bash

# RAG 功能测试脚本

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "RAG 功能测试脚本"
echo "=========================================="
echo ""

# 测试 1: 加载文档
echo "1. 加载测试文档..."
echo ""

curl -X POST "${BASE_URL}/api/rag/load" \
  -H "Content-Type: application/json" \
  -d "{
    \"path\": \"$(pwd)/test-documents\"
  }" | jq '.'

echo ""
echo ""

# 等待几秒让文档处理完成
echo "等待文档处理..."
sleep 3
echo ""

# 测试 2: RAG 查询（不包含来源）
echo "2. 测试 RAG 查询（不包含来源）..."
echo ""

curl -X POST "${BASE_URL}/api/rag/query" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是 LangChain4j？",
    "topK": 3,
    "minScore": 0.5,
    "includeSource": false
  }' | jq '.'

echo ""
echo ""

# 测试 3: RAG 查询（包含来源）
echo "3. 测试 RAG 查询（包含来源）..."
echo ""

curl -X POST "${BASE_URL}/api/rag/query" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "LangChain4j 有哪些主要特性？",
    "topK": 5,
    "minScore": 0.5,
    "includeSource": true
  }' | jq '.'

echo ""
echo ""

# 测试 4: RAG 查询（关于 RAG 工作流程）
echo "4. 测试 RAG 查询（关于 RAG 工作流程）..."
echo ""

curl -X POST "${BASE_URL}/api/rag/query" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "RAG 的工作流程是什么？",
    "topK": 3,
    "minScore": 0.5,
    "includeSource": true
  }' | jq '.'

echo ""
echo ""
echo "=========================================="
echo "测试完成！"
echo "=========================================="
