/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message item component for discuss module
*/

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
} from '@/shared/components/ui';
import { Edit2, Trash2, Reply, MoreVertical, Check } from 'lucide-react';
import type { Message } from '../types';
import { ReactionPicker } from './ReactionPicker';
import { AttachmentPreview } from './AttachmentPreview';

interface MessageItemProps {
  message: Message;
  isOwn: boolean;
  showAvatar?: boolean;
  isGrouped?: boolean;
  onEdit?: (message: Message) => void;
  onDelete?: (message: Message) => void;
  onReply?: (message: Message) => void;
  onReaction?: (messageId: string, emoji: string) => void;
  onRemoveReaction?: (messageId: string, emoji: string) => void;
}

const formatMessageTime = (timestamp: string) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  });
};

const getUserInitials = (name: string) => {
  return name
    .split(' ')
    .map((word) => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const MessageItem: React.FC<MessageItemProps> = ({
  message,
  isOwn,
  showAvatar = true,
  isGrouped = false,
  onEdit,
  onDelete,
  onReply,
  onReaction,
  onRemoveReaction,
}) => {
  const [isHovered, setIsHovered] = React.useState(false);

  const handleReactionClick = (emoji: string) => {
    // Check if user already reacted with this emoji
    const existingReaction = message.reactions.find((r) => r.emoji === emoji);
    const currentUserId = '1'; // TODO: Get from auth context

    if (existingReaction?.userIds.includes(currentUserId)) {
      onRemoveReaction?.(message.id, emoji);
    } else {
      onReaction?.(message.id, emoji);
    }
  };

  // Get sender info from message.sender (new structure)
  const senderName = message.sender?.name || 'Unknown User';
  const senderAvatar = message.sender?.avatarUrl;

  return (
    <div
      className={cn(
        'group relative flex gap-3 px-6 py-1',
        isGrouped ? 'mt-0.5' : 'mt-4',
        isOwn ? 'flex-row-reverse' : 'flex-row'
      )}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Avatar */}
      <div className='flex-shrink-0'>
        {showAvatar && !isGrouped ? (
          <Avatar className='h-10 w-10 ring-2 ring-white dark:ring-slate-900 shadow-sm'>
            <AvatarImage src={senderAvatar} alt={senderName} />
            <AvatarFallback
              className={cn(
                'text-xs font-semibold',
                isOwn
                  ? 'bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white'
                  : 'bg-gradient-to-br from-slate-200 to-slate-300 dark:from-slate-700 dark:to-slate-600 text-slate-700 dark:text-slate-200'
              )}
            >
              {getUserInitials(senderName)}
            </AvatarFallback>
          </Avatar>
        ) : (
          <div className='h-10 w-10' /> // Spacer for grouped messages
        )}
      </div>

      {/* Message Content */}
      <div
        className={cn(
          'flex flex-col max-w-[70%]',
          isOwn ? 'items-end' : 'items-start'
        )}
      >
        {/* Sender name & time (only if not grouped or not own) */}
        {!isGrouped && !isOwn && (
          <div className='flex items-center gap-2 mb-1 px-1'>
            <span className='text-xs font-bold text-slate-700 dark:text-slate-300'>
              {senderName}
            </span>
            <span className='text-xs text-slate-400 dark:text-slate-500'>
              {formatMessageTime(message.createdAt)}
            </span>
          </div>
        )}

        {/* Message bubble */}
        <div className='relative group/bubble'>
          <div
            className={cn(
              'relative px-4 py-2.5 rounded-2xl transition-all duration-200',
              'shadow-sm',
              isOwn
                ? cn(
                    'bg-gradient-to-br from-violet-500 to-fuchsia-600',
                    'text-white',
                    'rounded-br-md',
                    message.isEdited && 'border-2 border-violet-300/50'
                  )
                : cn(
                    'bg-white dark:bg-slate-800',
                    'text-slate-900 dark:text-slate-100',
                    'border border-slate-200 dark:border-slate-700',
                    'rounded-bl-md',
                    message.isEdited && 'border-slate-300 dark:border-slate-600'
                  )
            )}
          >
            {/* Reply indicator */}
            {message.parentId && (
              <div
                className={cn(
                  'text-xs mb-2 pb-2 border-l-2 pl-2',
                  isOwn
                    ? 'border-white/30 text-white/80'
                    : 'border-slate-300 dark:border-slate-600 text-slate-500 dark:text-slate-400'
                )}
              >
                Replying to a message...
              </div>
            )}

            {/* Content */}
            <p className='text-sm leading-relaxed break-words whitespace-pre-wrap'>
              {message.content}
            </p>

            {/* Attachments */}
            {message.attachments && message.attachments.length > 0 && (
              <div className='mt-3 space-y-2'>
                {message.attachments.map((attachment) => (
                  <AttachmentPreview
                    key={attachment.id}
                    attachment={attachment}
                    allAttachments={message.attachments}
                  />
                ))}
              </div>
            )}

            {/* Metadata */}
            <div
              className={cn(
                'flex items-center gap-2 mt-1.5',
                isOwn ? 'justify-end' : 'justify-start'
              )}
            >
              {message.isEdited && (
                <span
                  className={cn(
                    'text-xs italic',
                    isOwn
                      ? 'text-white/70'
                      : 'text-slate-400 dark:text-slate-500'
                  )}
                >
                  edited
                </span>
              )}

              {isOwn && (
                <span className='text-xs text-white/90'>
                  {formatMessageTime(message.createdAt)}
                </span>
              )}

              {/* Read receipts (only for own messages) */}
              {isOwn && <Check className='h-3.5 w-3.5 text-white/70' />}
            </div>
          </div>

          {/* Reactions */}
          {message.reactions && message.reactions.length > 0 && (
            <div className='absolute -bottom-3 left-4 flex gap-1 flex-wrap'>
              {message.reactions.map((reaction, idx) => {
                const currentUserId = '1'; // TODO: Get from auth context
                const isUserReacted = reaction.userIds.includes(currentUserId);

                return (
                  <button
                    key={idx}
                    onClick={() => handleReactionClick(reaction.emoji)}
                    className={cn(
                      'h-6 px-2 text-xs rounded-full transition-all duration-200',
                      'border shadow-sm hover:shadow-md',
                      'flex items-center gap-1',
                      isUserReacted
                        ? 'bg-gradient-to-r from-violet-100 to-fuchsia-100 dark:from-violet-900/50 dark:to-fuchsia-900/50 border-violet-300 dark:border-violet-700 scale-105'
                        : 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:border-violet-300 dark:hover:border-violet-700'
                    )}
                  >
                    <span>{reaction.emoji}</span>
                    <span
                      className={cn(
                        'font-semibold',
                        isUserReacted
                          ? 'text-violet-700 dark:text-violet-300'
                          : 'text-slate-600 dark:text-slate-400'
                      )}
                    >
                      {reaction.count}
                    </span>
                  </button>
                );
              })}
            </div>
          )}

          {/* Action buttons (on hover) */}
          {isHovered && (
            <div
              className={cn(
                'absolute top-0 flex items-center gap-1 opacity-0 group-hover/bubble:opacity-100 transition-opacity',
                isOwn ? 'right-full mr-2' : 'left-full ml-2'
              )}
            >
              <ReactionPicker
                onReactionSelect={(emoji) => onReaction?.(message.id, emoji)}
                existingReactions={message.reactions.map((r) => r.emoji)}
              />

              <button
                onClick={() => onReply?.(message)}
                className='p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-md hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors'
                title='Reply'
              >
                <Reply className='h-3.5 w-3.5 text-slate-600 dark:text-slate-400' />
              </button>

              {isOwn && (
                <>
                  <button
                    onClick={() => onEdit?.(message)}
                    className='p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-md hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors'
                    title='Edit'
                  >
                    <Edit2 className='h-3.5 w-3.5 text-slate-600 dark:text-slate-400' />
                  </button>
                  <button
                    onClick={() => onDelete?.(message)}
                    className='p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-md hover:bg-rose-50 dark:hover:bg-rose-900/20 transition-colors'
                    title='Delete'
                  >
                    <Trash2 className='h-3.5 w-3.5 text-rose-600 dark:text-rose-400' />
                  </button>
                </>
              )}

              <button
                className='p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-md hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors'
                title='More actions'
              >
                <MoreVertical className='h-3.5 w-3.5 text-slate-600 dark:text-slate-400' />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
