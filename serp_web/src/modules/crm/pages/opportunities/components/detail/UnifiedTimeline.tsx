'use client';

import { useMemo } from 'react';
import { MessageSquare, Phone, Mail, Calendar, User } from 'lucide-react';
import type { Activity, Note } from '../../../../types';
import { Avatar, AvatarFallback } from '@/shared/components/ui';

interface UnifiedTimelineProps {
  activities: Activity[];
  notes: Note[];
  isLoading: boolean;
  getUserName?: (userId?: string | number) => string;
}

type TimelineItem =
  | { type: 'note'; id: string; timestamp: string; user: string; content: string }
  | { type: 'activity'; id: string; timestamp: string; user: string; subject: string; subtype: string; status: string; content?: string };

export function UnifiedTimeline({ activities, notes, isLoading, getUserName }: UnifiedTimelineProps) {
  const timelineItems = useMemo(() => {
    const items: TimelineItem[] = [];

    notes.forEach((n) => {
      items.push({
        type: 'note',
        id: n.id,
        timestamp: n.createdAt,
        user: getUserName ? getUserName(n.createdBy) : (n.createdBy || 'User'),
        content: n.content,
      });
    });

    activities.forEach((a) => {
      items.push({
        type: 'activity',
        id: String(a.id),
        timestamp: a.scheduledDate || a.createdAt || '',
        user: a.assignedToName || (getUserName ? getUserName(a.assignedTo) : (a.assignedTo ? `User #${a.assignedTo}` : 'System')),
        subject: a.subject || 'Activity log',
        subtype: a.type || 'CALL',
        status: a.status || 'PLANNED',
        content: a.description,
      });
    });

    // Sort chronological descending
    return items.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  }, [activities, notes, getUserName]);

  const formatTime = (timeStr: string) => {
    if (!timeStr) return '';
    return new Date(timeStr).toLocaleString();
  };

  if (isLoading) {
    return <div className="text-center py-8 text-sm text-muted-foreground">Loading history...</div>;
  }

  if (timelineItems.length === 0) {
    return <div className="text-center py-12 border border-dashed rounded-xl text-muted-foreground text-sm">No activity recorded yet. Start interacting above!</div>;
  }

  return (
    <div className="relative pl-6 space-y-6 before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[2px] before:bg-muted/80">
      {timelineItems.map((item) => (
        <div key={`${item.type}-${item.id}`} className="relative group">
          {/* Left timeline circle icon */}
          <div className="absolute -left-[27px] top-1.5 flex h-[24px] w-[24px] items-center justify-center rounded-full border bg-background shadow-sm text-muted-foreground">
            {item.type === 'note' ? (
              <MessageSquare className="h-3 w-3" />
            ) : item.subtype === 'CALL' ? (
              <Phone className="h-3 w-3" />
            ) : item.subtype === 'EMAIL' ? (
              <Mail className="h-3 w-3" />
            ) : (
              <Calendar className="h-3 w-3" />
            )}
          </div>

          <div className="border border-muted/40 bg-card/60 p-4 rounded-xl shadow-sm transition hover:shadow-md hover:border-muted/70">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1.5 mb-2">
              <div className="flex items-center gap-2">
                <Avatar className="h-5 w-5">
                  <AvatarFallback className="text-[9px] bg-muted/80"><User className="h-2.5 w-2.5" /></AvatarFallback>
                </Avatar>
                <span className="text-xs font-semibold text-foreground">{item.user}</span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-muted/60 text-muted-foreground">
                  {item.type === 'note' ? 'Note' : 'Activity'}
                </span>
              </div>
              <span className="text-[10px] text-muted-foreground">{formatTime(item.timestamp)}</span>
            </div>

            {item.type === 'note' ? (
              <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap">{item.content}</p>
            ) : (
              <div className="space-y-1.5">
                <p className="text-xs font-semibold text-foreground">{item.subject}</p>
                {item.content && (
                  <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed">{item.content}</p>
                )}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
