import { useMemo } from 'react';
import { MessageSquare, Phone, Mail, Calendar } from 'lucide-react';
import type { Activity } from '../../../../types';
import type { Note } from '../../../../api/noteApi';
import { Avatar, AvatarFallback } from '@/shared/components/ui';

interface UnifiedTimelineProps {
  activities: Activity[];
  notes: Note[];
  isLoading: boolean;
}

type TimelineItem =
  | { type: 'note'; id: string; timestamp: string; user: string; content: string }
  | { type: 'activity'; id: string; timestamp: string; user: string; subject: string; subtype: string; status: string; content?: string };

export function UnifiedTimeline({ activities, notes, isLoading }: UnifiedTimelineProps) {
  const timelineItems = useMemo(() => {
    const items: TimelineItem[] = [];

    notes.forEach((n) => {
      items.push({
        type: 'note',
        id: n.id,
        timestamp: n.createdAt,
        user: n.createdBy || 'User',
        content: n.content,
      });
    });

    activities.forEach((a) => {
      items.push({
        type: 'activity',
        id: String(a.id),
        timestamp: a.scheduledDate || a.createdAt || '',
        user: a.assignedToName || (a.assignedTo ? `User #${a.assignedTo}` : 'System'),
        subject: a.subject || 'Activity log',
        subtype: a.type || 'CALL',
        status: a.status || 'PLANNED',
        content: a.description,
      });
    });

    // Sort chronological descending
    return items.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  }, [activities, notes]);

  const formatTime = (timeStr: string) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (isLoading) {
    return <div className="text-center py-8 text-muted-foreground text-sm">Loading timeline activities...</div>;
  }

  if (timelineItems.length === 0) {
    return (
      <div className="text-center py-12 border border-dashed rounded-xl text-muted-foreground text-sm">
        No interaction history logged yet. Use the composer above to add notes or activities.
      </div>
    );
  }

  return (
    <div className="relative pl-6 border-l border-muted/50 ml-4 space-y-6">
      {timelineItems.map((item) => {
        const isNote = item.type === 'note';
        const initials = item.user.slice(0, 2).toUpperCase();

        return (
          <div key={`${item.type}-${item.id}`} className="relative group">
            {/* Timeline marker */}
            <div className="absolute -left-[37px] top-1.5 flex h-6 w-6 items-center justify-center rounded-full bg-background border border-muted shadow-sm">
              {isNote ? (
                <MessageSquare className="h-3.5 w-3.5 text-amber-500" />
              ) : item.subtype === 'CALL' ? (
                <Phone className="h-3.5 w-3.5 text-blue-500" />
              ) : item.subtype === 'EMAIL' ? (
                <Mail className="h-3.5 w-3.5 text-purple-500" />
              ) : (
                <Calendar className="h-3.5 w-3.5 text-green-500" />
              )}
            </div>

            {/* Box Details */}
            <div className="bg-card p-4 rounded-xl border border-muted/40 shadow-sm space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Avatar className="h-6 w-6">
                    <AvatarFallback className="text-[10px] bg-muted">{initials}</AvatarFallback>
                  </Avatar>
                  <span className="text-xs font-semibold text-foreground">{item.user}</span>
                  <span className="text-xs text-muted-foreground font-normal">
                    {isNote ? 'added a note' : `logged an activity (${item.subtype.toLowerCase()})`}
                  </span>
                </div>
                <span className="text-[10px] text-muted-foreground">{formatTime(item.timestamp)}</span>
              </div>

              <div className="text-sm font-medium text-foreground">
                {isNote ? null : item.subject}
              </div>

              {isNote ? (
                <p className="text-sm text-foreground/80 whitespace-pre-wrap leading-relaxed">{item.content}</p>
              ) : item.content ? (
                <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed bg-muted/20 p-2 rounded-md border border-muted/20">
                  {item.content}
                </p>
              ) : null}

              {!isNote && (
                <div className="flex justify-end">
                  <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground border px-1.5 py-0.5 rounded">
                    {item.status}
                  </span>
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
