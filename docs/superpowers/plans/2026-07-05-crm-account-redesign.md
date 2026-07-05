# CRM Account UX/UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the CRM Account Detail page (`CustomerDetailPageEnhanced.tsx`) into a modern 3-column asymmetric layout and restructure the `AccountForm` component into a grid layout with toggle-badge widgets.

**Architecture:** Split the monolith detail page into modular, self-contained sub-components under `src/modules/crm/pages/customers/components/detail/`. Use RTK Query to load accounts, leads, contacts, activities, and opportunities, rendering them across the 3-column layout.

**Tech Stack:** React 19, Next.js 15, Tailwind CSS, Lucide Icons, Shadcn UI Components.

---

### Task 1: Create `AccountProfileSidebar` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/customers/components/detail/AccountProfileSidebar.tsx`

- [ ] **Step 1: Write the sidebar component code**

Create `AccountProfileSidebar.tsx` with click-to-edit capabilities for B2B details.

```tsx
'use client';

import { useState } from 'react';
import { Edit, Check, X, Building2, Mail, Phone, Globe, MapPin, ShieldAlert, Users, Clock, Languages, Tag } from 'lucide-react';
import { Input, Button, Textarea, Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/shared/components/ui';
import { CRMDatePicker } from '../../components/shared';
import type { Customer } from '../../types';

interface AccountProfileSidebarProps {
  customer: Customer;
  isUpdating: boolean;
  onUpdateAccount: (data: any) => Promise<void>;
}

export function AccountProfileSidebar({
  customer,
  isUpdating,
  onUpdateAccount,
}: AccountProfileSidebarProps) {
  const [editingField, setEditingField] = useState<string | null>(null);
  const [tempValue, setTempValue] = useState<any>(null);

  const startEditing = (field: string, val: any) => {
    setEditingField(field);
    setTempValue(val);
  };

  const handleSave = async (field: string) => {
    try {
      await onUpdateAccount({ [field]: tempValue });
      setEditingField(null);
    } catch {
      // Handled in parent
    }
  };

  const handleCancel = () => {
    setEditingField(null);
    setTempValue(null);
  };

  return (
    <div className="space-y-6 text-sm">
      <div>
        <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Account Profile</div>
        {editingField === 'name' ? (
          <div className="flex items-center gap-1 mt-1">
            <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
            <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('name')} disabled={isUpdating}>
              <Check className="h-4 w-4" />
            </Button>
            <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        ) : (
          <h2 className="text-lg font-bold mt-1 group flex items-center gap-2 cursor-pointer hover:text-primary transition" onClick={() => startEditing('name', customer.name)}>
            {customer.name}
            <Edit className="h-3.5 w-3.5 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
          </h2>
        )}
      </div>

      <div className="space-y-4 pt-4 border-t border-muted/50">
        {/* Company Name */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Building2 className="h-3 w-3" /> Company
          </label>
          {editingField === 'companyName' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('companyName')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('companyName', customer.companyName || '')}>
              <span className="text-foreground">{customer.companyName || 'Not set'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Email */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Mail className="h-3 w-3" /> Email
          </label>
          {editingField === 'email' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('email')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('email', customer.email)}>
              <span className="text-foreground">{customer.email}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Phone */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Phone className="h-3 w-3" /> Phone
          </label>
          {editingField === 'phone' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('phone')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('phone', customer.phone || '')}>
              <span className="text-foreground">{customer.phone || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Industry */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <Tag className="h-3 w-3" /> Industry
          </label>
          {editingField === 'industry' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('industry')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('industry', customer.industry || '')}>
              <span className="text-foreground capitalize">{customer.industry || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Tax Number */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase">Tax Number</label>
          {editingField === 'taxNumber' ? (
            <div className="flex items-center gap-1">
              <Input value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} className="h-8 text-sm" />
              <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleSave('taxNumber')} disabled={isUpdating}>
                <Check className="h-4 w-4" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-rose-600" onClick={handleCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="group flex items-center justify-between cursor-pointer py-1 px-1.5 hover:bg-muted/40 rounded transition" onClick={() => startEditing('taxNumber', customer.taxNumber || '')}>
              <span className="text-foreground">{customer.taxNumber || 'N/A'}</span>
              <Edit className="h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Address */}
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground font-semibold uppercase flex items-center gap-1">
            <MapPin className="h-3 w-3" /> Address
          </label>
          {editingField === 'address' ? (
            <div className="space-y-1">
              <Textarea value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} rows={2} className="text-xs" />
              <div className="flex justify-end gap-1">
                <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('address')} disabled={isUpdating}>Save</Button>
              </div>
            </div>
          ) : (
            <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[40px]" onClick={() => startEditing('address', customer.address || '')}>
              <p className="text-xs whitespace-pre-wrap leading-relaxed">{customer.address || 'Add address...'}</p>
              <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>

        {/* Notes */}
        <div className="space-y-1 pt-2 border-t border-muted/30">
          <label className="text-xs text-muted-foreground font-semibold uppercase">Account Notes</label>
          {editingField === 'notes' ? (
            <div className="space-y-1">
              <Textarea value={tempValue || ''} onChange={(e) => setTempValue(e.target.value)} rows={3} className="text-xs" />
              <div className="flex justify-end gap-1">
                <Button size="sm" variant="ghost" className="h-7 text-xs text-rose-600" onClick={handleCancel}>Cancel</Button>
                <Button size="sm" className="h-7 text-xs text-white bg-emerald-600 hover:bg-emerald-700" onClick={() => handleSave('notes')} disabled={isUpdating}>Save</Button>
              </div>
            </div>
          ) : (
            <div className="group relative cursor-pointer p-2 hover:bg-muted/40 rounded border border-muted/20 transition min-h-[50px]" onClick={() => startEditing('notes', customer.notes || '')}>
              <p className="text-xs text-muted-foreground whitespace-pre-wrap leading-relaxed">{customer.notes || 'Add account remarks...'}</p>
              <Edit className="absolute top-2 right-2 h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/components/detail/AccountProfileSidebar.tsx
git commit -m "feat(crm): create AccountProfileSidebar component"
```

