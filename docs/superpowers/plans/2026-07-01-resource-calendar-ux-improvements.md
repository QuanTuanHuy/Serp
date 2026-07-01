# Resource Calendar UX/UI Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the Resource Calendar settings page in the PM module of `serp_web` into a modern integrated tabbed dashboard featuring a visual week grid editor, user directory with bulk-actions, and an interactive month calendar view for exceptions.

**Architecture:** 
- Restructure the parent container `PMResourceCalendarSettingsSection.tsx` to handle tabbed layout and fetch all organization user profiles.
- Introduce a visual schedule grid and "Copy to weekdays" button in `PMResourceCalendarProfileDialog.tsx`.
- Redesign `PMResourceCalendarAssignmentPanel.tsx` to display all users in a table with search/filtering and a floating action bar for bulk profile assignments.
- Redesign `PMResourceCalendarExceptionPanel.tsx` to include an interactive month grid that color-codes exceptions and resolves user names.

**Tech Stack:** React 19, TypeScript, Radix UI Tabs/Dialog/Select/Tooltip, Lucide Icons, TailwindCSS.

---

### Task 1: Resolve User ID to Name Map in Parent Component

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx`

- [ ] **Step 1: Add organization user query imports and local mapping logic**
  Update [PMResourceCalendarSettingsSection.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx) to fetch organization users and pass down a parsed `userMap` to the child panels.
  
  ```typescript
  // In imports, add:
  import { selectOrganizationId } from '@/modules/account/store';
  import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  import { useAppSelector } from '@/shared/hooks';
  import type { UserProfile } from '@/modules/admin/types';
  ```

- [ ] **Step 2: Add logic to fetch all PM module users and construct the mapping**
  Inside the `PMResourceCalendarSettingsSection` function:
  ```typescript
  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
  const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

  const usersQuery = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100, // Fetch up to 100 users for local mapping
      status: 'ACTIVE',
      moduleId: pmModuleId,
    },
    {
      skip: !organizationId,
    }
  );

  const userMap = useMemo(() => {
    const map = new Map<number, UserProfile>();
    (usersQuery.data?.data.items ?? []).forEach((user) => {
      map.set(user.id, user);
    });
    return map;
  }, [usersQuery.data]);
  ```

- [ ] **Step 3: Run build check to verify compilation**
  Run: `npm run type-check` from `serp_web/`
  Expected: PASS

- [ ] **Step 4: Commit**
  ```bash
  git add src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx
  git commit -m "feat: fetch and map organization users in Resource Calendar parent component"
  ```

---

### Task 2: Refactor Section Container to Use Tabs Layout

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx`

- [ ] **Step 1: Import Tabs components**
  Import `<Tabs>`, `<TabsList>`, `<TabsTrigger>`, and `<TabsContent>` from `@/shared/components/ui/tabs`.

