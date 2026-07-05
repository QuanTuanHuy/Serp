# CRM Activity UX/UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the CRM Activity Detail page into a 2-column asymmetric layout, group activities in the list page by urgency, and restructure `ActivityForm` into a dynamic 2-column layout.

**Architecture:** Split the detail page and list page logic into modular components under `src/modules/crm/pages/activities/components/`. Keep the calendar view untouched. Use React state and react-hook-form to implement dynamic input field visibility in `ActivityForm`.

**Tech Stack:** React 19, Next.js 15, Tailwind CSS, Lucide Icons, Shadcn UI.

---

### Task 1: Create `ActivityProfile` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/activities/components/detail/ActivityProfile.tsx`

- [ ] **Step 1: Write the component code**

Create `ActivityProfile.tsx` to display description, outcome, follow-up, and tags.

```tsx
'use client';

import { FileText, CheckCircle, Bell, Tag } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, Badge } from '@/shared/components/ui';
import type { Activity } from '../../../../types';

interface ActivityProfileProps {
  activity: Activity;
  formatDate: (dateString?: string) => string;
}

export function ActivityProfile({ activity, formatDate }: ActivityProfileProps) {
  const isCompleted = activity.status === 'COMPLETED';

  return (
    <div className="space-y-6">
      {/* Description */}
      <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
        <CardHeader className="pb-3">
          <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
            <FileText className="h-4 w-4" /> Description
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap">
            {activity.description || 'No description provided.'}
          </p>
        </CardContent>
      </Card>

      {/* Outcome (Only if Completed) */}
      {isCompleted && activity.outcome && (
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
              <CheckCircle className="h-4 w-4 text-emerald-600" /> Outcome / Results
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-foreground/80 leading-relaxed whitespace-pre-wrap">
              {activity.outcome}
            </p>
          </CardContent>
        </Card>
      )}

      {/* Follow-up Required (Only if applicable) */}
      {activity.followUpRequired && (
        <Card className="border border-amber-200/50 bg-amber-50/50 dark:bg-amber-950/20 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-bold uppercase tracking-wider text-amber-800 dark:text-amber-400 flex items-center gap-2">
              <Bell className="h-4 w-4 animate-bounce" /> Follow-up Action Required
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-amber-700 dark:text-amber-300">
              Follow-up scheduled date: <span className="font-semibold">{formatDate(activity.followUpDate)}</span>
            </p>
          </CardContent>
        </Card>
      )}

      {/* Tags */}
      {activity.tags && activity.tags.length > 0 && (
        <Card className="border border-muted/50 shadow-sm rounded-xl overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
              <Tag className="h-4 w-4" /> Activity Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-1.5">
              {activity.tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-[10px] px-2 py-0.5">
                  {tag}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/components/detail/ActivityProfile.tsx
git commit -m "feat(crm): create ActivityProfile sub-component for detail page"
```

---

### Task 2: Create `ActivityNotesTab` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/activities/components/detail/ActivityNotesTab.tsx`

- [ ] **Step 1: Write the component code**

Create `ActivityNotesTab.tsx` to handle listing notes for the activity.

```tsx
'use client';

import { MessageSquare } from 'lucide-react';
import { Card, CardContent, Avatar, AvatarFallback } from '@/shared/components/ui';

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
                        <AvatarFallback className="text-[10px] bg-muted">{initials}</AvatarFallback>
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
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/components/detail/ActivityNotesTab.tsx
git commit -m "feat(crm): create ActivityNotesTab sub-component"
```

---

### Task 3: Create `ActivityMetadataSidebar` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/activities/components/detail/ActivityMetadataSidebar.tsx`

- [ ] **Step 1: Write the component code**

Create `ActivityMetadataSidebar.tsx` to handle linked entity, assignee, schedule card, and quick actions.

