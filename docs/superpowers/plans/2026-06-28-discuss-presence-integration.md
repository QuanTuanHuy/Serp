# Discuss Module Presence Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate user presence status (Online, Do Not Disturb, Offline) and custom status messages in the Discuss module UI of `serp_web`.

**Architecture:** We use RTK Query Cache Invalidation. WebSockets notify the client of presence updates, which invalidates the `Presence` tag, causing active presence queries to automatically refetch.

**Tech Stack:** Next.js 15, React 19, RTK Query, WebSockets (STOMP), TailwindCSS.

---

### Task 1: Create UserProfileCard Component and Add to Sidebar

**Files:**
- Create: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/UserProfileCard.tsx`
- Modify: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelList.tsx`
- Modify: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/index.ts`

- [ ] **Step 1: Create the UserProfileCard.tsx component**
Create the component with a Popover for selecting status (`ONLINE`, `BUSY`, `OFFLINE`) and setting/clearing custom status messages.

```tsx
/*
Author: QuanTuanHuy
Description: Part of Serp Project - User Profile Card and Status Switcher Popover
*/

'use client';

import React, { useState } from 'react';
import { useAuth } from '@/modules/account';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui';
import { cn, getAvatarColor } from '@/shared/utils';
import {
  useGetMyPresenceQuery,
  useUpdateMyPresenceMutation,
} from '../api/discussApi';
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
import { Loader2 } from 'lucide-react';
import type { UserStatus } from '../api/presence.api';

