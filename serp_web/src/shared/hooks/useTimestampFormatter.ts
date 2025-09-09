/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Hook for formatting timestamps from transformed API data
 */

import { useMemo } from 'react';

export interface TimestampFormatOptions {
  locale?: string;
  dateStyle?: 'full' | 'long' | 'medium' | 'short';
  timeStyle?: 'full' | 'long' | 'medium' | 'short';
  includeTime?: boolean;
  relative?: boolean;
}

/**
 * Hook to format timestamp strings (ISO format) for display
 */
export const useTimestampFormatter = () => {
  const formatTimestamp = useMemo(() => {
    return (
      timestamp: string,
      options: TimestampFormatOptions = {}
    ): string => {
      const {
        locale = 'en-US ',
        dateStyle = 'medium',
        timeStyle = 'short',
        includeTime = true,
        relative = false,
      } = options;

      if (!timestamp) {
        return '';
      }

      try {
        const date = new Date(timestamp);

        if (relative) {
          return formatRelativeTime(date);
        }

        const formatOptions: Intl.DateTimeFormatOptions = {
          dateStyle,
        };

        if (includeTime) {
          formatOptions.timeStyle = timeStyle;
        }

        return date.toLocaleString(locale, formatOptions);
      } catch (error) {
        console.warn('Invalid timestamp format:', timestamp);
        return timestamp;
      }
    };
  }, []);

  return { formatTimestamp };
};

/**
 * Format relative time (e.g., "2 hours ago", "3 days ago")
 */
function formatRelativeTime(date: Date): string {
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  const intervals = [
    { label: 'year', seconds: 31536000 },
    { label: 'month', seconds: 2592000 },
    { label: 'week', seconds: 604800 },
    { label: 'day', seconds: 86400 },
    { label: 'hour', seconds: 3600 },
    { label: 'minute', seconds: 60 },
    { label: 'second', seconds: 1 },
  ];

  for (const interval of intervals) {
    const count = Math.floor(diffInSeconds / interval.seconds);
    if (count > 0) {
      const rtf = new Intl.RelativeTimeFormat('en-US', { numeric: 'auto' });
      return rtf.format(-count, interval.label as Intl.RelativeTimeFormatUnit);
    }
  }

  return 'just now';
}

/**
 * Hook specifically for user account timestamps
 */
export const useUserTimestamps = (user: { createdAt: string; updatedAt: string }) => {
  const { formatTimestamp } = useTimestampFormatter();

  const formattedCreatedAt = useMemo(() => {
    return formatTimestamp(user.createdAt, {
      dateStyle: 'long',
      timeStyle: 'short',
    });
  }, [user.createdAt, formatTimestamp]);

  const formattedUpdatedAt = useMemo(() => {
    return formatTimestamp(user.updatedAt, {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }, [user.updatedAt, formatTimestamp]);

  const relativeCreatedAt = useMemo(() => {
    return formatTimestamp(user.createdAt, { relative: true });
  }, [user.createdAt, formatTimestamp]);

  const relativeUpdatedAt = useMemo(() => {
    return formatTimestamp(user.updatedAt, { relative: true });
  }, [user.updatedAt, formatTimestamp]);

  return {
    formattedCreatedAt,
    formattedUpdatedAt,
    relativeCreatedAt,
    relativeUpdatedAt,
  };
};
