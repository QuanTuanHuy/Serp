/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Export Utilities
 */

// Generic type for exportable data - relaxed constraint
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type ExportableData = Record<string, any>;

// Export format types
export type ExportFormat = 'csv' | 'excel' | 'json';

// Column definition for exports
export interface ExportColumn<T> {
  key: keyof T;
  header: string;
  formatter?: (value: unknown, row: T) => string;
}

// Export options
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface ExportOptions<T = any> {
  filename: string;
  columns: ExportColumn<T>[];
  data: T[];
  format: ExportFormat;
  includeHeaders?: boolean;
}

/**
 * Format a value for CSV export (handle special characters, quotes, etc.)
 */
function formatCSVValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }

  const stringValue = String(value);

  // If the value contains commas, newlines, or quotes, wrap in quotes
  if (
    stringValue.includes(',') ||
    stringValue.includes('\n') ||
    stringValue.includes('"')
  ) {
    // Escape quotes by doubling them
    return `"${stringValue.replace(/"/g, '""')}"`;
  }

  return stringValue;
}

/**
 * Convert data to CSV format
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function convertToCSV<T = any>(
  data: T[],
  columns: ExportColumn<T>[],
  includeHeaders = true
): string {
  const rows: string[] = [];

  // Add header row
  if (includeHeaders) {
    const headerRow = columns
      .map((col) => formatCSVValue(col.header))
      .join(',');
    rows.push(headerRow);
  }

  // Add data rows
  for (const item of data) {
    const row = columns
      .map((col) => {
        const value = (item as Record<string, unknown>)[col.key as string];
        const formattedValue = col.formatter
          ? col.formatter(value, item)
          : value;
        return formatCSVValue(formattedValue);
      })
      .join(',');
    rows.push(row);
  }

  return rows.join('\n');
}

/**
 * Convert data to JSON format
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function convertToJSON<T = any>(
  data: T[],
  columns: ExportColumn<T>[]
): string {
  const exportData = data.map((item) => {
    const row: Record<string, unknown> = {};
    for (const col of columns) {
      const value = (item as Record<string, unknown>)[col.key as string];
      row[col.header] = col.formatter ? col.formatter(value, item) : value;
    }
    return row;
  });

  return JSON.stringify(exportData, null, 2);
}

/**
 * Download a file with the given content
 */
export function downloadFile(
  content: string,
  filename: string,
  mimeType: string
): void {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  URL.revokeObjectURL(url);
}

/**
 * Export data to a file
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function exportData<T = any>(options: ExportOptions<T>): void {
  const { filename, columns, data, format, includeHeaders = true } = options;

  let content: string;
  let mimeType: string;
  let extension: string;

  switch (format) {
    case 'csv':
      content = convertToCSV(data, columns, includeHeaders);
      mimeType = 'text/csv;charset=utf-8;';
      extension = 'csv';
      break;
    case 'json':
      content = convertToJSON(data, columns);
      mimeType = 'application/json;charset=utf-8;';
      extension = 'json';
      break;
    case 'excel':
      // For Excel, we'll use CSV with BOM for better Excel compatibility
      content = '\uFEFF' + convertToCSV(data, columns, includeHeaders);
      mimeType = 'text/csv;charset=utf-8;';
      extension = 'csv';
      break;
    default:
      throw new Error(`Unsupported export format: ${format}`);
  }

  downloadFile(content, `${filename}.${extension}`, mimeType);
}

/**
 * Format date for export
 */
export function formatDateForExport(
  date: string | Date | null | undefined
): string {
  if (!date) return '';
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

/**
 * Format datetime for export
 */
export function formatDateTimeForExport(
  date: string | Date | null | undefined
): string {
  if (!date) return '';
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Format currency for export
 */
export function formatCurrencyForExport(
  value: number | null | undefined
): string {
  if (value === null || value === undefined) return '';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value);
}

/**
 * Format percentage for export
 */
export function formatPercentageForExport(
  value: number | null | undefined
): string {
  if (value === null || value === undefined) return '';
  return `${value}%`;
}

// ==========================================
// Pre-defined column configurations
// ==========================================

import type { Customer, Lead, Opportunity, Activity } from '../types';

export const CUSTOMER_EXPORT_COLUMNS: ExportColumn<Customer>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Tên khách hàng' },
  { key: 'email', header: 'Email' },
  { key: 'phone', header: 'Số điện thoại' },
  {
    key: 'customerType',
    header: 'Loại',
    formatter: (v) => (v === 'INDIVIDUAL' ? 'Cá nhân' : 'Doanh nghiệp'),
  },
  {
    key: 'status',
    header: 'Trạng thái',
    formatter: (v) =>
      v === 'ACTIVE'
        ? 'Đang hoạt động'
        : v === 'INACTIVE'
          ? 'Không hoạt động'
          : 'Tiềm năng',
  },
  { key: 'companyName', header: 'Công ty' },
  { key: 'address', header: 'Địa chỉ' },
  {
    key: 'createdAt',
    header: 'Ngày tạo',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'lastContactDate',
    header: 'Liên hệ cuối',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'totalValue',
    header: 'Tổng giá trị',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
];

export const LEAD_EXPORT_COLUMNS: ExportColumn<Lead>[] = [
  { key: 'id', header: 'ID' },
  { key: 'firstName', header: 'Họ' },
  { key: 'lastName', header: 'Tên' },
  { key: 'email', header: 'Email' },
  { key: 'phone', header: 'Số điện thoại' },
  { key: 'company', header: 'Công ty' },
  { key: 'jobTitle', header: 'Chức vụ' },
  { key: 'source', header: 'Nguồn' },
  { key: 'status', header: 'Trạng thái' },
  { key: 'priority', header: 'Ưu tiên' },
  {
    key: 'estimatedValue',
    header: 'Giá trị ước tính',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
  {
    key: 'expectedCloseDate',
    header: 'Ngày dự kiến chốt',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'createdAt',
    header: 'Ngày tạo',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'lastActivityDate',
    header: 'Hoạt động cuối',
    formatter: (v) => formatDateForExport(v as string),
  },
];

export const OPPORTUNITY_EXPORT_COLUMNS: ExportColumn<Opportunity>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Tên cơ hội' },
  { key: 'customerName', header: 'Khách hàng' },
  {
    key: 'value',
    header: 'Giá trị',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
  { key: 'stage', header: 'Giai đoạn' },
  {
    key: 'probability',
    header: 'Xác suất',
    formatter: (v) => formatPercentageForExport(v as number),
  },
  {
    key: 'expectedCloseDate',
    header: 'Ngày dự kiến chốt',
    formatter: (v) => formatDateForExport(v as string),
  },
  { key: 'type', header: 'Loại' },
  {
    key: 'createdAt',
    header: 'Ngày tạo',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'updatedAt',
    header: 'Cập nhật cuối',
    formatter: (v) => formatDateForExport(v as string),
  },
];

export const ACTIVITY_EXPORT_COLUMNS: ExportColumn<Activity>[] = [
  { key: 'id', header: 'ID' },
  { key: 'type', header: 'Loại' },
  { key: 'subject', header: 'Tiêu đề' },
  { key: 'description', header: 'Mô tả' },
  { key: 'status', header: 'Trạng thái' },
  { key: 'priority', header: 'Ưu tiên' },
  {
    key: 'scheduledDate',
    header: 'Ngày lên lịch',
    formatter: (v) => formatDateTimeForExport(v as string),
  },
  {
    key: 'createdAt',
    header: 'Ngày tạo',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'actualDate',
    header: 'Ngày thực hiện',
    formatter: (v) => formatDateTimeForExport(v as string),
  },
];
