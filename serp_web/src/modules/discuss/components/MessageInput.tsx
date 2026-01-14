/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message input component with rich text support
*/

'use client';

import React, { useState, useRef, KeyboardEvent } from 'react';
import { cn } from '@/shared/utils';
import { Button, Textarea } from '@/shared/components/ui';
import {
  Send,
  Paperclip,
  AtSign,
  Bold,
  Italic,
  Code,
  X,
  File as FileIcon,
} from 'lucide-react';
import type { Message, Attachment } from '../types';
import { EmojiPicker } from './EmojiPicker';
import { useSendMessageWithFilesMutation } from '../api/discussApi';

interface MessageInputProps {
  channelId: string;
  onSendMessage: (content: string, attachments?: Attachment[]) => void;
  replyingTo?: Message | null;
  onCancelReply?: () => void;
  editingMessage?: Message | null;
  onCancelEdit?: () => void;
  placeholder?: string;
  className?: string;
}

export const MessageInput: React.FC<MessageInputProps> = ({
  channelId,
  onSendMessage,
  replyingTo,
  onCancelReply,
  editingMessage,
  onCancelEdit,
  placeholder = 'Type a message...',
  className,
}) => {
  const [content, setContent] = useState(editingMessage?.content || '');
  const [isFocused, setIsFocused] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const [files, setFiles] = useState<File[]>([]);
  const [sendMessageWithFiles, { isLoading: isUploading }] =
    useSendMessageWithFilesMutation();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const dropZoneRef = useRef<HTMLDivElement>(null);

  // Update content when editing message changes
  React.useEffect(() => {
    if (editingMessage) {
      setContent(editingMessage.content);
      textareaRef.current?.focus();
    }
  }, [editingMessage]);

  const handleSend = () => {
    const trimmedContent = content.trim();
    if (!trimmedContent && files.length === 0) return;

    // Send message (will call sendMessage or sendMessageWithFiles depending on files)
    onSendMessage(trimmedContent, files as any); // Pass files instead of attachments
    setContent('');
    setFiles([]);

    // Reset height
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = e.target.files;
    if (!selectedFiles || selectedFiles.length === 0) return;

    // Just store files - they'll be uploaded when sending the message
    setFiles((prev) => [...prev, ...Array.from(selectedFiles)]);

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  // Drag and drop handlers
  const handleDragEnter = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();

    // Only set dragging false if leaving the drop zone entirely
    if (
      dropZoneRef.current &&
      !dropZoneRef.current.contains(e.relatedTarget as Node)
    ) {
      setIsDragging(false);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const droppedFiles = Array.from(e.dataTransfer.files);
    if (droppedFiles.length > 0) {
      setFiles((prev) => [...prev, ...droppedFiles]);
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    // Send on Enter (without Shift)
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }

    // Cancel edit/reply on Escape
    if (e.key === 'Escape') {
      if (editingMessage && onCancelEdit) {
        onCancelEdit();
        setContent('');
      } else if (replyingTo && onCancelReply) {
        onCancelReply();
      }
    }
  };

  const handleTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setContent(e.target.value);

    // Auto-resize textarea
    e.target.style.height = 'auto';
    e.target.style.height = `${Math.min(e.target.scrollHeight, 200)}px`;
  };

  const insertFormatting = (prefix: string, suffix: string = '') => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = content.substring(start, end);
    const newText =
      content.substring(0, start) +
      prefix +
      selectedText +
      (suffix || prefix) +
      content.substring(end);

    setContent(newText);

    // Restore cursor position
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + prefix.length, end + prefix.length);
    }, 0);
  };

  const handleEmojiSelect = (emoji: string) => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const newText =
      content.substring(0, start) + emoji + content.substring(end);

    setContent(newText);

    // Restore focus and cursor position
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + emoji.length, start + emoji.length);
    }, 0);
  };

  const canSend = content.trim().length > 0 || files.length > 0;

  return (
    <div
      className={cn(
        'border-t border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900',
        className
      )}
    >
      {/* Reply/Edit indicator */}
      {(replyingTo || editingMessage) && (
        <div className='px-6 pt-3 pb-2'>
          <div className='flex items-center justify-between px-3 py-2 bg-slate-100 dark:bg-slate-800 rounded-lg border-l-4 border-violet-500'>
            <div className='flex items-center gap-2'>
              <div className='text-xs font-semibold text-violet-600 dark:text-violet-400'>
                {editingMessage
                  ? 'Editing message'
                  : `Replying to ${replyingTo?.sender?.name || 'Unknown'}`}
              </div>
              {replyingTo && (
                <div className='text-xs text-slate-500 dark:text-slate-400 truncate max-w-md'>
                  {replyingTo.content}
                </div>
              )}
            </div>
            <button
              onClick={editingMessage ? onCancelEdit : onCancelReply}
              className='p-1 hover:bg-slate-200 dark:hover:bg-slate-700 rounded transition-colors'
            >
              <X className='h-4 w-4 text-slate-500 dark:text-slate-400' />
            </button>
          </div>
        </div>
      )}

      {/* Input area */}
      <div className='px-6 py-4'>
        <div
          ref={dropZoneRef}
          onDragEnter={handleDragEnter}
          onDragLeave={handleDragLeave}
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          className={cn(
            'relative flex flex-col gap-3 px-4 py-3 rounded-2xl transition-all duration-200',
            'bg-slate-50 dark:bg-slate-800',
            isDragging
              ? 'ring-2 ring-violet-500 bg-violet-50 dark:bg-violet-900/20 scale-[1.02]'
              : isFocused
                ? 'ring-2 ring-violet-500 bg-white dark:bg-slate-800/80'
                : 'ring-1 ring-slate-200 dark:ring-slate-700'
          )}
        >
          {/* Drag overlay */}
          {isDragging && (
            <div className='absolute inset-0 z-10 flex items-center justify-center bg-violet-100/80 dark:bg-violet-900/40 rounded-2xl backdrop-blur-sm'>
              <div className='text-center'>
                <Paperclip className='h-12 w-12 text-violet-600 dark:text-violet-400 mx-auto mb-2 animate-bounce' />
                <p className='text-sm font-semibold text-violet-700 dark:text-violet-300'>
                  Drop files here
                </p>
              </div>
            </div>
          )}
          {/* Formatting toolbar */}
          <div className='flex items-center gap-1 pb-2 border-b border-slate-200 dark:border-slate-700'>
            <button
              onClick={() => insertFormatting('**')}
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
              title='Bold (Ctrl+B)'
            >
              <Bold className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>
            <button
              onClick={() => insertFormatting('*')}
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
              title='Italic (Ctrl+I)'
            >
              <Italic className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>
            <button
              onClick={() => insertFormatting('`')}
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
              title='Code'
            >
              <Code className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>

            <div className='flex-1' />

            <button
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
              title='Mention (@)'
            >
              <AtSign className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>
            <EmojiPicker
              onEmojiSelect={handleEmojiSelect}
              triggerClassName='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
            />
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors disabled:opacity-50'
              title='Attach file'
            >
              <Paperclip className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>
            <input
              ref={fileInputRef}
              type='file'
              multiple
              onChange={handleFileSelect}
              className='hidden'
              accept='image/*,.pdf,.doc,.docx,.zip'
            />
          </div>

          {/* File previews */}
          {files.length > 0 && (
            <div className='space-y-2 pb-2 border-b border-slate-200 dark:border-slate-700'>
              <div className='flex items-center justify-between'>
                <p className='text-xs font-semibold text-slate-600 dark:text-slate-400'>
                  {files.length} {files.length === 1 ? 'file' : 'files'}{' '}
                  attached
                </p>
                {isUploading && (
                  <p className='text-xs text-violet-600 dark:text-violet-400'>
                    Uploading...
                  </p>
                )}
              </div>
              <div className='flex flex-wrap gap-2'>
                {files.map((file, index) => {
                  const isImage = file.type.startsWith('image/');
                  return (
                    <div
                      key={index}
                      className='group relative flex items-center gap-2 px-3 py-2 bg-violet-50 dark:bg-violet-950/30 rounded-lg border border-violet-200 dark:border-violet-800 hover:bg-violet-100 dark:hover:bg-violet-900/40 transition-colors'
                    >
                      {isImage ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={URL.createObjectURL(file)}
                          alt={file.name}
                          className='w-10 h-10 object-cover rounded'
                          onLoad={(e) => {
                            // Clean up object URL after image loads
                            const img = e.target as HTMLImageElement;
                            setTimeout(() => URL.revokeObjectURL(img.src), 100);
                          }}
                        />
                      ) : (
                        <div className='w-10 h-10 bg-violet-100 dark:bg-violet-900/50 rounded flex items-center justify-center'>
                          <FileIcon className='w-5 h-5 text-violet-600 dark:text-violet-400' />
                        </div>
                      )}
                      <div className='flex-1 min-w-0'>
                        <p
                          className='text-xs font-medium text-slate-700 dark:text-slate-300 truncate max-w-[120px]'
                          title={file.name}
                        >
                          {file.name}
                        </p>
                        <p className='text-xs text-slate-500 dark:text-slate-400'>
                          {formatFileSize(file.size)}
                        </p>
                      </div>
                      <button
                        onClick={() => handleRemoveFile(index)}
                        className='p-1 hover:bg-violet-200 dark:hover:bg-violet-900 rounded transition-colors opacity-0 group-hover:opacity-100'
                        title='Remove file'
                      >
                        <X className='h-3.5 w-3.5 text-slate-500 dark:text-slate-400' />
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Text input */}
          <div className='flex items-end gap-3'>
            <Textarea
              ref={textareaRef}
              value={content}
              onChange={handleTextareaChange}
              onKeyDown={handleKeyDown}
              onFocus={() => setIsFocused(true)}
              onBlur={() => setIsFocused(false)}
              placeholder={placeholder}
              className={cn(
                'min-h-[44px] max-h-[200px] resize-none',
                'bg-transparent border-none',
                'text-sm leading-relaxed',
                'placeholder:text-slate-400 dark:placeholder:text-slate-500',
                'focus-visible:ring-0 focus-visible:ring-offset-0',
                'p-0'
              )}
              rows={1}
            />

            {/* Send button */}
            <Button
              onClick={handleSend}
              disabled={!canSend}
              size='sm'
              className={cn(
                'h-9 px-4 flex-shrink-0 transition-all duration-200',
                canSend
                  ? 'bg-gradient-to-r from-violet-500 to-fuchsia-600 hover:from-violet-600 hover:to-fuchsia-700 text-white shadow-md shadow-violet-500/25'
                  : 'bg-slate-200 dark:bg-slate-700 text-slate-400 dark:text-slate-500 cursor-not-allowed'
              )}
            >
              <Send className='h-4 w-4 mr-1.5' />
              Send
            </Button>
          </div>

          {/* Hint text */}
          <div className='text-xs text-slate-400 dark:text-slate-500'>
            <span className='font-medium'>Enter</span> to send,{' '}
            <span className='font-medium'>Shift + Enter</span> for new line
          </div>
        </div>
      </div>
    </div>
  );
};
