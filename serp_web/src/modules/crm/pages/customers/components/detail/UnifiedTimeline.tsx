'use client';

import { useMemo } from 'react';
import { Calendar, Phone, Mail, Users, FileText, CheckCircle2, Clock } from 'lucide-react';
import { Card, CardContent, Badge } from '@/shared/components/ui';
import type { Activity } from '../../../../types';

interface NoteItem {
  id: string | number;
  content: string;
  createdAt: string;
  createdBy?: string | number;
  createdByName?: string;
}

interface UnifiedTimelineProps {
  activities: Activity[];
  notes: NoteItem[];
  isLoading: boolean;
  getUserName: (userId?: string | number) => string;
}

export function UnifiedTimeline({ activities, notes, isLoading, getUserName }: UnifiedTimelineProps) {
  const mergedItems = useMemo(() => {
    const list: {
      id: string;
      timestamp: number;
      type: 'activity' | 'note';
      icon: any;
      title: string;
      description: string;
      creator: string;
      status?: string;
    }[] = [];

    activities.forEach((act) => {
      let icon = CheckCircle2;
      if (act.type === 'CALL') icon = Phone;
      else if (act.type === 'EMAIL') icon = Mail;
      else if (act.type === 'MEETING') icon = Users;

      list.push({
        id: `activity-${act.id}`,
        timestamp: act.scheduledDate ? new Date(act.scheduledDate).getTime() : Date.now(),
        type: 'activity',
        icon,
        title: act.subject,
        description: act.description || 'No description provided.',
        creator: act.assignedToName || 'System',
        status: act.status,
      });
    });

    notes.forEach((nt) => {
      list.push({
        id: `note-${nt.id}`,
        timestamp: nt.createdAt ? new Date(nt.createdAt).getTime() : Date.now(),
        type: 'note',
        icon: FileText,
        title: 'Static Note Added',
        description: nt.content,
        creator: nt.createdByName || getUserName(nt.createdBy),
      });
    });

    return list.sort((a, b) => b.timestamp - a.timestamp);
  }, [activities, notes, getUserName]);

  if (isLoading) {
    return <div className="text-center py-8 text-muted-foreground text-xs">Loading interaction stream...</div>;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
        <Clock className="h-4 w-4" /> Interaction & Activity Timeline
      </h3>

      {mergedItems.length === 0 ? (
        <Card className="border border-dashed border-muted/60 p-8 text-center rounded-xl">
          <Calendar className="mx-auto mb-2.5 h-10 w-10 text-muted-foreground/60" />
          <p className="text-xs text-muted-foreground font-medium">No history logged yet. Use the composer above to add notes or log tasks.</p>
        </Card>
      ) : (
        <div className="relative pl-5 space-y-4 before:absolute before:left-[9px] before:top-2 before:bottom-2 before:w-[2px] before:bg-muted/70">
          {mergedItems.map((item) => {
            const Icon = item.icon;
            const formattedTime = new Date(item.timestamp).toLocaleString('vi-VN', {
              month: 'short',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            });

            return (
              <div key={item.id} className="relative group animate-in fade-in duration-300">
                {/* Timeline dot */}
                <div className={`absolute -left-[23px] top-1.5 h-4 w-4 rounded-full border bg-background flex items-center justify-center transition-all ${
                  item.type === 'note' ? 'border-amber-400 text-amber-500' : 'border-blue-400 text-blue-500'
                }`}>
                  <Icon className="h-2.5 w-2.5" />
                </div>

                <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden hover:border-muted-foreground/30 transition-all duration-300">
                  <CardContent className="p-4 space-y-2">
                    <div className="flex justify-between items-start gap-4">
                      <div>
                        <h4 className="text-sm font-bold text-foreground leading-snug">{item.title}</h4>
                        <div className="text-[10px] text-muted-foreground/80 mt-0.5">
                          Logged by <span className="font-semibold">{item.creator}</span> • {formattedTime}
                        </div>
                      </div>
                      {item.status && (
                        <Badge variant={item.status === 'COMPLETED' ? 'default' : 'secondary'} className="text-[9px] uppercase px-1.5 h-5">
                          {item.status}
                        </Badge>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground leading-relaxed whitespace-pre-wrap">{item.description}</p>
                  </CardContent>
                </Card>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
