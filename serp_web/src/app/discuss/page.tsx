/*
Author: QuanTuanHuy
Description: Part of Serp Project - Discuss demo page for testing ChannelList sidebar
*/

'use client';

import React, { useState } from 'react';
import { ChannelList } from '@/modules/discuss';
import type { Channel } from '@/modules/discuss';

export default function DiscussDemo() {
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);

  const handleChannelSelect = (channel: Channel) => {
    setSelectedChannel(channel);
    console.log('Selected channel:', channel);
  };

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-slate-900">
      {/* Sidebar */}
      <div className="w-96 border-r border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800">
        <ChannelList
          onChannelSelect={handleChannelSelect}
          selectedChannelId={selectedChannel?.id}
        />
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex items-center justify-center p-8">
        {selectedChannel ? (
          <div className="text-center">
            <h1 className="text-3xl font-bold mb-4 bg-gradient-to-r from-violet-600 to-fuchsia-600 bg-clip-text text-transparent">
              {selectedChannel.name}
            </h1>
            <p className="text-slate-600 dark:text-slate-400 mb-2">
              Type: <span className="font-semibold">{selectedChannel.type}</span>
            </p>
            <p className="text-slate-600 dark:text-slate-400 mb-2">
              Members: <span className="font-semibold">{selectedChannel.memberCount}</span>
            </p>
            {selectedChannel.unreadCount > 0 && (
              <p className="text-rose-600 dark:text-rose-400 font-semibold">
                {selectedChannel.unreadCount} unread messages
              </p>
            )}
            <div className="mt-8 p-6 bg-slate-100 dark:bg-slate-800 rounded-lg">
              <p className="text-sm text-slate-500 dark:text-slate-400">
                Chat window will be implemented in Day 3-4
              </p>
            </div>
          </div>
        ) : (
          <div className="text-center">
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center">
              <svg
                className="w-8 h-8 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mb-2">
              Welcome to Discuss
            </h2>
            <p className="text-slate-600 dark:text-slate-400">
              Select a channel from the sidebar to start messaging
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
