#!/bin/bash

# 医療系電子申請システム - パフォーマンス監視スクリプト

# 設定
LOG_DIR="/app/logs"
METRICS_FILE="$LOG_DIR/performance-metrics.log"
ALERT_FILE="$LOG_DIR/performance-alerts.log"
DOCKER_COMPOSE_FILE="docker-compose.yml"

# ログディレクトリの作成
mkdir -p "$LOG_DIR"

# ログ関数
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$METRICS_FILE"
}

log_alert() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - ALERT: $1" | tee -a "$ALERT_FILE"
}

# システムリソース監視
monitor_system_resources() {
    log_message "=== システムリソース監視 ==="
    
    # CPU使用率
    cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    log_message "CPU使用率: ${cpu_usage}%"
    
    # メモリ使用率
    memory_info=$(free -m | grep Mem)
    total_memory=$(echo $memory_info | awk '{print $2}')
    used_memory=$(echo $memory_info | awk '{print $3}')
    memory_usage=$(echo "scale=2; $used_memory * 100 / $total_memory" | bc)
    log_message "メモリ使用率: ${memory_usage}% (${used_memory}MB/${total_memory}MB)"
    
    # ディスク使用率
    disk_usage=$(df -h / | awk 'NR==2 {print $5}' | cut -d'%' -f1)
    log_message "ディスク使用率: ${disk_usage}%"
    
    # アラートチェック
    if (( $(echo "$cpu_usage > 80" | bc -l) )); then
        log_alert "CPU使用率が高い: ${cpu_usage}%"
    fi
    
    if (( $(echo "$memory_usage > 85" | bc -l) )); then
        log_alert "メモリ使用率が高い: ${memory_usage}%"
    fi
    
    if [ "$disk_usage" -gt 90 ]; then
        log_alert "ディスク使用率が高い: ${disk_usage}%"
    fi
}

# Dockerコンテナ監視
monitor_docker_containers() {
    log_message "=== Dockerコンテナ監視 ==="
    
    # コンテナの状態確認
    containers=$(docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}")
    log_message "実行中コンテナ:"
    echo "$containers" | while IFS= read -r line; do
        log_message "  $line"
    done
    
    # コンテナのリソース使用量
    log_message "コンテナリソース使用量:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | while IFS= read -r line; do
        log_message "  $line"
    done
    
    # 停止したコンテナの確認
    stopped_containers=$(docker ps -a --filter "status=exited" --format "{{.Names}}")
    if [ -n "$stopped_containers" ]; then
        log_alert "停止したコンテナ: $stopped_containers"
    fi
}

