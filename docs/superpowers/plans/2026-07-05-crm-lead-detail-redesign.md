# CRM Lead Detail Page UX/UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the CRM Lead Detail Page (`LeadDetailPageEnhanced.tsx`) into a modern, 3-column action-oriented workspace with inline editing, a unified activity timeline, and a quick composer.

**Architecture:** Decompose the massive `LeadDetailPageEnhanced.tsx` into four focused sub-components under `pages/leads/components/detail/` to ensure high maintainability, single-responsibility, and easy re-rendering optimization. Fetch data in parallel in the parent page shell and propagate states via props.

**Tech Stack:** React 19, Next.js 16 (App Router), RTK Query, Lucide Icons, Radix UI primitives (shadcn/ui), Tailwind CSS.

---

## Bố cục & Cấu trúc Thư mục mới
Chúng ta sẽ tạo thư mục con chứa các sub-component chuyên biệt để chia nhỏ code:
- `serp_web/src/modules/crm/pages/leads/components/detail/LeadProfileSidebar.tsx` (Sidebar trái)
- `serp_web/src/modules/crm/pages/leads/components/detail/QuickComposer.tsx` (Trình soạn thảo nhanh)
- `serp_web/src/modules/crm/pages/leads/components/detail/UnifiedTimeline.tsx` (Dòng thời gian hợp nhất)
- `serp_web/src/modules/crm/pages/leads/components/detail/InsightsSidebar.tsx` (Sidebar phải)

---

## Các Nhiệm vụ chi tiết (Tasks)

### Task 1: Thiết lập thư mục và component rỗng (Scaffolding)

**Files:**
- Create: `serp_web/src/modules/crm/pages/leads/components/detail/LeadProfileSidebar.tsx`
- Create: `serp_web/src/modules/crm/pages/leads/components/detail/QuickComposer.tsx`
- Create: `serp_web/src/modules/crm/pages/leads/components/detail/UnifiedTimeline.tsx`
- Create: `serp_web/src/modules/crm/pages/leads/components/detail/InsightsSidebar.tsx`

- [ ] **Step 1: Khởi tạo file `LeadProfileSidebar.tsx` với skeleton**
  ```tsx
  import type { Lead } from '../../../../types';

  interface LeadProfileSidebarProps {
    lead: Lead;
    isUpdating: boolean;
    onUpdateLead: (data: Partial<Lead>) => Promise<void>;
  }

  export function LeadProfileSidebar({ lead, isUpdating, onUpdateLead }: LeadProfileSidebarProps) {
    return <div className="p-4 border rounded-xl bg-card text-card-foreground">Lead Profile Sidebar Skeleton</div>;
  }
  ```

- [ ] **Step 2: Khởi tạo file `QuickComposer.tsx` với skeleton**
  ```tsx
  interface QuickComposerProps {
    leadId: string;
    onAddNote: (content: string) => Promise<void>;
    onAddActivity: (data: { subject: string; type: string; notes?: string }) => Promise<void>;
  }

  export function QuickComposer({ leadId, onAddNote, onAddActivity }: QuickComposerProps) {
    return <div className="p-4 border rounded-xl bg-card text-card-foreground">Quick Composer Skeleton</div>;
  }
  ```

- [ ] **Step 3: Khởi tạo file `UnifiedTimeline.tsx` với skeleton**
  ```tsx
  import type { Activity } from '../../../../types';
  import type { Note } from '../../../../api/noteApi';

  interface UnifiedTimelineProps {
    activities: Activity[];
    notes: Note[];
    isLoading: boolean;
  }

  export function UnifiedTimeline({ activities, notes, isLoading }: UnifiedTimelineProps) {
    return <div className="p-4 border rounded-xl bg-card text-card-foreground">Unified Timeline Skeleton</div>;
  }
  ```

- [ ] **Step 4: Khởi tạo file `InsightsSidebar.tsx` với skeleton**
  ```tsx
  import type { Lead } from '../../../../types';

  interface InsightsSidebarProps {
    lead: Lead;
    leadScore: number;
    onAssignLead: (assignedToId: number) => Promise<void>;
    onUpdateStatus: (toStatus: any, payload?: any) => Promise<void>;
    onOpenConvert: () => void;
    onOpenQualify: () => void;
    onOpenDisqualify: () => void;
    onOpenMeetingRequest: () => void;
  }

  export function InsightsSidebar({ lead, leadScore }: InsightsSidebarProps) {
    return <div className="p-4 border rounded-xl bg-card text-card-foreground">Insights Sidebar Skeleton</div>;
  }
  ```

- [ ] **Step 5: Chạy TypeScript check để kiểm tra lỗi biên dịch**
  Run: `npm run type-check` (ở thư mục `serp_web`)
  Expected: Command hoàn thành mà không có lỗi biên dịch liên quan đến các file mới tạo.

