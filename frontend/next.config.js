/** @type {import('next').NextConfig} */
const nextConfig = {
  // 画像最適化設定
  images: {
    domains: ['localhost', 'vercel.app', 'vercel.com'],
    formats: ['image/webp', 'image/avif'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60,
  },

  // 圧縮設定
  compress: true,

  // パフォーマンス最適化
  poweredByHeader: false,
  generateEtags: true,

  // ヘッダー設定
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
          {
            key: 'Access-Control-Allow-Origin',
            value: '*',
          },
          {
            key: 'Access-Control-Allow-Methods',
            value: 'GET, POST, PUT, DELETE, OPTIONS',
          },
          {
            key: 'Access-Control-Allow-Headers',
            value: 'Content-Type, Authorization',
          },
        ],
      },
      {
        source: '/api/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=300, s-maxage=600',
          },
        ],
      },
      {
        source: '/static/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=31536000, immutable',
          },
        ],
      },
    ];
  },

  // リダイレクト設定
  async redirects() {
    return [
      {
        source: '/health',
        destination: '/api/health',
        permanent: false,
      },
    ];
  },

  // Webpack設定
  webpack: (config, { dev, isServer }) => {
    // 本番環境での最適化
    if (!dev && !isServer) {
      config.optimization.splitChunks = {
        chunks: 'all',
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendors',
            chunks: 'all',
          },
          common: {
            name: 'common',
            minChunks: 2,
            chunks: 'all',
            enforce: true,
          },
        },
      };
    }

    // バンドルサイズ最適化
    config.optimization.minimize = !dev;
    
    // ソースマップ設定
    if (dev) {
      config.devtool = 'eval-source-map';
    } else {
      config.devtool = 'source-map';
    }

    return config;
  },

  // 出力設定
  output: 'standalone',
  
  // Vercel用設定
  trailingSlash: false,
  swcMinify: true,
};

module.exports = nextConfig; 