```tsx
'use client';

import Link from 'next/link';
import { Calendar, User, Phone, Mail, Video, Clock, MapPin, Building2, Target, ExternalLink, CheckCircle, AlertCircle, Ban } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, Button, Avatar, AvatarFallback, Badge, Separator } from '@/shared/components/ui';
import type { Activity } from '../../../../types';

interface ActivityMetadataSidebarProps {
  activity: Activity;
  assigneeDisplayName: string;
  assigneeInitials: string;
  formatDate: (dateString?: string) => string;
  formatDuration: (minutes?: number) => string;
  onOpenReschedule: () => void;
  onOpenStatusUpdate: () => void;
  onDeleteActivity: () => void;
}

export function ActivityMetadataSidebar({
  activity,
  assigneeDisplayName,
  assigneeInitials,
  formatDate,
  formatDuration,
  onOpenReschedule,
  onOpenStatusUpdate,
  onDeleteActivity,
}: ActivityMetadataSidebarProps) {
  const getRelatedLink = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return `/crm/accounts/${activity.relatedTo.id}`;
      case 'LEAD':
        return `/crm/leads/${activity.relatedTo.id}`;
      case 'OPPORTUNITY':
        return `/crm/opportunities/${activity.relatedTo.id}`;
      default:
        return '#';
    }
  };

  const getRelatedIcon = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return Building2;
      case 'LEAD':
        return User;
      case 'OPPORTUNITY':
        return Target;
      default:
        return User;
    }
  };

  const getRelatedLabel = () => {
    switch (activity.relatedTo.type) {
      case 'CUSTOMER':
        return 'Account';
      case 'LEAD':
        return 'Lead';
      case 'OPPORTUNITY':
        return 'Opportunity';
      default:
        return 'Entity';
    }
  };

  const RelatedIcon = getRelatedIcon();
  const relatedName = activity.relatedTo.name || 'Linked Object';

  return (
    <div className="space-y-6">
      {/* Linked Entity Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Linked Workspace</CardTitle>
        </CardHeader>
        <CardContent>
          <Link href={getRelatedLink()} className="block">
            <div className="flex items-center justify-between p-2.5 bg-muted/40 hover:bg-muted/70 border border-muted/30 rounded-xl transition-all group">
              <div className="flex items-center gap-2.5 overflow-hidden">
                <div className="rounded-lg bg-blue-100 dark:bg-blue-900/30 p-2 text-blue-600 dark:text-blue-400">
                  <RelatedIcon className="h-4.5 w-4.5" />
                </div>
                <div className="overflow-hidden">
                  <div className="text-[10px] text-muted-foreground uppercase font-bold">{getRelatedLabel()}</div>
                  <div className="text-xs font-bold text-foreground truncate group-hover:text-primary transition">{relatedName}</div>
                </div>
              </div>
              <ExternalLink className="h-3.5 w-3.5 text-muted-foreground group-hover:text-primary transition shrink-0" />
            </div>
          </Link>
        </CardContent>
      </Card>

      {/* Assignee Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Assignee</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-3">
            <Avatar className="h-9 w-9">
              <AvatarFallback className="text-xs bg-primary text-primary-foreground font-semibold">{assigneeInitials}</AvatarFallback>
            </Avatar>
            <div>
              <p className="text-xs font-bold text-foreground">{assigneeDisplayName}</p>
              <p className="text-[10px] text-muted-foreground">CRM Representative</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Schedule Info Card */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Schedule & Place</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3.5 text-xs">
          <div className="flex items-start gap-2.5">
            <Calendar className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
            <div>
              <span className="text-muted-foreground">Scheduled At</span>
              <p className="font-bold text-foreground mt-0.5">{formatDate(activity.scheduledDate)}</p>
            </div>
          </div>

          {activity.actualDate && (
            <div className="flex items-start gap-2.5">
              <CheckCircle className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <span className="text-muted-foreground">Actual At</span>
                <p className="font-bold text-foreground mt-0.5">{formatDate(activity.actualDate)}</p>
              </div>
            </div>
          )}

          <div className="flex items-start gap-2.5">
            <Clock className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
            <div>
              <span className="text-muted-foreground">Duration</span>
              <p className="font-bold text-foreground mt-0.5">{formatDuration(activity.duration)}</p>
            </div>
          </div>

          {activity.location && (
            <div className="flex items-start gap-2.5">
              <MapPin className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <span className="text-muted-foreground">Location</span>
                <p className="font-bold text-foreground mt-0.5">{activity.location}</p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Quick Action List */}
      <Card className="border border-muted/50 shadow-sm rounded-xl">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Actions Hub</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {activity.status !== 'COMPLETED' && (
            <Button className="w-full justify-start text-xs h-9" variant="outline" onClick={onOpenStatusUpdate}>
              <CheckCircle className="mr-2 h-4 w-4 text-emerald-600" /> Mark Completed
            </Button>
          )}
          <Button className="w-full justify-start text-xs h-9 border-muted/50 hover:bg-muted/40 text-foreground" variant="outline" onClick={onOpenReschedule}>
            <Calendar className="mr-2 h-4 w-4 text-muted-foreground" /> Reschedule Date
          </Button>
          <Button variant="ghost" className="w-full justify-start text-xs h-9 text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100/80" onClick={onDeleteActivity}>
            Delete Activity Record
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/components/detail/ActivityMetadataSidebar.tsx
git commit -m "feat(crm): create ActivityMetadataSidebar sub-component"
```

