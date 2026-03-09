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
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import {
  useCreateChannelMutation,
  useCreateDirectChannelMutation,
} from '../api/discussApi';
import { Loader2, Users, Hash, MessageCircle } from 'lucide-react';
import { cn } from '@/shared/utils';
import { UserSearch } from './UserSearch';
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
  const [activeTab, setActiveTab] = useState('channel');

  // Group/Topic Channel State
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [channelType, setChannelType] = useState<ChannelType>('GROUP');
  const [memberIds, setMemberIds] = useState<string[]>([]);

  // Direct Message State
  const [selectedUserId, setSelectedUserId] = useState<string[]>([]);

  const [createChannel, { isLoading: isCreatingChannel }] =
    useCreateChannelMutation();
  const [createDirectChannel, { isLoading: isCreatingDirect }] =
    useCreateDirectChannelMutation();

  const handleCreateChannel = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      alert('Please enter a channel name');
      return;
    }

    try {
      await createChannel({
        name: name.trim(),
        description: description.trim() || undefined,
        type: channelType,
        memberIds: memberIds,
      }).unwrap();

      resetAndClose();
      alert('Channel created successfully!');
    } catch (error: any) {
      console.error('Failed to create channel:', error);
      alert(error?.data?.message || 'Failed to create channel.');
    }
  };

  const handleCreateDirect = async () => {
    if (selectedUserId.length === 0) return;

    try {
      await createDirectChannel(selectedUserId[0]).unwrap();
      resetAndClose();
      // No alert needed for DM usually, just open it (logic handled by parent usually)
    } catch (error: any) {
      console.error('Failed to create conversation:', error);
      alert(error?.data?.message || 'Failed to start conversation.');
    }
  };

  const resetAndClose = () => {
    setName('');
    setDescription('');
    setChannelType('GROUP');
    setMemberIds([]);
    setSelectedUserId([]);
    setActiveTab('channel');
    onOpenChange(false);
    onSuccess?.();
  };

  const isLoading = isCreatingChannel || isCreatingDirect;

  return (
    <Dialog open={open} onOpenChange={(val) => !isLoading && onOpenChange(val)}>
      <DialogContent className='sm:max-w-[500px] gap-0 p-0 overflow-hidden'>
        <div className='p-6 pb-2'>
          <DialogHeader>
            <DialogTitle className='text-xl font-bold bg-gradient-to-r from-violet-600 to-fuchsia-600 bg-clip-text text-transparent'>
              Start a Conversation
            </DialogTitle>
            <DialogDescription>
              Create a channel or start a direct message.
            </DialogDescription>
          </DialogHeader>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab} className='w-full'>
          <div className='px-6'>
            <TabsList className='grid w-full grid-cols-2'>
              <TabsTrigger value='channel'>New Channel</TabsTrigger>
              <TabsTrigger value='direct'>Direct Message</TabsTrigger>
            </TabsList>
          </div>

          <div className='p-6 pt-4'>
            <TabsContent value='channel' className='mt-0 space-y-4'>
              <form onSubmit={handleCreateChannel} className='space-y-4'>
                {/* Channel Type Selection */}
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
                          ? 'text-violet-600'
                          : 'text-slate-500'
                      )}
                    />
                    <span
                      className={cn(
                        'font-semibold',
                        channelType === 'GROUP'
                          ? 'text-violet-700 dark:text-violet-300'
                          : 'text-slate-700'
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
                          ? 'text-violet-600'
                          : 'text-slate-500'
                      )}
                    />
                    <span
                      className={cn(
                        'font-semibold',
                        channelType === 'TOPIC'
                          ? 'text-violet-700 dark:text-violet-300'
                          : 'text-slate-700'
                      )}
                    >
                      Topic
                    </span>
                  </button>
                </div>

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
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='description'>Description</Label>
                  <Textarea
                    id='description'
                    placeholder='What is this channel about?'
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={2}
                    className='resize-none'
                  />
                </div>

                <div className='space-y-2'>
                  <Label>Add Members</Label>
                  <UserSearch
                    selectedUserIds={memberIds}
                    onSelectionChange={setMemberIds}
                    mode='multi'
                    placeholder='Search people to add...'
                  />
                </div>

                <div className='flex gap-3 pt-2'>
                  <Button
                    type='button'
                    variant='outline'
                    onClick={resetAndClose}
                    className='flex-1'
                    disabled={isLoading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type='submit'
                    disabled={isLoading || !name.trim()}
                    className='flex-1 bg-gradient-to-r from-violet-600 to-fuchsia-600'
                  >
                    {isLoading ? (
                      <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                    ) : (
                      'Create Channel'
                    )}
                  </Button>
                </div>
              </form>
            </TabsContent>

            <TabsContent value='direct' className='mt-0 space-y-4'>
              <div className='space-y-4 py-4'>
                <div className='space-y-2'>
                  <Label>Select a person</Label>
                  <UserSearch
                    selectedUserIds={selectedUserId}
                    onSelectionChange={setSelectedUserId}
                    mode='single'
                    placeholder='Search by name or email...'
                  />
                  <p className='text-sm text-slate-500'>
                    Search for a colleague to start a private conversation.
                  </p>
                </div>
              </div>

              <div className='flex gap-3 pt-12'>
                <Button
                  type='button'
                  variant='outline'
                  onClick={resetAndClose}
                  className='flex-1'
                  disabled={isLoading}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleCreateDirect}
                  disabled={isLoading || selectedUserId.length === 0}
                  className='flex-1 bg-gradient-to-r from-violet-600 to-fuchsia-600'
                >
                  {isLoading ? (
                    <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  ) : (
                    <>
                      <MessageCircle className='h-4 w-4 mr-2' />
                      Start Conversation
                    </>
                  )}
                </Button>
              </div>
            </TabsContent>
          </div>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
};
