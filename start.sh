#!/bin/bash

echo "=================================="
echo "    在线问答平台 - 快速启动脚本    "
echo "=================================="
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

echo "✅ Docker环境检查通过"
echo ""

# 询问是否包含合规检测服务
read -p "是否启动合规检测服务？(y/n) [n]: " include_compliance
include_compliance=${include_compliance:-n}

echo ""
echo "开始启动服务..."
echo ""

if [ "$include_compliance" = "y" ] || [ "$include_compliance" = "Y" ]; then
    echo "启动模式：包含合规检测服务"
    docker-compose --profile with-compliance up -d
else
    echo "启动模式：不包含合规检测服务"
    docker-compose up -d
fi

echo ""
echo "等待服务启动..."
sleep 10

echo ""
echo "=================================="
echo "         服务启动完成！            "
echo "=================================="
echo ""
echo "📱 前端访问地址: http://localhost:3000"
echo "🔧 后端API地址:  http://localhost:8080/api"
echo "💾 MySQL地址:    localhost:3306"
echo ""
echo "👤 默认管理员账号: admin / admin123"
echo "👤 默认测试账号:   testuser / user123"
echo ""
echo "📝 查看日志: docker-compose logs -f"
echo "🛑 停止服务: docker-compose down"
echo ""
echo "=================================="