- [ ] **Step 6: Commit các file rỗng**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/components/detail/
  git commit -m "feat: scaffold lead detail sub-components"
  ```

---

### Task 2: Implement Component `LeadProfileSidebar` (Cột Trái)

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/components/detail/LeadProfileSidebar.tsx`

- [ ] **Step 1: Viết chi tiết mã nguồn cho `LeadProfileSidebar` hỗ trợ Inline Edit**
  Cấu trúc bao gồm: Avatar, tên lead, badge trạng thái/nguồn, và danh sách các trường thông tin click-to-edit hỗ trợ `Blur` tự động lưu hoặc nhấn `Enter` để lưu.
  
  ```tsx
  import { useState } from 'react';
  import { Edit, Mail, Phone, Building2, Briefcase, Calendar, DollarSign, Loader2 } from 'lucide-react';
  import { Avatar, AvatarFallback, Badge, Input } from '@/shared/components/ui';
  import type { Lead } from '../../../../types';

  interface LeadProfileSidebarProps {
    lead: Lead;
    isUpdating: boolean;
    onUpdateLead: (data: Partial<Lead>) => Promise<void>;
  }

  export function LeadProfileSidebar({ lead, isUpdating, onUpdateLead }: LeadProfileSidebarProps) {
    const [editingField, setEditingField] = useState<string | null>(null);
    const [editValue, setEditValue] = useState<string>('');

    const handleStartEdit = (field: string, val: string) => {
      setEditingField(field);
      setEditValue(val);
    };

    const handleSave = async (field: keyof Lead) => {
      if (editValue === String(lead[field])) {
        setEditingField(null);
        return;
      }
      try {
        let typedValue: any = editValue;
        if (field === 'estimatedValue') typedValue = Number(editValue) || 0;
        await onUpdateLead({ [field]: typedValue });
      } catch (err) {
        // Parent will toast error
      } finally {
        setEditingField(null);
      }
    };

    const handleKeyDown = (e: React.KeyboardEvent, field: keyof Lead) => {
      if (e.key === 'Enter') handleSave(field);
      if (e.key === 'Escape') setEditingField(null);
    };

    const initials = lead.name ? lead.name.slice(0, 2).toUpperCase() : 'LD';

    return (
      <div className="space-y-6">
        <div className="flex flex-col items-center text-center space-y-3">
          <Avatar className="h-20 w-20 ring-4 ring-muted shadow-sm">
            <AvatarFallback className="bg-gradient-to-tr from-blue-500 to-indigo-600 text-white text-xl font-bold">
              {initials}
            </AvatarFallback>
          </Avatar>
          <div>
            <h2 className="text-xl font-bold text-foreground">{lead.name || 'Unnamed Lead'}</h2>
            <div className="mt-1 flex items-center justify-center gap-2">
              <Badge variant="secondary">{lead.leadStatus}</Badge>
              <Badge variant="outline">{lead.leadSource}</Badge>
            </div>
          </div>
        </div>

        <div className="border-t border-muted/50 pt-4 space-y-4">
          <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Contact Info</h3>
          
          {/* Email field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Email</span>
            {editingField === 'email' ? (
              <Input
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('email')}
                onKeyDown={(e) => handleKeyDown(e, 'email')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('email', lead.email || '')} className="flex items-center gap-2 cursor-pointer mt-1">
                <Mail className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">{lead.email || 'Click to add email'}</span>
                <Edit className="h-3..w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>

          {/* Phone field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Phone</span>
            {editingField === 'phone' ? (
              <Input
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('phone')}
                onKeyDown={(e) => handleKeyDown(e, 'phone')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('phone', lead.phone || '')} className="flex items-center gap-2 cursor-pointer mt-1">
                <Phone className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">{lead.phone || 'Click to add phone'}</span>
                <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>

          {/* Company field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Company</span>
            {editingField === 'company' ? (
              <Input
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('company')}
                onKeyDown={(e) => handleKeyDown(e, 'company')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('company', lead.company || '')} className="flex items-center gap-2 cursor-pointer mt-1">
                <Building2 className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">{lead.company || 'Click to add company'}</span>
                <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>

          {/* Job Title field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Job Title</span>
            {editingField === 'jobTitle' ? (
              <Input
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('jobTitle')}
                onKeyDown={(e) => handleKeyDown(e, 'jobTitle')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('jobTitle', lead.jobTitle || '')} className="flex items-center gap-2 cursor-pointer mt-1">
                <Briefcase className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">{lead.jobTitle || 'Click to add job title'}</span>
                <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>

          {/* Estimated Value field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Estimated Value</span>
            {editingField === 'estimatedValue' ? (
              <Input
                type="number"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('estimatedValue')}
                onKeyDown={(e) => handleKeyDown(e, 'estimatedValue')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('estimatedValue', String(lead.estimatedValue || ''))} className="flex items-center gap-2 cursor-pointer mt-1">
                <DollarSign className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">
                  {lead.estimatedValue
                    ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(lead.estimatedValue)
                    : 'Click to add value'}
                </span>
                <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>

          {/* Follow Up Date field */}
          <div className="group relative py-1 rounded-md px-2 hover:bg-muted/30 transition-colors">
            <span className="text-xs text-muted-foreground block">Follow Up Date</span>
            {editingField === 'followUpDate' ? (
              <Input
                type="date"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={() => handleSave('followUpDate')}
                onKeyDown={(e) => handleKeyDown(e, 'followUpDate')}
                autoFocus
                className="h-8 py-1 mt-1 text-sm"
              />
            ) : (
              <div onClick={() => handleStartEdit('followUpDate', lead.followUpDate ? lead.followUpDate.split('T')[0] : '')} className="flex items-center gap-2 cursor-pointer mt-1">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium text-foreground">
                  {lead.followUpDate
                    ? new Date(lead.followUpDate).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })
                    : 'Click to add follow up date'}
                </span>
                <Edit className="h-3 w-3 opacity-0 group-hover:opacity-100 ml-auto text-muted-foreground transition-opacity" />
              </div>
            )}
          </div>
        </div>
        {isUpdating && (
          <div className="flex items-center justify-center gap-2 text-xs text-muted-foreground">
            <Loader2 className="h-3.5 w-3.5 animate-spin" /> Saving changes...
          </div>
        )}
      </div>
    );
  }
  ```

