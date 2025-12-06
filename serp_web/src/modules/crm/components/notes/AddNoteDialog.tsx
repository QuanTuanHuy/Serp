/*
Author: QuanTuanHuy
Description: Part of Serp Project - Add Note Dialog Component for CRM
*/

'use client';

import { useState, useRef } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Button,
  Avatar,
  AvatarFallback,
} from '@/shared/components/ui';
import {
  Send,
  Paperclip,
  ImageIcon,
  AtSign,
  Smile,
  X,
  Loader2,
} from 'lucide-react';
import { cn } from '@/shared/utils';

interface AddNoteDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (content: string, attachments?: File[]) => void;
  placeholder?: string;
  title?: string;
  description?: string;
  isLoading?: boolean;
}

export const AddNoteDialog: React.FC<AddNoteDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  placeholder = 'Enter note content...',
  title = 'Add New Note',
  description = 'Note will be saved and displayed in timeline',
  isLoading = false,
}) => {
  const [content, setContent] = useState('');
  const [attachments, setAttachments] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = () => {
    if (content.trim()) {
      onSubmit(content, attachments.length > 0 ? attachments : undefined);
      setContent('');
      setAttachments([]);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    setAttachments((prev) => [...prev, ...files]);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const removeAttachment = (index: number) => {
    setAttachments((prev) => prev.filter((_, i) => i !== index));
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-lg'>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <div className='space-y-4 py-4'>
          {/* User Info */}
          <div className='flex items-center gap-3'>
            <Avatar className='h-9 w-9'>
              <AvatarFallback className='bg-primary/10 text-primary text-xs'>
                NV
              </AvatarFallback>
            </Avatar>
            <div>
              <p className='text-sm font-medium'>Nguyễn Văn A</p>
              <p className='text-xs text-muted-foreground'>Đang viết...</p>
            </div>
          </div>

          {/* Content Textarea */}
          <div className='relative'>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              onKeyDown={handleKeyDown}
              className='w-full min-h-[150px] p-3 border rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-primary bg-background text-foreground'
              placeholder={placeholder}
              autoFocus
            />
            <div className='absolute bottom-2 right-2 text-xs text-muted-foreground'>
              {content.length} ký tự
            </div>
          </div>

          {/* Attachments Preview */}
          {attachments.length > 0 && (
            <div className='space-y-2'>
              <p className='text-sm font-medium'>
                Tệp đính kèm ({attachments.length})
              </p>
              <div className='flex flex-wrap gap-2'>
                {attachments.map((file, index) => (
                  <div
                    key={index}
                    className='flex items-center gap-2 px-3 py-1.5 bg-muted rounded-md text-sm'
                  >
                    <span className='truncate max-w-[150px]'>{file.name}</span>
                    <span className='text-muted-foreground text-xs'>
                      {formatFileSize(file.size)}
                    </span>
                    <button
                      onClick={() => removeAttachment(index)}
                      className='hover:text-red-500 transition-colors'
                    >
                      <X className='h-3 w-3' />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Toolbar */}
          <div className='flex items-center justify-between border-t pt-3'>
            <div className='flex items-center gap-1'>
              <input
                ref={fileInputRef}
                type='file'
                multiple
                className='hidden'
                onChange={handleFileSelect}
              />
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8'
                onClick={() => fileInputRef.current?.click()}
                title='Đính kèm tệp'
              >
                <Paperclip className='h-4 w-4' />
              </Button>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8'
                onClick={() => {
                  if (fileInputRef.current) {
                    fileInputRef.current.accept = 'image/*';
                    fileInputRef.current.click();
                    fileInputRef.current.accept = '';
                  }
                }}
                title='Add image'
              >
                <ImageIcon className='h-4 w-4' />
              </Button>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8'
                onClick={() => setContent((prev) => prev + '@')}
                title='Mention user'
              >
                <AtSign className='h-4 w-4' />
              </Button>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8'
                title='Add emoji'
              >
                <Smile className='h-4 w-4' />
              </Button>
            </div>

            <p className='text-xs text-muted-foreground'>
              Press Ctrl+Enter to send
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => {
              onOpenChange(false);
              setContent('');
              setAttachments([]);
            }}
          >
            Hủy
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={!content.trim() || isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                Đang lưu...
              </>
            ) : (
              <>
                <Send className='h-4 w-4 mr-2' />
                Lưu ghi chú
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default AddNoteDialog;
