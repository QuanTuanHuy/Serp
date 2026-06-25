'use client';

import React, { useState, useRef, useCallback } from 'react';
import {
  Upload,
  Download,
  FileSpreadsheet,
  Loader2,
  X,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api/utils';
import { useAppSelector } from '@/shared/hooks';
import { selectToken } from '@/modules/account/store';
import {
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui';
import { useImportDispatcherLocationsMutation } from '../api/ttcrsApi';
import type { LocationImportResult } from '../types';

interface ImportLocationsDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export function ImportLocationsDialog({
  open,
  onClose,
  onSuccess,
}: ImportLocationsDialogProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [result, setResult] = useState<LocationImportResult | null>(null);
  const [isDownloadingTemplate, setIsDownloadingTemplate] = useState(false);

  const token = useAppSelector(selectToken);
  const [importLocations, { isLoading }] =
    useImportDispatcherLocationsMutation();

  const reset = useCallback(() => {
    setFile(null);
    setResult(null);
  }, []);

  // Reset when dialog opens
  React.useEffect(() => {
    if (open) reset();
  }, [open, reset]);

  const downloadTemplate = async () => {
    setIsDownloadingTemplate(true);
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/ttcrs/api/v1/dispatcher/locations/template`,
        {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        }
      );
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `HTTP ${res.status}`);
      }
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'location_import_template.xlsx';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      toast.error(
        `Failed to download template: ${err instanceof Error ? err.message : 'Unknown error'}`
      );
    } finally {
      setIsDownloadingTemplate(false);
    }
  };

  const handleFile = (f: File | null) => {
    if (!f) return;
    const name = f.name.toLowerCase();
    if (!name.endsWith('.xlsx') && !name.endsWith('.xls')) {
      toast.error('Only .xlsx or .xls files are accepted');
      return;
    }
    setFile(f);
    setResult(null);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    handleFile(e.dataTransfer.files[0] ?? null);
  };

  const handleSubmit = async () => {
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await importLocations(formData).unwrap();
      setResult(res.data);

      if (res.data.errorCount === 0) {
        toast.success(
          `Successfully imported ${res.data.successCount} location${res.data.successCount !== 1 ? 's' : ''}`
        );
        onSuccess?.();
      }
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Failed to import locations');
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      reset();
      onClose();
    }
  };

  const hasErrors = result && result.errorCount > 0;
  const hasSuccess = result && result.successCount > 0;

  return (
    <Dialog
      open={open}
      onOpenChange={(o) => {
        if (!o) handleClose();
      }}
    >
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-[680px]'>
        <DialogHeader>
          <DialogTitle className='text-xl font-semibold'>
            Import Locations from Excel
          </DialogTitle>
        </DialogHeader>

        <div className='space-y-5 pt-2'>
          {/* Template download */}
          <div className='flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4'>
            <div className='flex items-center gap-3'>
              <FileSpreadsheet className='h-8 w-8 text-green-600' />
              <div>
                <p className='text-sm font-medium'>Download Template</p>
                <p className='text-xs text-muted-foreground'>
                  Use this template to prepare your location data. The Type
                  column has a dropdown for valid options.
                </p>
              </div>
            </div>
            <Button
              variant='outline'
              size='sm'
              onClick={downloadTemplate}
              disabled={isDownloadingTemplate}
            >
              {isDownloadingTemplate ? (
                <Loader2 className='mr-1.5 h-4 w-4 animate-spin' />
              ) : (
                <Download className='mr-1.5 h-4 w-4' />
              )}
              Template
            </Button>
          </div>

          {/* Upload area */}
          {!result && (
            <div
              className={`relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-10 transition-colors ${
                isDragging
                  ? 'border-primary bg-primary/5'
                  : 'border-border bg-muted/10 hover:border-muted-foreground/40'
              }`}
              onDragOver={(e) => {
                e.preventDefault();
                setIsDragging(true);
              }}
              onDragLeave={() => setIsDragging(false)}
              onDrop={handleDrop}
            >
              {file ? (
                <div className='flex flex-col items-center gap-2'>
                  <FileSpreadsheet className='h-10 w-10 text-green-600' />
                  <p className='text-sm font-medium'>{file.name}</p>
                  <p className='text-xs text-muted-foreground'>
                    {(file.size / 1024).toFixed(1)} KB
                  </p>
                  <Button
                    variant='ghost'
                    size='sm'
                    onClick={() => setFile(null)}
                    disabled={isLoading}
                  >
                    <X className='mr-1 h-3 w-3' />
                    Remove
                  </Button>
                </div>
              ) : (
                <>
                  <Upload className='mb-3 h-10 w-10 text-muted-foreground/60' />
                  <p className='mb-1 text-sm font-medium'>
                    Drag & drop your Excel file here
                  </p>
                  <p className='mb-4 text-xs text-muted-foreground'>
                    or click the button below to browse
                  </p>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <FileSpreadsheet className='mr-1.5 h-4 w-4' />
                    Browse Files
                  </Button>
                  <input
                    ref={fileInputRef}
                    type='file'
                    accept='.xlsx,.xls'
                    className='hidden'
                    onChange={(e) => handleFile(e.target.files?.[0] ?? null)}
                  />
                </>
              )}
            </div>
          )}

          {/* Results */}
          {result && (
            <div className='space-y-4'>
              {/* Summary */}
              <div className='flex items-center gap-4 rounded-lg border border-border p-4'>
                {hasSuccess && (
                  <div className='flex items-center gap-2 text-green-600'>
                    <CheckCircle2 className='h-5 w-5' />
                    <span className='text-sm font-medium'>
                      {result.successCount} created
                    </span>
                  </div>
                )}
                {hasErrors && (
                  <div className='flex items-center gap-2 text-destructive'>
                    <AlertCircle className='h-5 w-5' />
                    <span className='text-sm font-medium'>
                      {result.errorCount} error
                      {result.errorCount !== 1 ? 's' : ''}
                    </span>
                  </div>
                )}
                <span className='text-xs text-muted-foreground'>
                  {result.totalRows} row{result.totalRows !== 1 ? 's' : ''}{' '}
                  processed
                </span>
              </div>

              {/* Error table */}
              {hasErrors && (
                <div className='rounded-lg border border-destructive/30'>
                  <div className='border-b border-destructive/20 bg-destructive/5 px-4 py-2'>
                    <p className='text-sm font-medium text-destructive'>
                      Validation Errors
                    </p>
                  </div>
                  <div className='max-h-60 overflow-y-auto'>
                    <table className='w-full text-sm'>
                      <thead>
                        <tr className='border-b border-border bg-muted/30 text-left'>
                          <th className='px-4 py-2 text-xs font-semibold uppercase text-muted-foreground'>
                            Row
                          </th>
                          <th className='px-4 py-2 text-xs font-semibold uppercase text-muted-foreground'>
                            Field
                          </th>
                          <th className='px-4 py-2 text-xs font-semibold uppercase text-muted-foreground'>
                            Message
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {result.errors.map((err, idx) => (
                          <tr
                            key={idx}
                            className='border-b border-border last:border-0 hover:bg-muted/20'
                          >
                            <td className='px-4 py-2 font-mono text-xs tabular-nums'>
                              {err.row}
                            </td>
                            <td className='px-4 py-2 text-xs font-medium'>
                              {err.field}
                            </td>
                            <td className='px-4 py-2 text-xs text-muted-foreground'>
                              {err.message}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Import another */}
              {!isLoading && (
                <Button
                  variant='outline'
                  size='sm'
                  onClick={reset}
                  className='w-full'
                >
                  Import Another File
                </Button>
              )}
            </div>
          )}
        </div>

        <DialogFooter className='gap-2 pt-2'>
          <Button
            type='button'
            variant='outline'
            onClick={handleClose}
            disabled={isLoading}
          >
            {result ? 'Close' : 'Cancel'}
          </Button>
          {!result && (
            <Button onClick={handleSubmit} disabled={!file || isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Import
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