export const UserProfileCard: React.FC = () => {
  const { user } = useAuth();
  const { data: myPresenceData, isLoading } = useGetMyPresenceQuery();
  const [updateMyPresence, { isLoading: isUpdating }] =
    useUpdateMyPresenceMutation();

  const [statusMessage, setStatusMessage] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  if (isLoading || !user) {
    return (
      <div className='flex items-center gap-3 px-3 py-2 animate-pulse'>
        <div className='h-9 w-9 rounded-full bg-slate-200 dark:bg-slate-700' />
        <div className='flex-1 space-y-2 py-1'>
          <div className='h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4' />
          <div className='h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/2' />
        </div>
      </div>
    );
  }

  const currentPresence = myPresenceData?.data;
  const currentStatus = currentPresence?.status || 'OFFLINE';
  const currentMessage = currentPresence?.statusMessage || '';

  const mapStatusToOnlineStatus = (status: UserStatus) => {
    switch (status) {
      case 'ONLINE':
        return 'online';
      case 'BUSY':
        return 'busy';
      default:
        return 'offline';
    }
  };

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (open) {
      setStatusMessage(currentMessage);
    }
  };

  const handleStatusSelect = async (status: UserStatus) => {
    try {
      await updateMyPresence({ status, statusMessage: currentMessage }).unwrap();
    } catch (err) {
      console.error('Failed to update status:', err);
    }
  };

  const handleSaveMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateMyPresence({
        status: currentStatus,
        statusMessage: statusMessage.trim() || undefined,
      }).unwrap();
      setIsOpen(false);
    } catch (err) {
      console.error('Failed to update status message:', err);
    }
  };

  const handleClearMessage = async () => {
    try {
      await updateMyPresence({
        status: currentStatus,
        statusMessage: undefined,
      }).unwrap();
      setStatusMessage('');
      setIsOpen(false);
    } catch (err) {
      console.error('Failed to clear status message:', err);
    }
  };

  const initials = user.fullName
    ? user.fullName
        .split(' ')
        .map((w) => w[0])
        .join('')
        .slice(0, 2)
        .toUpperCase()
    : 'U';

  return (
    <Popover open={isOpen} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <button
          className={cn(
            'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left transition-all duration-200',
            'hover:bg-slate-100 dark:hover:bg-slate-800/60',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500'
          )}
        >
          <div className='relative flex-shrink-0'>
            <Avatar className='h-9 w-9 border border-slate-200 dark:border-slate-700'>
              {user.avatarUrl && (
                <AvatarImage src={user.avatarUrl} alt={user.fullName} />
              )}
              <AvatarFallback
                className={cn(
                  'text-xs font-semibold text-white bg-gradient-to-br',
                  getAvatarColor(user.fullName || '')
                )}
              >
                {initials}
              </AvatarFallback>
            </Avatar>
            <div className='absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 bg-white dark:bg-slate-900 rounded-full flex items-center justify-center border border-slate-200 dark:border-slate-800'>
              <OnlineStatusIndicator
                status={mapStatusToOnlineStatus(currentStatus)}
                size='sm'
                showPulse={false}
              />
            </div>
          </div>
          <div className='flex-1 min-w-0'>
            <p className='text-sm font-semibold text-slate-900 dark:text-slate-100 truncate'>
              {user.fullName}
            </p>
            {currentMessage ? (
              <p className='text-xs text-slate-500 dark:text-slate-400 truncate italic'>
                {currentMessage}
              </p>
            ) : (
              <p className='text-xs text-slate-400 dark:text-slate-500 truncate'>
                Set status...
              </p>
            )}
          </div>
        </button>
      </PopoverTrigger>
      <PopoverContent className='w-72 p-3' align='start' side='top' sideOffset={8}>
        <div className='space-y-3'>
          <div className='flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800'>
            <span className='text-xs font-semibold text-slate-500 uppercase tracking-wide'>
              Set presence status
            </span>
          </div>

          {/* Quick status selector */}
          <div className='grid grid-cols-3 gap-1.5'>
            {(['ONLINE', 'BUSY', 'OFFLINE'] as UserStatus[]).map((statusOption) => {
              const mapped = mapStatusToOnlineStatus(statusOption);
              const isActive = currentStatus === statusOption;
              const label =
                statusOption === 'ONLINE'
                  ? 'Online'
                  : statusOption === 'BUSY'
                    ? 'DND'
                    : 'Offline';

              return (
                <Button
                  key={statusOption}
                  variant={isActive ? 'default' : 'outline'}
                  size='sm'
                  onClick={() => handleStatusSelect(statusOption)}
                  disabled={isUpdating}
                  className={cn(
                    'h-8 gap-1.5 text-xs',
                    isActive &&
                      statusOption === 'ONLINE' &&
                      'bg-emerald-600 hover:bg-emerald-700',
                    isActive &&
                      statusOption === 'BUSY' &&
                      'bg-rose-600 hover:bg-rose-700'
                  )}
                >
                  <OnlineStatusIndicator status={mapped} size='sm' showPulse={false} />
                  {label}
                </Button>
              );
            })}
          </div>

          {/* Status Message Form */}
          <form onSubmit={handleSaveMessage} className='space-y-2 pt-2 border-t border-slate-100 dark:border-slate-800'>
            <div className='space-y-1.5'>
              <label className='text-xs font-medium text-slate-500'>
                Status message
              </label>
              <Input
                placeholder='What is on your mind?'
                value={statusMessage}
                onChange={(e) => setStatusMessage(e.target.value)}
                maxLength={255}
                disabled={isUpdating}
                className='h-8 text-xs focus-visible:ring-violet-500'
              />
            </div>
            <div className='flex items-center justify-between gap-2 pt-1'>
              {currentMessage ? (
                <Button
                  type='button'
                  variant='ghost'
                  size='sm'
                  onClick={handleClearMessage}
                  disabled={isUpdating}
                  className='h-7 text-xs px-2 text-rose-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/20'
                >
                  Clear Status
                </Button>
              ) : (
                <div />
              )}
              <Button
                type='submit'
                size='sm'
                disabled={isUpdating}
                className='h-7 text-xs px-3 bg-violet-600 hover:bg-violet-700'
              >
                {isUpdating && <Loader2 className='h-3 w-3 animate-spin mr-1' />}
                Save
              </Button>
            </div>
          </form>
        </div>
      </PopoverContent>
    </Popover>
  );
};
```

- [ ] **Step 2: Export UserProfileCard component**
Add `export { UserProfileCard } from './UserProfileCard';` to `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/index.ts`.

- [ ] **Step 3: Modify ChannelList.tsx to render UserProfileCard**
Add `UserProfileCard` at the bottom of the ChannelList sidebar panel, right above the "New Channel" button.

```tsx
      {/* Footer - Profile & New Channel */}
      <div className='flex-shrink-0 p-3 border-t border-slate-200 dark:border-slate-700 space-y-2 bg-slate-50/50 dark:bg-slate-900/10'>
        <UserProfileCard />
        <Button
          variant='outline'
          size='sm'
          onClick={handleOpenCreateDialog}
          className={cn(
            'w-full',
            'bg-gradient-to-r from-violet-500/10 to-fuchsia-500/10',
            'hover:from-violet-500/20 hover:to-fuchsia-500/20',
            'border-violet-200 dark:border-violet-800',
            'text-violet-700 dark:text-violet-300',
            'font-semibold',
            'transition-all duration-200'
          )}
        >
          <Plus className='h-4 w-4 mr-2' />
          New Channel
        </Button>
      </div>
