# Channel Management Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate channel editing (name & description), archiving, deleting, and leaving capabilities from `discuss_service` into the `serp_web` frontend UI.

**Architecture:** We use RTK Query mutations (`updateChannel`, `archiveChannel`, `deleteChannel`, `leaveChannel`) in `ConversationDetailsSidebar` and leverage `useGetChannelQuery` in `ChatWindow` for live caching and reactivity. Termination actions (Archive, Delete, Leave) bubble up to the parent page via callbacks to deselect the closed channel.

**Tech Stack:** React 19, TypeScript, Next.js 15, RTK Query.

---

### Task 1: Parent-Child Callback Wiring for Closing Channels

**Files:**
- Modify: `serp_web/src/app/discuss/page.tsx`
- Modify: `serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Update page.tsx to support handleChannelClose callback**

Modify `serp_web/src/app/discuss/page.tsx` around lines 43-48 to add the close callback:

```typescript
  const handleChannelClose = () => {
    setSelectedChannel(null);
  };
```

Pass the callback to `<ChatWindow>`:

```tsx
        {/* Main Content Area */}
        <div className='flex-1 flex items-center justify-center'>
          {selectedChannel ? (
            <ChatWindow
              channel={selectedChannel}
              currentUserId={currentUserId}
              currentUserName={user?.fullName}
              currentUserAvatarUrl={user?.avatarUrl}
              onChannelClose={handleChannelClose}
              className='w-full h-full'
            />
          ) : (
```

- [ ] **Step 2: Update ChatWindow.tsx props and pass callback to Sidebar**

Modify `serp_web/src/modules/discuss/components/ChatWindow.tsx` around lines 53-60:

```typescript
interface ChatWindowProps {
  channel: Channel;
  currentUserId: string;
  currentUserName?: string;
  currentUserAvatarUrl?: string;
  className?: string;
  onChannelClose?: () => void;
}
```

Update details sidebar invocation inside `ChatWindow.tsx` around lines 800-840:

```tsx
      <ConversationDetailsSidebar
        channel={channel}
        currentUserId={currentUserId}
        open={detailsSidebarOpen}
        onOpenChange={setDetailsSidebarOpen}
        onClose={onChannelClose}
      />
```

- [ ] **Step 3: Verify build compiles without issues**

Run command in `serp_web`:
`npm run type-check`
Expected: Successful compilation without TypeScript errors.

- [ ] **Step 4: Commit**

```bash
git add src/app/discuss/page.tsx src/modules/discuss/components/ChatWindow.tsx
git commit -m "feat: add channel close callback wiring between Page and ChatWindow"
```

---

### Task 2: Enable Live Channel Synchronization in ChatWindow

**Files:**
- Modify: `serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Import useGetChannelQuery in ChatWindow.tsx**

Modify imports in `serp_web/src/modules/discuss/components/ChatWindow.tsx` around lines 38-49:

```typescript
import {
  useGetMessagesQuery,
  useLazyGetMessagesBeforeQuery,
  useLazyGetMessagesAroundQuery,
  useSendMessageMutation,
  useSendMessageWithFilesMutation,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
  useGetChannelPresenceQuery,
  useGetChannelQuery,
} from '../api/discussApi';
```

- [ ] **Step 2: Query live channel state in ChatWindow component**

Call the hook at the top of the component in `serp_web/src/modules/discuss/components/ChatWindow.tsx` around line 125:

```typescript
  // Live channel details sync
  const { data: liveChannelResponse } = useGetChannelQuery(channel.id);
  const liveChannel = liveChannelResponse?.data || channel;
```

- [ ] **Step 3: Replace old channel prop references with liveChannel**

In `ChatWindow.tsx`, replace `channel.name`, `channel.description`, `channel.avatarUrl`, `channel.memberCount` with `liveChannel.name`, `liveChannel.description`, `liveChannel.avatarUrl`, `liveChannel.memberCount` respectively inside the JSX, and pass `liveChannel` to the sidebar:

```tsx
      <ConversationDetailsSidebar
        channel={liveChannel}
        currentUserId={currentUserId}
        open={detailsSidebarOpen}
        onOpenChange={setDetailsSidebarOpen}
        onClose={onChannelClose}
      />
```

- [ ] **Step 4: Verify type safety**

Run: `npm run type-check`
Expected: Compile SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/modules/discuss/components/ChatWindow.tsx
git commit -m "feat: implement live channel sync using useGetChannelQuery in ChatWindow"
```

---

### Task 3: Implement Inline Editing and Danger Zone in Sidebar

**Files:**
- Modify: `serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx`

- [ ] **Step 1: Import mutations and update props interface**

Modify imports and interface in `serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx` around lines 28-40:

```typescript
import {
  useLazyGetAttachmentDownloadUrlQuery,
  useGetChannelAttachmentsQuery,
  useUpdateChannelMutation,
  useArchiveChannelMutation,
  useDeleteChannelMutation,
  useLeaveChannelMutation,
} from '../api/discussApi';
import type { Attachment, Channel } from '../types';
import { ChannelMembersPanel } from './ChannelMembersPanel';
import { Edit2, Save, Trash2, Archive, LogOut } from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';

interface ConversationDetailsSidebarProps {
  channel: Channel;
  currentUserId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onClose?: () => void;
}
```

- [ ] **Step 2: Add inline edit states and fetch mutations**

Inside the `ConversationDetailsSidebar` component body (around line 110):

```typescript
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState(channel.name);
  const [editDescription, setEditDescription] = useState(channel.description || '');

  const [updateChannel, { isLoading: isUpdating }] = useUpdateChannelMutation();
  const [archiveChannel, { isLoading: isArchiving }] = useArchiveChannelMutation();
  const [deleteChannel, { isLoading: isDeleting }] = useDeleteChannelMutation();
  const [leaveChannel, { isLoading: isLeaving }] = useLeaveChannelMutation();

  // Reset editing values when channel changes
  React.useEffect(() => {
    setEditName(channel.name);
    setEditDescription(channel.description || '');
    setIsEditing(false);
  }, [channel]);

  const currentUserMember = channel.members?.find(
    (m) => String(m.userId) === String(currentUserId)
  );
  const canManage =
    channel.createdBy === currentUserId ||
    currentUserMember?.role === 'OWNER' ||
    currentUserMember?.role === 'ADMIN';

  const canLeave =
    channel.type !== 'DIRECT' && currentUserMember?.role !== 'OWNER';
```

- [ ] **Step 3: Implement Update, Archive, Delete, and Leave handlers**

Add handlers inside the component:

```typescript
  const handleSave = async () => {
    if (!editName.trim()) {
      alert('Channel name cannot be empty');
      return;
    }
    try {
      await updateChannel({
        id: channel.id,
        data: { name: editName.trim(), description: editDescription.trim() },
      }).unwrap();
      setIsEditing(false);
    } catch (err) {
      console.error('Failed to update channel:', err);
      alert('Failed to update channel details');
    }
  };

  const handleArchive = async () => {
    if (!confirm('Are you sure you want to archive this channel? Archived channels will become read-only.')) {
      return;
    }
    try {
      await archiveChannel(channel.id).unwrap();
      onClose?.();
    } catch (err) {
      console.error('Failed to archive channel:', err);
      alert('Failed to archive channel');
    }
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this channel? This action is permanent and cannot be undone.')) {
      return;
    }
    try {
      await deleteChannel(channel.id).unwrap();
      onClose?.();
    } catch (err) {
      console.error('Failed to delete channel:', err);
      alert('Failed to delete channel');
    }
  };

  const handleLeave = async () => {
    if (!confirm('Are you sure you want to leave this channel? You will no longer receive messages from it.')) {
      return;
    }
    try {
      await leaveChannel(channel.id).unwrap();
      onClose?.();
    } catch (err) {
      console.error('Failed to leave channel:', err);
      alert('Failed to leave channel');
    }
  };
```

- [ ] **Step 4: Update the Overview Tab UI**

Modify the JSX in the Overview tab of `ConversationDetailsSidebar.tsx` around lines 166-189:

```tsx
            <TabsContent value='overview' className='mt-0 space-y-6'>
              {isEditing ? (
                // Edit Mode
                <div className='space-y-4 rounded-lg border border-slate-200 p-4 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/25'>
                  <div className='space-y-1.5'>
                    <label className='text-xs font-semibold uppercase tracking-wider text-slate-500'>
                      Channel Name
                    </label>
                    <Input
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      placeholder='Channel Name'
                      className='bg-white dark:bg-slate-950'
                      disabled={isUpdating}
                    />
                  </div>
                  <div className='space-y-1.5'>
                    <label className='text-xs font-semibold uppercase tracking-wider text-slate-500'>
                      Description
                    </label>
                    <Textarea
                      value={editDescription}
                      onChange={(e) => setEditDescription(e.target.value)}
                      placeholder='What is this channel about?'
                      className='bg-white dark:bg-slate-950 resize-none'
                      rows={3}
                      disabled={isUpdating}
                    />
                  </div>
                  <div className='flex gap-2 justify-end pt-1'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setIsEditing(false)}
                      disabled={isUpdating}
                    >
                      Cancel
                    </Button>
                    <Button
                      size='sm'
                      onClick={handleSave}
                      disabled={isUpdating || !editName.trim()}
                      className='bg-violet-600 hover:bg-violet-700'
                    >
                      {isUpdating && <Loader2 className='mr-1.5 h-3.5 w-3.5 animate-spin' />}
                      Save
                    </Button>
                  </div>
                </div>
              ) : (
                // View Mode
                <div className='space-y-4'>
                  <div className='flex items-center justify-between gap-3'>
                    <div className='flex items-center gap-3 min-w-0'>
                      <div className='flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-violet-100 dark:bg-violet-950/50'>
                        <Info className='h-6 w-6 text-violet-600 dark:text-violet-300' />
                      </div>
                      <div className='min-w-0'>
                        <p className='truncate font-semibold text-slate-900 dark:text-slate-100'>
                          {channel.name}
                        </p>
                        <Badge variant='outline'>{channelTypeLabel}</Badge>
                      </div>
                    </div>

                    {canManage && (
                      <Button
                        variant='ghost'
                        size='icon'
                        onClick={() => setIsEditing(true)}
                        className='h-8 w-8 text-slate-500 hover:text-slate-900 dark:hover:text-slate-100'
                        title='Edit name and description'
                      >
                        <Edit2 className='h-4 w-4' />
                      </Button>
                    )}
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
                </div>
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

              {/* Danger Zone */}
              {(canManage || canLeave) && (
                <div className='pt-6 border-t border-slate-200 dark:border-slate-800 space-y-3'>
                  <h4 className='text-xs font-semibold uppercase tracking-wider text-rose-500'>
                    Danger Zone
                  </h4>
                  
                  {canLeave && (
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={handleLeave}
                      disabled={isLeaving}
                      className='w-full justify-start text-rose-600 hover:text-rose-700 border-rose-200 dark:border-rose-900/50 hover:bg-rose-50 dark:hover:bg-rose-950/20'
                    >
                      <LogOut className='mr-2 h-4 w-4' />
                      Leave Channel
                    </Button>
                  )}

                  {canManage && (
                    <>
                      <Button
                        variant='outline'
                        size='sm'
                        onClick={handleArchive}
                        disabled={isArchiving}
                        className='w-full justify-start text-amber-600 hover:text-amber-700 border-amber-200 dark:border-amber-900/50 hover:bg-amber-50 dark:hover:bg-amber-950/20'
                      >
                        <Archive className='mr-2 h-4 w-4' />
                        Archive Channel
                      </Button>
                      
                      <Button
                        variant='outline'
                        size='sm'
                        onClick={handleDelete}
                        disabled={isDeleting}
                        className='w-full justify-start text-red-600 hover:text-red-700 border-red-200 dark:border-red-900/50 hover:bg-red-50 dark:hover:bg-red-950/20'
                      >
                        <Trash2 className='mr-2 h-4 w-4' />
                        Delete Channel vĩnh viễn
                      </Button>
                    </>
                  )}
                </div>
              )}
            </TabsContent>
```

- [ ] **Step 5: Verify build compiles without issues**

Run command in `serp_web`:
`npm run type-check`
Expected: Compile SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/modules/discuss/components/ConversationDetailsSidebar.tsx
git commit -m "feat: implement inline editing, archive, delete, and leave channel actions in sidebar"
```
