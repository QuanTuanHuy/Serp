/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Date transformation utilities
 */

/**
 * Convert epoch timestamp to Date object
 * Handles both seconds and milliseconds automatically
 */
export const epochToDate = (timestamp: number): Date => {
  // If timestamp is in seconds (10 digits), convert to milliseconds
  const ms = timestamp.toString().length === 10 ? timestamp * 1000 : timestamp;
  return new Date(ms);
};

/**
 * Convert epoch timestamp to ISO string
 */
export const epochToISOString = (timestamp: number): string => {
  return epochToDate(timestamp).toISOString();
};

/**
 * Convert Date to epoch milliseconds
 */
export const dateToEpoch = (date: Date): number => {
  return date.getTime();
};

/**
 * Convert ISO string to epoch milliseconds
 */
export const isoToEpoch = (isoString: string): number => {
  return new Date(isoString).getTime();
};

/**
 * Format epoch timestamp to readable string
 */
export const formatEpochDate = (
  timestamp: number,
  locale: string = 'en-US',
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }
): string => {
  return epochToDate(timestamp).toLocaleDateString(locale, options);
};

/**
 * Get current timestamp in milliseconds
 */
export const getCurrentTimestamp = (): number => {
  return Date.now();
};
