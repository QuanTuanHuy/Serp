import { useState } from 'react';
import { Button, Textarea, Tabs, TabsList, TabsTrigger, TabsContent, Input, Label } from '@/shared/components/ui';
import { MessageSquare, Calendar, Loader2 } from 'lucide-react';

interface QuickComposerProps {
  leadId: string;
  onAddNote: (content: string) => Promise<void>;
  onAddActivity: (data: { subject: string; type: string; notes?: string }) => Promise<void>;
}

export function QuickComposer({ leadId, onAddNote, onAddActivity }: QuickComposerProps) {
  const [activeTab, setActiveTab] = useState('note');
  const [noteContent, setNoteContent] = useState('');
  const [activityForm, setActivityForm] = useState({ subject: '', type: 'CALL', notes: '' });
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
      await onAddActivity({
        subject: activityForm.subject.trim(),
        type: activityForm.type,
        notes: activityForm.notes.trim() || undefined,
      });
      setActivityForm({ subject: '', type: 'CALL', notes: '' });
    } catch (err) {
      // Handled in parent
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="border border-muted/50 rounded-xl bg-card text-card-foreground shadow-sm overflow-hidden">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="w-full justify-start rounded-none border-b border-muted/50 bg-muted/20 px-4 h-12">
          <TabsTrigger value="note" className="flex items-center gap-2 text-sm data-[state=active]:bg-background">
            <MessageSquare className="h-4 w-4" /> Add Note
          </TabsTrigger>
          <TabsTrigger value="activity" className="flex items-center gap-2 text-sm data-[state=active]:bg-background">
            <Calendar className="h-4 w-4" /> Log Activity
          </TabsTrigger>
        </TabsList>

        <TabsContent value="note" className="p-4 space-y-3 mt-0">
          <Textarea
            placeholder="Write a note about this lead..."
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
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="activitySubject" className="text-xs">Subject</Label>
              <Input
                id="activitySubject"
                placeholder="e.g., Follow up call"
                value={activityForm.subject}
                onChange={(e) => setActivityForm(prev => ({ ...prev, subject: e.target.value }))}
                className="h-8 text-sm"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="activityType" className="text-xs">Type</Label>
              <select
                id="activityType"
                value={activityForm.type}
                onChange={(e) => setActivityForm(prev => ({ ...prev, type: e.target.value }))}
                className="w-full h-8 px-2 text-sm border rounded-md bg-background focus:outline-none focus:ring-1 focus:ring-ring"
              >
                <option value="CALL">Call</option>
                <option value="EMAIL">Email</option>
                <option value="MEETING">Meeting</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
          <div className="space-y-1">
            <Label htmlFor="activityNotes" className="text-xs">Notes (optional)</Label>
            <Textarea
              id="activityNotes"
              placeholder="What did you discuss?"
              value={activityForm.notes}
              onChange={(e) => setActivityForm(prev => ({ ...prev, notes: e.target.value }))}
              className="resize-none text-sm min-h-[60px]"
            />
          </div>
          <div className="flex justify-end pt-2 border-t border-muted/30">
            <Button size="sm" onClick={handleSaveActivity} disabled={isSubmitting || !activityForm.subject.trim()}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Log Activity
            </Button>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