---

### Task 2: Create Account `QuickComposer` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/customers/components/detail/QuickComposer.tsx`

- [ ] **Step 1: Write the QuickComposer component code**

Create a component for Account activity logging and note adding.

```tsx
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
      <Tabs defaultValue="note">
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
                  className="w-full h-8 px-2 text-xs border border-input bg-background rounded-md"
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
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/components/detail/QuickComposer.tsx
git commit -m "feat(crm): create Account QuickComposer component"
```

---

### Task 3: Create Account `UnifiedTimeline` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/customers/components/detail/UnifiedTimeline.tsx`

- [ ] **Step 1: Write the UnifiedTimeline component code**

Create `UnifiedTimeline.tsx` combining both activities and notes sorted by timestamp.

```tsx
'use client';

import { useMemo } from 'react';
import { Calendar, Phone, Mail, Users, FileText, CheckCircle2, MessageSquare, Clock } from 'lucide-react';
import { Card, CardContent, Avatar, AvatarFallback, Badge } from '@/shared/components/ui';
import type { Activity } from '../../types';

interface NoteItem {
  id: number;
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
              <div key={item.id} className="relative group">
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
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/components/detail/UnifiedTimeline.tsx
git commit -m "feat(crm): create Account UnifiedTimeline component"
```

---

### Task 4: Create `OpportunityDealList` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/customers/components/detail/OpportunityDealList.tsx`

- [ ] **Step 1: Write the OpportunityDealList component code**

Create `OpportunityDealList.tsx` to list client's opportunities in horizontal cards.

