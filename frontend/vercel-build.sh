#!/bin/bash

echo "========================================"
echo "Vercel ビルドスクリプト"
echo "========================================"

echo ""
echo "1. 依存関係をインストール..."
npm install

echo ""
echo "2. プロジェクトをビルド..."
npm run build

echo ""
echo "3. ビルド完了！"
echo "Vercelにデプロイできます。"
echo ""
