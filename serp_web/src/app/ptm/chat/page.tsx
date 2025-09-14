/**
 * PTM Chat Bot Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - AI Assistant for Productivity
 */

'use client';

import React, { useState } from 'react';
import { Button, Card, Input } from '@/shared/components';
import {
  Bot,
  Send,
  Mic,
  Paperclip,
  MoreHorizontal,
  User,
  Sparkles,
  Clock,
  MessageSquare,
} from 'lucide-react';

interface Message {
  id: number;
  content: string;
  sender: 'user' | 'bot';
  timestamp: string;
  type?: 'text' | 'suggestion' | 'task';
}

const Chat: React.FC = () => {
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      content:
        "Hello! I'm your productivity assistant. How can I help you optimize your tasks today?",
      sender: 'bot',
      timestamp: '9:00 AM',
      type: 'text',
    },
    {
      id: 2,
      content: 'Can you help me prioritize my tasks for today?',
      sender: 'user',
      timestamp: '9:01 AM',
      type: 'text',
    },
    {
      id: 3,
      content:
        "I'd be happy to help! Based on your calendar and current projects, here are my recommendations:",
      sender: 'bot',
      timestamp: '9:01 AM',
      type: 'text',
    },
    {
      id: 4,
      content:
        '1. Complete code review (High priority, 2 hours)\n2. Attend team meeting (Scheduled, 30 min)\n3. Update documentation (Medium priority, 1 hour)',
      sender: 'bot',
      timestamp: '9:01 AM',
      type: 'suggestion',
    },
  ]);

  const quickSuggestions = [
    'Optimize my schedule for today',
    'Create a new task',
    'Show my productivity stats',
    'Suggest break times',
    'Help with time blocking',
  ];

  const handleSendMessage = () => {
    if (message.trim()) {
      const newMessage: Message = {
        id: messages.length + 1,
        content: message,
        sender: 'user',
        timestamp: new Date().toLocaleTimeString('en-US', {
          hour: 'numeric',
          minute: '2-digit',
        }),
        type: 'text',
      };

      setMessages([...messages, newMessage]);
      setMessage('');

      // Simulate bot response
      setTimeout(() => {
        const botResponse: Message = {
          id: messages.length + 2,
          content:
            'I understand you want help with that. Let me analyze your current situation and provide personalized recommendations...',
          sender: 'bot',
          timestamp: new Date().toLocaleTimeString('en-US', {
            hour: 'numeric',
            minute: '2-digit',
          }),
          type: 'text',
        };
        setMessages((prev) => [...prev, botResponse]);
      }, 1000);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className='flex h-[calc(100vh-2rem)] flex-col space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Bot className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>AI Productivity Assistant</h1>
        </div>
        <Button variant='outline' size='sm'>
          <MoreHorizontal className='h-4 w-4' />
        </Button>
      </div>

      {/* Chat Container */}
      <div className='flex flex-1 gap-6'>
        {/* Main Chat */}
        <Card className='flex flex-1 flex-col'>
          {/* Chat Messages */}
          <div className='flex-1 overflow-y-auto p-6'>
            <div className='space-y-4'>
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex ${
                    msg.sender === 'user' ? 'justify-end' : 'justify-start'
                  }`}
                >
                  <div
                    className={`flex max-w-[80%] gap-3 ${
                      msg.sender === 'user' ? 'flex-row-reverse' : 'flex-row'
                    }`}
                  >
                    {/* Avatar */}
                    <div
                      className={`flex h-8 w-8 items-center justify-center rounded-full ${
                        msg.sender === 'user'
                          ? 'bg-primary text-primary-foreground'
                          : 'bg-muted'
                      }`}
                    >
                      {msg.sender === 'user' ? (
                        <User className='h-4 w-4' />
                      ) : (
                        <Bot className='h-4 w-4' />
                      )}
                    </div>

                    {/* Message Content */}
                    <div
                      className={`rounded-lg px-4 py-2 ${
                        msg.sender === 'user'
                          ? 'bg-primary text-primary-foreground'
                          : msg.type === 'suggestion'
                            ? 'bg-blue-50 text-blue-900 border border-blue-200'
                            : 'bg-muted'
                      }`}
                    >
                      <div className='whitespace-pre-wrap text-sm'>
                        {msg.content}
                      </div>
                      <div
                        className={`mt-1 text-xs ${
                          msg.sender === 'user'
                            ? 'text-primary-foreground/70'
                            : 'text-muted-foreground'
                        }`}
                      >
                        {msg.timestamp}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Message Input */}
          <div className='border-t p-4'>
            <div className='flex gap-2'>
              <div className='flex-1'>
                <Input
                  placeholder='Ask me anything about your productivity...'
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyPress={handleKeyPress}
                  className='resize-none'
                />
              </div>
              <Button size='sm' variant='outline'>
                <Paperclip className='h-4 w-4' />
              </Button>
              <Button size='sm' variant='outline'>
                <Mic className='h-4 w-4' />
              </Button>
              <Button size='sm' onClick={handleSendMessage}>
                <Send className='h-4 w-4' />
              </Button>
            </div>
          </div>
        </Card>

        {/* Sidebar */}
        <div className='w-80 space-y-4'>
          {/* Quick Suggestions */}
          <Card className='p-4'>
            <h3 className='mb-3 flex items-center gap-2 text-sm font-semibold'>
              <Sparkles className='h-4 w-4' />
              Quick Suggestions
            </h3>
            <div className='space-y-2'>
              {quickSuggestions.map((suggestion, index) => (
                <button
                  key={index}
                  className='w-full rounded-lg border p-2 text-left text-sm hover:bg-muted transition-colors'
                  onClick={() => setMessage(suggestion)}
                >
                  {suggestion}
                </button>
              ))}
            </div>
          </Card>

          {/* Chat Stats */}
          <Card className='p-4'>
            <h3 className='mb-3 flex items-center gap-2 text-sm font-semibold'>
              <MessageSquare className='h-4 w-4' />
              Chat Statistics
            </h3>
            <div className='space-y-3'>
              <div className='flex justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Messages Today
                </span>
                <span className='text-sm font-medium'>12</span>
              </div>
              <div className='flex justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Tasks Created
                </span>
                <span className='text-sm font-medium'>5</span>
              </div>
              <div className='flex justify-between'>
                <span className='text-sm text-muted-foreground'>
                  Time Saved
                </span>
                <span className='text-sm font-medium'>2.5 hours</span>
              </div>
            </div>
          </Card>

          {/* Recent Conversations */}
          <Card className='p-4'>
            <h3 className='mb-3 flex items-center gap-2 text-sm font-semibold'>
              <Clock className='h-4 w-4' />
              Recent Topics
            </h3>
            <div className='space-y-2'>
              <div className='rounded-lg border p-2'>
                <div className='text-xs font-medium'>Task Prioritization</div>
                <div className='text-xs text-muted-foreground'>2 hours ago</div>
              </div>
              <div className='rounded-lg border p-2'>
                <div className='text-xs font-medium'>Schedule Optimization</div>
                <div className='text-xs text-muted-foreground'>Yesterday</div>
              </div>
              <div className='rounded-lg border p-2'>
                <div className='text-xs font-medium'>Meeting Preparation</div>
                <div className='text-xs text-muted-foreground'>2 days ago</div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Chat;
