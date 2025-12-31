/**
 * Check if the current platform is macOS
 * @returns boolean
 */
export function isMac(): boolean {
  // 1. Kiểm tra môi trường SSR (Server Side Rendering) của Next.js
  if (typeof window === 'undefined' || !window.navigator) {
    return false;
  }

  const nav = window.navigator as any;

  // 2. Sử dụng User-Agent Client Hints API (Hiện đại, bảo mật hơn)
  // Lưu ý: Cần kiểm tra tồn tại vì không phải trình duyệt nào cũng có userAgentData
  if (nav.userAgentData?.platform) {
    return nav.userAgentData.platform.toLowerCase().includes('mac');
  }

  // 3. Fallback cho các trình duyệt cũ hoặc không hỗ trợ Client Hints (Safari, Firefox)
  // navigator.platform dù bị deprecated nhưng vẫn rất phổ biến cho việc check này
  const platform = nav.platform?.toLowerCase() || '';
  const userAgent = nav.userAgent?.toLowerCase() || '';

  return platform.includes('mac') || userAgent.includes('mac');
}
