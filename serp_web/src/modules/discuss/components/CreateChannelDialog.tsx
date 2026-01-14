/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create Channel Dialog component
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  Button,
  Input,
  Label,
  Textarea,
} from '@/shared/components/ui';
import { useCreateChannelMutation } from '../api/discussApi';
import { Loader2, Users, Hash } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { ChannelType } from '../types';

interface CreateChannelDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
}

export const CreateChannelDialog: React.FC<CreateChannelDialogProps> = ({
  open,
  onOpenChange,
  onSuccess,
}) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [channelType, setChannelType] = useState<ChannelType>('GROUP');
  const [memberIds, setMemberIds] = useState<string>('');

  const [createChannel, { isLoading }] = useCreateChannelMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      alert('Please enter a channel name');
      return;
    }

    try {
      const memberIdArray = memberIds
        .split(',')
        .map((id) => id.trim())
        .filter((id) => id.length > 0);

      await createChannel({
        name: name.trim(),
        description: description.trim() || undefined,
        type: channelType,
        memberIds: memberIdArray,
      }).unwrap();

      // Reset form
      setName('');
      setDescription('');
      setChannelType('GROUP');
      setMemberIds('');

      // Close dialog and trigger success callback
      onOpenChange(false);
      onSuccess?.();

      // Show success message (you can replace with toast)
      alert('Channel created successfully!');
    } catch (error: any) {
      console.error('Failed to create channel:', error);
      alert(
        error?.data?.message || 'Failed to create channel. Please try again.'
      );
    }
  };

  const handleClose = () => {
    setName('');
    setDescription('');
    setChannelType('GROUP');
    setMemberIds('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[500px]'>
        <DialogHeader>
          <DialogTitle className='text-xl font-bold bg-gradient-to-r from-violet-600 to-fuchsia-600 bg-clip-text text-transparent'>
            Create New Channel
          </DialogTitle>
          <DialogDescription>
            Create a new channel to start collaborating with your team.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-5 mt-4'>
          {/* Channel Type Selection */}
          <div className='space-y-2'>
            <Label>Channel Type</Label>
            <div className='grid grid-cols-2 gap-3'>
              <button
                type='button'
                onClick={() => setChannelType('GROUP')}
                className={cn(
                  'flex items-center gap-2 px-4 py-3 rounded-lg border-2 transition-all',
                  channelType === 'GROUP'
                    ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20'
                    : 'border-slate-200 dark:border-slate-700 hover:border-violet-300'
                )}
              >
                <Users
                  className={cn(
                    'h-5 w-5',
                    channelType === 'GROUP'
                      ? 'text-violet-600 dark:text-violet-400'
                      : 'text-slate-500'
                  )}
                />
                <span
                  className={cn(
                    'font-semibold',
                    channelType === 'GROUP'
                      ? 'text-violet-700 dark:text-violet-300'
                      : 'text-slate-700 dark:text-slate-300'
                  )}
                >
                  Group
                </span>
              </button>

              <button
                type='button'
                onClick={() => setChannelType('TOPIC')}
                className={cn(
                  'flex items-center gap-2 px-4 py-3 rounded-lg border-2 transition-all',
                  channelType === 'TOPIC'
                    ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20'
                    : 'border-slate-200 dark:border-slate-700 hover:border-violet-300'
                )}
              >
                <Hash
                  className={cn(
                    'h-5 w-5',
                    channelType === 'TOPIC'
                      ? 'text-violet-600 dark:text-violet-400'
                      : 'text-slate-500'
                  )}
                />
                <span
                  className={cn(
                    'font-semibold',
                    channelType === 'TOPIC'
                      ? 'text-violet-700 dark:text-violet-300'
                      : 'text-slate-700 dark:text-slate-300'
                  )}
                >
                  Topic
                </span>
              </button>
            </div>
          </div>

          {/* Channel Name */}
          <div className='space-y-2'>
            <Label htmlFor='channel-name'>
              Channel Name <span className='text-rose-500'>*</span>
            </Label>
            <Input
              id='channel-name'
              placeholder='e.g., Marketing Team'
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className='focus-visible:ring-violet-500'
            />
          </div>

          {/* Description */}
          <div className='space-y-2'>
            <Label htmlFor='description'>Description (Optional)</Label>
            <Textarea
              id='description'
              placeholder='What is this channel about?'
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className='focus-visible:ring-violet-500 resize-none'
            />
          </div>

          {/* Member IDs */}
          <div className='space-y-2'>
            <Label htmlFor='member-ids'>
              Member IDs (comma-separated, optional)
            </Label>
            <Input
              id='member-ids'
              placeholder='e.g., 1, 2, 3'
              value={memberIds}
              onChange={(e) => setMemberIds(e.target.value)}
              className='focus-visible:ring-violet-500'
            />
            <p className='text-xs text-slate-500 dark:text-slate-400'>
              Enter user IDs separated by commas
            </p>
          </div>

          {/* Actions */}
          <div className='flex gap-3 pt-4'>
            <Button
              type='button'
              variant='outline'
              onClick={handleClose}
              className='flex-1'
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              disabled={isLoading || !name.trim()}
              className='flex-1 bg-gradient-to-r from-violet-600 to-fuchsia-600 hover:from-violet-700 hover:to-fuchsia-700'
            >
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Creating...
                </>
              ) : (
                'Create Channel'
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
