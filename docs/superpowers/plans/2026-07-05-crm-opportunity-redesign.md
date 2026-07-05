# CRM Opportunity UX/UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the CRM Opportunity detail page into a modern 3-column asymmetric layout with click-to-edit inline fields, interactive vertical pipeline stepper, win probability gauge, and an enhanced Opportunity form with a real-time live deal calculator.

**Architecture:** 
- Modularized React components using a 3-column grid model.
- Client-side parallel API data fetching via RTK Query hooks (eliminating waterfalls).
- Real CRM Notes API integration utilizing mapped organization user names.

**Tech Stack:** React 19, Next.js 16 (Turbopack), TailwindCSS, RTK Query, Lucide Icons, Shadcn UI (Radix).

---

### Task 1: Create `OpportunityProfileSidebar` component

**Files:**
- Create: [OpportunityProfileSidebar.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/opportunities/components/detail/OpportunityProfileSidebar.tsx)

- [ ] **Step 1: Write the OpportunityProfileSidebar component**
  Create the file with click-to-edit fields for: Name, Account, Lead, Estimated Value, Expected Close Date, and Description.
  
  ```tsx
  'use client';

  import { useState, useEffect } from 'react';
  import { Edit, Check, X, Calendar, DollarSign, Building, User } from 'lucide-react';
  import { Button, Input, Textarea, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui';
  import { CRMDatePicker } from '../../../components/shared';
  import { formatCurrency, toLocalDateInputValue } from '../../../utils';
  import type { Opportunity } from '../../../types';

  interface OpportunityProfileSidebarProps {
    opportunity: Opportunity;
    accounts: any[];
    leads: any[];
    isUpdating: boolean;
    onUpdateOpportunity: (data: any) => Promise<void>;
  }

  export function OpportunityProfileSidebar({
    opportunity,
    accounts,
    leads,
    isUpdating,
    onUpdateOpportunity,
  }: OpportunityProfileSidebarProps) {
    const [editingField, setEditingField] = useState<string | null>(null);
    const [tempValue, setTempValue] = useState<any>(null);

    const startEditing = (field: string, initialVal: any) => {
      setEditingField(field);
      setTempValue(initialVal);
    };

    const handleSave = async (field: string) => {
      try {
        let val = tempValue;
        if (field === 'estimatedValue') val = Number(tempValue) || 0;
        await onUpdateOpportunity({ [field]: val });
        setEditingField(null);
      } catch (err) {
        // Handled in parent toast
      }
    };

    const handleCancel = () => {
      setEditingField(null);
      setTempValue(null);
    };

    const getAccountName = (id?: string) => {
      const acc = accounts.find((a) => String(a.id) === String(id));
      return acc ? acc.name : opportunity.customerName || 'No account';
    };

    const getLeadName = (id?: string) => {
      const ld = leads.find((l) => String(l.id) === String(id));
      return ld ? (ld.name || ld.email) : 'No lead';
    };

    return (
      <div className="space-y-6">
        <div>
          <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Deal Info</div>
          {editingField === 'name' ? (
            <div className="flex items-center gap-1 mt-1">
              <Input value={tempValue} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('name')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <h2 className="text-lg font-bold mt-1 group flex items-center gap-2 cursor-pointer hover:text-primary transition" onClick={() => startEditing('name', opportunity.name)}>
              {opportunity.name}
              <Edit className="h-3.5 w-3.5 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </h2>
          )}
        </div>

        <div className="space-y-4 pt-4 border-t border-muted/50 text-sm">
          {/* Account */}
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
              <Building className="h-3 w-3" /> Account
            </label>
            {editingField === 'accountId' ? (
              <div className="flex items-center gap-1">
                <Select value={tempValue} onValueChange={setTempValue}>
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue placeholder="Select account" />
                  </SelectTrigger>
                  <SelectContent>
                    {accounts.map((acc) => (
                      <SelectItem key={acc.id} value={acc.id}>{acc.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('accountId')} disabled={isUpdating}>
                  <Check className="h-4 w-4" />
                </Button>
                <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('accountId', opportunity.accountId || '')}>
                <span className="font-medium text-foreground">{getAccountName(opportunity.accountId)}</span>
                <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
              </div>
            )}
          </div>

          {/* Value */}
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
              <DollarSign className="h-3 w-3" /> Estimated Value
            </label>
            {editingField === 'estimatedValue' ? (
              <div className="flex items-center gap-1">
                <Input type="number" value={tempValue} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
                <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('estimatedValue')} disabled={isUpdating}>
                  <Check className="h-4 w-4" />
                </Button>
                <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('estimatedValue', opportunity.estimatedValue || 0)}>
                <span className="font-semibold text-emerald-700">{formatCurrency(opportunity.estimatedValue || 0)}</span>
                <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
              </div>
            )}
          </div>

          {/* Expected Close Date */}
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
              <Calendar className="h-3 w-3" /> Expected Close Date
            </label>
            {editingField === 'expectedCloseDate' ? (
              <div className="flex items-center gap-1">
                <CRMDatePicker value={tempValue ? new Date(tempValue) : undefined} onChange={(date) => setTempValue(date ? toLocalDateInputValue(date) : '')} />
                <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('expectedCloseDate')} disabled={isUpdating}>
                  <Check className="h-4 w-4" />
                </Button>
                <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('expectedCloseDate', opportunity.expectedCloseDate)}>
                <span className="font-medium text-foreground">{opportunity.expectedCloseDate ? new Date(opportunity.expectedCloseDate).toLocaleDateString() : 'Not set'}</span>
                <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
              </div>
            )}
          </div>

          {/* Description */}
          <div className="space-y-1 pt-2 border-t border-muted/30">
            <label className="text-xs text-muted-foreground font-semibold uppercase">Description</label>
            {editingField === 'description' ? (
              <div className="space-y-1">
                <Textarea value={tempValue} onChange={(e) => setTempValue(e.target.value)} rows={3} className="text-xs" />
                <div className="flex justify-end gap-1">
                  <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                  <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('description')} disabled={isUpdating}>Save</Button>
                </div>
              </div>
            ) : (
              <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[50px]" onClick={() => startEditing('description', opportunity.description || '')}>
                <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed">{opportunity.description || 'Add opportunity description...'}</p>
                <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/opportunities/components/detail/OpportunityProfileSidebar.tsx
  git commit -m "feat(crm): create OpportunityProfileSidebar sub-component"
  ```

