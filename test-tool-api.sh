#!/bin/bash

echo "=== 测试工具调用 API ==="
echo ""

BASE_URL="http://localhost:8080"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试 1: 获取可用工具列表
echo -e "${YELLOW}测试 1: 获取可用工具列表${NC}"
echo "GET $BASE_URL/api/tool/available"
echo ""
response=$(curl -s "$BASE_URL/api/tool/available")
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

# 测试 2: 简单计算
echo -e "${YELLOW}测试 2: 简单计算 (2 + 3)${NC}"
echo "POST $BASE_URL/api/tool/chat"
echo '{"message": "计算 2 加 3", "enabledTools": ["calculator"]}'
echo ""
response=$(curl -s -X POST "$BASE_URL/api/tool/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "计算 2 加 3", "enabledTools": ["calculator"]}')
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .

    # 检查响应内容
    content=$(echo "$response" | jq -r '.content')
    success=$(echo "$response" | jq -r '.success')

    if [ "$success" = "true" ] && [ -n "$content" ] && [ "$content" != "null" ]; then
        echo -e "${GREEN}✓ 响应内容正常: $content${NC}"
    else
        echo -e "${RED}✗ 响应内容异常${NC}"
    fi
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

# 测试 3: 复杂计算
echo -e "${YELLOW}测试 3: 复杂计算${NC}"
echo "POST $BASE_URL/api/tool/chat"
echo '{"message": "先计算 10 减 5，然后把结果乘以 2", "enabledTools": ["calculator"]}'
echo ""
response=$(curl -s -X POST "$BASE_URL/api/tool/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "先计算 10 减 5，然后把结果乘以 2", "enabledTools": ["calculator"]}')
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .

    content=$(echo "$response" | jq -r '.content')
    if [ -n "$content" ] && [ "$content" != "null" ]; then
        echo -e "${GREEN}✓ 响应内容: $content${NC}"
    fi
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

# 测试 4: 日期时间
echo -e "${YELLOW}测试 4: 日期时间工具${NC}"
echo "POST $BASE_URL/api/tool/chat"
echo '{"message": "现在几点了？", "enabledTools": ["datetime"]}'
echo ""
response=$(curl -s -X POST "$BASE_URL/api/tool/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？", "enabledTools": ["datetime"]}')
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .

    content=$(echo "$response" | jq -r '.content')
    if [ -n "$content" ] && [ "$content" != "null" ]; then
        echo -e "${GREEN}✓ 响应内容: $content${NC}"
    fi
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

# 测试 5: 文本处理
echo -e "${YELLOW}测试 5: 文本处理工具${NC}"
echo "POST $BASE_URL/api/tool/chat"
echo '{"message": "把 \"hello world\" 转换成大写", "enabledTools": ["text"]}'
echo ""
response=$(curl -s -X POST "$BASE_URL/api/tool/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "把 \"hello world\" 转换成大写", "enabledTools": ["text"]}')
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .

    content=$(echo "$response" | jq -r '.content')
    if [ -n "$content" ] && [ "$content" != "null" ]; then
        echo -e "${GREEN}✓ 响应内容: $content${NC}"
    fi
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

# 测试 6: 所有工具（默认）
echo -e "${YELLOW}测试 6: 使用所有工具（默认）${NC}"
echo "POST $BASE_URL/api/tool/chat"
echo '{"message": "计算 100 除以 4"}'
echo ""
response=$(curl -s -X POST "$BASE_URL/api/tool/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "计算 100 除以 4"}')
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 成功${NC}"
    echo "$response" | jq .

    content=$(echo "$response" | jq -r '.content')
    if [ -n "$content" ] && [ "$content" != "null" ]; then
        echo -e "${GREEN}✓ 响应内容: $content${NC}"
    fi
else
    echo -e "${RED}✗ 失败${NC}"
fi
echo ""
echo "---"
echo ""

echo -e "${GREEN}测试完成！${NC}"
