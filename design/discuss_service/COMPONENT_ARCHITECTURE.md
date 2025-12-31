# Channel List Sidebar - Component Architecture

## 📐 Component Hierarchy

```
ChannelList (Main Container)
│
├── Header Section
│   ├── Title ("Discuss") + Total Unread Badge
│   └── Search Input
│
├── ScrollArea (Channel List)
│   ├── Channel Group: DIRECT MESSAGES
│   │   ├── ChannelGroupHeader
│   │   │   ├── Chevron Icon
│   │   │   ├── MessageSquare Icon (blue)
│   │   │   ├── Label ("DIRECT MESSAGES")
│   │   │   └── Count Badge
│   │   └── Channel Items (if expanded)
│   │       └── ChannelItem (Alice Johnson)
│   │           ├── Avatar (with online dot if unread)
│   │           ├── Channel Info
│   │           │   ├── Name + Timestamp
│   │           │   └── Last Message Preview
│   │           └── Unread Badge [2]
│   │
│   ├── Channel Group: GROUPS
│   │   ├── ChannelGroupHeader
│   │   │   ├── Chevron Icon
│   │   │   ├── Users Icon (violet)
│   │   │   ├── Label ("GROUPS")
│   │   │   └── Count Badge
│   │   └── Channel Items (if expanded)
│   │       ├── ChannelItem (Engineering Team)
│   │       │   ├── Icon (Users in circle)
│   │       │   ├── Channel Info
│   │       │   │   ├── Name + Timestamp
│   │       │   │   └── Last Message Preview
│   │       │   └── Unread Badge [1]
│   │       └── ChannelItem (Product Updates)
│   │           ├── Icon (Users in circle)
│   │           └── Channel Info
│   │               ├── Name + Timestamp
│   │               └── Last Message Preview
│   │
│   └── Channel Group: TOPICS
│       ├── ChannelGroupHeader
│       │   ├── Chevron Icon
│       │   ├── Hash Icon (emerald)
│       │   ├── Label ("TOPICS")
│       │   └── Count Badge
│       └── Channel Items (if expanded)
│           ├── ChannelItem (Q1 Planning)
│           │   ├── Icon (Hash in circle)
│           │   └── Channel Info
│           │       ├── Name + Timestamp
│           │       └── Last Message Preview
│           └── ChannelItem (Support Tickets)
│               ├── Icon (Hash in circle)
│               └── Channel Info
│                   ├── Name + Timestamp
│                   └── Last Message Preview
│
└── Footer Section
    └── New Channel Button
```

---

## 🔄 Data Flow

```
1. Component Mount
   ↓
2. useGetChannelsQuery() → RTK Query
   ↓
3. API Request to /discuss/api/v1/channels
   ↓
4. Mock API returns 500ms delayed response
   ↓
5. Response: PaginatedResponse<Channel>
   ↓
6. useMemo: Group by type (DIRECT, GROUP, TOPIC)
   ↓
7. useMemo: Filter by searchQuery
   ↓
8. useMemo: Sort by lastMessageAt
   ↓
9. Render ChannelList
   ↓
10. User clicks channel
    ↓
11. onChannelSelect(channel) callback
    ↓
12. Parent updates selectedChannelId
    ↓
13. ChannelItem re-renders with isActive=true
```

---

## 🎨 State Management

### Local Component State
```typescript
// ChannelList.tsx
const [searchQuery, setSearchQuery] = useState('');
const [expandedGroups, setExpandedGroups] = useState<ExpandedState>({
  DIRECT: true,
  GROUP: true,
  TOPIC: true,
});
```

### RTK Query State (Global)
```typescript
// Redux store managed by RTK Query
{
  discuss: {
    queries: {
      'getChannels({"filters":{},"pagination":{"page":1,"limit":100}})': {
        status: 'fulfilled',
        data: {
          success: true,
          message: 'Channels fetched successfully',
          data: {
            data: [/* 5 channels */],
            total: 5,
            page: 1,
            limit: 100
          }
        }
      }
    }
  }
}
```

### Parent Component State (Demo Page)
```typescript
// page.tsx
const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
```

---

## 🧩 Props Interface

### ChannelList Props
```typescript
interface ChannelListProps {
  onChannelSelect: (channel: Channel) => void;  // Callback for selection
  selectedChannelId?: string;                    // Currently active channel ID
  className?: string;                            // Optional CSS classes
}
```