- [ ] **Step 2: Refactor return statement to include Tabs**
  Update [PMResourceCalendarSettingsSection.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx)'s layout:
  ```tsx
  return (
    <div className='space-y-4'>
      {/* Title Header */}
      <div className='flex flex-col gap-3 border-b border-border/60 pb-4 md:flex-row md:items-start md:justify-between'>
        <div className='space-y-1'>
          <h1 className='text-2xl font-semibold tracking-tight'>Resource calendars</h1>
          <p className='text-sm text-muted-foreground'>
            Configure workspace-level working calendars and capacity for PM optimization.
          </p>
        </div>
        <Button type='button' onClick={() => setProfileDialog({ mode: 'create' })}>
          <Plus className='mr-2 h-4 w-4' />
          Add calendar
        </Button>
      </div>

      {/* Stats Cards */}
      <div className='grid gap-3 md:grid-cols-4'>
        <MiniStat title='Profiles' value={profileCount} />
        <MiniStat title='Assigned users' value={assignedUserCount} />
        <MiniStat title='Exceptions' value={exceptions.length} />
        <MiniStat title='Unassigned users' value={overview?.unassignedUserIds.length ?? 0} />
      </div>

      <Tabs defaultValue='profiles' className='w-full space-y-4'>
        <TabsList className='grid w-full grid-cols-3 max-w-[400px]'>
          <TabsTrigger value='profiles'>Profiles</TabsTrigger>
          <TabsTrigger value='assignments'>Assignments</TabsTrigger>
          <TabsTrigger value='exceptions'>Exceptions</TabsTrigger>
        </TabsList>

        <TabsContent value='profiles'>
          <Card className='border-border/60 bg-background/90 shadow-sm'>
            <CardHeader className='flex flex-row items-center justify-between border-b py-4'>
              <CardTitle className='flex items-center gap-2 text-sm'>
                <CalendarClock className='h-4 w-4 text-muted-foreground' />
                Calendar profiles
              </CardTitle>
            </CardHeader>
            <CardContent className='p-0'>
              <PMResourceCalendarProfileTable
                profiles={profiles}
                onEdit={(profile) => setProfileDialog({ mode: 'edit', item: profile })}
                onDelete={(profile) => setDeleteTarget({ kind: 'profile', item: profile })}
              />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='assignments'>
          <div className='grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(320px,0.4fr)]'>
            <PMResourceCalendarAssignmentPanel
              profiles={profiles}
              assignments={assignments}
              selectedProfileId={selectedProfileId}
              isSubmitting={replaceAssignmentsState.isLoading}
              onProfileChange={setSelectedProfileId}
              onSubmit={handleAssignmentsSubmit}
              userMap={userMap}
              users={usersQuery.data?.data.items ?? []}
            />
            <Card className='border-border/60 bg-background/90 shadow-sm self-start'>
              <CardHeader className='border-b py-4'>
                <CardTitle className='flex items-center gap-2 text-sm'>
                  <Users className='h-4 w-4 text-muted-foreground' />
                  Coverage
                </CardTitle>
              </CardHeader>
              <CardContent className='space-y-2 p-4 text-sm text-muted-foreground'>
                <p>{assignedUserCount} assigned user(s) use calendar profiles.</p>
                <p>
                  Materialized window:{' '}
                  {overview?.materializedWindowStart ? formatDate(overview.materializedWindowStart) : '-'} to{' '}
                  {overview?.materializedWindowEnd ? formatDate(overview.materializedWindowEnd) : '-'}
                </p>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value='exceptions'>
          <PMResourceCalendarExceptionPanel
            exceptions={exceptions}
            isSubmitting={isExceptionSaving}
            onSubmit={handleExceptionSubmit}
            onDelete={(exception) => setDeleteTarget({ kind: 'exception', item: exception })}
            userMap={userMap}
          />
        </TabsContent>
      </Tabs>
      {/* Dialogs and ConfirmDialog remain the same */}
    </div>
  );
  ```

- [ ] **Step 3: Run compilation and lint check**
  Run: `npm run lint` from `serp_web/`
  Expected: PASS

- [ ] **Step 4: Commit**
  ```bash
  git add src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx
  git commit -m "feat: restructure Resource Calendar section layout using tabs"
  ```

---

