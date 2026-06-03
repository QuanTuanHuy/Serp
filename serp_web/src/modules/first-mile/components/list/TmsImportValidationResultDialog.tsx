/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS Excel import validation result dialog
 */

'use client';

import React from 'react';
import { CheckCircle2, FileUp, Loader2, XCircle } from 'lucide-react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { cn } from '@/shared/utils';
import type { ValidateImportFileResponse } from '../../types';

interface TmsImportValidationResultDialogProps<T extends object> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  result: ValidateImportFileResponse<T> | null;
  entityLabel: string;
  isImporting?: boolean;
  onConfirmImport?: () => void;
}

interface DisplayRow {
  item: Record<string, unknown>;
  rowNumber: number;
  sourceRowIndex: number;
  key: string;
  errors: string[];
}

const ROW_NUMBER_PATTERN = /\brow\s*(\d+)\b|[A-Z]+(\d+)/gi;
const PRODUCT_COLUMN_KEYS = new Set([
  'product_name',
  'product_value',
  'product_quantity',
  'product_weight',
  'product_type',
]);

const toDisplayValue = (value: unknown): string => {
  if (value === null || value === undefined || value === '') {
    return '-';
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return '-';
    }

    return value
      .map((item) => toDisplayValue(item))
      .filter((entryValue) => entryValue !== '-')
      .join('; ');
  }

  if (typeof value === 'object') {
    return JSON.stringify(value);
  }

  return String(value);
};

const splitErrorMessages = (errorMessage?: string): string[] =>
  (errorMessage ?? '')
    .split(/\r?\n|;\s*/)
    .map((message) => message.trim())
    .filter(Boolean);

const getErrorRows = (message: string): number[] => {
  const rows = new Set<number>();
  let match: RegExpExecArray | null;

  ROW_NUMBER_PATTERN.lastIndex = 0;
  while ((match = ROW_NUMBER_PATTERN.exec(message)) !== null) {
    const value = Number(match[1] ?? match[2]);
    if (Number.isInteger(value) && value > 0) {
      rows.add(value);
    }
  }

  return Array.from(rows);
};

const getProductValue = (
  rowItem: Record<string, unknown>,
  key: string,
  sourceRowIndex: number
): unknown => {
  const products = Array.isArray(rowItem.products)
    ? (rowItem.products as Record<string, unknown>[])
    : [];
  const product = products[sourceRowIndex];

  if (!product) {
    return undefined;
  }

  switch (key) {
    case 'product_name':
      return product.name;
    case 'product_value':
      return product.value;
    case 'product_quantity':
      return product.quantity;
    case 'product_weight':
      return product.weight_gram;
    case 'product_type':
      return (
        [product.product_type_code, product.product_type_name]
          .filter(Boolean)
          .join(' - ') || product.product_type_id
      );
    default:
      return undefined;
  }
};

const getCellValue = (
  rowItem: Record<string, unknown>,
  key: string,
  rowNumber: number,
  sourceRowIndex: number
): unknown => {
  if (key === 'stt') {
    return rowNumber;
  }

  if (PRODUCT_COLUMN_KEYS.has(key)) {
    return getProductValue(rowItem, key, sourceRowIndex) ?? rowItem[key];
  }

  if (sourceRowIndex > 0) {
    return undefined;
  }

  return rowItem[key];
};

const buildDisplayRows = <T extends object>(
  rows: T[],
  errorsByRow: Map<number, string[]>
): DisplayRow[] =>
  rows.flatMap((item, index) => {
    const rowItem = item as Record<string, unknown>;
    const sourceRows = Array.isArray(rowItem.source_rows)
      ? rowItem.source_rows
          .map((row) => Number(row))
          .filter((row) => Number.isInteger(row) && row > 0)
      : [];
    const effectiveSourceRows =
      sourceRows.length > 0 ? sourceRows : [index + 1];

    return effectiveSourceRows.map((rowNumber, sourceRowIndex) => ({
      item: rowItem,
      rowNumber,
      sourceRowIndex,
      key: `${rowNumber}-${sourceRowIndex}-${index}`,
      errors: errorsByRow.get(rowNumber) ?? [],
    }));
  });

