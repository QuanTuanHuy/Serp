/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Shared TMS list filter helpers
 */

export const normalizeFilterText = (value: string): string | undefined => {
  const trimmedValue = value.trim();
  return trimmedValue ? trimmedValue : undefined;
};

export const parseOptionalNonNegativeInteger = (
  rawValue: string,
  fieldLabel: string
): number | undefined => {
  const trimmedValue = rawValue.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue < 0) {
    throw new Error(`${fieldLabel} must be a non-negative integer.`);
  }

  return parsedValue;
};

export const validateNumericRange = (
  minValue: number | undefined,
  maxValue: number | undefined,
  fieldLabel: string
) => {
  if (minValue !== undefined && maxValue !== undefined && minValue > maxValue) {
    throw new Error(
      `${fieldLabel}: min value must be less than or equal to max value.`
    );
  }
};

/** Maps datetime-local input to API ISO local date-time (no timezone shift). */
export const formatDateTimeLocalForApi = (
  value: string
): string | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  if (trimmedValue.length === 16) {
    return `${trimmedValue}:00`;
  }

  return trimmedValue;
};

export const parseTriStateBoolean = (
  value: 'ALL' | 'YES' | 'NO'
): boolean | undefined => {
  if (value === 'ALL') {
    return undefined;
  }

  return value === 'YES';
};