- [ ] **Step 2: Chạy TypeScript check**
  Run: `npm run type-check` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 3: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/components/detail/LeadProfileSidebar.tsx
  git commit -m "feat: implement LeadProfileSidebar with inline editing"
  ```

---

### Task 3: Implement Component `QuickComposer` (Cột Giữa - Phía trên)

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/components/detail/QuickComposer.tsx`

- [ ] **Step 1: Viết chi tiết mã nguồn cho `QuickComposer`**
  Hỗ trợ gõ nhanh ghi chú hoặc lên lịch/ghi nhận cuộc gọi, email.
  
  ```tsx
  import { useState } from 'react';
  import { Button, Textarea, Tabs, TabsList, TabsTrigger, TabsContent, Input, Label } from '@/shared/components/ui';
  import { MessageSquare, Calendar, Loader2 } from 'lucide-react';

  interface QuickComposerProps {
    leadId: string;
    onAddNote: (content: string) => Promise<void>;
    onAddActivity: (data: { subject: string; type: string; notes?: string }) => Promise<void>;
  }

  export function QuickComposer({ leadId, onAddNote, onAddActivity }: QuickComposerProps) {
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
        <Tabs defaultValue="note" className="w-full">
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
  ```

- [ ] **Step 2: Chạy TypeScript check**
  Run: `npm run type-check` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 3: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/components/detail/QuickComposer.tsx
  git commit -m "feat: implement QuickComposer for Notes & Activities"
  ```

---

### Task 4: Implement Component `UnifiedTimeline` (Cột Giữa - Phía dưới)

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/components/detail/UnifiedTimeline.tsx`

- [ ] **Step 1: Viết chi tiết mã nguồn cho `UnifiedTimeline`**
  Hợp nhất Ghi chú, Hoạt động thành dòng lịch sử sắp xếp giảm dần theo thời gian thực.
  
  ```tsx
  import { useMemo } from 'react';
  import { MessageSquare, Phone, Mail, Calendar, Activity as ActivityIcon, User } from 'lucide-react';
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
          user: a.assignedTo ? `User #${a.assignedTo}` : 'System',
          subject: a.subject || 'Activity log',
          subtype: a.type || 'CALL',
          status: a.status || 'PLANNED',
          content: a.notes,
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
                    <span className="text-xs text-muted-foreground">
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
  ```

- [ ] **Step 2: Chạy TypeScript check**
  Run: `npm run type-check` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 3: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/components/detail/UnifiedTimeline.tsx
  git commit -m "feat: implement UnifiedTimeline merging notes and activities"
  ```

---

