# package.json 設定説明

このファイルは医療系電子申請システムのフロントエンド（Next.js）の依存関係とスクリプトを定義しています。

## プロジェクト基本情報

- **name**: medicalcare-frontend - プロジェクト名
- **version**: 0.1.0 - バージョン
- **private**: true - プライベートパッケージ（npm公開しない）

## npmスクリプト定義

- **dev**: next dev - 開発サーバー起動
- **build**: next build - 本番用ビルド
- **start**: next start - 本番サーバー起動
- **lint**: next lint - コード品質チェック

## 本番依存関係

### UI・コンポーネント関連
- **@headlessui/react**: ^1.7.17 - アクセシブルなUIコンポーネント
- **@heroicons/react**: ^2.0.18 - SVGアイコンライブラリ

### 型定義
- **@types/node**: ^20 - Node.js型定義
- **@types/react**: ^18 - React型定義
- **@types/react-dom**: ^18 - React DOM型定義

### CSS・スタイリング関連
- **autoprefixer**: ^10.0.1 - CSSベンダープレフィックス自動付与
- **tailwindcss**: ^3.3.0 - CSSフレームワーク
- **postcss**: ^8 - CSS後処理ツール

### HTTP・データ関連
- **axios**: ^1.6.0 - HTTPクライアント
- **react-query**: ^3.39.3 - サーバー状態管理ライブラリ

### フォーム・日付関連
- **react-hook-form**: ^7.48.2 - フォーム管理ライブラリ
- **date-fns**: ^2.30.0 - 日付操作ライブラリ

### フレームワーク・言語
- **next**: 14.0.0 - Reactフレームワーク
- **react**: ^18 - Reactライブラリ
- **react-dom**: ^18 - React DOM
- **typescript**: 5.8.3 - TypeScript

## 開発依存関係

- **eslint**: ^8 - コード品質チェッカー
- **eslint-config-next**: 14.0.0 - Next.js用ESLint設定

## 注意事項

- JSONファイルではコメントが許可されていないため、この説明は別ファイル（package.md）として提供しています
- 依存関係のバージョンは定期的に更新することを推奨します
- セキュリティ上の理由から、本番環境では最新の安定版を使用してください 