```

- [ ] **Step 4: Verify task compilation**
Verify types compile with: `npm run type-check` (run in `d:/User2/open_source/serp/serp_web`).
Expected: PASS with no type errors.

- [ ] **Step 5: Commit changes**
```bash
git add serp_web/src/modules/discuss/components/UserProfileCard.tsx serp_web/src/modules/discuss/components/ChannelList.tsx serp_web/src/modules/discuss/components/index.ts
git commit -m "feat(discuss): add UserProfileCard to sidebar for presence switcher"
```

---

### Task 2: Update ChannelItem Direct Channel Presence Indicator

**Files:**
- Modify: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelItem.tsx`

- [ ] **Step 1: Modify ChannelItem.tsx to import OnlineStatusIndicator and map statuses**
Update imports and add status mapping helper.
```typescript
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
```
```typescript
  const mapUserStatus = (status?: string): 'online' | 'busy' | 'offline' => {
    switch (status) {
      case 'ONLINE':
        return 'online';
      case 'BUSY':
        return 'busy';
      default:
        return 'offline';
    }
  };

  const dmStatus = otherDirectUser ? mapUserStatus(otherDirectUser.status) : 'offline';
```

- [ ] **Step 2: Replace DM status indicator dot**
Replace the static green dot logic around lines 134-139:
```tsx
        {/* Online indicator for DIRECT channels */}
        {channel.type === 'DIRECT' && (
          <div className='absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 bg-white dark:bg-slate-900 rounded-full flex items-center justify-center border border-slate-100 dark:border-slate-800'>
            <OnlineStatusIndicator status={dmStatus} size='sm' showPulse={dmStatus === 'online'} />
          </div>
        )}
```

- [ ] **Step 3: Add hover title/tooltip containing status message**
Update button properties to include status details when hovering DMs:
```typescript
      title={
        channel.type === 'DIRECT' && otherDirectUser
          ? `${channel.name} - ${otherDirectUser.status === 'BUSY' ? 'Busy' : otherDirectUser.isOnline ? 'Online' : 'Offline'}${otherDirectUser.statusMessage ? `: "${otherDirectUser.statusMessage}"` : ''}`
          : undefined
      }
```

- [ ] **Step 4: Verify task compilation**
Verify types compile with: `npm run type-check` (run in `d:/User2/open_source/serp/serp_web`).
Expected: PASS with no type errors.

- [ ] **Step 5: Commit changes**
```bash
git add serp_web/src/modules/discuss/components/ChannelItem.tsx
git commit -m "feat(discuss): update ChannelItem DM status indicator and add tooltips"
```

---

### Task 3: Display Presence Status & Message in ChatWindow Header

**Files:**
- Modify: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Update ChatWindow.tsx header to render status indicator and messages**
Modify lines 707-743 in `ChatWindow.tsx` to output mapped status dot and custom status message:
```tsx
                  {channel.type === 'DIRECT' ? (
                    (() => {
                      // Find the other user's presence from channel presence data
                      const presence = presenceData?.data;
                      const allUsers = presence?.statusGroups
                        ? Object.values(presence.statusGroups).flat()
                        : [];
                      const otherUser = allUsers.find(
                        (u) => String(u.userId) !== currentUserId
                      );
                      const status = otherUser
                        ? mapUserStatus(otherUser.status)
                        : 'offline';
                      const statusText = otherUser?.isOnline
                        ? status === 'busy'
                          ? 'Busy'
                          : 'Online'
                        : otherUser?.lastSeenText || 'Offline';

                      return (
                        <span className='flex items-center gap-1.5 min-w-0'>
                          <OnlineStatusIndicator status={status} size='sm' showPulse={status === 'online'} />
                          <span
                            className={cn(
                              'font-medium truncate',
                              status === 'online'
                                ? 'text-emerald-600 dark:text-emerald-400'
                                : status === 'busy'
                                  ? 'text-rose-600 dark:text-rose-400'
                                  : 'text-slate-500 dark:text-slate-400'
                            )}
                          >
                            {statusText}
                          </span>
                          {otherUser?.statusMessage && (
                            <span
                              className='text-xs text-slate-400 dark:text-slate-500 italic truncate max-w-[250px]'
                              title={otherUser.statusMessage}
                            >
                              - &ldquo;{otherUser.statusMessage}&rdquo;
                            </span>
                          )}
                        </span>
                      );
                    })()
                  ) : (
```