export function TmsImportValidationResultDialog<T extends object>({
  open,
  onOpenChange,
  result,
  entityLabel,
  isImporting = false,
  onConfirmImport,
}: TmsImportValidationResultDialogProps<T>) {
  const columns = React.useMemo(
    () => Object.entries(result?.header ?? {}),
    [result?.header]
  );

  const errorMessages = React.useMemo(
    () => splitErrorMessages(result?.error_message),
    [result?.error_message]
  );

  const errorsByRow = React.useMemo(() => {
    const nextErrors = new Map<number, string[]>();

    errorMessages.forEach((message) => {
      const rows = getErrorRows(message);

      rows.forEach((rowNumber) => {
        const rowErrors = nextErrors.get(rowNumber) ?? [];
        rowErrors.push(message);
        nextErrors.set(rowNumber, rowErrors);
      });
    });

    return nextErrors;
  }, [errorMessages]);

  const rows = React.useMemo(() => result?.data ?? [], [result?.data]);
  const displayRows = React.useMemo(
    () => buildDisplayRows(rows, errorsByRow),
    [errorsByRow, rows]
  );

  if (!result) {
    return null;
  }

  const isSuccess = result.is_success;
  const canConfirmImport = isSuccess && Boolean(onConfirmImport);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='flex h-[88vh] w-[calc(100vw-2rem)] max-w-[calc(100vw-2rem)] flex-col overflow-hidden p-4 sm:max-w-6xl'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            {isSuccess ? (
              <CheckCircle2 className='h-5 w-5 text-emerald-600' />
            ) : (
              <XCircle className='h-5 w-5 text-destructive' />
            )}
            {isSuccess
              ? 'Your Excel file is ready to import'
              : 'Review Excel rows before importing'}
          </DialogTitle>
          <DialogDescription>
            Green rows are valid. Red rows need changes; review the Errors
            column, update the Excel file, then import the {entityLabel} file
            again.
          </DialogDescription>
        </DialogHeader>

        <div className='min-h-0 flex-1 overflow-auto rounded-md border'>
          <div className='min-w-max'>
            <Table className='min-w-max'>
              <TableHeader className='sticky top-0 z-10 bg-background'>
                <TableRow>
                  <TableHead className='min-w-20'>Row</TableHead>
                  {columns.map(([key, label]) => (
                    <TableHead key={key} className='min-w-44'>
                      {label || key}
                    </TableHead>
                  ))}
                  <TableHead className='min-w-72'>Errors</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {displayRows.length > 0 ? (
                  displayRows.map((row) => {
                    const hasErrors = row.errors.length > 0;

                    return (
                      <TableRow
                        key={row.key}
                        className={cn(
                          hasErrors
                            ? 'bg-red-50 hover:bg-red-50'
                            : 'bg-emerald-50/60 hover:bg-emerald-50'
                        )}
                      >
                        <TableCell className='font-medium'>
                          {row.rowNumber}
                        </TableCell>
                        {columns.map(([key]) => (
                          <TableCell
                            key={key}
                            className='max-w-72 min-w-44 truncate'
                          >
                            {toDisplayValue(
                              getCellValue(
                                row.item,
                                key,
                                row.rowNumber,
                                row.sourceRowIndex
                              )
                            )}
                          </TableCell>
                        ))}
                        <TableCell
                          className={cn(
                            'whitespace-normal text-muted-foreground',
                            hasErrors && 'text-destructive'
                          )}
                        >
                          {hasErrors
                            ? Array.from(new Set(row.errors)).join(' ')
                            : 'OK'}
                        </TableCell>
                      </TableRow>
                    );
                  })
                ) : (
                  <TableRow>
                    <TableCell
                      colSpan={columns.length + 2}
                      className='h-24 text-center text-muted-foreground'
                    >
                      {errorMessages.length > 0
                        ? errorMessages.join(' ')
                        : 'No parsed rows are available.'}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
        </div>
        <DialogFooter className='gap-2 sm:justify-end'>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isImporting}
          >
            Cancel
          </Button>
          <Button
            type='button'
            variant='destructive'
            onClick={onConfirmImport}
            disabled={!canConfirmImport || isImporting}
            className={cn(!canConfirmImport && 'opacity-40')}
          >
            {isImporting ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <FileUp className='mr-2 h-4 w-4' />
            )}
            Confirm import
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