---

### Task 4: Create `ActivityUrgencyGroups` component

**Files:**
- Create: `serp_web/src/modules/crm/pages/activities/components/list/ActivityUrgencyGroups.tsx`

- [ ] **Step 1: Write the component code**

Create `ActivityUrgencyGroups.tsx` to sort and display activities in accordion-like collapse sections.

```tsx
'use client';

import { useState, useMemo } from 'react';
import { Calendar, CheckCircle2, ChevronDown, ChevronRight, Clock, AlertCircle } from 'lucide-react';
import { Card, CardContent, Badge } from '@/shared/components/ui';
import type { Activity } from '../../../../types';

interface ActivityUrgencyGroupsProps {
  activities: Activity[];
  selectedActivityIds: Set<string>;
  onSelectActivity: (id: string) => void;
  onViewActivity: (id: string) => void;
  getActivityIcon: (type: any) => any;
  getActivityColor: (type: any) => string;
  formatDate: (dateString?: string) => string;
}

export function ActivityUrgencyGroups({
  activities,
  selectedActivityIds,
  onSelectActivity,
  onViewActivity,
  getActivityIcon,
  getActivityColor,
  formatDate,
}: ActivityUrgencyGroupsProps) {
  const [collapsedGroups, setCollapsedGroups] = useState<Record<string, boolean>>({
    overdue: false,
    today: false,
    upcoming: false,
    history: true,
  });

  const toggleGroup = (groupKey: string) => {
    setCollapsedGroups((prev) => ({ ...prev, [groupKey]: !prev[groupKey] }));
  };

  const groups = useMemo(() => {
    const overdue: Activity[] = [];
    const today: Activity[] = [];
    const upcoming: Activity[] = [];
    const history: Activity[] = [];

    const now = new Date();
    const todayStr = now.toDateString();

    activities.forEach((act) => {
      if (act.status === 'COMPLETED' || act.status === 'CANCELLED') {
        history.push(act);
      } else {
        const actDate = act.scheduledDate ? new Date(act.scheduledDate) : null;
        if (!actDate) {
          upcoming.push(act);
        } else if (actDate.getTime() < now.getTime() && actDate.toDateString() !== todayStr) {
          overdue.push(act);
        } else if (actDate.toDateString() === todayStr) {
          today.push(act);
        } else {
          upcoming.push(act);
        }
      }
    });

    return { overdue, today, upcoming, history };
  }, [activities]);

  const renderSection = (title: string, groupKey: string, list: Activity[], colorClass: string) => {
    const isCollapsed = collapsedGroups[groupKey];
    if (list.length === 0) return null;

    return (
      <div className="space-y-2.5">
        <button
          onClick={() => toggleGroup(groupKey)}
          className="flex items-center gap-2 text-xs font-extrabold uppercase tracking-wider text-muted-foreground hover:text-foreground transition select-none"
        >
          {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
          {title} ({list.length})
        </button>

        {!isCollapsed && (
          <div className="space-y-2 pl-4 border-l border-muted/50">
            {list.map((act) => {
              const Icon = getActivityIcon(act.type);
              const badgeColor = getActivityColor(act.type);
              const isSelected = selectedActivityIds.has(act.id);

              return (
                <div
                  key={act.id}
                  className={`flex items-center justify-between p-3.5 bg-card border border-muted/50 hover:border-muted-foreground/30 rounded-xl transition duration-300 ${
                    isSelected ? 'ring-2 ring-primary' : 'shadow-sm'
                  }`}
                >
                  <div className="flex items-center gap-3 overflow-hidden">
                    <input
                      type="checkbox"
                      checked={isSelected}
                      onChange={() => onSelectActivity(act.id)}
                      className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary shrink-0"
                    />
                    <div className={`p-2 rounded-lg shrink-0 ${badgeColor}`}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="overflow-hidden">
                      <div
                        className="text-xs font-bold text-foreground hover:text-primary cursor-pointer truncate transition"
                        onClick={() => onViewActivity(act.id)}
                      >
                        {act.subject}
                      </div>
                      <div className="text-[10px] text-muted-foreground/80 flex items-center gap-2 mt-0.5">
                        <Clock className="h-3 w-3" /> {formatDate(act.scheduledDate)}
                        {act.assignedToName && <span>• Rep: {act.assignedToName}</span>}
                      </div>
                    </div>
                  </div>

                  <Badge variant={act.priority === 'URGENT' || act.priority === 'HIGH' ? 'destructive' : 'secondary'} className="text-[9px] px-1.5 h-5">
                    {act.priority}
                  </Badge>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {renderSection('Overdue Tasks', 'overdue', groups.overdue, 'bg-destructive/10 text-destructive')}
      {renderSection("Today's Agenda", 'today', groups.today, 'bg-blue-100 text-blue-700')}
      {renderSection('Upcoming Tasks', 'upcoming', groups.upcoming, 'bg-emerald-100 text-emerald-700')}
      {renderSection('Recently Closed / History', 'history', groups.history, 'bg-gray-100 text-gray-700')}
    </div>
  );
}
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/components/list/ActivityUrgencyGroups.tsx
git commit -m "feat(crm): create ActivityUrgencyGroups component"
```

