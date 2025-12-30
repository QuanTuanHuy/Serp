/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message input component with rich text support
*/

'use client';

import React, { useState, useRef, KeyboardEvent } from 'react';
import { cn } from '@/shared/utils';
import { Button, Textarea } from '@/shared/components/ui';
import { Send, Paperclip, AtSign, Bold, Italic, Code, X } from 'lucide-react';
import type { Message } from '../types';
import { EmojiPicker } from './EmojiPicker';

interface MessageInputProps {
  channelId: string;
  onSendMessage: (content: string) => void;
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
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Update content when editing message changes
  React.useEffect(() => {
    if (editingMessage) {
      setContent(editingMessage.content);
      textareaRef.current?.focus();
    }
  }, [editingMessage]);

  const handleSend = () => {
    const trimmedContent = content.trim();
    if (!trimmedContent) return;

    onSendMessage(trimmedContent);
    setContent('');

    // Reset height
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
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

  const canSend = content.trim().length > 0;

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
                  : `Replying to ${replyingTo?.userName}`}
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
          className={cn(
            'relative flex flex-col gap-3 px-4 py-3 rounded-2xl transition-all duration-200',
            'bg-slate-50 dark:bg-slate-800',
            isFocused
              ? 'ring-2 ring-violet-500 bg-white dark:bg-slate-800/80'
              : 'ring-1 ring-slate-200 dark:ring-slate-700'
          )}
        >
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
              className='p-1.5 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors'
              title='Attach file'
            >
              <Paperclip className='h-4 w-4 text-slate-600 dark:text-slate-400' />
            </button>
          </div>

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
