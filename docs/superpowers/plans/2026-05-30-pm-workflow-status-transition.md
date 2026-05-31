# PM Workflow Status And Transition Editing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users remove workflow transitions reliably and create a new status by name/category while adding it to a workflow draft.

**Architecture:** Reuse existing PM backend endpoints and frontend RTK Query structure. Add missing frontend contracts/hooks for status creation and status category listing, then extend `PMWorkflowEditorPage` with a two-mode add-status dialog and a direct transition remove action in the diagram inspector.

**Tech Stack:** Next.js 15, React 19, TypeScript, RTK Query, Tailwind/Shadcn UI, existing Spring Boot PM APIs.

---

## File Structure

- Modify `serp_web/src/modules/pm/types/work-item-api.types.ts`: add `PMStatusCategoryApi` and `PMCreateStatusRequest` contracts.
- Modify `serp_web/src/modules/pm/api/workItemApi.ts`: add `getPmStatusCategories` query and `createPmStatus` mutation.
- Modify `serp_web/src/modules/pm/api/index.ts`: export new hooks and types.
- Modify `serp_web/src/modules/pm/pages/PMWorkflowEditorPage.tsx`: wire category query/status mutation, add slug generator, update add-step flow, extend `AddStepDialog`, and add inspector remove/edit buttons for selected transitions.

No backend code change is planned because `pm_core` already provides required endpoints.

## Task 1: Add Status API Contracts And Hooks

**Files:**
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`

- [ ] **Step 1: Add status category and create-status types**

In `serp_web/src/modules/pm/types/work-item-api.types.ts`, add after `PMStatusApi`:

```ts
export interface PMStatusCategoryApi {
  id: number;
  tenantId: number;
  name: string;
  key: string;
  color?: string | null;
  isSystem: boolean;
  readOnly: boolean;
  createdAt?: number;
  createdBy?: number;
  updatedAt?: number;
  updatedBy?: number;
}

export interface PMCreateStatusRequest {
  statusKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  statusCategoryId: number;
}
```

- [ ] **Step 2: Import new contracts in work item API**

In `serp_web/src/modules/pm/api/workItemApi.ts`, extend the type import block:

```ts
  PMCreateStatusRequest,
  PMStatusCategoryApi,