### Task 3: Visual Weekly Blocks Editor & Quick Copy Action

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileDialog.tsx`

- [ ] **Step 1: Build the Visual Mon-Sun Scheduler grid**
  In [PMResourceCalendarProfileDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileDialog.tsx), add a summary box of the blocks at the top of the blocks section:
  ```tsx
  // Add a component to display day badges
  const daysMap: Record<number, string> = {
    1: 'Mon', 2: 'Tue', 3: 'Wed', 4: 'Thu', 5: 'Fri', 6: 'Sat', 7: 'Sun'
  };

  // Inside PMResourceCalendarProfileDialog:
  const getBlocksForDay = (day: number) => {
    return blocks.filter(b => b.dayOfWeek === day);
  };
  ```

- [ ] **Step 2: Add "Copy Monday to Weekdays" function**
  Add a handler to replicate Monday's blocks to Tuesday, Wednesday, Thursday, and Friday:
  ```tsx
  const handleCopyMondayToWeekdays = () => {
    const mondayBlocks = blocks.filter((b) => b.dayOfWeek === 1);
    if (mondayBlocks.length === 0) {
      toast.error("Please configure Monday's hours first.");
      return;
    }

    // Generate duplicate blocks for days 2, 3, 4, and 5
    const duplicatedBlocks: BlockDraft[] = [];
    [2, 3, 4, 5].forEach((day) => {
      mondayBlocks.forEach((monBlock) => {
        duplicatedBlocks.push({
          key: crypto.randomUUID(),
          dayOfWeek: day,
          startTime: monBlock.startTime,
          endTime: monBlock.endTime,
          capacityFactor: monBlock.capacityFactor,
        });
      });
    });

    // Keep weekend and Monday blocks, replace Tue-Fri
    setBlocks((current) => {
      const nonWeekdayBlocks = current.filter((b) => b.dayOfWeek === 1 || b.dayOfWeek === 6 || b.dayOfWeek === 7);
      return [...nonWeekdayBlocks, ...duplicatedBlocks].sort((a, b) => a.dayOfWeek - b.dayOfWeek);
    });
    toast.success("Copied Monday's schedule to weekdays.");
  };
  ```

- [ ] **Step 3: Update Weekly Blocks form section**
  Insert the visual week cards and copy button before the blocks listing:
  ```tsx
  <div className='space-y-3'>
    <div className='flex items-center justify-between gap-3'>
      <div className='space-y-1'>
        <Label className='text-base font-semibold'>Weekly blocks</Label>
        <p className='text-xs text-muted-foreground'>Configure working blocks per day of the week.</p>
      </div>
      <div className='flex gap-2'>
        <Button type='button' variant='outline' size='sm' onClick={handleCopyMondayToWeekdays}>
          Copy Monday to Weekdays (Tue-Fri)
        </Button>
        <Button type='button' variant='outline' size='sm' onClick={addBlock}>
          <Plus className='mr-2 h-4 w-4' />
          Add block
        </Button>
      </div>
    </div>

    {/* Visual 7-day grid */}
    <div className='grid grid-cols-7 gap-2 border rounded-md p-3 bg-muted/40'>
      {[1, 2, 3, 4, 5, 6, 7].map((day) => {
        const dayBlocks = getBlocksForDay(day);
        return (
          <div key={day} className={cn(
            'flex flex-col items-center justify-center p-2 rounded-md border text-center text-xs',
            dayBlocks.length > 0 ? 'bg-primary/10 border-primary/20 text-primary-foreground font-medium' : 'bg-muted border-border text-muted-foreground'
          )}>
            <span className='font-semibold'>{daysMap[day]}</span>
            {dayBlocks.length > 0 ? (
              dayBlocks.map((b, i) => (
                <span key={i} className='text-[10px] mt-1 block'>
                  {b.startTime}-{b.endTime}
                </span>
              ))
            ) : (
              <span className='text-[10px] mt-1 opacity-70'>Off</span>
            )}
          </div>
        );
      })}
    </div>
    {/* Chronological Blocks input fields list */}
  </div>
  ```

- [ ] **Step 4: Run type-check and lint**
  Run: `npm run type-check` from `serp_web/`
  Expected: PASS

- [ ] **Step 5: Commit**
  ```bash
  git add src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileDialog.tsx
  git commit -m "feat: add visual scheduler week grid and copy monday action in Profile dialog"
  ```

---

### Task 4: Resource Directory and Bulk Assignment

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarAssignmentPanel.tsx`

- [ ] **Step 1: Restructure Panel to display All Users Table**
  Modify [PMResourceCalendarAssignmentPanel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarAssignmentPanel.tsx) to list all organization users. Resolve their names and emails.
  
  ```typescript
  import type { UserProfile } from '@/modules/admin/types';
  import { Checkbox } from '@/shared/components/ui/checkbox';
  import { Input } from '@/shared/components/ui/input';
  import { Badge } from '@/shared/components/ui/badge';
  import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
  } from '@/shared/components/ui/table';
  ```

- [ ] **Step 2: Add Search, Filter and Bulk Select states**
  Inside the panel component:
  ```typescript
  const [searchTerm, setSearchTerm] = useState('');
  const [filterProfileId, setFilterProfileId] = useState<string>('all');
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);
  const [bulkProfileId, setBulkProfileId] = useState<string>('');
  const [bulkEffectiveFrom, setBulkEffectiveFrom] = useState('');
  const [bulkEffectiveTo, setBulkEffectiveTo] = useState('');
  ```