---

### Task 2: Create Opportunity `QuickComposer` component

**Files:**
- Create: [QuickComposer.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/opportunities/components/detail/QuickComposer.tsx)

- [ ] **Step 1: Write the QuickComposer component**
  Implement activity logger and notes composer tabs.
  
  ```tsx
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
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/opportunities/components/detail/QuickComposer.tsx
  git commit -m "feat(crm): create Opportunity QuickComposer sub-component"
  ```

---

### Task 3: Create Opportunity `UnifiedTimeline` component

**Files:**
- Create: [UnifiedTimeline.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/opportunities/components/detail/UnifiedTimeline.tsx)

- [ ] **Step 1: Write the UnifiedTimeline component**
  Displays notes and activities, using the resolved user names.
  
  ```tsx
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
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/opportunities/components/detail/UnifiedTimeline.tsx
  git commit -m "feat(crm): create Opportunity UnifiedTimeline sub-component"
  ```

---

### Task 4: Create `OpportunityInsightsSidebar` component

**Files:**
- Create: [OpportunityInsightsSidebar.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/opportunities/components/detail/OpportunityInsightsSidebar.tsx)

- [ ] **Step 1: Write the OpportunityInsightsSidebar component**
  Implements the win probability gauge, vertical pipeline stage stepper, and assignee selector.
  
  ```tsx
  'use client';

  import { ChevronDown, TrendingUp, Sparkles, UserPlus, Calendar, CheckCircle, AlertCircle, RefreshCw } from 'lucide-react';
  import { Button, Card, CardContent, CardHeader, CardTitle, Avatar, AvatarFallback, Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui';
  import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from '@/shared/components/ui';
  import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from '@/shared/components/ui';
  import type { Opportunity, OpportunityStage } from '../../../types';
  import { formatCurrency } from '../../../utils';

  interface OpportunityInsightsSidebarProps {
    opportunity: Opportunity;
    probability: number;
    weightedValue: number;
    daysInPipeline: number;
    daysUntilClose: number;
    users: any[];
    getUserName?: (userId?: string | number) => string;
    onAssignOpportunity: (userId: number) => Promise<void>;
    onChangeStage: (stage: OpportunityStage) => void;
    onOpenMeetingRequest: () => void;
    onReopen: () => void;
    onDelete: () => void;
  }

  export function OpportunityInsightsSidebar({
    opportunity,
    probability,
    weightedValue,
    daysInPipeline,
    daysUntilClose,
    users,
    getUserName,
    onAssignOpportunity,
    onChangeStage,
    onOpenMeetingRequest,
    onReopen,
    onDelete,
  }: OpportunityInsightsSidebarProps) {
    const isClosed = opportunity.stage === 'CLOSED_WON' || opportunity.stage === 'CLOSED_LOST';
    const gaugeColor = probability >= 75 ? 'stroke-emerald-500' : probability >= 50 ? 'stroke-amber-500' : probability >= 25 ? 'stroke-blue-500' : 'stroke-rose-500';

    const stagesList: { code: OpportunityStage; label: string; prob: number }[] = [
      { code: 'PROSPECTING', label: 'Prospecting', prob: 10 },
      { code: 'QUALIFICATION', label: 'Qualification', prob: 25 },
      { code: 'PROPOSAL', label: 'Proposal', prob: 50 },
      { code: 'NEGOTIATION', label: 'Negotiation', prob: 75 },
    ];

    return (
      <div className="space-y-6">
        {/* Probability Gauge */}
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center justify-between">
              Forecast Score
              <span className="text-[10px] text-muted-foreground lowercase flex items-center"><TrendingUp className="h-3 w-3 mr-1" /> win probability</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center py-4">
            <div className="relative h-28 w-28">
              <svg className="h-full w-full -rotate-90" viewBox="0 0 36 36">
                <path className="stroke-muted/30" strokeWidth="3" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path
                  className={`transition-all duration-1000 ease-out ${gaugeColor}`}
                  strokeWidth="3"
                  strokeDasharray={`${probability}, 100`}
                  strokeLinecap="round"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-2xl font-extrabold text-foreground">{probability}%</span>
                <span className="text-[8px] text-muted-foreground uppercase font-bold tracking-wider">Chance</span>
              </div>
            </div>
            
            <div className="text-center mt-3">
              <div className="text-xs text-muted-foreground">Weighted Forecast</div>
              <div className="text-lg font-bold text-foreground">{formatCurrency(weightedValue)}</div>
            </div>
          </CardContent>
        </Card>

        {/* Vertical Pipeline Progress Stepper */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Interactive Stage Stepper</CardTitle>
          </CardHeader>
          <CardContent className="pt-2">
            <div className="relative pl-5 space-y-4 before:absolute before:left-[7px] before:top-2 before:bottom-2 before:w-[2px] before:bg-muted/80">
              {stagesList.map((stage) => {
                const isActive = opportunity.stage === stage.code;
                const isCompleted = stagesList.findIndex((s) => s.code === opportunity.stage) > stagesList.findIndex((s) => s.code === stage.code);
                
                return (
                  <div key={stage.code} className="relative group cursor-pointer" onClick={() => !isClosed && onChangeStage(stage.code)}>
                    <div className={`absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border transition-all duration-300 ${
                      isActive ? 'bg-primary border-primary scale-125 ring-2 ring-primary/20 glow' : isCompleted ? 'bg-emerald-600 border-emerald-600' : 'bg-background border-muted'
                    }`} />
                    <div className="flex justify-between items-center pl-2">
                      <span className={`text-xs font-medium transition ${isActive ? 'text-primary font-bold' : 'text-muted-foreground group-hover:text-foreground'}`}>
                        {stage.label}
                      </span>
                      <span className="text-[10px] text-muted-foreground/70">{stage.prob}%</span>
                    </div>
                  </div>
                );
              })}

              {/* Closed stage handling */}
              <div className="relative group pl-2 pt-2 border-t border-muted/30">
                <div className="flex justify-between items-center">
                  <span className="text-xs font-semibold text-muted-foreground">Closed Status:</span>
                  {isClosed ? (
                    <span className={`text-xs font-bold ${opportunity.stage === 'CLOSED_WON' ? 'text-emerald-600' : 'text-rose-600'}`}>
                      {opportunity.stage === 'CLOSED_WON' ? 'Closed Won (100%)' : 'Closed Lost (0%)'}
                    </span>
                  ) : (
                    <div className="flex gap-1.5">
                      <Button size="xs" variant="outline" className="text-[10px] text-emerald-600 h-6 px-2 hover:bg-emerald-50 border-emerald-300" onClick={() => onChangeStage('CLOSED_WON')}>Won</Button>
                      <Button size="xs" variant="outline" className="text-[10px] text-rose-600 h-6 px-2 hover:bg-rose-50 border-rose-300" onClick={() => onChangeStage('CLOSED_LOST')}>Lost</Button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Assigned Rep selection */}
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
                      {opportunity.assignedTo ? (getUserName ? getUserName(opportunity.assignedTo) : `User #${opportunity.assignedTo}`) : 'Unassigned'}
                    </span>
                  </div>
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                </div>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="max-h-60 overflow-y-auto w-56" align="end">
                {users.map((user) => (
                  <DropdownMenuItem key={user.id} onClick={() => onAssignOpportunity(Number(user.id))}>
                    {[user.firstName, user.lastName].filter(Boolean).join(' ') || user.email}
                  </DropdownMenuItem>
                ))}
              </DropdownMenuContent>
            </DropdownMenu>
          </CardContent>
        </Card>

        {/* Deal Transition Actions */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Actions Hub</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <Button className="w-full justify-start border-muted/50 hover:bg-muted/40 text-foreground" variant="outline" onClick={onOpenMeetingRequest}>
              <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Request Meeting
            </Button>
            {isClosed && (
              <Button className="w-full justify-start text-emerald-600 hover:text-emerald-700 bg-emerald-50 hover:bg-emerald-100/80" variant="ghost" onClick={onReopen}>
                <RefreshCw className="mr-2 h-4 w-4" /> Reopen Deal
              </Button>
            )}
            <Button className="w-full justify-start text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" variant="ghost" onClick={onDelete}>
              <AlertCircle className="mr-2 h-4 w-4" /> Delete Opportunity
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/opportunities/components/detail/OpportunityInsightsSidebar.tsx
  git commit -m "feat(crm): create OpportunityInsightsSidebar sub-component"
  ```

---

### Task 5: Enhance `OpportunityForm` with Live Calculator

**Files:**
- Modify: [OpportunityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/OpportunityForm.tsx)

- [ ] **Step 1: Update OpportunityForm layout and add Live Calculator**
  Revamp the form layout. Incorporate the live probability and weighted value calculator cards in a grid next to form columns.
  Let's replace lines 187-442 to apply the new layout.
  
  ```tsx
  // ... imports and schema remain same
  
  // replace from return ( ... ) onwards
  return (
    <div className={cn("grid grid-cols-1 lg:grid-cols-3 gap-6", className)}>
      <Card className="lg:col-span-2 border border-muted/50 shadow-sm rounded-xl">
        <CardHeader>
          <CardTitle className="text-xl font-extrabold tracking-tight text-foreground">
            {isEditing ? 'Edit Opportunity Profile' : 'Create Opportunity Profile'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onFormSubmit} className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Basic Information</h3>
              
              <div className="space-y-2">
                <Label htmlFor="name">Opportunity Name *</Label>
                <Input
                  id="name"
                  {...register('name')}
                  placeholder="e.g. Enterprise CRM Deal"
                  className={errors.name ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Account *</Label>
                  <Select value={watch('accountId')} onValueChange={(v) => setValue('accountId', v)} disabled={isLoading || isAccountsLoading}>
                    <SelectTrigger className={errors.accountId ? 'border-destructive' : ''}>
                      <SelectValue placeholder={isAccountsLoading ? 'Loading accounts...' : 'Select account'} />
                    </SelectTrigger>
                    <SelectContent>
                      {accounts.map((acc) => (
                        <SelectItem key={acc.id} value={acc.id}>{acc.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.accountId && <p className="text-xs text-destructive">{errors.accountId.message}</p>}
                </div>

                <div className="space-y-2">
                  <Label>Lead Source</Label>
                  <Select value={watch('leadId') || ''} onValueChange={(v) => setValue('leadId', v)} disabled={isLoading || isLeadsLoading}>
                    <SelectTrigger>
                      <SelectValue placeholder={isLeadsLoading ? 'Loading leads...' : 'Select lead (optional)'} />
                    </SelectTrigger>
                    <SelectContent>
                      {leads.map((ld) => (
                        <SelectItem key={ld.id} value={ld.id}>{ld.name || ld.email || `Lead #${ld.id}`}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Pipeline Settings</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Stage *</Label>
                  <Select value={watch('stage')} onValueChange={(v) => setValue('stage', v as OpportunityStage)} disabled={isLoading}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PROSPECTING">Prospecting</SelectItem>
                      <SelectItem value="QUALIFICATION">Qualification</SelectItem>
                      <SelectItem value="PROPOSAL">Proposal</SelectItem>
                      <SelectItem value="NEGOTIATION">Negotiation</SelectItem>
                      <SelectItem value="CLOSED_WON">Closed Won</SelectItem>
                      <SelectItem value="CLOSED_LOST">Closed Lost</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="assignedTo">Assigned Rep</Label>
                  <Controller
                    name="assignedTo"
                    control={control}
                    render={({ field }) => (
                      <CRMUserSelect
                        id="assignedTo"
                        value={field.value}
                        onChange={field.onChange}
                        fallbackUserName={opportunity?.assignedToName}
                        disabled={isLoading}
                      />
                    )}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="estimatedValue">Estimated Value *</Label>
                  <Input
                    id="estimatedValue"
                    type="number"
                    min={0}
                    {...register('estimatedValue', { valueAsNumber: true })}
                    placeholder="0"
                    className={errors.estimatedValue ? 'border-destructive' : ''}
                    disabled={isLoading}
                  />
                  {errors.estimatedValue && <p className="text-xs text-destructive">{errors.estimatedValue.message}</p>}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expectedCloseDate">Expected Close Date *</Label>
                  <Controller
                    name="expectedCloseDate"
                    control={control}
                    render={({ field }) => (
                      <CRMDatePicker
                        id="expectedCloseDate"
                        value={field.value}
                        onChange={(date) => field.onChange(date ? toLocalDateInputValue(date) : '')}
                        disabled={isLoading}
                        className={errors.expectedCloseDate ? 'border-destructive' : ''}
                      />
                    )}
                  />
                  {errors.expectedCloseDate && <p className="text-xs text-destructive">{errors.expectedCloseDate.message}</p>}
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Additional Notes</h3>
              <div className="space-y-2">
                <Label htmlFor="description">Deal Description</Label>
                <Textarea id="description" {...register('description')} rows={3} placeholder="Describe the opportunity terms..." disabled={isLoading} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="notes">Static Notes</Label>
                <Textarea id="notes" {...register('notes')} rows={2} placeholder="Add miscellaneous remarks..." disabled={isLoading} />
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-6 border-t border-muted/30">
              {onCancel && (
                <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading || isSubmitting}>
                  Cancel
                </Button>
              )}
              <Button type="submit" disabled={isLoading || isSubmitting}>
                {isSubmitting ? 'Saving...' : isEditing ? 'Update Opportunity' : 'Create Opportunity'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Live Calculator Column */}
      <div className="lg:col-span-1 space-y-6">
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden sticky top-6">
          <CardHeader className="bg-primary/[0.03] border-b border-muted/40">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <Sparkles className="h-4 w-4 text-amber-500 animate-pulse" /> Live Deal Calculator
            </CardTitle>
          </CardHeader>
          <CardContent className="p-5 space-y-6">
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Deal Stage</div>
              <div className="text-sm font-bold text-foreground">{watchedStage}</div>
            </div>
            
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Win Probability</div>
              <div className="text-3xl font-extrabold text-primary">{computedProbability}%</div>
            </div>

            <div className="space-y-1 pt-4 border-t border-muted/30">
              <div className="text-xs text-muted-foreground uppercase font-semibold">Weighted Value</div>
              <div className="text-xl font-bold text-emerald-600">{formatCurrency(weightedValue)}</div>
              <div className="text-[10px] text-muted-foreground italic mt-1">
                Weighted Value = Est. Value ({formatCurrency(estimatedValue)}) × Prob. ({computedProbability}%)
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/OpportunityForm.tsx
  git commit -m "feat(crm): enhance OpportunityForm with grid layout and Live Calculator"
  ```

---

### Task 6: Refactor `OpportunityDetailPage` to use new layout

**Files:**
- Modify: [OpportunityDetailPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx)

- [ ] **Step 1: Re-assemble OpportunityDetailPage with modular sub-components**
  Let's replace the page body to use the new 3-column layout. Integrate:
  - Parallel API queries (`useGetLeadQuery`, `useGetAccountsQuery`, `useGetLeadsQuery`, `useGetOrganizationUsersQuery`, etc.) to eliminate waterfalls.
  - Sub-components: `OpportunityProfileSidebar`, `QuickComposer`, `UnifiedTimeline`, and `OpportunityInsightsSidebar`.
  - Proper handlers for `handleAddNote`, `handleAddActivity`, `handleUpdateOpportunityField`, `handleStageChange`, `handleReopen`, and `handleDelete`.
  
  Let's write this update cleanly.
  
  ```tsx
  // replace the file body of OpportunityDetailPage.tsx
  // detailed imports and hook setups in Page
  ```

- [ ] **Step 2: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx
  git commit -m "feat(crm): refactor OpportunityDetailPage with modern 3-column layout"
  ```

---

### Task 7: Build and Verify Compilation

- [ ] **Step 1: Run type checking**
  Run: `npm run type-check`
  Expected: PASS

- [ ] **Step 2: Run linter**
  Run: `npm run lint`
  Expected: PASS

- [ ] **Step 3: Run project build**
  Run: `npm run build`
  Expected: PASS
