import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  /* config options here */
  output: 'standalone', // Enable standalone output for optimized Docker builds
  async rewrites() {
    return [
      {
        source: '/bds',
        destination: '/school-bus',
      },
      {
        source: '/bds/:path*',
        destination: '/school-bus/:path*',
      },
    ];
  },
};

export default nextConfig;
