# Medical Care Imaging Embed (WordPress)

ショートコード `[medicalcare_imaging]` で Next.js 医療画像AI画面を埋め込みます。

## インストール

1. `medicalcare-imaging-embed` フォルダを `wp-content/plugins/` へコピー
2. 管理画面でプラグインを有効化
3. 投稿/固定ページにショートコードを配置

```
[medicalcare_imaging]
[medicalcare_imaging url="https://your-host/imaging" height="1000"]
```

環境変数 `MEDICALCARE_IMAGING_URL` でもデフォルトURLを指定できます。