### Task 5: Implement Component `InsightsSidebar` (Cột Phải)

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/components/detail/InsightsSidebar.tsx`

- [ ] **Step 1: Viết chi tiết mã nguồn cho `InsightsSidebar`**
  Bao gồm điểm số Lead Score, Người phụ trách với quick-reassign, hành động chính/phụ (Convert, Qualify, Disqualify, Meeting), thông tin Metadata dạng collapsible.
  
  ```tsx
  import { ChevronDown, TrendingUp, UserPlus, CheckCircle, AlertCircle, Calendar, Sparkles } from 'lucide-react';
  import { Button, Card, CardContent, CardHeader, CardTitle, Avatar, AvatarFallback, Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui';
  import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from '@/shared/components/ui';
  import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from '@/shared/components/ui';
  import type { Lead } from '../../../../types';

  interface InsightsSidebarProps {
    lead: Lead;
    leadScore: number;
    onAssignLead: (assignedToId: number) => Promise<void>;
    onOpenConvert: () => void;
    onOpenQualify: () => void;
    onOpenDisqualify: () => void;
    onOpenMeetingRequest: () => void;
  }

  export function InsightsSidebar({
    lead,
    leadScore,
    onAssignLead,
    onOpenConvert,
    onOpenQualify,
    onOpenDisqualify,
    onOpenMeetingRequest,
  }: InsightsSidebarProps) {
    const scoreColor = leadScore >= 75 ? 'stroke-emerald-500' : leadScore >= 50 ? 'stroke-amber-500' : 'stroke-rose-500';
    const scoreTextClass = leadScore >= 75 ? 'text-emerald-500' : leadScore >= 50 ? 'text-amber-500' : 'text-rose-500';

    const formatDate = (dateStr?: string) => {
      if (!dateStr) return 'Not available';
      return new Date(dateStr).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' });
    };

    return (
      <div className="space-y-6">
        {/* Lead Score Radial Arc */}
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-semibold flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-muted-foreground" /> Lead Quality Score
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center pb-4">
            <div className="relative h-32 w-32 flex items-center justify-center">
              <svg className="h-full w-full -rotate-90">
                <circle cx="64" cy="64" r="50" fill="none" className="stroke-muted/40" strokeWidth="8" />
                <circle
                  cx="64"
                  cy="64"
                  r="50"
                  fill="none"
                  className={scoreColor}
                  strokeWidth="8"
                  strokeDasharray={`${(leadScore / 100) * 314} 314`}
                  strokeLinecap="round"
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-3xl font-extrabold text-foreground">{leadScore}</span>
                <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Score</span>
              </div>
            </div>
            
            {/* Popover explaining score */}
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="ghost" size="sm" className="mt-2 text-xs text-muted-foreground hover:text-foreground">
                  <Sparkles className="h-3 w-3 mr-1" /> View calculation details
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-64 p-3 text-xs space-y-2">
                <h4 className="font-bold text-foreground">Score Calculation</h4>
                <div className="space-y-1 text-muted-foreground">
                  <div className="flex justify-between"><span>Email provided</span><span className="text-emerald-600 font-semibold">+20</span></div>
                  <div className="flex justify-between"><span>Phone provided</span><span className="text-emerald-600 font-semibold">+15</span></div>
                  <div className="flex justify-between"><span>Company provided</span><span className="text-emerald-600 font-semibold">+20</span></div>
                  <div className="flex justify-between"><span>Status state weight</span><span className="text-emerald-600 font-semibold">+25</span></div>
                </div>
              </PopoverContent>
            </Popover>
          </CardContent>
        </Card>

        {/* Assigned User Selection */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Assigned Rep</CardTitle>
          </CardHeader>
          <CardContent>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <div className="flex items-center justify-between p-2 hover:bg-muted/40 rounded-lg cursor-pointer border border-muted/30">
                  <div className="flex items-center gap-2">
                    <Avatar className="h-7 w-7">
                      <AvatarFallback className="text-[10px] bg-muted">R</AvatarFallback>
                    </Avatar>
                    <span className="text-sm font-medium text-foreground">
                      {lead.assignedTo ? `User #${lead.assignedTo}` : 'Unassigned'}
                    </span>
                  </div>
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                </div>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end">
                <DropdownMenuItem onClick={() => onAssignLead(1)}>User #1 (Admin)</DropdownMenuItem>
                <DropdownMenuItem onClick={() => onAssignLead(2)}>User #2 (Sales Rep)</DropdownMenuItem>
                <DropdownMenuItem onClick={() => onAssignLead(3)}>User #3 (Manager)</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </CardContent>
        </Card>

        {/* Action Hub buttons */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Lead Transition Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {lead.leadStatus === 'QUALIFIED' ? (
              <Button onClick={onOpenConvert} className="w-full justify-start bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm">
                <UserPlus className="mr-2 h-4 w-4" /> Convert to Account
              </Button>
            ) : (
              <Button onClick={onOpenQualify} variant="outline" className="w-full justify-start hover:bg-muted/40">
                <CheckCircle className="mr-2 h-4 w-4 text-emerald-500" /> Qualify Lead
              </Button>
            )}
            <Button onClick={onOpenDisqualify} variant="outline" className="w-full justify-start hover:bg-muted/40">
              <AlertCircle className="mr-2 h-4 w-4 text-rose-500" /> Disqualify Lead
            </Button>
            <Button
              onClick={onOpenMeetingRequest}
              variant="outline"
              disabled={!lead.convertedAccountId}
              className="w-full justify-start hover:bg-muted/40"
              title={lead.convertedAccountId ? undefined : "Convert this lead to an account before scheduling meetings"}
            >
              <Calendar className="mr-2 h-4 w-4 text-blue-500" /> Request local meeting
            </Button>
          </CardContent>
        </Card>

        {/* Collapsible Metadata */}
        <Accordion type="single" collapsible className="w-full">
          <AccordionItem value="meta" className="border-none">
            <AccordionTrigger className="text-xs font-semibold uppercase tracking-wider text-muted-foreground hover:no-underline py-2">
              System Metadata
            </AccordionTrigger>
            <AccordionContent className="space-y-2 text-xs text-muted-foreground pt-1">
              <div className="flex justify-between"><span>Lead ID</span><span className="font-mono">#{lead.id}</span></div>
              <div className="flex justify-between"><span>Created</span><span>{formatDate(lead.createdAt)}</span></div>
              <div className="flex justify-between"><span>Last Update</span><span>{formatDate(lead.updatedAt)}</span></div>
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      </div>
    );
  }
  ```

- [ ] **Step 2: Chạy TypeScript check**
  Run: `npm run type-check` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 3: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/components/detail/InsightsSidebar.tsx
  git commit -m "feat: implement InsightsSidebar with clean radial score gauge"
  ```

---

### Task 6: Thay thế mã nguồn trong `LeadDetailPageEnhanced.tsx`

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/LeadDetailPageEnhanced.tsx`

- [ ] **Step 1: Viết lại toàn bộ `LeadDetailPageEnhanced.tsx` tích hợp các sub-component**
  Dọn dẹp code rác, nhập các component con mới, gọi truy vấn song song cho Lead, Notes và Activities, đồng thời chuyển trạng thái hành động hợp lệ sang các Dialog.
  
  ```tsx
  /**
   * Lead Detail Page Component - Enhanced UX/UI 3-Column Version
   * Author: QuanTuanHuy
   * Description: Part of Serp Project - Detailed lead view with conversion flow
   */

  'use client';

  import { useMemo, useState } from 'react';
  import Link from 'next/link';
  import { useRouter } from 'next/navigation';
  import { ArrowLeft, Trash2, Edit, MoreHorizontal, AlertCircle, RefreshCw } from 'lucide-react';
  import { getErrorMessage } from '@/lib/store/api';
  import {
    Button,
    Progress,
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
    Textarea,
    Input,
    Label,
  } from '@/shared/components/ui';
  import { toast } from 'sonner';
  
  // Custom API imports
  import {
    useDeleteLeadMutation,
    useGetLeadActivitiesQuery,
    useGetLeadQuery,
    useUpdateLeadStatusMutation,
    useUpdateLeadMutation,
    useAssignLeadMutation,
    useGetNotesQuery,
    useCreateNoteMutation,
    useCreateActivityMutation,
  } from '../../api/crmApi';

  // Sub-components
  import { LeadProfileSidebar } from './components/detail/LeadProfileSidebar';
  import { QuickComposer } from './components/detail/QuickComposer';
  import { UnifiedTimeline } from './components/detail/UnifiedTimeline';
  import { InsightsSidebar } from './components/detail/InsightsSidebar';
  import { RequestMeetingDialog } from '../../components/meeting-requests';
  import type { LeadStatus } from '../../types';

  interface LeadDetailPageProps {
    leadId: string;
  }

  export function LeadDetailPage({ leadId }: LeadDetailPageProps) {
    const router = useRouter();

    // Dialog trigger states
    const [showDeleteDialog, setShowDeleteDialog] = useState(false);
    const [showConvertDialog, setShowConvertDialog] = useState(false);
    const [showQualifyDialog, setShowQualifyDialog] = useState(false);
    const [showDisqualifyDialog, setShowDisqualifyDialog] = useState(false);
    const [showMeetingRequestDialog, setShowMeetingRequestDialog] = useState(false);

    // Dialog note fields
    const [qualifyNotes, setQualifyNotes] = useState('');
    const [disqualifyNotes, setDisqualifyNotes] = useState('');
    const [convertForm, setConvertForm] = useState({
      createAccount: true,
      createOpportunity: true,
      existingAccountId: '',
      accountName: '',
      accountNotes: '',
      opportunityName: '',
      opportunityAmount: '',
      opportunityNotes: '',
    });

    // API Hooks (RSC Waterfalls eliminated - executing parallel requests via RTK Query)
    const { data: leadResponse, isLoading: isLoadingLead } = useGetLeadQuery(leadId);
    const { data: activitiesResponse, isLoading: isLoadingActivities } = useGetLeadActivitiesQuery({ leadId, page: 1, size: 50 });
    const { data: notesResponse, isLoading: isLoadingNotes } = useGetNotesQuery({ entityType: 'LEAD', entityId: leadId });

    const [deleteLead] = useDeleteLeadMutation();
    const [updateLeadStatus] = useUpdateLeadStatusMutation();
    const [updateLead, { isLoading: isUpdatingLead }] = useUpdateLeadMutation();
    const [assignLead] = useAssignLeadMutation();
    const [createNote] = useCreateNoteMutation();
    const [createActivity] = useCreateActivityMutation();

    const lead = leadResponse?.data;
    const activities = activitiesResponse?.data.data || [];
    const notes = notesResponse?.data.data || [];

    // Calculate lead status progress
    const statusProgress = useMemo(() => {
      if (!lead) return 0;
      const statusMap: Record<LeadStatus, number> = {
        NEW: 25,
        CONTACTED: 50,
        NURTURING: 50,
        QUALIFIED: 75,
        CONVERTED: 100,
        DISQUALIFIED: 0,
        LOST: 0,
      };
      return statusMap[lead.leadStatus || 'NEW'] || 0;
    }, [lead]);

    // Calculate score using Vercel best practices (derived inline from static details)
    const leadScore = useMemo(() => {
      if (!lead) return 0;
      if (typeof lead.leadScore === 'number') return lead.leadScore;

      let score = 0;
      if (lead.email) score += 20;
      if (lead.phone) score += 15;
      if (lead.company) score += 20;
      if (lead.estimatedValue && lead.estimatedValue > 0) score += 20;
      if (lead.leadStatus === 'QUALIFIED') score += 25;
      else if (lead.leadStatus === 'CONTACTED' || lead.leadStatus === 'NURTURING') score += 15;
      else if (lead.leadStatus === 'NEW') score += 5;

      return Math.min(score, 100);
    }, [lead]);

    // Mutations wrapper logic
    const handleUpdateLeadField = async (data: any) => {
      try {
        await updateLead({ id: leadId, data }).unwrap();
        toast.success('Lead updated successfully');
      } catch (error) {
        toast.error('Failed to update lead', { description: getErrorMessage(error) });
        throw error;
      }
    };

    const handleAssignLead = async (assignedToId: number) => {
      try {
        await assignLead({ id: leadId, data: { assignedTo: assignedToId } }).unwrap();
        toast.success('Lead reassigned successfully');
      } catch (error) {
        toast.error('Failed to assign lead', { description: getErrorMessage(error) });
      }
    };

    const handleAddNote = async (content: string) => {
      try {
        await createNote({ entityType: 'LEAD', entityId: Number(leadId), content }).unwrap();
        toast.success('Note added successfully');
      } catch (error) {
        toast.error('Failed to create note', { description: getErrorMessage(error) });
        throw error;
      }
    };

    const handleAddActivity = async (data: any) => {
      try {
        await createActivity({
          leadId: Number(leadId),
          subject: data.subject,
          type: data.type,
          notes: data.notes,
          status: 'COMPLETED',
          scheduledDate: new Date().toISOString(),
        }).unwrap();
        toast.success('Activity logged successfully');
      } catch (error) {
        toast.error('Failed to log activity', { description: getErrorMessage(error) });
        throw error;
      }
    };

    const handleDelete = async () => {
      try {
        await deleteLead(leadId).unwrap();
        toast.success('Delete lead successfully');
        router.push('/crm/leads');
      } catch (error) {
        toast.error('Failed to delete lead', { description: getErrorMessage(error) });
      }
    };

    const handleQualify = async () => {
      try {
        const result = await updateLeadStatus({
          id: leadId,
          data: {
            fromStatus: lead?.leadStatus,
            toStatus: 'QUALIFIED',
            notes: qualifyNotes.trim(),
          },
        }).unwrap();
        toast.success(result.data.message || 'Qualify lead successfully');
        setShowQualifyDialog(false);
        setQualifyNotes('');
      } catch (error) {
        toast.error('Failed to qualify lead', { description: getErrorMessage(error) });
      }
    };

    const handleDisqualify = async () => {
      try {
        const result = await updateLeadStatus({
          id: leadId,
          data: {
            fromStatus: lead?.leadStatus,
            toStatus: 'DISQUALIFIED',
            notes: disqualifyNotes.trim(),
          },
        }).unwrap();
        toast.success(result.data.message || 'Disqualify lead successfully');
        setShowDisqualifyDialog(false);
        setDisqualifyNotes('');
      } catch (error) {
        toast.error('Failed to disqualify lead', { description: getErrorMessage(error) });
      }
    };

    const handleConvert = async () => {
      try {
        const result = await updateLeadStatus({
          id: leadId,
          data: {
            fromStatus: lead?.leadStatus,
            toStatus: 'CONVERTED',
            conversion: {
              createAccount: convertForm.createAccount,
              createOpportunity: convertForm.createOpportunity,
              existingAccountId: convertForm.existingAccountId ? Number(convertForm.existingAccountId) : undefined,
              accountData: convertForm.createAccount ? {
                name: convertForm.accountName || lead?.company || lead?.name,
                notes: convertForm.accountNotes || lead?.notes,
              } : undefined,
              opportunityData: convertForm.createOpportunity ? {
                name: convertForm.opportunityName || `Opportunity from ${lead?.name}`,
                amount: convertForm.opportunityAmount ? Number(convertForm.opportunityAmount) : lead?.estimatedValue,
                notes: convertForm.opportunityNotes || lead?.notes,
              } : undefined,
            },
          },
        }).unwrap();
        toast.success(result.data.message || 'Convert lead successfully');
        setShowConvertDialog(false);
        if (result.data.accountId) {
          router.push(`/crm/accounts/${result.data.accountId}`);
        }
      } catch (error) {
        toast.error('Failed to convert lead', { description: getErrorMessage(error) });
      }
    };

    if (isLoadingLead) {
      return (
        <div className="flex h-[60vh] items-center justify-center gap-2">
          <RefreshCw className="h-6 w-6 animate-spin text-primary" /> Loading lead details...
        </div>
      );
    }

    if (!lead) {
      return (
        <div className="flex h-[60vh] flex-col items-center justify-center">
          <AlertCircle className="mb-4 h-16 w-16 text-muted-foreground" />
          <h2 className="mb-2 text-xl font-semibold text-foreground">Lead not found</h2>
          <p className="mb-4 text-muted-foreground">This lead does not exist or has been deleted.</p>
          <Button asChild>
            <Link href="/crm/leads">
              <ArrowLeft className="mr-2 h-4 w-4" /> Back to lead list
            </Link>
          </Button>
        </div>
      );
    }

    const linkedAccountId = lead.convertedAccountId?.trim() || lead.convertedToCustomerId?.trim() || undefined;

    return (
      <div className="space-y-6">
        {/* Header Action Bar */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-muted/50">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" asChild className="rounded-full">
              <Link href="/crm/leads">
                <ArrowLeft className="h-5 w-5" />
              </Link>
            </Button>
            <div>
              <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">CRM Lead Workspace</div>
              <h1 className="text-2xl font-extrabold text-foreground tracking-tight">{lead.name}</h1>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" asChild>
              <Link href={`/crm/leads/${leadId}/edit`}>
                <Edit className="mr-1.5 h-4 w-4" /> Edit
              </Link>
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon" className="h-9 w-9">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-40">
                <DropdownMenuItem className="text-rose-600 focus:text-rose-600 focus:bg-rose-50" onClick={() => setShowDeleteDialog(true)}>
                  <Trash2 className="mr-2 h-4 w-4" /> Delete Lead
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        {/* Minimal flat pipeline progress bar */}
        <div className="space-y-1">
          <div className="flex justify-between text-xs text-muted-foreground font-semibold">
            <span>Pipeline Progression</span>
            <span>{lead.leadStatus} ({statusProgress}%)</span>
          </div>
          <Progress value={statusProgress} className="h-1.5 bg-muted/60" />
        </div>

        {/* Main 3-Column Responsive Grid */}
        <div className="grid gap-6 grid-cols-1 lg:grid-cols-4">
          {/* Column 1: Profile Sidebar */}
          <div className="lg:col-span-1 border border-muted/50 bg-card rounded-2xl p-5 shadow-sm self-start">
            <LeadProfileSidebar lead={lead} isUpdating={isUpdatingLead} onUpdateLead={handleUpdateLeadField} />
          </div>

          {/* Column 2 & 3: Interaction Timeline Hub */}
          <div className="lg:col-span-2 space-y-6">
            <QuickComposer leadId={leadId} onAddNote={handleAddNote} onAddActivity={handleAddActivity} />
            <UnifiedTimeline activities={activities} notes={notes} isLoading={isLoadingActivities || isLoadingNotes} />
          </div>

          {/* Column 4: Insights & Conversion Hub */}
          <div className="lg:col-span-1 space-y-6">
            <InsightsSidebar
              lead={lead}
              leadScore={leadScore}
              onAssignLead={handleAssignLead}
              onOpenConvert={() => setShowConvertDialog(true)}
              onOpenQualify={() => setShowQualifyDialog(true)}
              onOpenDisqualify={() => setShowDisqualifyDialog(true)}
              onOpenMeetingRequest={() => setShowMeetingRequestDialog(true)}
            />
          </div>
        </div>

        {/* Dialog: Delete */}
        <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Confirm Lead Deletion</DialogTitle>
              <DialogDescription>
                Are you sure you want to delete lead &quot;{lead.name}&quot;? This action cannot be undone.
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>Cancel</Button>
              <Button variant="destructive" onClick={handleDelete}>Delete lead</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Dialog: Qualify */}
        <Dialog open={showQualifyDialog} onOpenChange={setShowQualifyDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Qualify lead</DialogTitle>
              <DialogDescription>Add qualification notes for this lead.</DialogDescription>
            </DialogHeader>
            <Textarea rows={4} value={qualifyNotes} onChange={(e) => setQualifyNotes(e.target.value)} />
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowQualifyDialog(false)}>Cancel</Button>
              <Button onClick={handleQualify} disabled={!qualifyNotes.trim()}>Qualify</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Dialog: Disqualify */}
        <Dialog open={showDisqualifyDialog} onOpenChange={setShowDisqualifyDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Disqualify lead</DialogTitle>
              <DialogDescription>Add disqualification notes for this lead.</DialogDescription>
            </DialogHeader>
            <Textarea rows={4} value={disqualifyNotes} onChange={(e) => setDisqualifyNotes(e.target.value)} />
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowDisqualifyDialog(false)}>Cancel</Button>
              <Button onClick={handleDisqualify} disabled={!disqualifyNotes.trim()}>Disqualify</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Dialog: Convert */}
        <Dialog open={showConvertDialog} onOpenChange={setShowConvertDialog}>
          <DialogContent className="w-[95vw] max-w-2xl">
            <DialogHeader>
              <DialogTitle>Convert lead</DialogTitle>
              <DialogDescription>Create account/opportunity from this lead.</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="accountName">Account Name</Label>
                  <Input
                    id="accountName"
                    value={convertForm.accountName}
                    onChange={(e) => setConvertForm((prev) => ({ ...prev, accountName: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="existingAccountId">Existing Account ID</Label>
                  <Input
                    id="existingAccountId"
                    value={convertForm.existingAccountId}
                    onChange={(e) => setConvertForm((prev) => ({ ...prev, existingAccountId: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="opportunityName">Opportunity Name</Label>
                  <Input
                    id="opportunityName"
                    value={convertForm.opportunityName}
                    onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityName: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="opportunityAmount">Opportunity Amount</Label>
                  <Input
                    id="opportunityAmount"
                    type="number"
                    value={convertForm.opportunityAmount}
                    onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityAmount: e.target.value }))}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="accountNotes">Account Notes</Label>
                <Textarea
                  id="accountNotes"
                  rows={3}
                  value={convertForm.accountNotes}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, accountNotes: e.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="opportunityNotes">Opportunity Notes</Label>
                <Textarea
                  id="opportunityNotes"
                  rows={3}
                  value={convertForm.opportunityNotes}
                  onChange={(e) => setConvertForm((prev) => ({ ...prev, opportunityNotes: e.target.value }))}
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowConvertDialog(false)}>Cancel</Button>
              <Button onClick={handleConvert}>Convert</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Dialog: Local Meeting Request */}
        {linkedAccountId && (
          <RequestMeetingDialog
            open={showMeetingRequestDialog}
            onOpenChange={setShowMeetingRequestDialog}
            accountId={linkedAccountId}
            accountName={lead.company || lead.name}
          />
        )}
      </div>
    );
  }

  export default LeadDetailPage;
  ```

- [ ] **Step 2: Chạy TypeScript check**
  Run: `npm run type-check` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 3: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/leads/LeadDetailPageEnhanced.tsx
  git commit -m "refactor: restructure LeadDetailPage to use clean 3-column subcomponents"
  ```

---

### Task 7: Xác thực mã nguồn & Dọn dẹp (Verification & Cleanup)

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/index.ts`

- [ ] **Step 1: Xác nhận file index.ts xuất chính xác component**
  Đảm bảo file index.ts của module leads xuất component `LeadDetailPage` (chứ không bị lỗi import).
  Kiểm tra: `serp_web/src/modules/crm/pages/leads/index.ts`.

- [ ] **Step 2: Chạy eslint kiểm tra code style**
  Run: `npm run lint` (ở `serp_web`)
  Expected: Succeeded, không có lỗi lint nghiêm trọng.

- [ ] **Step 3: Chạy build build test ứng dụng**
  Run: `npm run build` (ở `serp_web`)
  Expected: Succeeded.

- [ ] **Step 4: Commit**
  ```bash
  git add .
  git commit -m "chore: verify build and styling lint checks pass successfully"
  ```