### ChannelItem Props
```typescript
interface ChannelItemProps {
  channel: Channel;                              // Channel data
  isActive?: boolean;                            // Whether this channel is selected
  onClick: (channel: Channel) => void;           // Click handler
}
```

### ChannelGroupHeader Props
```typescript
interface ChannelGroupHeaderProps {
  type: ChannelType;                             // DIRECT | GROUP | TOPIC
  count: number;                                 // Number of channels in group
  isExpanded: boolean;                           // Expanded state
  onToggle: () => void;                          // Toggle expand/collapse
}
```

---

## 🔌 External Dependencies

### Shadcn UI Components
```typescript
import {
  Input,          // Search input
  ScrollArea,     // Scrollable container
  Button,         // New Channel button
  Avatar,         // User/channel avatars
  AvatarFallback, // Fallback initials
  AvatarImage,    // Avatar image
  Badge,          // Unread count badges
} from '@/shared/components/ui';
```

### Lucide Icons
```typescript
import {
  Search,         // Search icon
  Plus,           // New Channel icon
  Loader2,        // Loading spinner
  AlertCircle,    // Error icon
  Hash,           // Topic channels
  Users,          // Group channels
  MessageSquare,  // Direct messages
  Lock,           // Archived channels
  ChevronDown,    // Expand/collapse
} from 'lucide-react';
```

### RTK Query Hooks
```typescript
import {
  useGetChannelsQuery,   // Fetch channels query
  // Future hooks:
  // useGetChannelQuery,
  // useCreateChannelMutation,
  // etc.
} from '../api/discussApi';
```

### Type Definitions
```typescript
import type {
  Channel,        // Main channel interface
  ChannelType,    // 'DIRECT' | 'GROUP' | 'TOPIC'
  // Other types available:
  // Message, Activity, UserPresence, etc.
} from '../types';
```

---

## 🎯 Event Handlers

### User Interactions
```typescript
// Search input
onChange={(e) => setSearchQuery(e.target.value)}

// Group header click
onClick={() => toggleGroup(type)}

// Channel item click
onClick={() => onChannelSelect(channel)}

// New Channel button click
onClick={() => {/* Not implemented yet */}}
```

### Toggle Group Logic
```typescript
const toggleGroup = (type: ChannelType) => {
  setExpandedGroups((prev) => ({
    ...prev,
    [type]: !prev[type],
  }));
};
```

---

## 🧮 Computed Values (useMemo)

### Grouped Channels
```typescript
const groupedChannels = useMemo(() => {
  const channels = channelsResponse?.data?.data || [];
  
  // 1. Filter by search
  const filtered = channels.filter((channel: Channel) =>
    channel.name.toLowerCase().includes(searchQuery.toLowerCase())
  );
  
  // 2. Group by type
  const groups = { DIRECT: [], GROUP: [], TOPIC: [] };
  filtered.forEach((channel: Channel) => {
    if (!channel.isArchived) {
      groups[channel.type].push(channel);
    }
  });
  
  // 3. Sort by last message time
  Object.keys(groups).forEach((type) => {
    groups[type].sort((a, b) => {
      const timeA = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0;
      const timeB = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0;
      return timeB - timeA;
    });
  });
  
  return groups;
}, [channelsResponse, searchQuery]);
```

### Total Unread Count
```typescript
const totalUnread = useMemo(() => {
  const channels = channelsResponse?.data?.data || [];
  return channels.reduce(
    (sum: number, channel: Channel) => sum + channel.unreadCount,
    0
  );
}, [channelsResponse]);
```

---

## 🎨 Styling System

### Color Palette
```typescript
const COLORS = {
  // Primary gradients
  header: 'from-violet-600 to-fuchsia-600',
  activeChannel: 'from-violet-500/10 to-fuchsia-500/10',
  avatar: 'from-violet-500 to-fuchsia-500',
  unreadBadge: 'from-rose-500 to-pink-500',
  
  // Type-specific colors
  direct: 'text-blue-500',
  group: 'text-violet-500',
  topic: 'text-emerald-500',
  
  // States
  hover: 'from-slate-100 to-slate-50',
  focus: 'ring-violet-500',
};
```

