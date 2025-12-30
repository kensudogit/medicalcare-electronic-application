#!/bin/bash

# 医療系電子申請システム - パフォーマンス監視セットアップスクリプト

echo "=== パフォーマンス監視セットアップ開始 ==="

# スクリプトディレクトリの作成
mkdir -p scripts
mkdir -p logs

# 監視スクリプトに実行権限を付与
chmod +x scripts/performance-monitor.sh

# 必要なツールのインストール確認
echo "必要なツールの確認中..."

# bcコマンドの確認
if ! command -v bc &> /dev/null; then
    echo "bcコマンドをインストール中..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y bc
    elif command -v yum &> /dev/null; then
        sudo yum install -y bc
    elif command -v apk &> /dev/null; then
        sudo apk add bc
    else
        echo "警告: bcコマンドが見つかりません。手動でインストールしてください。"
    fi
fi

# curlコマンドの確認
if ! command -v curl &> /dev/null; then
    echo "curlコマンドをインストール中..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get install -y curl
    elif command -v yum &> /dev/null; then
        sudo yum install -y curl
    elif command -v apk &> /dev/null; then
        sudo apk add curl
    else
        echo "警告: curlコマンドが見つかりません。手動でインストールしてください。"
    fi
fi

# netstatコマンドの確認
if ! command -v netstat &> /dev/null; then
    echo "netstatコマンドをインストール中..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get install -y net-tools
    elif command -v yum &> /dev/null; then
        sudo yum install -y net-tools
    elif command -v apk &> /dev/null; then
        sudo apk add net-tools
    else
        echo "警告: netstatコマンドが見つかりません。手動でインストールしてください。"
    fi
fi

# crontabの設定
echo "crontabの設定中..."

# 既存のcrontabをバックアップ
crontab -l > /tmp/crontab_backup 2>/dev/null || true

# 新しいcrontabエントリを追加
{
    echo "# 医療系電子申請システム - パフォーマンス監視"
    echo "# 5分ごとに監視実行"
    echo "*/5 * * * * cd $(pwd) && ./scripts/performance-monitor.sh > /dev/null 2>&1"
    echo "# 毎日午前2時にログローテーション"
    echo "0 2 * * * find $(pwd)/logs -name '*.log' -mtime +7 -delete"
    echo "# 毎週日曜日の午前3時に詳細レポート生成"
    echo "0 3 * * 0 cd $(pwd) && ./scripts/generate-performance-report.sh > /dev/null 2>&1"
} > /tmp/new_crontab

# 既存のcrontabと新しいエントリを結合
if [ -f /tmp/crontab_backup ]; then
    cat /tmp/crontab_backup /tmp/new_crontab | crontab -
else
    crontab /tmp/new_crontab
fi

# 一時ファイルの削除
rm -f /tmp/crontab_backup /tmp/new_crontab

echo "crontab設定完了"

# ログローテーション設定
echo "ログローテーション設定中..."

cat > /etc/logrotate.d/medicalcare << EOF
$(pwd)/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
    postrotate
        systemctl reload rsyslog > /dev/null 2>&1 || true
    endscript
}
EOF

echo "ログローテーション設定完了"

# 環境変数ファイルの作成
echo "環境変数ファイルの作成中..."

cat > .env.performance << EOF
# パフォーマンス監視設定
PERFORMANCE_LOG_DIR=$(pwd)/logs
PERFORMANCE_METRICS_FILE=\$PERFORMANCE_LOG_DIR/performance-metrics.log
PERFORMANCE_ALERT_FILE=\$PERFORMANCE_LOG_DIR/performance-alerts.log

# アラート閾値設定
CPU_THRESHOLD=80
MEMORY_THRESHOLD=85
DISK_THRESHOLD=90
RESPONSE_TIME_THRESHOLD=2.0
ERROR_COUNT_THRESHOLD=10

# 通知設定
ENABLE_EMAIL_ALERTS=false
EMAIL_RECIPIENTS=admin@medicalcare.com
SMTP_SERVER=smtp.gmail.com
SMTP_PORT=587

# 監視間隔設定
MONITORING_INTERVAL=300
DETAILED_REPORT_INTERVAL=604800
EOF

echo "環境変数ファイル作成完了"

# 初期監視実行
echo "初期監視実行中..."
./scripts/performance-monitor.sh

echo "=== パフォーマンス監視セットアップ完了 ==="
echo ""
echo "設定内容:"
echo "- 監視間隔: 5分ごと"
echo "- ログ保存期間: 30日"
echo "- ログディレクトリ: $(pwd)/logs"
echo "- アラートファイル: $(pwd)/logs/performance-alerts.log"
echo ""
echo "監視を開始するには:"
echo "  ./scripts/performance-monitor.sh"
echo ""
echo "crontabの確認:"
echo "  crontab -l"
echo ""
echo "ログの確認:"
echo "  tail -f $(pwd)/logs/performance-metrics.log" 