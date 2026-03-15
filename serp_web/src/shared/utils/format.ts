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

/**
 * Format a string of numbers into Vietnamese currency format (VND)
 * @param value - String value to format
 * @returns Formatted currency string (e.g., 1.000.000 ₫)
 */
export function formatStringCurrencyVN(value: string): string {
  const numericString = value.replace(/\D/g, '');

  const amount = numericString ? parseInt(numericString, 10) : 0;

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
}

/**
 * Format phone number to xxxx.xxx.xxx format
 * @param phone - Phone number string or number
 * @returns Formatted phone number string
 */
export function formatPhoneNumber(phone: string | number): string {
  const phoneStr = phone.toString().replace(/\D/g, '');

  if (phoneStr.length === 10) {
    return phoneStr.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3');
  }

  return phoneStr;
}

/**
 * Format date to Vietnamese string (DD/MM/YYYY)
 * @param date - Date object or string
 * @returns Formatted date string (e.g., 15/03/2026)
 */
export function formatDateVN(date: Date | string): string {
  return new Date(date).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

/**
 * Format date string to Vietnamese string (DD/MM/YYYY)
 * @param dateString
 * @returns
 */
export const formatDateStringVN = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

/**
 * Format date and time to Vietnamese string (HH:mm - DD/MM/YYYY)
 * @param date - Date object or string
 * @returns Formatted date and time string (e.g., 15:50 15/03/2026)
 */
export function formatDateTimeVN(date: Date | string): string {
  return new Date(date).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Format date to Vietnamese full text
 * @param date - Date object or string
 * @returns Formatted full text date string (e.g., Chủ Nhật, 15 tháng 3, 2026)
 */
export function formatDateFullVN(date: Date | string): string {
  return new Date(date).toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}
