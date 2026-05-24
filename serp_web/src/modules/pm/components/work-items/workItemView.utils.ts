/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - shared PM work item view helpers
 */

export function parseIssueId(value: string | null): number | undefined {
  if (!value) return undefined;
  const issueId = Number(value);
  return Number.isFinite(issueId) ? issueId : undefined;
}

export function parseNumberList(value: string | null): number[] {
  if (!value) return [];
  return value
    .split(',')
    .map((part) => Number(part))
    .filter((item) => Number.isFinite(item));
}

export function serializeNumberList(values: number[]): string | undefined {
  return values.length ? values.join(',') : undefined;
}

export function formatDate(
  value?: number | string | null,
  fallback = 'No date',
  options: Intl.DateTimeFormatOptions = {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }
): string {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return date.toLocaleDateString('en-US', options);
}

export function getInitials(name?: string | null): string {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

export function countActiveFilters(
  values: Array<number | number[] | undefined>
) {
  return values.reduce<number>((total, value) => {
    if (Array.isArray(value)) {
      return total + value.length;
    }

    return total + (value ? 1 : 0);
  }, 0);
}
