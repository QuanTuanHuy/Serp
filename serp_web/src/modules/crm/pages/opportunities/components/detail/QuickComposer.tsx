'use client';

import { useState } from 'react';
import { MessageSquare, Calendar, Loader2 } from 'lucide-react';
import { Button, Card, Tabs, TabsList, TabsTrigger, TabsContent, Textarea, Input, Label, Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/shared/components/ui';

interface QuickComposerProps {
  opportunityId: string;
  onAddNote: (content: string) => Promise<void>;
  onAddActivity: (data: any) => Promise<void>;
}

export function QuickComposer({ opportunityId, onAddNote, onAddActivity }: QuickComposerProps) {
  const [activeTab, setActiveTab] = useState('note');
  const [noteContent, setNoteContent] = useState('');
  const [activityForm, setActivityForm] = useState({
    subject: '',
    type: 'CALL',
    notes: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSaveNote = async () => {
    if (!noteContent.trim()) return;
    setIsSubmitting(true);
    try {
      await onAddNote(noteContent.trim());
      setNoteContent('');
    } catch (err) {
      // Handled in parent
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSaveActivity = async () => {
    if (!activityForm.subject.trim()) return;
    setIsSubmitting(true);
    try {
      await onAddActivity(activityForm);
      setActivityForm({ subject: '', type: 'CALL', notes: '' });
    } catch (err) {
      // Handled in parent
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="w-full justify-start rounded-none border-b border-muted/30 bg-muted/20 p-0 h-11">
          <TabsTrigger value="note" className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent px-5 h-full">
            <MessageSquare className="h-4 w-4 mr-2" /> Add Note
          </TabsTrigger>
          <TabsTrigger value="activity" className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent px-5 h-full">
            <Calendar className="h-4 w-4 mr-2" /> Log Activity
          </TabsTrigger>
        </TabsList>

        <TabsContent value="note" className="p-4 space-y-3 mt-0">
          <Textarea
            placeholder="Write a note about this opportunity..."
            value={noteContent}
            onChange={(e) => setNoteContent(e.target.value)}
            className="resize-none border-none focus-visible:ring-0 p-0 text-sm min-h-[80px]"
          />
          <div className="flex justify-end pt-2 border-t border-muted/30">
            <Button size="sm" onClick={handleSaveNote} disabled={isSubmitting || !noteContent.trim()}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Save Note
            </Button>
          </div>
        </TabsContent>

        <TabsContent value="activity" className="p-4 space-y-4 mt-0">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="md:col-span-2 space-y-1.5">
              <Label htmlFor="subject" className="text-xs">Subject *</Label>
              <Input
                id="subject"
                placeholder="e.g. Discussed pricing proposal"
                value={activityForm.subject}
                onChange={(e) => setActivityForm({ ...activityForm, subject: e.target.value })}
                className="h-8 text-xs"
              />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">Type</Label>
              <Select value={activityForm.type} onValueChange={(v) => setActivityForm({ ...activityForm, type: v })}>
                <SelectTrigger className="h-8 text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CALL">Call</SelectItem>
                  <SelectItem value="EMAIL">Email</SelectItem>
                  <SelectItem value="MEETING">Meeting</SelectItem>
                  <SelectItem value="OTHER">Task</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="actNotes" className="text-xs">Description</Label>
            <Textarea
              id="actNotes"
              placeholder="Log call summary or meeting highlights..."
              value={activityForm.notes}
              onChange={(e) => setActivityForm({ ...activityForm, notes: e.target.value })}
              className="resize-none text-xs min-h-[60px]"
            />
          </div>
          <div className="flex justify-end pt-2 border-t border-muted/30">
            <Button size="sm" onClick={handleSaveActivity} disabled={isSubmitting || !activityForm.subject.trim()}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Log Activity
            </Button>
          </div>
        </TabsContent>
      </Tabs>
    </Card>
  );
}
