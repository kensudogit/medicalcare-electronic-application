@echo off
setlocal enabledelayedexpansion

REM 医療系電子申請システム - 最適化版クイックスタートスクリプト (Windows版)

echo 🚀 医療系電子申請システム - 最適化版を起動中...
echo ==================================================

REM 前提条件チェック
echo [INFO] 前提条件をチェック中...

REM Docker確認
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Dockerがインストールされていません。
    pause
    exit /b 1
)

REM Docker Compose確認
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Composeがインストールされていません。
    pause
    exit /b 1
)

REM 必要なディレクトリ作成
if not exist "logs" mkdir logs
if not exist "uploads" mkdir uploads
if not exist "nginx\ssl" mkdir nginx\ssl
if not exist "postgres" mkdir postgres

echo [INFO] 前提条件チェック完了

REM 環境設定
echo [INFO] 環境設定中...

REM 環境変数ファイル作成
if not exist ".env" (
    (
        echo # 医療系電子申請システム - 環境変数
        echo COMPOSE_PROJECT_NAME=medicalcare
        echo POSTGRES_DB=medicalcare_db
        echo POSTGRES_USER=medicalcare_user
        echo POSTGRES_PASSWORD=medicalcare_password
        echo JWT_SECRET=your-secret-key-here-make-it-long-and-secure
        echo NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
    ) > .env
    echo [INFO] .envファイルを作成しました
)

REM データベース初期化
echo [INFO] データベース初期化中...

REM スキーマファイル確認
if not exist "src\main\resources\schema.sql" (
    echo [WARN] データベーススキーマファイルが見つかりません。
    (
        echo -- 基本的なデータベーススキーマ
        echo CREATE TABLE IF NOT EXISTS users ^(
        echo     id SERIAL PRIMARY KEY,
        echo     username VARCHAR^(50^) UNIQUE NOT NULL,
        echo     email VARCHAR^(100^) UNIQUE NOT NULL,
        echo     password_hash VARCHAR^(255^) NOT NULL,
        echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        echo ^);
        echo.
        echo CREATE TABLE IF NOT EXISTS applications ^(
        echo     id SERIAL PRIMARY KEY,
        echo     user_id INTEGER REFERENCES users^(id^),
        echo     title VARCHAR^(200^) NOT NULL,
        echo     content TEXT,
        echo     status VARCHAR^(20^) DEFAULT 'pending',
        echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        echo ^);
        echo.
        echo -- インデックス作成
        echo CREATE INDEX IF NOT EXISTS idx_users_email ON users^(email^);
        echo CREATE INDEX IF NOT EXISTS idx_applications_user_id ON applications^(user_id^);
        echo CREATE INDEX IF NOT EXISTS idx_applications_status ON applications^(status^);
    ) > src\main\resources\schema.sql
    echo [INFO] 基本的なスキーマファイルを作成しました
)

REM アプリケーション起動
echo [INFO] アプリケーションを起動中...

REM 既存のコンテナを停止・削除
echo [INFO] 既存のコンテナを停止中...
docker-compose down --remove-orphans >nul 2>&1

REM イメージをビルド
echo [INFO] Dockerイメージをビルド中...
echo [INFO] この処理には数分かかる場合があります...
docker-compose build --no-cache
if errorlevel 1 (
    echo [ERROR] Dockerイメージのビルドに失敗しました。
    echo [INFO] ログを確認してください: docker-compose logs
    pause
    exit /b 1
)

REM アプリケーション起動
echo [INFO] サービスを起動中...
docker-compose up -d
if errorlevel 1 (
    echo [ERROR] サービスの起動に失敗しました。
    echo [INFO] ログを確認してください: docker-compose logs
    pause
    exit /b 1
)

REM 起動待機
echo [INFO] サービス起動を待機中...
timeout /t 30 /nobreak >nul

REM ヘルスチェック
echo [INFO] ヘルスチェック実行中...

REM PostgreSQL
docker exec medicalcare-postgres pg_isready -U medicalcare_user -d medicalcare_db >nul 2>&1
if errorlevel 1 (
    echo ⚠️  PostgreSQL: 接続できません
) else (
    echo ✅ PostgreSQL: 正常
)

REM Redis
docker exec medicalcare-redis redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Redis: 接続できません
) else (
    echo ✅ Redis: 正常
)

REM Backend
curl -s -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Backend: 接続できません
) else (
    echo ✅ Backend: 正常
)

REM Frontend
curl -s -f http://localhost:3000/api/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Frontend: 接続できません
) else (
    echo ✅ Frontend: 正常
)

REM Nginx
curl -s -f http://localhost/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Nginx: 接続できません
) else (
    echo ✅ Nginx: 正常
)

REM 情報表示
echo.
echo 🎉 医療系電子申請システム - 最適化版が起動しました！
echo ==================================================
echo.
echo 📱 アクセスURL:
echo    - フロントエンド: http://localhost:3000
echo    - バックエンドAPI: http://localhost:8080
echo    - Nginx ^(リバースプロキシ^): http://localhost
echo.
echo 🔧 管理・監視:
echo    - Spring Boot Actuator: http://localhost:8080/actuator
echo    - ヘルスチェック: http://localhost:8080/actuator/health
echo    - Prometheus メトリクス: http://localhost:8080/actuator/prometheus
echo.
echo 📊 パフォーマンス監視:
echo    - 監視ログ: type logs\performance-metrics.log
echo    - アラートログ: type logs\performance-alerts.log
echo    - 手動監視: scripts\performance-monitor.sh
echo.
echo 🐳 Docker管理:
echo    - コンテナ状態: docker-compose ps
echo    - ログ確認: docker-compose logs -f [service-name]
echo    - リソース使用量: docker stats
echo.
echo 🛑 停止方法:
echo    docker-compose down
echo.
echo 📚 詳細ドキュメント:
echo    PERFORMANCE_OPTIMIZATION.md
echo.

echo [INFO] 起動完了！
echo [INFO] 問題がある場合は、docker-compose logs でログを確認してください。
pause 