```

- [ ] **Step 3: Add status category query and status create mutation**

In `serp_web/src/modules/pm/api/workItemApi.ts`, add after `getPmStatuses`:

```ts
    getPmStatusCategories: builder.query<
      PaginatedResponse<PMStatusCategoryApi>,
      PMProjectScopedListParams | void
    >({
      query: (params) => ({
        url: '/status-categories',
        method: 'GET',
        params: buildProjectScopedListParams(params || undefined),
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMStatusCategoryApi>(),
    }),

    createPmStatus: builder.mutation<PMStatusApi, PMCreateStatusRequest>({
      query: (body) => ({
        url: '/statuses',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMStatusApi>(),
    }),
```

- [ ] **Step 4: Export hooks and types**

In `serp_web/src/modules/pm/api/workItemApi.ts`, add exports at bottom:

```ts
  useCreatePmStatusMutation,
  useGetPmStatusCategoriesQuery,
```

In `serp_web/src/modules/pm/api/index.ts`, add to `workItemApi` export block:

```ts
  useCreatePmStatusMutation,
  useGetPmStatusCategoriesQuery,
```

Add to type export block:

```ts
  PMCreateStatusRequest,
  PMStatusCategoryApi,
```

- [ ] **Step 5: Run type check for API contract changes**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: no TypeScript errors from new API contracts.

## Task 2: Extend Workflow Add Status Dialog

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMWorkflowEditorPage.tsx`

- [ ] **Step 1: Import new hooks and type**

In `PMWorkflowEditorPage.tsx`, extend imports from `../api`:

```ts
  useCreatePmStatusMutation,
  useGetPmStatusCategoriesQuery,
```

Extend type imports from `../api`:

```ts
  PMStatusCategoryApi,
```

- [ ] **Step 2: Add create-status form type and key generator**

Near existing local type declarations, add:

```ts
type AddStepValues =
  | {
      mode: 'existing';
      statusId: number;
      isInitial: boolean;
      isTerminal: boolean;
    }
  | {
      mode: 'new';
      name: string;
      statusCategoryId: number;
      isInitial: boolean;
      isTerminal: boolean;
    };

function generateStatusKey(name: string): string {
  return name
    .trim()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase();
}
```

If `AddStepValues` already exists, replace it with this union instead of adding a duplicate.

- [ ] **Step 3: Wire categories and create-status mutation**

Inside `PMWorkflowEditorPage`, next to `statusesQuery`, add:

```ts
  const statusCategoriesQuery = useGetPmStatusCategoriesQuery({
    page: 0,
    pageSize: 100,
    sortBy: 'name',
    sortDirection: 'ASC',
  });
```

Next to `addStep` mutation, add:

```ts
  const [createStatus, createStatusState] = useCreatePmStatusMutation();
```

Next to `statuses`, add:

```ts
  const statusCategories = statusCategoriesQuery.data?.data.items ?? [];
```

- [ ] **Step 4: Update add-step handler for existing and new mode**

Replace `handleAddStep` with:

```ts
  const handleAddStep = useCallback(
    async (values: AddStepValues) => {
      const draftEditor = await ensureDraft();
      if (!draftEditor) {
        return;
      }

      try {
        const statusId =
          values.mode === 'existing'
            ? values.statusId
            : (
                await createStatus({
                  statusKey: generateStatusKey(values.name),
                  name: values.name.trim(),
                  description: null,
                  iconUrl: null,
                  statusCategoryId: values.statusCategoryId,
                }).unwrap()
              ).id;

        await addStep({
          workflowId,
          body: {
            statusId,
            isInitial: values.isInitial,
            isTerminal: values.isTerminal,
          },
        }).unwrap();
        await Promise.all([
          editorQuery.refetch().unwrap(),
          statusesQuery.refetch().unwrap(),
        ]);
        setStepDialogOpen(false);
        setValidation(null);
        toast.success(
          values.mode === 'new' ? 'Status created and added.' : 'Status added.'
        );
      } catch (error) {
        toast.error('Unable to add status', {
          description: getErrorMessage(error),
        });
      }
    },
    [addStep, createStatus, editorQuery, ensureDraft, statusesQuery, workflowId]
  );
```

- [ ] **Step 5: Pass categories and combined submitting state to dialog**

Replace `AddStepDialog` usage with:

```tsx
      <AddStepDialog
        open={stepDialogOpen}
        statuses={availableStatuses}
        statusCategories={statusCategories}
        isSubmitting={addStepState.isLoading || createStatusState.isLoading}
        onOpenChange={setStepDialogOpen}
        onSubmit={handleAddStep}
      />
```

- [ ] **Step 6: Replace AddStepDialog implementation**

Replace the full `AddStepDialog` function with:

```tsx
function AddStepDialog({
  open,
  statuses,
  statusCategories,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  open: boolean;
  statuses: PMStatusApi[];
  statusCategories: PMStatusCategoryApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: AddStepValues) => void;
}) {
  const [mode, setMode] = useState<'existing' | 'new'>('existing');
  const [statusId, setStatusId] = useState('');
  const [name, setName] = useState('');
  const [statusCategoryId, setStatusCategoryId] = useState('');
  const [isInitial, setIsInitial] = useState(false);
  const [isTerminal, setIsTerminal] = useState(false);

  useEffect(() => {
    if (!open) {
      setMode('existing');
      setStatusId('');
      setName('');
      setStatusCategoryId('');
      setIsInitial(false);
      setIsTerminal(false);
      return;
    }

    if (mode === 'existing' && statuses.length === 1) {
      setStatusId(String(statuses[0].id));
    }

    if (
      mode === 'existing' &&
      statusId &&
      statuses.every((status) => String(status.id) !== statusId)
    ) {
      setStatusId('');
    }

    if (mode === 'new' && statusCategories.length === 1) {
      setStatusCategoryId(String(statusCategories[0].id));
    }

    if (
      mode === 'new' &&
      statusCategoryId &&
      statusCategories.every(
        (category) => String(category.id) !== statusCategoryId
      )
    ) {
      setStatusCategoryId('');
    }
  }, [mode, open, statusCategoryId, statusCategories, statusId, statuses]);

  const canSubmit =
    mode === 'existing'
      ? Boolean(statusId)
      : Boolean(name.trim()) && Boolean(statusCategoryId);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add status</DialogTitle>
          <DialogDescription>
            Choose an existing status or create a new one for this workflow draft.
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='grid grid-cols-2 gap-2 rounded-md bg-muted p-1'>
            <Button
              type='button'
              variant={mode === 'existing' ? 'secondary' : 'ghost'}
              onClick={() => setMode('existing')}
            >
              Existing
            </Button>
            <Button
              type='button'
              variant={mode === 'new' ? 'secondary' : 'ghost'}
              onClick={() => setMode('new')}
            >
              New status
            </Button>
          </div>

          {mode === 'existing' ? (
            <div className='space-y-2'>
              <Label>Status</Label>
              <Select value={statusId} onValueChange={setStatusId}>
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  {statuses.map((status) => (
                    <SelectItem key={status.id} value={String(status.id)}>
                      {status.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ) : (
            <>
              <div className='space-y-2'>
                <Label>Name</Label>
                <Input
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='In QA Review'
                />
              </div>
              <div className='space-y-2'>
                <Label>Category</Label>
                <Select
                  value={statusCategoryId}
                  onValueChange={setStatusCategoryId}
                >
                  <SelectTrigger className='w-full'>
                    <SelectValue placeholder='Select category' />
                  </SelectTrigger>
                  <SelectContent>
                    {statusCategories.map((category) => (
                      <SelectItem key={category.id} value={String(category.id)}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </>
          )}

          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={isInitial}
              onCheckedChange={(checked) => setIsInitial(checked === true)}
            />
            Initial status
          </label>
          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={isTerminal}
              onCheckedChange={(checked) => setIsTerminal(checked === true)}
            />
            Terminal status
          </label>
        </div>
        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            disabled={!canSubmit || isSubmitting}
            onClick={() => {
              if (mode === 'existing') {
                onSubmit({
                  mode: 'existing',
                  statusId: Number(statusId),
                  isInitial,
                  isTerminal,
                });
                return;
              }

              onSubmit({
                mode: 'new',
                name,
                statusCategoryId: Number(statusCategoryId),
                isInitial,
                isTerminal,
              });
            }}
          >
            {mode === 'new' ? 'Create and add' : 'Add status'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
```

- [ ] **Step 7: Run frontend type check**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: pass. If import errors appear for `Input`, import it from `@/shared/components/ui/input` following existing import ordering.

## Task 3: Make Diagram Transition Removal Reachable

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMWorkflowEditorPage.tsx`

- [ ] **Step 1: Extend inspector props**

Update `WorkflowInspector` call:

```tsx
            <WorkflowInspector
              selectedId={selectedId}
              steps={steps}
              transitions={transitions}
              stepById={stepById}
              editable={editable}
              onEditTransition={handleEditTransition}
              onRemoveTransition={handleRemoveTransition}
            />
```

Update function signature props:

```tsx
function WorkflowInspector({
  selectedId,
  steps,
  transitions,
  stepById,
  editable,
  onEditTransition,
  onRemoveTransition,
}: {
  selectedId: string | null;
  steps: PMWorkflowStepApi[];
  transitions: PMWorkflowTransitionApi[];
  stepById: Map<number, PMWorkflowStepApi>;
  editable: boolean;
  onEditTransition: (transition: PMWorkflowTransitionApi) => void;
  onRemoveTransition: (transition: PMWorkflowTransitionApi) => void;
}) {
```

- [ ] **Step 2: Add inspector transition actions**

Inside selected-transition block, after sequence badge, add:

```tsx
              <div className='flex gap-2 pt-2'>
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  disabled={!editable}
                  onClick={() => onEditTransition(selectedTransition)}
                >
                  Edit
                </Button>
                <Button
                  type='button'
                  variant='destructive'
                  size='sm'
                  disabled={!editable}
                  onClick={() => onRemoveTransition(selectedTransition)}
                >
                  Remove
                </Button>
              </div>
```

- [ ] **Step 3: Keep draft transition matching unchanged unless failing**

Run manual code review on `findMatchingTransition(...)` and `handleRemoveTransition(...)`. If matching compares source/target/name and current draft steps, keep it. If it compares stale ids only, update matching to use `name`, `fromStepId` status identity, and `toStepId` status identity.

- [ ] **Step 4: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all pass.

## Task 4: Manual Verification

**Files:**
- No code files.

- [ ] **Step 1: Start required services**

Use existing project dev setup. If backend/frontend already run, keep them running.

- [ ] **Step 2: Verify create-and-add status**

In browser:

1. Open PM settings workflow editor.
2. Click `Add status`.
3. Choose `New status`.
4. Enter `In QA Review`.
5. Select category.
6. Submit.

Expected: toast says `Status created and added.` and workflow step appears.

- [ ] **Step 3: Verify remove transition from text tab**

In browser:

1. Open workflow editor `Text` tab.
2. Click remove icon for a transition.
3. Confirm browser prompt.

Expected: transition row disappears after refetch.

- [ ] **Step 4: Verify remove transition from diagram inspector**

In browser:

1. Open workflow editor `Diagram` tab.
2. Click a transition edge.
3. Click `Remove` in inspector.
4. Confirm browser prompt.

Expected: transition edge disappears after refetch.

## Self-Review

- Spec coverage: transition removal, inline status creation, key generation, API reuse, validation/error surfacing, and verification all have tasks.
- Placeholder scan: no TBD/TODO placeholders remain.
- Type consistency: `PMStatusCategoryApi`, `PMCreateStatusRequest`, `AddStepValues`, and hook names match across tasks.
