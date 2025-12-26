/**
 * Format utilities
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

/**
 * Format date to readable string
 * @param date - Date object or string
 * @returns Formatted date string
 */
export function formatDate(date: Date | string): string {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

/**
 * Format date and time to readable string
 * @param date - Date object or string
 * @returns Formatted date and time string
 */
export function formatDateTime(date: Date | string): string {
  return new Date(date).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Format currency
 * @param amount - Amount to format
 * @param currency - Currency code (default: VND)
 * @returns Formatted currency string
 */
export function formatCurrency(
  amount: number,
  currency: string = 'VND'
): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency,
  }).format(amount);
}