```tsx
'use client';

import Link from 'next/link';
import { TrendingUp, Calendar, User, ArrowRight, DollarSign } from 'lucide-react';
import { Card, CardContent, Badge, Button } from '@/shared/components/ui';
import type { Opportunity } from '../../types';
import { formatCurrency } from '../../utils';

interface OpportunityDealListProps {
  opportunities: Opportunity[];
  isLoading: boolean;
}

export function OpportunityDealList({ opportunities, isLoading }: OpportunityDealListProps) {
  if (isLoading) {
    return <div className="text-center py-8 text-muted-foreground text-xs">Loading client opportunities...</div>;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
        <TrendingUp className="h-4 w-4 text-emerald-600" /> Associated Opportunities
      </h3>

      {opportunities.length === 0 ? (
        <Card className="border border-dashed border-muted/60 p-8 text-center rounded-xl">
          <DollarSign className="mx-auto mb-2.5 h-10 w-10 text-muted-foreground/60" />
          <p className="text-xs text-muted-foreground font-medium">No sales opportunities linked to this account yet.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-3">
          {opportunities.map((opp) => (
            <Card key={opp.id} className="border border-muted/50 shadow-sm rounded-xl overflow-hidden hover:border-muted-foreground/30 transition-all duration-300">
              <CardContent className="p-4 flex flex-col sm:flex-row justify-between sm:items-center gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-sm font-bold text-foreground">{opp.name}</span>
                    <Badge variant="outline" className="text-[10px] capitalize px-1.5">
                      {opp.stage.replace('_', ' ').toLowerCase()}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-[10px] text-muted-foreground/80 flex-wrap">
                    <span className="flex items-center gap-1"><Calendar className="h-3 w-3" /> Closes: {opp.expectedCloseDate ? new Date(opp.expectedCloseDate).toLocaleDateString() : 'N/A'}</span>
                    {opp.assignedToName && <span className="flex items-center gap-1"><User className="h-3 w-3" /> Owner: {opp.assignedToName}</span>}
                  </div>
                </div>

                <div className="flex items-center gap-4 justify-between sm:justify-end">
                  <div className="text-right">
                    <div className="text-xs text-muted-foreground uppercase font-semibold">Value</div>
                    <div className="text-sm font-bold text-emerald-700">{formatCurrency(opp.estimatedValue || 0)}</div>
                  </div>
                  <Button size="sm" variant="ghost" className="h-8 px-2" asChild>
                    <Link href={`/crm/opportunities/${opp.id}`}>
                      View <ArrowRight className="ml-1 h-3.5 w-3.5" />
                    </Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/components/detail/OpportunityDealList.tsx
git commit -m "feat(crm): create OpportunityDealList component for Account detail"
```

---

### Task 5: Create `AccountInsightsSidebar` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/customers/components/detail/AccountInsightsSidebar.tsx`

- [ ] **Step 1: Write the AccountInsightsSidebar component code**

Create `AccountInsightsSidebar.tsx` to handle Credit utilization bar, Primary contact, preferences, and quick actions.