- [ ] **Step 3: Implement Table, Search, and Bulk assignment submission**
  Render a list of users. If a user is assigned, show their profile name in a Badge.
  If they are checked, show a floating Bulk Action Bar. When clicking "Assign in Bulk", build and dispatch the list of updated assignments.
  
  ```tsx
  // In handleSubmit for bulk assignments:
  const handleBulkSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bulkProfileId || selectedUserIds.length === 0 || !bulkEffectiveFrom) {
      toast.error('Please select a profile, effective date, and at least one user.');
      return;
    }

    const targetProfileId = Number(bulkProfileId);
    
    // Build new assignments:
    // Gather all existing assignments for the TARGET profile, but replace/add the new bulk ones
    const currentProfileAssignments = assignments.filter(a => a.profileId === targetProfileId);
    const otherProfileAssignments = assignments.filter(a => a.profileId !== targetProfileId);

    // Filter out users from other profiles if they are being assigned to this new profile
    const filteredOtherAssignments = otherProfileAssignments.filter(
      a => !selectedUserIds.includes(a.userId)
    );

    const newAssignments = [
      ...currentProfileAssignments.filter(a => !selectedUserIds.includes(a.userId)),
      ...selectedUserIds.map(userId => ({
        userId,
        effectiveFrom: bulkEffectiveFrom,
        effectiveTo: bulkEffectiveTo.trim() || null,
      }))
    ];

    await onSubmit(targetProfileId, { assignments: newAssignments.map(a => ({
      userId: a.userId,
      effectiveFrom: a.effectiveFrom,
      effectiveTo: a.effectiveTo
    })) });
    setSelectedUserIds([]);
  };
  ```

- [ ] **Step 4: Run build check**
  Run: `npm run lint` and `npm run type-check` from `serp_web/`
  Expected: PASS

- [ ] **Step 5: Commit**
  ```bash
  git add src/modules/pm/components/settings/resource-calendar/PMResourceCalendarAssignmentPanel.tsx
  git commit -m "feat: implement Resource Directory table and bulk profile assignments"
  ```

---

### Task 5: Interactive Exceptions Calendar and User Mapping

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarExceptionPanel.tsx`

- [ ] **Step 1: Add Calendar View / List View toggle**
  In [PMResourceCalendarExceptionPanel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarExceptionPanel.tsx), add a local state:
  ```typescript
  const [viewMode, setViewMode] = useState<'calendar' | 'list'>('calendar');
  const [currentDate, setCurrentDate] = useState(new Date());
  ```

- [ ] **Step 2: Build the interactive Monthly Lịch grid**
  Render a custom monthly calendar. Compute days in the month, fill empty days before/after.
  Map exceptions to days based on start/end timestamps.
  ```typescript
  const getDaysInMonth = (year: number, month: number) => {
    const date = new Date(year, month, 1);
    const days = [];
    while (date.getMonth() === month) {
      days.push(new Date(date));
      date.setDate(date.getDate() + 1);
    }
    return days;
  };
  ```
  Render daily cells with exceptions display. Display color-coded event bars:
  - `UNAVAILABLE` exception shows in Red badge.
  - `CAPACITY_OVERRIDE` exception shows in Yellow/Orange badge.

- [ ] **Step 3: Update Exception Dialog fields**
  Inside the `ExceptionDialog` form:
  - If `exceptionType === 'UNAVAILABLE'`, disable/hide the `Capacity factor` input.
  - Pre-fill dates when creating an exception from a calendar cell click.
  - Map `userId` inside tables to user profiles name/email using the provided `userMap`.

- [ ] **Step 4: Run build check**
  Run: `npm run build` from `serp_web/`
  Expected: Success without errors.

- [ ] **Step 5: Commit**
  ```bash
  git add src/modules/pm/components/settings/resource-calendar/PMResourceCalendarExceptionPanel.tsx
  git commit -m "feat: implement Month Exception calendar view and user name mapping"
  ```
