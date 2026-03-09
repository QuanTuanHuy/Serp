/*
Author: QuanTuanHuy
Description: Part of Serp Project - File attachment uploader with drag & drop
NOTE: This component is currently not used. Files are now uploaded inline with messages via sendMessageWithFiles API.
*/

'use client';

import React, { useCallback, useState } from 'react';
import { Upload, X, File, Image, FileText, Archive } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
// import { useUploadAttachmentMutation } from '../api/discussApi'; // Removed - use sendMessageWithFiles instead
import type { Attachment } from '../types';

interface AttachmentUploaderProps {
  channelId: string;
  onUploadComplete?: (attachment: Attachment) => void;
  onUploadError?: (error: string) => void;
  maxSizeMB?: number;
  acceptedTypes?: string[];
  className?: string;
}

interface UploadingFile {
  file: File;
  progress: number;
  status: 'uploading' | 'success' | 'error';
  error?: string;
  attachment?: Attachment;
}

// File type icons
const getFileIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) return Image;
  if (fileType.includes('pdf') || fileType.includes('document'))
    return FileText;
  if (fileType.includes('zip') || fileType.includes('archive')) return Archive;
  return File;
};

// File size formatter
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};

export function AttachmentUploader({
  channelId,
  onUploadComplete,
  onUploadError,
  maxSizeMB = 100,
  acceptedTypes = [
    'image/*',
    'application/pdf',
    '.zip',
    '.doc',
    '.docx',
    '.txt',
  ],
  className = '',
}: AttachmentUploaderProps) {
  // const [uploadAttachment] = useUploadAttachmentMutation(); // Not available anymore
  const [isDragging, setIsDragging] = useState(false);
  const [uploadingFiles, setUploadingFiles] = useState<
    Map<string, UploadingFile>
  >(new Map());

  const validateFile = (file: File): string | null => {
    // Size validation
    const maxSize = maxSizeMB * 1024 * 1024;
    if (file.size > maxSize) {
      return `File size exceeds ${maxSizeMB}MB limit`;
    }

    // Type validation (basic - in real app, check MIME type properly)
    const isValid = acceptedTypes.some((type) => {
      if (type === 'image/*') return file.type.startsWith('image/');
      if (type.startsWith('.')) return file.name.toLowerCase().endsWith(type);
      return file.type === type;
    });

    if (!isValid) {
      return 'File type not supported';
    }

    return null;
  };

  const uploadFile = async (file: File) => {
    const fileId = `${file.name}-${Date.now()}`;
    const error = validateFile(file);

    if (error) {
      setUploadingFiles((prev) => {
        const updated = new Map(prev);
        updated.set(fileId, {
          file,
          progress: 0,
          status: 'error',
          error,
        });
        return updated;
      });
      onUploadError?.(error);
      return;
    }

    // Add to uploading list
    setUploadingFiles((prev) => {
      const updated = new Map(prev);
      updated.set(fileId, {
        file,
        progress: 0,
        status: 'uploading',
      });
      return updated;
    });

    try {
      // TODO: Implement with sendMessageWithFiles mutation instead
      // Simulate upload progress (in real app, use XHR or fetch with progress)
      const progressInterval = setInterval(() => {
        setUploadingFiles((prev) => {
          const updated = new Map(prev);
          const current = updated.get(fileId);
          if (current && current.progress < 90) {
            updated.set(fileId, {
              ...current,
              progress: current.progress + 10,
            });
          }
          return updated;
        });
      }, 200);

      // const result = await uploadAttachment({ file, channelId }).unwrap();
      // Placeholder: simulate success after 2 seconds
      await new Promise((resolve) => setTimeout(resolve, 2000));

      clearInterval(progressInterval);

      // Mock success
      const mockAttachment: Attachment = {
        id: String(Date.now()),
        messageId: '',
        fileName: file.name,
        fileType: file.type,
        fileSize: file.size,
        s3Key: '',
        s3Bucket: '',
        downloadUrl: '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setUploadingFiles((prev) => {
        const updated = new Map(prev);
        updated.set(fileId, {
          file,
          progress: 100,
          status: 'success',
          attachment: mockAttachment,
        });
        return updated;
      });
      onUploadComplete?.(mockAttachment);

      // Remove from list after 2 seconds
      setTimeout(() => {
        setUploadingFiles((prev) => {
          const updated = new Map(prev);
          updated.delete(fileId);
          return updated;
        });
      }, 2000);
    } catch (err: any) {
      const errorMsg = err.message || 'Upload failed';
      setUploadingFiles((prev) => {
        const updated = new Map(prev);
        updated.set(fileId, {
          file,
          progress: 0,
          status: 'error',
          error: errorMsg,
        });
        return updated;
      });
      onUploadError?.(errorMsg);
    }
  };

  const handleFiles = (files: FileList | null) => {
    if (!files) return;
    Array.from(files).forEach((file) => uploadFile(file));
  };

  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    handleFiles(e.dataTransfer.files);
  }, []);

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files);
    e.target.value = ''; // Reset input
  };

  const removeFile = (fileId: string) => {
    setUploadingFiles((prev) => {
      const updated = new Map(prev);
      updated.delete(fileId);
      return updated;
    });
  };

  return (
    <div className={className}>
      {/* Upload Zone */}
      <div
        onDragEnter={handleDragEnter}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`
          relative border-2 border-dashed rounded-2xl transition-all duration-300
          ${
            isDragging
              ? 'border-violet-500 bg-gradient-to-br from-violet-50 to-fuchsia-50 dark:from-violet-950/30 dark:to-fuchsia-950/30 scale-[1.02]'
              : 'border-gray-200 dark:border-gray-700 hover:border-violet-300 dark:hover:border-violet-700'
          }
        `}
      >
        <label
          htmlFor='file-upload'
          className='flex flex-col items-center justify-center p-8 cursor-pointer'
        >
          <div
            className={`
            w-16 h-16 rounded-full mb-4 flex items-center justify-center
            bg-gradient-to-br from-violet-100 to-fuchsia-100
            dark:from-violet-900/30 dark:to-fuchsia-900/30
            transition-all duration-300
            ${isDragging ? 'scale-110' : 'hover:scale-105'}
          `}
          >
            <Upload
              className={`w-8 h-8 transition-colors ${isDragging ? 'text-violet-600 dark:text-violet-400' : 'text-gray-500 dark:text-gray-400'}`}
            />
          </div>

          <h3 className='text-lg font-bold bg-gradient-to-r from-violet-600 to-fuchsia-600 dark:from-violet-400 dark:to-fuchsia-400 bg-clip-text text-transparent mb-2'>
            Drop files here or click to browse
          </h3>

          <p className='text-sm text-gray-500 dark:text-gray-400 text-center'>
            Supports images, PDFs, documents up to {maxSizeMB}MB
          </p>

          <input
            id='file-upload'
            type='file'
            multiple
            accept={acceptedTypes.join(',')}
            onChange={handleFileInput}
            className='hidden'
          />
        </label>
      </div>

      {/* Uploading Files List */}
      {uploadingFiles.size > 0 && (
        <div className='mt-4 space-y-3'>
          {Array.from(uploadingFiles.entries()).map(([fileId, upload]) => {
            const FileIcon = getFileIcon(upload.file.type);

            return (
              <div
                key={fileId}
                className={`
                  group relative p-4 rounded-xl border-2 transition-all duration-300
                  ${
                    upload.status === 'success'
                      ? 'border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-950/20'
                      : upload.status === 'error'
                        ? 'border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-950/20'
                        : 'border-violet-200 dark:border-violet-800 bg-white dark:bg-gray-900'
                  }
                `}
              >
                <div className='flex items-start gap-3'>
                  {/* File Icon */}
                  <div
                    className={`
                    w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0
                    ${
                      upload.status === 'success'
                        ? 'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400'
                        : upload.status === 'error'
                          ? 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400'
                          : 'bg-violet-100 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400'
                    }
                  `}
                  >
                    <FileIcon className='w-5 h-5' />
                  </div>

                  {/* File Info */}
                  <div className='flex-1 min-w-0'>
                    <div className='flex items-center justify-between mb-1'>
                      <p className='text-sm font-semibold text-gray-900 dark:text-gray-100 truncate'>
                        {upload.file.name}
                      </p>
                      {upload.status !== 'uploading' && (
                        <Button
                          variant='ghost'
                          size='icon'
                          className='h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity'
                          onClick={() => removeFile(fileId)}
                        >
                          <X className='h-4 w-4' />
                        </Button>
                      )}
                    </div>

                    <p className='text-xs text-gray-500 dark:text-gray-400 mb-2'>
                      {formatFileSize(upload.file.size)}
                      {upload.status === 'success' && ' • Uploaded'}
                      {upload.status === 'error' && ` • ${upload.error}`}
                    </p>

                    {/* Progress Bar */}
                    {upload.status === 'uploading' && (
                      <div className='w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden'>
                        <div
                          className='h-full bg-gradient-to-r from-violet-500 to-fuchsia-600 transition-all duration-300 ease-out rounded-full'
                          style={{ width: `${upload.progress}%` }}
                        />
                      </div>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
