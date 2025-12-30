# バックエンド用Dockerfile - Spring Bootアプリケーションのコンテナ化設定（性能最適化版）

# ビルドステージ
FROM eclipse-temurin:17-jdk AS builder

# 作業ディレクトリの設定
WORKDIR /app

# システムパッケージの更新と必要なツールのインストール
RUN apt-get update && apt-get install -y wget curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Gradleラッパーとプロジェクトファイルをコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY gradle.properties .
COPY src src

# gradle-wrapper.jarをダウンロードして配置
RUN wget -O gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar

# Gradleラッパーに実行権限を付与
RUN chmod +x ./gradlew

# 依存関係のダウンロードとビルド（キャッシュ最適化）
RUN ./gradlew build --no-daemon --parallel

# 実行ステージ
FROM eclipse-temurin:17-jre-alpine

# セキュリティ更新と必要なパッケージのインストール
RUN apk update && apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# 非rootユーザーの作成
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 作業ディレクトリの設定
WORKDIR /app

# ビルドステージからJARファイルをコピー
COPY --from=builder /app/build/libs/*.jar app.jar

# アップロードディレクトリとログディレクトリの作成
RUN mkdir -p /app/uploads /app/logs && \
    chown -R appuser:appgroup /app

# ユーザーを変更
USER appuser

# ポートの公開設定
EXPOSE 8080

# 環境変数の設定
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom"

# ヘルスチェック
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# アプリケーション起動コマンド（性能最適化）
CMD ["java", "-jar", "app.jar"] 