- [ ] **Step 2: Verify task compilation**
Verify types compile with: `npm run type-check` (run in `d:/User2/open_source/serp/serp_web`).
Expected: PASS with no type errors.

- [ ] **Step 3: Commit changes**
```bash
git add serp_web/src/modules/discuss/components/ChatWindow.tsx
git commit -m "feat(discuss): render status indicators and status messages in ChatWindow header"
```

---

### Task 4: Integrate Presence Details into ChannelMembersPanel

**Files:**
- Modify: `d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelMembersPanel.tsx`

- [ ] **Step 1: Import OnlineStatusIndicator and DTO types**
Add imports in `ChannelMembersPanel.tsx`:
```typescript
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
import type { UserPresenceResponse } from '../api/presence.api';
```

- [ ] **Step 2: Build Map of presence by User ID and map user status**
Replace the `onlineUserIds` memo hook and add a mapping helper:
```typescript
  // Build a map of user presence from presence data
  const presenceMap = React.useMemo(() => {
    const map = new Map<string, UserPresenceResponse>();
    const statusGroups = presenceResponse?.data?.statusGroups;
    if (statusGroups) {
      Object.values(statusGroups).forEach((users) => {
        users.forEach((u) => {
          map.set(String(u.userId), u);
        });
      });
    }
    return map;
  }, [presenceResponse]);

  const mapUserStatus = (status?: string): 'online' | 'busy' | 'offline' => {
    switch (status) {
      case 'ONLINE':
        return 'online';
      case 'BUSY':
        return 'busy';
      default:
        return 'offline';
    }
  };
```

- [ ] **Step 3: Update avatar presence indicator**
Replace lines 298-302 with `<OnlineStatusIndicator>`:
```tsx
                        {(() => {
                          const userPresence = presenceMap.get(member.userId);
                          const presenceStatus = userPresence ? mapUserStatus(userPresence.status) : 'offline';
                          return (
                            <div className='absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 bg-white dark:bg-slate-900 rounded-full flex items-center justify-center border border-slate-100 dark:border-slate-800'>
                              <OnlineStatusIndicator
                                status={presenceStatus}
                                size='sm'
                                showPulse={presenceStatus === 'online'}
                              />
                            </div>
                          );
                        })()}
```

- [ ] **Step 4: Render status message below member name**
Add status message rendering logic under the username block (around lines 315-317):
```tsx
                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2'>
                          <p className='text-sm font-semibold truncate'>
                            {userName}
                            {isCurrentUser && (
                              <span className='ml-1.5 text-xs text-violet-600 font-normal'>
                                (You)
                              </span>
                            )}
                          </p>
                          {getRoleIcon(member.role)}
                        </div>
                        {(() => {
                          const userPresence = presenceMap.get(member.userId);
                          return (
                            userPresence?.statusMessage && (
                              <p
                                className='text-xs text-slate-500 dark:text-slate-400 italic truncate mt-0.5 max-w-[250px]'
                                title={userPresence.statusMessage}
                              >
                                &ldquo;{userPresence.statusMessage}&rdquo;
                              </p>
                            )
                          );
                        })()}
                        <div className='flex items-center gap-2 mt-0.5'>
                          {getRoleBadge(member.role)}
                          {member.user?.email && (
                            <p className='text-xs text-slate-500 truncate'>
                              {member.user.email}
                            </p>
                          )}
                        </div>
                      </div>
```

- [ ] **Step 5: Verify task compilation**
Verify types compile with: `npm run type-check` (run in `d:/User2/open_source/serp/serp_web`).
Expected: PASS with no type errors.

- [ ] **Step 6: Commit changes**
```bash
git add serp_web/src/modules/discuss/components/ChannelMembersPanel.tsx
git commit -m "feat(discuss): display user status dots and custom status messages in ChannelMembersPanel"
```
