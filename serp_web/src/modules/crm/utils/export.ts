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
  { key: 'name', header: 'Customer Name' },
  { key: 'email', header: 'Email' },
  { key: 'phone', header: 'Phone Number' },
  {
    key: 'customerType',
    header: 'Type',
    formatter: (v) => (v === 'INDIVIDUAL' ? 'Individual' : 'Company'),
  },
  {
    key: 'status',
    header: 'Status',
    formatter: (v) =>
      v === 'ACTIVE' ? 'Active' : v === 'INACTIVE' ? 'Inactive' : 'Potential',
  },
  { key: 'companyName', header: 'Company' },
  { key: 'address', header: 'Address' },
  {
    key: 'createdAt',
    header: 'Created Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'lastContactDate',
    header: 'Last Contact',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'totalValue',
    header: 'Total Value',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
];

export const LEAD_EXPORT_COLUMNS: ExportColumn<Lead>[] = [
  { key: 'id', header: 'ID' },
  { key: 'firstName', header: 'First Name' },
  { key: 'lastName', header: 'Last Name' },
  { key: 'email', header: 'Email' },
  { key: 'phone', header: 'Phone Number' },
  { key: 'company', header: 'Company' },
  { key: 'jobTitle', header: 'Job Title' },
  { key: 'source', header: 'Source' },
  { key: 'status', header: 'Status' },
  { key: 'priority', header: 'Priority' },
  {
    key: 'estimatedValue',
    header: 'Estimated Value',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
  {
    key: 'expectedCloseDate',
    header: 'Expected Close Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'createdAt',
    header: 'Created Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'lastActivityDate',
    header: 'Last Activity',
    formatter: (v) => formatDateForExport(v as string),
  },
];

export const OPPORTUNITY_EXPORT_COLUMNS: ExportColumn<Opportunity>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Opportunity Name' },
  { key: 'customerName', header: 'Customer' },
  {
    key: 'value',
    header: 'Value',
    formatter: (v) => formatCurrencyForExport(v as number),
  },
  { key: 'stage', header: 'Stage' },
  {
    key: 'probability',
    header: 'Probability',
    formatter: (v) => formatPercentageForExport(v as number),
  },
  {
    key: 'expectedCloseDate',
    header: 'Expected Close Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  { key: 'type', header: 'Type' },
  {
    key: 'createdAt',
    header: 'Created Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'updatedAt',
    header: 'Last Updated',
    formatter: (v) => formatDateForExport(v as string),
  },
];

export const ACTIVITY_EXPORT_COLUMNS: ExportColumn<Activity>[] = [
  { key: 'id', header: 'ID' },
  { key: 'type', header: 'Type' },
  { key: 'subject', header: 'Subject' },
  { key: 'description', header: 'Description' },
  { key: 'status', header: 'Status' },
  { key: 'priority', header: 'Priority' },
  {
    key: 'scheduledDate',
    header: 'Scheduled Date',
    formatter: (v) => formatDateTimeForExport(v as string),
  },
  {
    key: 'createdAt',
    header: 'Created Date',
    formatter: (v) => formatDateForExport(v as string),
  },
  {
    key: 'actualDate',
    header: 'Actual Date',
    formatter: (v) => formatDateTimeForExport(v as string),
  },
];
