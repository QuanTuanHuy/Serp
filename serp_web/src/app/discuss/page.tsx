/*
Author: QuanTuanHuy
Description: Part of Serp Project - Discuss demo page for testing ChannelList sidebar and ChatWindow
*/

'use client';

import React, { useState } from 'react';
import { ChannelList } from '@/modules/discuss/components/ChannelList';
import { ChatWindow } from '@/modules/discuss/components/ChatWindow';
import { WebSocketProvider } from '@/modules/discuss/context/WebSocketContext';
import { useDiscussWebSocket } from '@/modules/discuss/hooks/useDiscussWebSocket';
import type { Channel } from '@/modules/discuss/types';
import { useAuth } from '@/modules/account';

function DiscussContent() {
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);

  const { user } = useAuth();
  const currentUserId = String(user?.id) || '';

  // Initialize WebSocket with current channel
  const wsApi = useDiscussWebSocket({
    channelId: selectedChannel?.id,
  });

  const handleChannelSelect = (channel: Channel) => {
    setSelectedChannel(channel);
    console.log('Selected channel:', channel);
  };

  return (
    <WebSocketProvider value={wsApi}>
      <div className='flex h-screen bg-slate-50 dark:bg-slate-900'>
        {/* Sidebar */}
        <div className='w-96 border-r border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800'>
          <ChannelList
            onChannelSelect={handleChannelSelect}
            selectedChannelId={selectedChannel?.id}
          />
        </div>

        {/* Main Content Area */}
        <div className='flex-1 flex items-center justify-center'>
          {selectedChannel ? (
            <ChatWindow
              channel={selectedChannel}
              currentUserId={currentUserId}
              className='w-full h-full'
            />
          ) : (
            <div className='text-center'>
              <div className='w-16 h-16 mx-auto mb-4 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center'>
                <svg
                  className='w-8 h-8 text-white'
                  fill='none'
                  stroke='currentColor'
                  viewBox='0 0 24 24'
                >
                  <path
                    strokeLinecap='round'
                    strokeLinejoin='round'
                    strokeWidth={2}
                    d='M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z'
                  />
                </svg>
              </div>
              <h2 className='text-2xl font-bold text-slate-900 dark:text-slate-100 mb-2'>
                Welcome to Discuss
              </h2>
              <p className='text-slate-600 dark:text-slate-400'>
                Select a channel from the sidebar to start messaging
              </p>
            </div>
          )}
        </div>
      </div>
    </WebSocketProvider>
  );
}

export default function DiscussDemo() {
  return <DiscussContent />;
}