---

### Task 5: Redesign `ActivityForm` with the dynamic 2-column grid layout

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/ActivityForm.tsx`

- [ ] **Step 1: Open and rewrite the layout in `ActivityForm.tsx`**

Modify `ActivityForm.tsx` return JSX to build a 2-column grid and dynamically hide/show fields (e.g. Location only for meetings, Outcome only for completed).

```tsx
// Rewrite the return statement of ActivityForm in ActivityForm.tsx to reflect:
  return (
    <div className={cn("grid grid-cols-1 lg:grid-cols-3 gap-6", className)}>
      <Card className="lg:col-span-2 border border-muted/50 shadow-sm rounded-xl">
        <CardHeader>
          <CardTitle className="text-xl font-extrabold tracking-tight">
            {activity ? 'Update Activity details' : 'Log New Activity'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onFormSubmit} className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">General Details</h3>
              
              <div className="space-y-2">
                <Label htmlFor="subject">Subject *</Label>
                <Input id="subject" {...register('subject')} placeholder="e.g. Q3 proposal phone call" className={errors.subject ? 'border-destructive' : ''} disabled={isLoading} />
                {errors.subject && <p className="text-xs text-destructive">{errors.subject.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Activity Type *</Label>
                  <Select value={watch('activityType')} onValueChange={(val) => setValue('activityType', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="CALL">Phone Call</SelectItem>
                      <SelectItem value="EMAIL">Email</SelectItem>
                      <SelectItem value="MEETING">Meeting</SelectItem>
                      <SelectItem value="TASK">Task</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="space-y-2">
                  <Label>Execution Status *</Label>
                  <Select value={watch('status')} onValueChange={(val) => setValue('status', val as any)} disabled={isLoading}>
                    <SelectTrigger><SelectValue placeholder="Select status" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PLANNED">Planned / Upcoming</SelectItem>
                      <SelectItem value="COMPLETED">Completed</SelectItem>
                      <SelectItem value="CANCELLED">Cancelled</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Task Description</Label>
                <Textarea id="description" {...register('description')} rows={3} placeholder="Provide details about the call or task goals..." disabled={isLoading} />
              </div>
            </div>

            {/* Dynamic location input */}
            {watchedActivityType === 'MEETING' && (
              <div className="space-y-4 pt-4 border-t border-muted/30 animate-in fade-in duration-300">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Meeting Details</h3>
                <div className="space-y-2">
                  <Label htmlFor="location">Meeting Location / Video Link</Label>
                  <Input id="location" {...register('location')} placeholder="Hanoi Hub Office or Google Meet link" disabled={isLoading} />
                </div>
              </div>
            )}

            {/* Dynamic outcome input */}
            {watch('status') === 'COMPLETED' && (
              <div className="space-y-4 pt-4 border-t border-muted/30 animate-in fade-in duration-300">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Outcomes</h3>
                <div className="space-y-2">
                  <Label htmlFor="outcome">Task Result Notes</Label>
                  <Textarea id="outcome" {...register('outcome')} rows={3} placeholder="Enter agreement terms, outcome notes, next steps..." disabled={isLoading} />
                </div>
              </div>
            )}

            <div className="flex justify-end space-x-3 pt-6 border-t border-muted/30">
              {onCancel && (
                <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading || isSubmitting}>
                  Cancel
                </Button>
              )}
              <Button type="submit" disabled={isLoading || isSubmitting}>
                {isSubmitting ? 'Saving...' : 'Save Activity'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Right Column details */}
      <div className="lg:col-span-1 space-y-6">
        <Card className="border border-muted/50 shadow-sm rounded-xl">
          <CardHeader>
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Task Assignments & Schedule</CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-4">
            <div className="space-y-2">
              <Label>Assigned Representative</Label>
              <Controller
                name="assignedTo"
                control={control}
                render={({ field }) => (
                  <CRMUserSelect
                    id="assignedTo"
                    value={field.value}
                    onChange={field.onChange}
                    fallbackUserName={activity?.assignedToName}
                    disabled={isLoading}
                  />
                )}
              />
            </div>

            <div className="space-y-2">
              <Label>Task Urgency / Priority *</Label>
              <Select value={watch('priority')} onValueChange={(val) => setValue('priority', val as any)} disabled={isLoading}>
                <SelectTrigger><SelectValue placeholder="Select priority" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="LOW">Low</SelectItem>
                  <SelectItem value="MEDIUM">Medium</SelectItem>
                  <SelectItem value="HIGH">High</SelectItem>
                  <SelectItem value="URGENT">Urgent</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="durationMinutes">Duration (min)</Label>
                <Input id="durationMinutes" {...register('durationMinutes')} placeholder="30" disabled={isLoading} />
              </div>
            </div>

            <div className="space-y-2">
              <Label>Target Schedule Date *</Label>
              <Controller
                name="scheduledDate"
                control={control}
                render={({ field }) => (
                  <CRMDatePicker
                    id="scheduledDate"
                    value={field.value}
                    onChange={(date) => field.onChange(date ? toLocalDateInputValue(date) : '')}
                    disabled={isLoading}
                  />
                )}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="scheduledTime">Target Hour</Label>
              <Input id="scheduledTime" type="time" {...register('scheduledTime')} disabled={isLoading} />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
```

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/components/forms/ActivityForm.tsx
git commit -m "feat(crm): redesign ActivityForm dynamic inputs grid"
```

---

### Task 6: Refactor `ActivityDetailPage.tsx`

**Files:**
- Modify: `serp_web/src/modules/crm/pages/activities/ActivityDetailPage.tsx`

- [ ] **Step 1: Wire detail page with sub-components**

Re-assemble the page to import: `ActivityProfile`, `ActivityNotesTab`, `ActivityMetadataSidebar`. Clean up old monolith HTML blocks and bind queries correctly.

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/ActivityDetailPage.tsx
git commit -m "refactor(crm): modularize ActivityDetailPage layout"
```

---

### Task 7: Refactor `ActivityListPage.tsx`

**Files:**
- Modify: `serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx`

- [ ] **Step 1: Integrate Urgency groups**

Import `ActivityUrgencyGroups` and replace the list-view JSX block with it. Make sure calendar grid and state remains completely intact.

- [ ] **Step 2: Commit changes**

```bash
git add serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx
git commit -m "refactor(crm): integrate ActivityUrgencyGroups in list view"
```

---

### Task 8: Build and Verify Compilation

**Files:**
- Test: Build validation

- [ ] **Step 1: Run type checks**

Run: `npm run type-check` (inside `serp_web/`)
Expected: success.

- [ ] **Step 2: Run lint check**

Run: `npm run lint` (inside `serp_web/`)
Expected: success.

- [ ] **Step 3: Run compiler build**

Run: `npm run build` (inside `serp_web/`)
Expected: success.
