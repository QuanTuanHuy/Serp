/*
Author: QuanTuanHuy
Description: Part of Serp Project - Note Card Component for CRM
*/

'use client';

import {
  Card,
  CardContent,
  Button,
  Avatar,
  AvatarFallback,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { MoreVertical, Edit, Trash2, Pin, Copy, Reply } from 'lucide-react';
import { cn } from '@/shared/utils';

export interface Note {
  id: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  createdBy: {
    id: string;
    name: string;
    avatar?: string;
  };
  isPinned?: boolean;
  mentions?: string[];
  attachments?: {
    id: string;
    name: string;
    url: string;
    type: string;
    size: string;
  }[];
}

interface NoteCardProps {
  note: Note;
  onEdit?: (note: Note) => void;
  onDelete?: (note: Note) => void;
  onPin?: (note: Note) => void;
  onReply?: (note: Note) => void;
  currentUserId?: string;
  className?: string;
}

export const NoteCard: React.FC<NoteCardProps> = ({
  note,
  onEdit,
  onDelete,
  onPin,
  onReply,
  currentUserId,
  className,
}) => {
  const isOwner = currentUserId === note.createdBy.id;
  const initials = note.createdBy.name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2);

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;

    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const copyContent = () => {
    navigator.clipboard.writeText(note.content);
  };

  return (
    <Card
      className={cn(
        'transition-shadow hover:shadow-md',
        note.isPinned && 'border-primary/50 bg-primary/5',
        className
      )}
    >
      <CardContent className='p-4'>
        <div className='flex items-start gap-3'>
          {/* Avatar */}
          <Avatar className='h-9 w-9'>
            {note.createdBy.avatar ? (
              <img src={note.createdBy.avatar} alt={note.createdBy.name} />
            ) : (
              <AvatarFallback className='bg-primary/10 text-primary text-xs'>
                {initials}
              </AvatarFallback>
            )}
          </Avatar>

          {/* Content */}
          <div className='flex-1 min-w-0'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <span className='font-medium text-sm'>
                  {note.createdBy.name}
                </span>
                <span className='text-xs text-muted-foreground'>
                  {formatDateTime(note.createdAt)}
                </span>
                {note.isPinned && (
                  <Pin className='h-3 w-3 text-primary fill-primary' />
                )}
                {note.updatedAt !== note.createdAt && (
                  <span className='text-xs text-muted-foreground'>
                    (đã chỉnh sửa)
                  </span>
                )}
              </div>

              {/* Actions Menu */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant='ghost' size='icon' className='h-7 w-7'>
                    <MoreVertical className='h-4 w-4' />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align='end'>
                  <DropdownMenuItem onClick={() => onReply?.(note)}>
                    <Reply className='h-4 w-4 mr-2' />
                    Trả lời
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={copyContent}>
                    <Copy className='h-4 w-4 mr-2' />
                    Sao chép
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onPin?.(note)}>
                    <Pin className='h-4 w-4 mr-2' />
                    {note.isPinned ? 'Bỏ ghim' : 'Ghim'}
                  </DropdownMenuItem>
                  {isOwner && (
                    <>
                      <DropdownMenuItem onClick={() => onEdit?.(note)}>
                        <Edit className='h-4 w-4 mr-2' />
                        Chỉnh sửa
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        onClick={() => onDelete?.(note)}
                        className='text-red-600 focus:text-red-600'
                      >
                        <Trash2 className='h-4 w-4 mr-2' />
                        Xóa
                      </DropdownMenuItem>
                    </>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            {/* Note Content */}
            <div className='mt-2 text-sm text-foreground whitespace-pre-wrap'>
              {note.content}
            </div>

            {/* Attachments */}
            {note.attachments && note.attachments.length > 0 && (
              <div className='mt-3 flex flex-wrap gap-2'>
                {note.attachments.map((attachment) => (
                  <a
                    key={attachment.id}
                    href={attachment.url}
                    target='_blank'
                    rel='noopener noreferrer'
                    className='flex items-center gap-2 px-3 py-1.5 bg-muted rounded-md text-xs hover:bg-muted/80 transition-colors'
                  >
                    <span className='truncate max-w-[150px]'>
                      {attachment.name}
                    </span>
                    <span className='text-muted-foreground'>
                      {attachment.size}
                    </span>
                  </a>
                ))}
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default NoteCard;
