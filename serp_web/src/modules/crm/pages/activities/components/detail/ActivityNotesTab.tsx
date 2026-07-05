'use client';

import { MessageSquare } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';

interface NoteItem {
  id: string;
  author: string;
  content: string;
  createdAt: string;
}

interface ActivityNotesTabProps {
  notes: NoteItem[];
}

export function ActivityNotesTab({ notes }: ActivityNotesTabProps) {
  return (
    <div className="space-y-4">
      {notes.length > 0 ? (
        <div className="space-y-3">
          {notes.map((note) => {
            const initials = note.author
              .split(' ')
              .map((n) => n[0])
              .join('')
              .toUpperCase()
              .slice(0, 2) || '?';

            return (
              <Card key={note.id} className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
                <CardContent className="p-4 space-y-2">
                  <div className="flex items-center justify-between gap-4">
                    <div className="flex items-center gap-2">
                      <Avatar className="h-6 w-6">
                        <AvatarFallback className="text-[10px] bg-muted font-bold">{initials}</AvatarFallback>
                      </Avatar>
                      <span className="text-xs font-bold">{note.author}</span>
                    </div>
                    <span className="text-[10px] text-muted-foreground">
                      {new Date(note.createdAt).toLocaleDateString('vi-VN', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </div>
                  <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap pl-8">
                    {note.content}
                  </p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      ) : (
        <Card className="border border-dashed border-muted/60 p-8 text-center rounded-xl">
          <MessageSquare className="mx-auto mb-2 h-8 w-8 text-muted-foreground/50" />
          <p className="text-xs text-muted-foreground font-medium">No internal notes added to this activity yet.</p>
        </Card>
      )}
    </div>
  );
}
