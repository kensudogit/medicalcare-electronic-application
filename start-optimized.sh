#!/bin/bash

# 医療系電子申請システム - 最適化版クイックスタートスクリプト

set -e

echo "🚀 医療系電子申請システム - 最適化版を起動中..."
echo "=================================================="

# 色付きログ関数
log_info() {
    echo -e "\033[32m[INFO]\033[0m $1"
}

log_warn() {
    echo -e "\033[33m[WARN]\033[0m $1"
}

log_error() {
    echo -e "\033[31m[ERROR]\033[0m $1"
}

# 前提条件チェック
check_prerequisites() {
    log_info "前提条件をチェック中..."
    
    # Docker確認
    if ! command -v docker &> /dev/null; then
        log_error "Dockerがインストールされていません。"
        exit 1
    fi
    
    # Docker Compose確認
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Composeがインストールされていません。"
        exit 1
    fi
    
    # 必要なディレクトリ作成
    mkdir -p logs uploads nginx/ssl postgres
    
    log_info "前提条件チェック完了"
}

# 環境設定
setup_environment() {
    log_info "環境設定中..."
    
    # 環境変数ファイル作成
    if [ ! -f .env ]; then
        cat > .env << EOF
# 医療系電子申請システム - 環境変数
COMPOSE_PROJECT_NAME=medicalcare
POSTGRES_DB=medicalcare_db
POSTGRES_USER=medicalcare_user
POSTGRES_PASSWORD=medicalcare_password
JWT_SECRET=your-secret-key-here-make-it-long-and-secure
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
EOF
        log_info ".envファイルを作成しました"
    fi
    
    # パフォーマンス監視セットアップ
    if [ -f scripts/setup-performance-monitoring.sh ]; then
        chmod +x scripts/setup-performance-monitoring.sh
        log_info "パフォーマンス監視をセットアップ中..."
        ./scripts/setup-performance-monitoring.sh || log_warn "パフォーマンス監視セットアップで警告がありました"
    fi
}

# データベース初期化
initialize_database() {
    log_info "データベース初期化中..."
    
    # PostgreSQL設定ファイル作成
    if [ ! -f postgres/postgresql.conf ]; then
        log_warn "PostgreSQL設定ファイルが見つかりません。デフォルト設定を使用します。"
    fi
    
    # スキーマファイル確認
    if [ ! -f src/main/resources/schema.sql ]; then
        log_warn "データベーススキーマファイルが見つかりません。"
        # 基本的なスキーマファイルを作成
        cat > src/main/resources/schema.sql << EOF
-- 基本的なデータベーススキーマ
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS applications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON applications(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
EOF
        log_info "基本的なスキーマファイルを作成しました"
    fi
}

# アプリケーション起動
start_application() {
    log_info "アプリケーションを起動中..."
    
    # 既存のコンテナを停止・削除
    docker-compose down --remove-orphans 2>/dev/null || true
    
    # イメージをビルド
    log_info "Dockerイメージをビルド中..."
    docker-compose build --no-cache
    
    # アプリケーション起動
    log_info "サービスを起動中..."
    docker-compose up -d
    
    # 起動待機
    log_info "サービス起動を待機中..."
    sleep 30
    
    # ヘルスチェック
    check_health
}

# ヘルスチェック
check_health() {
    log_info "ヘルスチェック実行中..."
    
    # PostgreSQL
    if docker exec medicalcare-postgres pg_isready -U medicalcare_user -d medicalcare_db >/dev/null 2>&1; then
        log_info "✅ PostgreSQL: 正常"
    else
        log_warn "⚠️  PostgreSQL: 接続できません"
    fi
    
    # Redis
    if docker exec medicalcare-redis redis-cli ping >/dev/null 2>&1; then
        log_info "✅ Redis: 正常"
    else
        log_warn "⚠️  Redis: 接続できません"
    fi
    
    # Backend
    if curl -s -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_info "✅ Backend: 正常"
    else
        log_warn "⚠️  Backend: 接続できません"
    fi
    
    # Frontend
    if curl -s -f http://localhost:3000/api/health >/dev/null 2>&1; then
        log_info "✅ Frontend: 正常"
    else
        log_warn "⚠️  Frontend: 接続できません"
    fi
    
    # Nginx
    if curl -s -f http://localhost/health >/dev/null 2>&1; then
        log_info "✅ Nginx: 正常"
    else
        log_warn "⚠️  Nginx: 接続できません"
    fi
}

# パフォーマンス監視開始
start_monitoring() {
    log_info "パフォーマンス監視を開始中..."
    
    if [ -f scripts/performance-monitor.sh ]; then
        chmod +x scripts/performance-monitor.sh
        # バックグラウンドで監視を開始
        nohup ./scripts/performance-monitor.sh > logs/monitor.log 2>&1 &
        log_info "パフォーマンス監視を開始しました"
    fi
}

# 情報表示
show_info() {
    echo ""
    echo "🎉 医療系電子申請システム - 最適化版が起動しました！"
    echo "=================================================="
    echo ""
    echo "📱 アクセスURL:"
    echo "   - フロントエンド: http://localhost:3000"
    echo "   - バックエンドAPI: http://localhost:8080"
    echo "   - Nginx (リバースプロキシ): http://localhost"
    echo ""
    echo "🔧 管理・監視:"
    echo "   - Spring Boot Actuator: http://localhost:8080/actuator"
    echo "   - ヘルスチェック: http://localhost:8080/actuator/health"
    echo "   - Prometheus メトリクス: http://localhost:8080/actuator/prometheus"
    echo ""
    echo "📊 パフォーマンス監視:"
    echo "   - 監視ログ: tail -f logs/performance-metrics.log"
    echo "   - アラートログ: tail -f logs/performance-alerts.log"
    echo "   - 手動監視: ./scripts/performance-monitor.sh"
    echo ""
    echo "🐳 Docker管理:"
    echo "   - コンテナ状態: docker-compose ps"
    echo "   - ログ確認: docker-compose logs -f [service-name]"
    echo "   - リソース使用量: docker stats"
    echo ""
    echo "🛑 停止方法:"
    echo "   docker-compose down"
    echo ""
    echo "📚 詳細ドキュメント:"
    echo "   PERFORMANCE_OPTIMIZATION.md"
    echo ""
}

# メイン処理
main() {
    echo "医療系電子申請システム - 最適化版クイックスタート"
    echo "=================================================="
    
    check_prerequisites
    setup_environment
    initialize_database
    start_application
    start_monitoring
    show_info
    
    log_info "起動完了！"
}

# スクリプト実行
main "$@" 