@echo off
echo ========================================
echo Vercel ビルドスクリプト
echo ========================================

echo.
echo 1. 依存関係をインストール...
call npm install

echo.
echo 2. プロジェクトをビルド...
call npm run build

echo.
echo 3. ビルド完了！
echo Vercelにデプロイできます。
echo.
pause
