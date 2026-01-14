/*
Author: QuanTuanHuy
Description: Part of Serp Project - Attachment preview with lightbox and download
*/

'use client';

import React, { useState } from 'react';
import {
  Download,
  X,
  FileText,
  Archive,
  File,
  ZoomIn,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Dialog, DialogContent } from '@/shared/components/ui/dialog';
import type { Attachment } from '../types';

interface AttachmentPreviewProps {
  attachment: Attachment;
  allAttachments?: Attachment[]; // For gallery navigation
  className?: string;
}

// File type detection
const isImage = (fileType: string) => fileType.startsWith('image/');
const isPDF = (fileType: string) => fileType.includes('pdf');

// File icon component
const getFileIcon = (fileType: string) => {
  if (isPDF(fileType)) return FileText;
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

export function AttachmentPreview({
  attachment,
  allAttachments,
  className = '',
}: AttachmentPreviewProps) {
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);

  const imageAttachments = allAttachments?.filter((att) =>
    isImage(att.fileType)
  );
  const currentImageIndex = imageAttachments?.findIndex(
    (att) => att.id === attachment.id
  );

  const handleDownload = () => {
    // In real app, this would download from S3 using presigned URL
    const link = document.createElement('a');
    link.href = attachment.downloadUrl;
    link.download = attachment.fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handlePrevImage = () => {
    if (!imageAttachments || currentImageIndex === undefined) return;
    const newIndex =
      (currentImageIndex - 1 + imageAttachments.length) %
      imageAttachments.length;
    setCurrentIndex(newIndex);
  };

  const handleNextImage = () => {
    if (!imageAttachments || currentImageIndex === undefined) return;
    const newIndex = (currentImageIndex + 1) % imageAttachments.length;
    setCurrentIndex(newIndex);
  };

  const displayAttachment =
    imageAttachments && currentImageIndex !== undefined
      ? imageAttachments[currentIndex]
      : attachment;

  // Image preview
  if (isImage(attachment.fileType)) {
    return (
      <>
        <div
          className={`group relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 hover:scale-[1.02] ${className}`}
          onClick={() => setIsLightboxOpen(true)}
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={attachment.thumbnailUrl || attachment.downloadUrl}
            alt={attachment.fileName}
            className='w-full h-auto max-h-80 object-cover'
          />

          {/* Overlay on hover */}
          <div className='absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300'>
            <div className='absolute bottom-3 left-3 right-3 flex items-center justify-between text-white'>
              <div className='flex-1 min-w-0'>
                <p className='text-sm font-semibold truncate'>
                  {attachment.fileName}
                </p>
                <p className='text-xs opacity-90'>
                  {formatFileSize(attachment.fileSize)}
                </p>
              </div>

              <div className='flex items-center gap-2 ml-3'>
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-8 w-8 bg-white/20 hover:bg-white/30 backdrop-blur-sm'
                  onClick={(e) => {
                    e.stopPropagation();
                    setIsLightboxOpen(true);
                  }}
                >
                  <ZoomIn className='h-4 w-4' />
                </Button>
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-8 w-8 bg-white/20 hover:bg-white/30 backdrop-blur-sm'
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDownload();
                  }}
                >
                  <Download className='h-4 w-4' />
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* Lightbox Dialog */}
        <Dialog open={isLightboxOpen} onOpenChange={setIsLightboxOpen}>
          <DialogContent className='max-w-7xl w-full h-[90vh] p-0 bg-black/95 border-none'>
            <div className='relative w-full h-full flex items-center justify-center'>
              {/* Close Button */}
              <Button
                variant='ghost'
                size='icon'
                className='absolute top-4 right-4 z-10 h-10 w-10 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white'
                onClick={() => setIsLightboxOpen(false)}
              >
                <X className='h-5 w-5' />
              </Button>

              {/* Download Button */}
              <Button
                variant='ghost'
                size='icon'
                className='absolute top-4 right-16 z-10 h-10 w-10 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white'
                onClick={handleDownload}
              >
                <Download className='h-5 w-5' />
              </Button>

              {/* Navigation Buttons (if multiple images) */}
              {imageAttachments && imageAttachments.length > 1 && (
                <>
                  <Button
                    variant='ghost'
                    size='icon'
                    className='absolute left-4 z-10 h-12 w-12 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white'
                    onClick={handlePrevImage}
                  >
                    <ChevronLeft className='h-6 w-6' />
                  </Button>
                  <Button
                    variant='ghost'
                    size='icon'
                    className='absolute right-4 z-10 h-12 w-12 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white'
                    onClick={handleNextImage}
                  >
                    <ChevronRight className='h-6 w-6' />
                  </Button>
                </>
              )}

              {/* Image */}
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={displayAttachment.downloadUrl}
                alt={displayAttachment.fileName}
                className='max-w-full max-h-full object-contain'
              />

              {/* Image Info */}
              <div className='absolute bottom-6 left-1/2 -translate-x-1/2 px-6 py-3 bg-white/10 backdrop-blur-md rounded-full border border-white/20'>
                <p className='text-sm font-medium text-white text-center'>
                  {displayAttachment.fileName} •{' '}
                  {formatFileSize(displayAttachment.fileSize)}
                  {imageAttachments && imageAttachments.length > 1 && (
                    <span className='ml-2 opacity-70'>
                      ({currentIndex + 1} / {imageAttachments.length})
                    </span>
                  )}
                </p>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </>
    );
  }

  // Non-image file preview (PDF, documents, etc.)
  const FileIcon = getFileIcon(attachment.fileType);

  return (
    <div
      className={`group relative p-4 rounded-xl border-2 border-gray-200 dark:border-gray-700 hover:border-violet-300 dark:hover:border-violet-700 bg-white dark:bg-gray-900 transition-all duration-300 hover:shadow-lg ${className}`}
    >
      <div className='flex items-center gap-3'>
        {/* File Icon */}
        <div className='w-12 h-12 rounded-lg bg-gradient-to-br from-violet-100 to-fuchsia-100 dark:from-violet-900/30 dark:to-fuchsia-900/30 flex items-center justify-center flex-shrink-0'>
          <FileIcon className='w-6 h-6 text-violet-600 dark:text-violet-400' />
        </div>

        {/* File Info */}
        <div className='flex-1 min-w-0'>
          <p className='text-sm font-semibold text-gray-900 dark:text-gray-100 truncate'>
            {attachment.fileName}
          </p>
          <p className='text-xs text-gray-500 dark:text-gray-400'>
            {formatFileSize(attachment.fileSize)}
          </p>
        </div>

        {/* Download Button */}
        <Button
          variant='ghost'
          size='icon'
          className='h-9 w-9 opacity-0 group-hover:opacity-100 transition-opacity'
          onClick={handleDownload}
        >
          <Download className='h-4 w-4' />
        </Button>
      </div>

      {/* PDF Preview (optional enhancement) */}
      {isPDF(attachment.fileType) && (
        <div className='mt-3 pt-3 border-t border-gray-200 dark:border-gray-700'>
          <Button
            variant='outline'
            size='sm'
            className='w-full text-xs'
            onClick={handleDownload}
          >
            <FileText className='h-3.5 w-3.5 mr-1.5' />
            Open PDF
          </Button>
        </div>
      )}
    </div>
  );
}
