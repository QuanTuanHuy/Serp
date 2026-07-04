import type { PagedResponse } from './types';

export const SCHOOL_BUS_OPTION_QUERY = {
  page: 0,
  size: 100,
  sortDirection: 'ASC' as const,
};

export const SCHOOL_BUS_PAGE_QUERY_OPTIONS = {
  refetchOnMountOrArgChange: true,
} as const;

export function getPageItems<T>(page?: PagedResponse<T> | null): T[] {
  return page?.items || [];
}

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'N/A';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleString();
}

export function formatDate(value?: string | null) {
  if (!value) {
    return 'N/A';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleDateString();
}

export function isValidCoordinatePair(
  latitude?: number | null,
  longitude?: number | null
) {
  return typeof latitude === 'number' && typeof longitude === 'number';
}

export function toCoordinateString(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '';
  }

  return value.toFixed(6);
}
