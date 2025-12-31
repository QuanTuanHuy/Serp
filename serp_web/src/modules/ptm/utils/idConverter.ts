/**
 * PTM - ID Converter Utility
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Convert string/number IDs to numeric
 */

/**
 * Convert string or number ID to numeric ID
 *
 * @param id - ID that can be string, number, or null
 * @returns Numeric ID or null
 *
 * @example
 * toNumericId("123") // 123
 * toNumericId(456) // 456
 * toNumericId(null) // null
 */
export function toNumericId(
  id: string | number | null | undefined
): number | null {
  if (id === null || id === undefined) return null;
  return typeof id === 'string' ? parseInt(id, 10) : id;
}

/**
 * Convert array of string/number IDs to numeric IDs
 */
export function toNumericIds(ids: Array<string | number>): number[] {
  return ids.map((id) => toNumericId(id)!).filter((id) => id !== null);
}