# データベース監視
monitor_database() {
    log_message "=== データベース監視 ==="
    
    # PostgreSQL接続数
    connection_count=$(docker exec medicalcare-postgres psql -U medicalcare_user -d medicalcare_db -t -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null | tr -d ' ')
    log_message "PostgreSQL接続数: ${connection_count:-N/A}"
    
    # アクティブなクエリ数
    active_queries=$(docker exec medicalcare-postgres psql -U medicalcare_user -d medicalcare_db -t -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';" 2>/dev/null | tr -d ' ')
    log_message "アクティブクエリ数: ${active_queries:-N/A}"
    
    # データベースサイズ
    db_size=$(docker exec medicalcare-postgres psql -U medicalcare_user -d medicalcare_db -t -c "SELECT pg_size_pretty(pg_database_size('medicalcare_db'));" 2>/dev/null | tr -d ' ')
    log_message "データベースサイズ: ${db_size:-N/A}"
    
    # スロークエリの確認
    slow_queries=$(docker exec medicalcare-postgres psql -U medicalcare_user -d medicalcare_db -t -c "SELECT query FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 5;" 2>/dev/null)
    if [ -n "$slow_queries" ]; then
        log_message "スロークエリ (上位5件):"
        echo "$slow_queries" | while IFS= read -r line; do
            log_message "  $line"
        done
    fi
}

# Redis監視
monitor_redis() {
    log_message "=== Redis監視 ==="
    
    # Redis接続数
    redis_connections=$(docker exec medicalcare-redis redis-cli info clients | grep "connected_clients:" | cut -d: -f2 | tr -d '\r')
    log_message "Redis接続数: ${redis_connections:-N/A}"
    
    # Redisメモリ使用量
    redis_memory=$(docker exec medicalcare-redis redis-cli info memory | grep "used_memory_human:" | cut -d: -f2 | tr -d '\r')
    log_message "Redisメモリ使用量: ${redis_memory:-N/A}"
    
    # Redisキー数
    redis_keys=$(docker exec medicalcare-redis redis-cli dbsize)
    log_message "Redisキー数: ${redis_keys:-N/A}"
    
    # Redisヒット率
    redis_hits=$(docker exec medicalcare-redis redis-cli info stats | grep "keyspace_hits:" | cut -d: -f2 | tr -d '\r')
    redis_misses=$(docker exec medicalcare-redis redis-cli info stats | grep "keyspace_misses:" | cut -d: -f2 | tr -d '\r')
    
    if [ -n "$redis_hits" ] && [ -n "$redis_misses" ] && [ "$redis_hits" -gt 0 ]; then
        hit_rate=$(echo "scale=2; $redis_hits * 100 / ($redis_hits + $redis_misses)" | bc)
        log_message "Redisヒット率: ${hit_rate}%"
    fi
}

# アプリケーション監視
monitor_application() {
    log_message "=== アプリケーション監視 ==="
    
    # バックエンドヘルスチェック
    backend_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    log_message "バックエンドヘルス: $backend_health"
    
    if [ "$backend_health" != "200" ]; then
        log_alert "バックエンドが異常: HTTP $backend_health"
    fi
    
    # フロントエンドヘルスチェック
    frontend_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/health)
    log_message "フロントエンドヘルス: $frontend_health"
    
    if [ "$frontend_health" != "200" ]; then
        log_alert "フロントエンドが異常: HTTP $frontend_health"
    fi
    
    # Nginxヘルスチェック
    nginx_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/health)
    log_message "Nginxヘルス: $nginx_health"
    
    if [ "$nginx_health" != "200" ]; then
        log_alert "Nginxが異常: HTTP $nginx_health"
    fi
    
    # レスポンス時間測定
    backend_response_time=$(curl -s -w "%{time_total}" -o /dev/null http://localhost:8080/actuator/health)
    log_message "バックエンドレスポンス時間: ${backend_response_time}s"
    
    if (( $(echo "$backend_response_time > 2.0" | bc -l) )); then
        log_alert "バックエンドレスポンス時間が遅い: ${backend_response_time}s"
    fi
}

# ネットワーク監視
monitor_network() {
    log_message "=== ネットワーク監視 ==="
    
    # ポート監視
    ports=("80" "443" "3000" "8080" "5432" "6379")
    for port in "${ports[@]}"; do
        if netstat -tuln | grep -q ":$port "; then
            log_message "ポート $port: 開放"
        else
            log_alert "ポート $port: 閉鎖"
        fi
    done
    
    # ネットワーク接続数
    tcp_connections=$(netstat -an | grep ESTABLISHED | wc -l)
    log_message "TCP接続数: $tcp_connections"
    
    if [ "$tcp_connections" -gt 1000 ]; then
        log_alert "TCP接続数が多い: $tcp_connections"
    fi
}

# ログ監視
monitor_logs() {
    log_message "=== ログ監視 ==="
    
    # エラーログの確認
    error_count=$(docker logs medicalcare-backend --since 1h 2>&1 | grep -i error | wc -l)
    log_message "バックエンドエラー数 (1時間): $error_count"
    
    if [ "$error_count" -gt 10 ]; then
        log_alert "バックエンドエラーが多い: $error_count"
    fi
    
    # 警告ログの確認
    warning_count=$(docker logs medicalcare-backend --since 1h 2>&1 | grep -i warn | wc -l)
    log_message "バックエンド警告数 (1時間): $warning_count"
}

# メイン処理
main() {
    log_message "パフォーマンス監視開始"
    
    monitor_system_resources
    monitor_docker_containers
    monitor_database
    monitor_redis
    monitor_application
    monitor_network
    monitor_logs
    
    log_message "パフォーマンス監視完了"
    echo ""
}

# スクリプト実行
main "$@" 