'use client';

import { useState } from 'react';
import { Phone, Mail, Users, FileText, CheckCircle2 } from 'lucide-react';
import { Card, Button, Input, Textarea, Tabs, TabsList, TabsTrigger, TabsContent } from '@/shared/components/ui';

interface QuickComposerProps {
  onAddNote: (content: string) => Promise<void>;
  onAddActivity: (data: { subject: string; type: string; notes: string }) => Promise<void>;
}

export function QuickComposer({ onAddNote, onAddActivity }: QuickComposerProps) {
  const [noteContent, setNoteContent] = useState('');
  const [activitySubject, setActivitySubject] = useState('');
  const [activityType, setActivityType] = useState('CALL');
  const [activityNotes, setActivityNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [activeTab, setActiveTab] = useState('note');

  const handleNoteSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!noteContent.trim()) return;
    setIsSubmitting(true);
    try {
      await onAddNote(noteContent);
      setNoteContent('');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleActivitySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activitySubject.trim()) return;
    setIsSubmitting(true);
    try {
      await onAddActivity({
        subject: activitySubject,
        type: activityType,
        notes: activityNotes,
      });
      setActivitySubject('');
      setActivityNotes('');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <div className="bg-muted/30 border-b border-muted/40 px-4 py-2">
          <TabsList className="bg-transparent border-0 gap-2 h-auto p-0">
            <TabsTrigger value="note" className="text-xs gap-1.5 h-8 data-[state=active]:bg-background data-[state=active]:shadow-sm">
              <FileText className="h-3.5 w-3.5" /> Note
            </TabsTrigger>
            <TabsTrigger value="activity" className="text-xs gap-1.5 h-8 data-[state=active]:bg-background data-[state=active]:shadow-sm">
              <CheckCircle2 className="h-3.5 w-3.5" /> Activity
            </TabsTrigger>
          </TabsList>
        </div>

        <TabsContent value="note" className="p-4 m-0">
          <form onSubmit={handleNoteSubmit} className="space-y-3">
            <Textarea
              placeholder="Jot down a quick remark about this account..."
              value={noteContent}
              onChange={(e) => setNoteContent(e.target.value)}
              rows={2}
              className="text-xs border-muted/70 focus-visible:ring-1 focus-visible:ring-primary"
            />
            <div className="flex justify-end">
              <Button type="submit" size="sm" disabled={isSubmitting || !noteContent.trim()}>
                Add Note
              </Button>
            </div>
          </form>
        </TabsContent>

        <TabsContent value="activity" className="p-4 m-0">
          <form onSubmit={handleActivitySubmit} className="space-y-3">
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2">
                <Input
                  placeholder="Subject (e.g. Discuss Q3 proposal)"
                  value={activitySubject}
                  onChange={(e) => setActivitySubject(e.target.value)}
                  className="h-8 text-xs"
                />
              </div>
              <div className="col-span-1">
                <select
                  value={activityType}
                  onChange={(e) => setActivityType(e.target.value)}
                  className="w-full h-8 px-2 text-xs border border-input bg-background rounded-md text-foreground"
                >
                  <option value="CALL">Call</option>
                  <option value="EMAIL">Email</option>
                  <option value="MEETING">Meeting</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
            </div>
            <Textarea
              placeholder="Additional details/outcomes..."
              value={activityNotes}
              onChange={(e) => setActivityNotes(e.target.value)}
              rows={2}
              className="text-xs"
            />
            <div className="flex justify-end">
              <Button type="submit" size="sm" disabled={isSubmitting || !activitySubject.trim()}>
                Log Activity
              </Button>
            </div>
          </form>
        </TabsContent>
      </Tabs>
    </Card>
  );
}