```tsx
'use client';

import { Calendar, User, Phone, Mail, ShieldCheck, ShieldAlert, CreditCard, ChevronRight, Ban, CheckCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, Button, Progress, Avatar, AvatarFallback, Badge } from '@/shared/components/ui';
import type { Customer, Contact } from '../../types';
import { formatCurrency } from '../../utils';

interface AccountInsightsSidebarProps {
  customer: Customer;
  primaryContact?: Contact;
  onOpenCreditLimitDialog: () => void;
  onOpenMeetingRequest: () => void;
  onActivate: () => Promise<void>;
  onDeactivate: () => Promise<void>;
  onDeleteAccount: () => void;
  isActivating: boolean;
  isDeactivating: boolean;
}

export function AccountInsightsSidebar({
  customer,
  primaryContact,
  onOpenCreditLimitDialog,
  onOpenMeetingRequest,
  onActivate,
  onDeactivate,
  onDeleteAccount,
  isActivating,
  isDeactivating,
}: AccountInsightsSidebarProps) {
  const creditLimit = Number(customer.creditLimit || 0);
  const totalValue = Number(customer.totalValue || 0);
  
  // Utilization computation
  const utilizationPercent = creditLimit > 0 ? Math.min(100, Math.round((totalValue / creditLimit) * 100)) : 0;
  const isHighUtilization = utilizationPercent >= 90;
  const barColor = isHighUtilization ? 'bg-destructive' : utilizationPercent >= 70 ? 'bg-amber-500' : 'bg-emerald-500';

  return (
    <div className="space-y-6">
      {/* Credit Utilization Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center justify-between">
            Credit Health Indicator
            <CreditCard className="h-3.5 w-3.5 text-muted-foreground" />
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold">
              <span>Utilization Rate</span>
              <span className={isHighUtilization ? 'text-destructive font-bold' : ''}>{utilizationPercent}%</span>
            </div>
            {creditLimit > 0 ? (
              <div className="w-full bg-muted rounded-full h-1.5 overflow-hidden">
                <div className={`h-full ${barColor}`} style={{ width: `${utilizationPercent}%` }} />
              </div>
            ) : (
              <div className="text-[10px] text-muted-foreground italic">No credit limit configured.</div>
            )}
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-muted/20">
            <div>
              <div className="text-[9px] text-muted-foreground uppercase font-bold">Total Spent</div>
              <div className="font-bold text-foreground">{formatCurrency(totalValue)}</div>
            </div>
            <div>
              <div className="text-[9px] text-muted-foreground uppercase font-bold">Limit</div>
              <div className="font-bold text-foreground">{creditLimit > 0 ? formatCurrency(creditLimit) : 'No limit'}</div>
            </div>
          </div>

          <Button size="sm" variant="outline" className="w-full text-xs h-8" onClick={onOpenCreditLimitDialog}>
            Adjust Credit Limit
          </Button>
        </CardContent>
      </Card>

      {/* Primary Contact Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Primary Contact</CardTitle>
        </CardHeader>
        <CardContent>
          {primaryContact ? (
            <div className="flex items-start gap-3">
              <Avatar className="h-9 w-9">
                <AvatarFallback className="text-xs bg-primary text-primary-foreground">
                  {`${primaryContact.firstName?.[0] || ''}${primaryContact.lastName?.[0] || ''}`.toUpperCase() || 'C'}
                </AvatarFallback>
              </Avatar>
              <div className="space-y-0.5 overflow-hidden">
                <div className="text-xs font-bold truncate">{primaryContact.firstName} {primaryContact.lastName}</div>
                {primaryContact.jobPosition && <div className="text-[10px] text-muted-foreground truncate">{primaryContact.jobPosition}</div>}
                <div className="flex flex-col text-[10px] text-muted-foreground/80 pt-1.5 space-y-1">
                  <span className="flex items-center gap-1.5"><Mail className="h-3 w-3" /> {primaryContact.email}</span>
                  {primaryContact.phone && <span className="flex items-center gap-1.5"><Phone className="h-3 w-3" /> {primaryContact.phone}</span>}
                </div>
              </div>
            </div>
          ) : (
            <div className="text-xs text-muted-foreground italic text-center py-2">No primary contact selected.</div>
          )}
        </CardContent>
      </Card>

      {/* Preferences Card */}
      {(customer.preferredDays?.length > 0 || customer.preferredTimeSlots?.length > 0) && (
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Communication Preferences</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {customer.preferredDays?.length > 0 && (
              <div className="space-y-1">
                <div className="text-[9px] text-muted-foreground uppercase font-bold">Best Days</div>
                <div className="flex flex-wrap gap-1">
                  {customer.preferredDays.map((day) => (
                    <Badge key={day} variant="secondary" className="text-[9px] px-1.5 py-0.5 lowercase">{day.toLowerCase()}</Badge>
                  ))}
                </div>
              </div>
            )}
            {customer.preferredTimeSlots?.length > 0 && (
              <div className="space-y-1">
                <div className="text-[9px] text-muted-foreground uppercase font-bold">Best Hours</div>
                <div className="flex flex-wrap gap-1">
                  {customer.preferredTimeSlots.map((slot) => (
                    <Badge key={slot} variant="secondary" className="text-[9px] px-1.5 py-0.5 lowercase">{slot.toLowerCase().replace('_', ' ')}</Badge>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Action Hub */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Quick Action Hub</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <Button variant="outline" className="w-full justify-start text-xs h-9 border-muted/50 hover:bg-muted/40 text-foreground" onClick={onOpenMeetingRequest}>
            <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Request Meeting
          </Button>
          {customer.status === 'INACTIVE' ? (
            <Button variant="outline" className="w-full justify-start text-xs h-9 text-emerald-600 border-emerald-200 hover:bg-emerald-50" onClick={onActivate} disabled={isActivating}>
              <CheckCircle className="mr-2 h-4 w-4" /> Activate Account
            </Button>
          ) : (
            <Button variant="outline" className="w-full justify-start text-xs h-9 text-amber-600 border-amber-200 hover:bg-amber-50" onClick={onDeactivate} disabled={isDeactivating}>
              <Ban className="mr-2 h-4 w-4" /> Deactivate Account
            </Button>
          )}
          <Button variant="ghost" className="w-full justify-start text-xs h-9 text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" onClick={onDeleteAccount}>
            Delete Account Profile
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/components/detail/AccountInsightsSidebar.tsx
git commit -m "feat(crm): create AccountInsightsSidebar component"
```

---

