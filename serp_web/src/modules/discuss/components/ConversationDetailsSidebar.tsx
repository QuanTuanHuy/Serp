/*
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation details sidebar for Discuss
*/

'use client';

import React, { useMemo, useState } from 'react';
import {
  Badge,
  Button,
  ScrollArea,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import {
  File as FileIcon,
  FileText,
  Image,
  Info,
  Loader2,
  Users,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useLazyGetAttachmentDownloadUrlQuery,
  useGetChannelAttachmentsQuery,
} from '../api/discussApi';
import type { Attachment, Channel } from '../types';
import { ChannelMembersPanel } from './ChannelMembersPanel';

interface ConversationDetailsSidebarProps {
  channel: Channel;
  currentUserId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const formatDate = (value?: string) => {
  if (!value) return '—';
  const date = new Date(Number.isNaN(Number(value)) ? value : Number(value));
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const getFileIcon = (attachment: Attachment) => {
  if (attachment.fileType?.startsWith('image/')) return Image;
  if (attachment.fileType === 'application/pdf') return FileText;
  return FileIcon;
};

function FileRow({ attachment }: { attachment: Attachment }) {
  const [trigger, { isFetching }] = useLazyGetAttachmentDownloadUrlQuery();

  const handleOpen = async () => {
    try {
      const response = await trigger(attachment.id).unwrap();
      if (response?.data?.downloadUrl) {
        window.open(response.data.downloadUrl, '_blank', 'noopener,noreferrer');
      }
    } catch (err) {
      console.error('Failed to open file:', err);
    }
  };

  const Icon = getFileIcon(attachment);

  return (
    <div className='flex items-center gap-3 rounded-lg border border-slate-200 p-3 dark:border-slate-700'>
      <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-violet-50 dark:bg-violet-950/40'>
        <Icon className='h-5 w-5 text-violet-600 dark:text-violet-300' />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='truncate text-sm font-medium text-slate-900 dark:text-slate-100'>
          {attachment.fileName}
        </p>
        <p className='text-xs text-slate-500 dark:text-slate-400'>
          {formatFileSize(attachment.fileSize)} ·{' '}
          {formatDate(attachment.createdAt)}
        </p>
      </div>
      <Button
        variant='ghost'
        size='sm'
        disabled={isFetching}
        onClick={handleOpen}
      >
        {isFetching ? <Loader2 className='h-4 w-4 animate-spin' /> : 'Open'}
      </Button>
    </div>
  );
}

export const ConversationDetailsSidebar: React.FC<
  ConversationDetailsSidebarProps
> = ({ channel, currentUserId, open, onOpenChange }) => {
  const [memberDialogOpen, setMemberDialogOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  const { data: filesResponse, isLoading: filesLoading } =
    useGetChannelAttachmentsQuery(
      { channelId: channel.id, pagination: { page: 1, limit: 30 } },
      { skip: !open }
    );

  const files = filesResponse?.data?.items ?? [];

  const channelTypeLabel = useMemo(
    () => channel.type.charAt(0) + channel.type.slice(1).toLowerCase(),
    [channel.type]
  );

  return (
    <aside
      className={cn(
        'fixed inset-y-0 right-0 z-40 w-full max-w-sm translate-x-full border-l border-slate-200 bg-white shadow-xl transition-transform duration-200 dark:border-slate-700 dark:bg-slate-900 lg:static lg:z-auto lg:w-96 lg:max-w-none lg:shadow-none',
        open ? 'translate-x-0' : 'lg:hidden'
      )}
    >
      <div className='flex h-full flex-col'>
        <div className='flex items-center justify-between border-b border-slate-200 px-4 py-3 dark:border-slate-700'>
          <div>
            <h3 className='font-semibold text-slate-900 dark:text-slate-100'>
              Conversation details
            </h3>
            <p className='truncate text-sm text-slate-500 dark:text-slate-400'>
              {channel.name}
            </p>
          </div>
          <Button
            aria-label='Close conversation details'
            variant='ghost'
            size='sm'
            onClick={() => onOpenChange(false)}
          >
            <X className='h-5 w-5' />
          </Button>
        </div>

        <Tabs
          value={activeTab}
          onValueChange={setActiveTab}
          className='flex min-h-0 flex-1 flex-col'
        >
          <TabsList className='mx-4 mt-4 grid grid-cols-3'>
            <TabsTrigger value='overview'>Overview</TabsTrigger>
            <TabsTrigger value='members'>Members</TabsTrigger>
            <TabsTrigger value='files'>Files</TabsTrigger>
          </TabsList>

          <ScrollArea className='min-h-0 flex-1 px-4 py-4'>
            <TabsContent value='overview' className='mt-0 space-y-4'>
              <div className='flex items-center gap-3'>
                <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-violet-100 dark:bg-violet-950/50'>
                  <Info className='h-6 w-6 text-violet-600 dark:text-violet-300' />
                </div>
                <div className='min-w-0'>
                  <p className='truncate font-semibold text-slate-900 dark:text-slate-100'>
                    {channel.name}
                  </p>
                  <Badge variant='outline'>{channelTypeLabel}</Badge>
                </div>
              </div>

              {channel.description && (
                <section>
                  <h4 className='mb-1 text-sm font-medium text-slate-700 dark:text-slate-300'>
                    Description
                  </h4>
                  <p className='text-sm text-slate-600 dark:text-slate-400'>
                    {channel.description}
                  </p>
                </section>
              )}

              <dl className='space-y-3 text-sm'>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Members</dt>
                  <dd className='font-medium'>{channel.memberCount}</dd>
                </div>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Messages</dt>
                  <dd className='font-medium'>{channel.messageCount ?? '—'}</dd>
                </div>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Created</dt>
                  <dd className='font-medium'>
                    {formatDate(channel.createdAt)}
                  </dd>
                </div>
                {channel.type === 'TOPIC' && (
                  <div className='flex justify-between gap-4'>
                    <dt className='text-slate-500'>Linked entity</dt>
                    <dd className='truncate font-medium'>
                      {channel.entityType}/{channel.entityId}
                    </dd>
                  </div>
                )}
              </dl>
            </TabsContent>

            <TabsContent value='members' className='mt-0 space-y-3'>
              <Button
                variant='outline'
                className='w-full justify-start gap-2'
                onClick={() => setMemberDialogOpen(true)}
              >
                <Users className='h-4 w-4' />
                Manage members
              </Button>
              <p className='text-sm text-slate-500 dark:text-slate-400'>
                Member management opens the existing member panel while this
                sidebar owns conversation details.
              </p>
            </TabsContent>

            <TabsContent value='files' className='mt-0 space-y-3'>
              {filesLoading && (
                <div className='flex items-center justify-center py-8'>
                  <Loader2 className='h-5 w-5 animate-spin text-slate-500' />
                </div>
              )}
              {!filesLoading && files.length === 0 && (
                <p className='rounded-lg border border-dashed border-slate-300 p-4 text-center text-sm text-slate-500 dark:border-slate-700'>
                  No files have been shared in this conversation yet.
                </p>
              )}
              {files.map((attachment) => (
                <FileRow key={attachment.id} attachment={attachment} />
              ))}
            </TabsContent>
          </ScrollArea>
        </Tabs>
      </div>

      <ChannelMembersPanel
        open={memberDialogOpen}
        onOpenChange={setMemberDialogOpen}
        channelId={channel.id}
        channelName={channel.name}
        currentUserId={currentUserId}
      />
    </aside>
  );
};