### Spacing System
```typescript
const SPACING = {
  padding: {
    container: 'px-4 py-3',
    item: 'px-3 py-2.5',
    badge: 'px-2 py-0.5',
  },
  gaps: {
    list: 'space-y-1',
    item: 'gap-3',
    header: 'gap-2',
  },
};
```

### Typography
```typescript
const TYPOGRAPHY = {
  header: 'text-lg font-bold',
  groupLabel: 'text-xs font-bold uppercase tracking-wider',
  channelName: 'text-sm font-semibold',
  preview: 'text-xs',
  timestamp: 'text-xs',
  count: 'text-xs font-semibold',
};
```

---

## ⚡ Performance Optimizations

### 1. Memoization
- ✅ `useMemo` for channel grouping (expensive operation)
- ✅ `useMemo` for unread count (array reduce)

### 2. Lazy Rendering
- ✅ `ScrollArea` for virtual scrolling (when many channels)
- ✅ Collapsed groups don't render child items

### 3. Event Delegation
- ✅ Single click handler per channel item
- ✅ No inline arrow functions in render

### 4. Conditional Rendering
```typescript
// Only render channels if group is expanded
{expandedGroups[type] && (
  <div className="pl-2 space-y-0.5">
    {channels.map((channel) => (
      <ChannelItem key={channel.id} ... />
    ))}
  </div>
)}
```

---

## 🔐 Type Safety

### Channel Interface
```typescript
interface Channel {
  id: string;
  name: string;
  type: ChannelType;
  description?: string;
  avatarUrl?: string;
  memberCount: number;
  unreadCount: number;
  lastMessage?: string;
  lastMessageAt?: string;
  isArchived: boolean;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}
```

### RTK Query Response Type
```typescript
APIResponse<PaginatedResponse<Channel>>

// Expands to:
{
  success: boolean;
  message: string;
  data: {
    data: Channel[];
    total: number;
    page: number;
    limit: number;
  }
}
```

---

## 🧪 Testing Strategy

### Unit Tests (Future)
```typescript
describe('ChannelList', () => {
  it('renders all channels from RTK Query');
  it('filters channels by search query');
  it('groups channels by type');
  it('sorts channels by last message time');
  it('shows loading state while fetching');
  it('shows error state on API failure');
  it('calculates total unread count correctly');
});

describe('ChannelItem', () => {
  it('displays unread badge when unreadCount > 0');
  it('highlights when isActive is true');
  it('calls onClick when clicked');
  it('formats timestamp correctly');
});

describe('ChannelGroupHeader', () => {
  it('displays correct icon for each type');
  it('shows channel count');
  it('toggles expand/collapse on click');
  it('rotates chevron icon on toggle');
});
```

### Integration Tests (Future)
```typescript
describe('ChannelList Integration', () => {
  it('selects channel and updates parent state');
  it('searches and filters across all groups');
  it('expands/collapses groups independently');
});
```

---

## 📦 File Structure

```
src/modules/discuss/components/
├── ChannelList.tsx           (200 lines)
│   ├── Header section
│   ├── ScrollArea with groups
│   └── Footer with New Channel button
│
├── ChannelItem.tsx            (170 lines)
│   ├── Avatar/Icon
│   ├── Channel info (name, preview, timestamp)
│   └── Unread badge
│
├── ChannelGroupHeader.tsx     (90 lines)
│   ├── Expand/collapse chevron
│   ├── Type icon
│   ├── Label
│   └── Count badge
│
└── index.ts                   (5 lines)
    └── Barrel exports
```

---

## 🚀 Future Enhancements

### Week 8 Day 3-4 (Next)
- [ ] ChatWindow component
- [ ] MessageList with infinite scroll
- [ ] MessageInput with rich text
- [ ] Typing indicators

### Week 8 Day 5
- [ ] Emoji picker
- [ ] Message reactions
- [ ] Thread replies
- [ ] Read receipts
- [ ] Online status indicators

### Future Features
- [ ] Channel context menu (right-click)
- [ ] Drag to reorder channels
- [ ] Pin/unpin channels
- [ ] Mute notifications
- [ ] Channel settings
- [ ] Archive/delete channels

---

*Last Updated: Week 8 Day 1-2 Completion*  
*Author: QuanTuanHuy*  
*Part of Serp Project - Discuss Module*