### Task 6: Redesign `AccountForm` component (in `CustomerForm.tsx`)

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/CustomerForm.tsx`

- [ ] **Step 1: Open and inspect imports of `CustomerForm.tsx`**

Make sure Zod schema handles preferences correctly, and replace the JSX layout with a 2-column Grid (2/3 left side, 1/3 right side containing interactive tag clouds and toggle badges).

- [ ] **Step 2: Modify `CustomerForm.tsx` return statement & styles**

Modify the form's return statement to achieve:
1. Two-pane grid structure.
2. Toggle Badges for Preferred Days and Slots instead of standard checkbox elements.
3. Clean tag cloud selector widget.

```tsx
// Complete rewrite of the return block of AccountForm inside CustomerForm.tsx to reflect:
  return (
    <div className={cn("grid grid-cols-1 lg:grid-cols-3 gap-6", className)}>
      <Card className="lg:col-span-2 border border-muted/50 shadow-sm rounded-xl">
        <CardHeader>
          <CardTitle className="text-xl font-extrabold tracking-tight">
            {customer ? 'Update Business Account' : 'Register Business Account'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onFormSubmit} className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Basic Profile</h3>

              <div className="space-y-2">
                <Label htmlFor="name">Account Name *</Label>
                <Input id="name" {...register('name')} placeholder="e.g. Acme Corporation" className={errors.name ? 'border-destructive' : ''} disabled={isLoading} />
                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="companyName">Company Registered Name</Label>
                  <Input id="companyName" {...register('companyName')} placeholder="e.g. Acme Corp LLC" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="taxNumber">Tax Identification Number</Label>
                  <Input id="taxNumber" {...register('taxNumber')} placeholder="e.g. 0102030405" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Customer Type *</Label>
                  <Select value={watch('customerType')} onValueChange={(val) => setValue('customerType', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="COMPANY">Company / Enterprise</SelectItem>
                      <SelectItem value="INDIVIDUAL">Individual Customer</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Account Tier</Label>
                  <Select value={watch('tier') || ''} onValueChange={(val) => setValue('tier', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select tier" /></SelectTrigger>
                    <SelectContent>
                      {ACCOUNT_TIERS.map((tier) => (
                        <SelectItem key={tier.value} value={tier.value}>{tier.label}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="industry">Industry / Sector</Label>
                  <Input id="industry" {...register('industry')} placeholder="e.g. Logistics" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="companySize">Company Size (Employees)</Label>
                  <Input id="companySize" type="number" {...register('companySize', { valueAsNumber: true })} placeholder="100" disabled={isLoading} />
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Contacts & Locale</h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Primary Email *</Label>
                  <Input id="email" type="email" {...register('email')} placeholder="billing@acme.com" className={errors.email ? 'border-destructive' : ''} disabled={isLoading} />
                  {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone Number</Label>
                  <Input id="phone" {...register('phone')} placeholder="+84 901234567" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="website">Website Address</Label>
                  <Input id="website" {...register('website')} placeholder="https://acme.com" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="timezone">Office Timezone</Label>
                  <Input id="timezone" {...register('timezone')} placeholder="Asia/Ho_Chi_Minh" disabled={isLoading} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="language">Preferred Language</Label>
                  <Input id="language" {...register('language')} placeholder="vi" disabled={isLoading} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="creditLimit">Credit Limit (VND)</Label>
                  <Input id="creditLimit" type="number" {...register('creditLimit', { valueAsNumber: true })} placeholder="100000000" disabled={isLoading} />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="address">Registered Office Address</Label>
                <Textarea id="address" {...register('address')} rows={2} placeholder="Building 12, Tech Park, Hanoi" disabled={isLoading} />
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-muted/30">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">Internal Remarks</h3>
              <div className="space-y-2">
                <Label htmlFor="notes">Static Account Notes</Label>
                <Textarea id="notes" {...register('notes')} rows={3} placeholder="Add specific terms or relationship summaries..." disabled={isLoading} />
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-6 border-t border-muted/30">
              {onCancel && (
                <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading || isSubmitting}>
                  Cancel
                </Button>
              )}
              <Button type="submit" disabled={isLoading || isSubmitting}>
                {isSubmitting ? 'Saving...' : customer ? 'Update Account' : 'Register Account'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Widget Column */}
      <div className="lg:col-span-1 space-y-6">
        {/* Tag Cloud Selector */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader>
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Account Tags Cloud</CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-3">
            <div className="flex flex-wrap gap-2">
              {['VIP', 'PARTNER', 'TARGET', 'KEY_ACCOUNT', 'PROSPECT', 'INACTIVE'].map((tg) => {
                const isSelected = watch('tags')?.includes(tg);
                return (
                  <Badge
                    key={tg}
                    variant={isSelected ? 'default' : 'outline'}
                    className="cursor-pointer text-[10px] px-2 py-0.5 select-none"
                    onClick={() => {
                      const cur = watch('tags') || [];
                      if (cur.includes(tg)) {
                        setValue('tags', cur.filter((t) => t !== tg));
                      } else {
                        setValue('tags', [...cur, tg]);
                      }
                    }}
                  >
                    {tg.replace('_', ' ')}
                  </Badge>
                );
              })}
            </div>
            <p className="text-[10px] text-muted-foreground italic">Click badges to toggle association.</p>
          </CardContent>
        </Card>

        {/* Preferred Communication Badges */}
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader>
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Best Contact Days</CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-4">
            <div className="flex flex-wrap gap-1.5">
              {PREFERRED_DAYS.map((day) => {
                const isSelected = watch('preferredDays')?.includes(day.value);
                return (
                  <Badge
                    key={day.value}
                    variant={isSelected ? 'default' : 'secondary'}
                    className="cursor-pointer text-[10px] px-2 py-1 select-none"
                    onClick={() => {
                      const cur = watch('preferredDays') || [];
                      if (cur.includes(day.value)) {
                        setValue('preferredDays', cur.filter((d) => d !== day.value));
                      } else {
                        setValue('preferredDays', [...cur, day.value]);
                      }
                    }}
                  >
                    {day.label}
                  </Badge>
                );
              })}
            </div>

            <div className="pt-3 border-t border-muted/20 space-y-2">
              <div className="text-[10px] text-muted-foreground uppercase font-bold">Best Hours</div>
              <div className="flex flex-wrap gap-1.5">
                {PREFERRED_TIME_SLOTS.map((slot) => {
                  const isSelected = watch('preferredTimeSlots')?.includes(slot.value);
                  return (
                    <Badge
                      key={slot.value}
                      variant={isSelected ? 'default' : 'secondary'}
                      className="cursor-pointer text-[10px] px-2 py-1 select-none"
                      onClick={() => {
                        const cur = watch('preferredTimeSlots') || [];
                        if (cur.includes(slot.value)) {
                          setValue('preferredTimeSlots', cur.filter((s) => s !== slot.value));
                        } else {
                          setValue('preferredTimeSlots', [...cur, slot.value]);
                        }
                      }}
                    >
                      {slot.label}
                    </Badge>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
```

- [ ] **Step 3: Commit changes**

```bash
git add serp_web/src/modules/crm/components/forms/CustomerForm.tsx
git commit -m "feat(crm): redesign AccountForm with two-pane grid and toggle badges"
```

---

### Task 7: Refactor `CustomerDetailPageEnhanced.tsx`

**Files:**
- Modify: `serp_web/src/modules/crm/pages/customers/CustomerDetailPageEnhanced.tsx`

- [ ] **Step 1: Restructure detail page and fetch associated opportunities**

Modify `CustomerDetailPageEnhanced.tsx` to:
1. Import all new sub-components: `AccountProfileSidebar`, `AccountInsightsSidebar`, `QuickComposer`, `UnifiedTimeline`, `OpportunityDealList`.
2. Add queries for related opportunities.
3. Assemble the layout into the 3-column asymmetric configuration.

```tsx
// Top level state imports and sub-components wiring in CustomerDetailPageEnhanced.tsx:
import { AccountProfileSidebar } from './components/detail/AccountProfileSidebar';
import { QuickComposer } from './components/detail/QuickComposer';
import { UnifiedTimeline } from './components/detail/UnifiedTimeline';
import { OpportunityDealList } from './components/detail/OpportunityDealList';
import { AccountInsightsSidebar } from './components/detail/AccountInsightsSidebar';
import { useGetOpportunitiesQuery } from '../../api/crmApi';
```

And update the return body to compile cleanly under the new layout.

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/customers/CustomerDetailPageEnhanced.tsx
git commit -m "refactor(crm): assemble redesigned 3-column Account Detail Page"
```

---

### Task 8: Build and Verify Compilation

**Files:**
- Test: Build commands

- [ ] **Step 1: Run type-check**

Run: `npm run type-check` (inside `serp_web/`)
Expected: Completed successfully (no compiler errors).

- [ ] **Step 2: Run lint check**

Run: `npm run lint` (inside `serp_web/`)
Expected: Completed successfully.

- [ ] **Step 3: Run Next.js production build**

Run: `npm run build` (inside `serp_web/`)
Expected: Successfully generated routes for `/crm/accounts/[accountId]`.

- [ ] **Step 4: Commit and finalize**

```bash
git commit --allow-empty -m "chore(crm): complete Account detail and form redesign verification"
```
