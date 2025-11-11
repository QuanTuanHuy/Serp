/**
 * Shared Utilities
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Utility function to merge Tailwind CSS classes with clsx
 * @param inputs - Classes to merge
 * @returns Merged class string
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Format date to readable string
 * @param date - Date object or string
 * @returns Formatted date string
 */
export function formatDate(date: Date | string): string {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
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
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Format currency
 * @param amount - Amount to format
 * @param currency - Currency code (default: USD)
 * @returns Formatted currency string
 */
export function formatCurrency(
  amount: number,
  currency: string = 'USD'
): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(amount);
}

/**
 * Debounce function
 * @param func - Function to debounce
 * @param wait - Wait time in milliseconds
 * @returns Debounced function
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

/**
 * Truncate text
 * @param text - Text to truncate
 * @param length - Maximum length
 * @returns Truncated text
 */
export function truncateText(text: string, length: number): string {
  if (text.length <= length) return text;
  return `${text.substring(0, length)}...`;
}

/**
 * Compute initials from a person's name or identifier.
 * Priority:
 * 1) firstName + lastName
 * 2) words in a full name string
 * 3) local-part of email before '@'
 * Fallback: empty string
 * @param input - Either a full string (name/email) or object with firstName/lastName/email
 * @param maxLetters - Max initials letters to return (default 2)
 */
export function getInitials(
  input:
    | string
    | {
        firstName?: string | null;
        lastName?: string | null;
        email?: string | null;
        fullName?: string | null;
      },
  maxLetters: number = 2
): string {
  const clamp = (s: string) => s.replace(/\s+/g, ' ').trim();

  const fromWords = (words: string[]) =>
    words
      .filter(Boolean)
      .slice(0, maxLetters)
      .map((w) => w.charAt(0))
      .join('')
      .toUpperCase();

  let firstName: string | undefined;
  let lastName: string | undefined;
  let email: string | undefined;
  let fullName: string | undefined;

  if (typeof input === 'string') {
    const s = clamp(input);
    if (s.includes('@')) {
      const local = s.split('@')[0];
      if (local) return fromWords([local]);
    }
    if (s) return fromWords(s.split(' '));
    return '';
  } else if (input && typeof input === 'object') {
    firstName = input.firstName ?? undefined;
    lastName = input.lastName ?? undefined;
    email = input.email ?? undefined;
    fullName = input.fullName ?? undefined;
  }

  if (firstName || lastName) {
    return fromWords([firstName ?? '', lastName ?? '']);
  }

  if (fullName) {
    const s = clamp(fullName);
    if (s) return fromWords(s.split(' '));
  }

  if (email) {
    const local = clamp(email).split('@')[0];
    if (local) return fromWords([local]);
  }

  return '';
}

export { getIconComponent } from './getIconComponent';
