@echo off
echo ========================================
echo Medical Care Electronic Application
echo Vercel デプロイスクリプト
echo ========================================

echo.
echo 1. フロントエンドディレクトリに移動...
cd frontend

echo.
echo 2. 依存関係をインストール...
call npm install

echo.
echo 3. プロジェクトをビルド...
call npm run build

echo.
echo 4. Vercelにデプロイ...
echo 注意: 初回はログインが必要です
call vercel --prod

echo.
echo デプロイが完了しました！
echo VercelダッシュボードでURLを確認してください。
echo.
pause
