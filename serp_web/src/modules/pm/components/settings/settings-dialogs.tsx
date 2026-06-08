/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings dialogs
 */

import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Textarea } from '@/shared/components/ui/textarea';

import type {
  PMCreateIssueTypeRequest,
  PMCreatePriorityRequest,
  PMCreateResolutionRequest,
  PMCreateWorkflowRequest,
  PMWorkflowSettingsApi,
  PMPrioritySettingsApi,
  PMResolutionSettingsApi,
  PMWorkTypeSettingsApi,
} from '../../types/api';
import { PriorityGlyph, WorkTypeGlyph } from './settings-glyphs';
import {
  HIERARCHY_OPTIONS,
  normalizeOptionalText,
  type PriorityDialogState,
  type PrioritySchemeDialogState,
  type ResolutionDialogState,
  type SchemeDialogState,
  type WorkflowDialogState,
  type WorkflowSchemeDialogState,
  type WorkTypeDialogState,
} from './settings-page.types';

export function WorkTypeDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: WorkTypeDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: WorkTypeDialogState['mode'],
    id: number | undefined,
    values: PMCreateIssueTypeRequest
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [typeKey, setTypeKey] = useState('');
  const [description, setDescription] = useState('');
  const [iconUrl, setIconUrl] = useState('');
  const [hierarchyLevel, setHierarchyLevel] = useState('1');

  useEffect(() => {
    if (!state) {
      return;
    }
    setName(state.item?.name ?? '');
    setTypeKey(state.item?.typeKey ?? '');
    setDescription(state.item?.description ?? '');
    setIconUrl(state.item?.iconUrl ?? '');
    setHierarchyLevel(String(state.item?.hierarchyLevel ?? 1));
  }, [state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }

    const cleanName = name.trim();
    const cleanKey = typeKey.trim();
    if (!cleanName || (!cleanKey && state.mode === 'create')) {
      toast.error('Name and key are required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      typeKey: cleanKey,
      description: normalizeOptionalText(description),
      iconUrl: normalizeOptionalText(iconUrl),
      hierarchyLevel: Number(hierarchyLevel),
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit work type' : 'Add work type'}
            </DialogTitle>
            <DialogDescription>
              Configure the label and hierarchy for this work item type.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='work-type-name'>Name</Label>
                <Input
                  id='work-type-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='Story'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='work-type-key'>Key</Label>
                <Input
                  id='work-type-key'
                  value={typeKey}
                  onChange={(event) => setTypeKey(event.target.value)}
                  placeholder='story'
                  disabled={state?.mode === 'edit'}
                />
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='work-type-description'>Description</Label>
              <Textarea
                id='work-type-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='work-type-icon'>Icon URL</Label>
                <Input
                  id='work-type-icon'
                  value={iconUrl}
                  onChange={(event) => setIconUrl(event.target.value)}
                  placeholder='https://...'
                />
              </div>
              <div className='space-y-2'>
                <Label>Hierarchy level</Label>
                <Select
                  value={hierarchyLevel}
                  onValueChange={setHierarchyLevel}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {HIERARCHY_OPTIONS.map((option) => (
                      <SelectItem
                        key={option.value}
                        value={String(option.value)}
                      >
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function SchemeDialog({
  state,
  workTypes,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: SchemeDialogState | null;
  workTypes: PMWorkTypeSettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: SchemeDialogState['mode'],
    id: number | undefined,
    values: {
      name: string;
      description: string | null;
      defaultIssueTypeId: number;
      issueTypeIds: number[];
    }
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [defaultIssueTypeId, setDefaultIssueTypeId] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    if (!state) {
      return;
    }
    const initialIds =
      state.mode === 'edit'
        ? state.item.workTypes.map((workType) => workType.id)
        : workTypes.slice(0, 1).map((workType) => workType.id);
    const initialDefault =
      state.mode === 'edit'
        ? state.item.defaultIssueTypeId
        : (initialIds[0] ?? workTypes[0]?.id);

    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setSelectedIds(initialIds);
    setDefaultIssueTypeId(initialDefault ? String(initialDefault) : '');
  }, [state, workTypes]);

  const toggleWorkType = (id: number, checked: boolean) => {
    setSelectedIds((current) => {
      if (checked) {
        return current.includes(id) ? current : [...current, id];
      }
      return current.filter((currentId) => currentId !== id);
    });
    if (!checked && defaultIssueTypeId === String(id)) {
      setDefaultIssueTypeId('');
    }
  };

  const handleDefaultChange = (value: string) => {
    const id = Number(value);
    setDefaultIssueTypeId(value);
    setSelectedIds((current) =>
      current.includes(id) ? current : [...current, id]
    );
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    const defaultId = Number(defaultIssueTypeId);
    if (!cleanName || !Number.isFinite(defaultId) || selectedIds.length === 0) {
      toast.error(
        'Name, default work type, and at least one option are required.'
      );
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      defaultIssueTypeId: defaultId,
      issueTypeIds: selectedIds,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit'
                ? 'Edit work type scheme'
                : 'Add work type scheme'}
            </DialogTitle>
            <DialogDescription>
              Select the work types available to projects using this scheme.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='scheme-name'>Name</Label>
                <Input
                  id='scheme-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label>Default work type</Label>
                <Select
                  value={defaultIssueTypeId}
                  onValueChange={handleDefaultChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select default' />
                  </SelectTrigger>
                  <SelectContent>
                    {workTypes.map((workType) => (
                      <SelectItem key={workType.id} value={String(workType.id)}>
                        {workType.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='scheme-description'>Description</Label>
              <Textarea
                id='scheme-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='space-y-2'>
              <Label>Options</Label>
              <ScrollArea className='h-56 rounded-md border'>
                <div className='divide-y'>
                  {workTypes.map((workType) => {
                    const checked = selectedIds.includes(workType.id);
                    return (
                      <label
                        key={workType.id}
                        className='flex cursor-pointer items-start gap-3 px-3 py-2'
                      >
                        <Checkbox
                          checked={checked}
                          onCheckedChange={(value) =>
                            toggleWorkType(workType.id, value === true)
                          }
                        />
                        <span className='flex min-w-0 items-start gap-2'>
                          <WorkTypeGlyph workType={workType} />
                          <span className='min-w-0'>
                            <span className='block text-sm font-medium'>
                              {workType.name}
                            </span>
                            <span className='block text-xs text-muted-foreground'>
                              {workType.description || workType.typeKey}
                            </span>
                          </span>
                        </span>
                      </label>
                    );
                  })}
                </div>
              </ScrollArea>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              disabled={isSubmitting || workTypes.length === 0}
            >
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function PriorityDialog({
  state,
  priorities,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: PriorityDialogState | null;
  priorities: PMPrioritySettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: PriorityDialogState['mode'],
    id: number | undefined,
    values: PMCreatePriorityRequest
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [iconUrl, setIconUrl] = useState('');
  const [color, setColor] = useState('#e34935');
  const [sequence, setSequence] = useState('1');

  useEffect(() => {
    if (!state) {
      return;
    }
    const nextSequence =
      priorities.reduce(
        (max, priority) => Math.max(max, priority.sequence ?? 0),
        0
      ) + 1;
    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setIconUrl(state.item?.iconUrl ?? '');
    setColor(state.item?.color ?? '#e34935');
    setSequence(String(state.item?.sequence ?? nextSequence));
  }, [priorities, state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }

    const cleanName = name.trim();
    const parsedSequence = Number(sequence);
    if (!cleanName || !Number.isInteger(parsedSequence) || parsedSequence < 0) {
      toast.error('Name and a non-negative order are required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      iconUrl: normalizeOptionalText(iconUrl),
      color: normalizeOptionalText(color),
      sequence: parsedSequence,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit priority' : 'Add priority'}
            </DialogTitle>
            <DialogDescription>
              Configure the priority label, order, and visual color.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='priority-name'>Name</Label>
                <Input
                  id='priority-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='High'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='priority-sequence'>Order</Label>
                <Input
                  id='priority-sequence'
                  type='number'
                  min={0}
                  value={sequence}
                  onChange={(event) => setSequence(event.target.value)}
                />
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='priority-description'>Description</Label>
              <Textarea
                id='priority-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='grid gap-4 sm:grid-cols-[1fr_140px]'>
              <div className='space-y-2'>
                <Label htmlFor='priority-icon'>Icon URL</Label>
                <Input
                  id='priority-icon'
                  value={iconUrl}
                  onChange={(event) => setIconUrl(event.target.value)}
                  placeholder='https://...'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='priority-color'>Color</Label>
                <Input
                  id='priority-color'
                  type='color'
                  value={color}
                  onChange={(event) => setColor(event.target.value)}
                  className='h-10 px-2'
                />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function ResolutionDialog({
  state,
  resolutions,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: ResolutionDialogState | null;
  resolutions: PMResolutionSettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: ResolutionDialogState['mode'],
    id: number | undefined,
    values: PMCreateResolutionRequest
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [sequence, setSequence] = useState('1');

  useEffect(() => {
    if (!state) {
      return;
    }
    const nextSequence =
      resolutions.reduce(
        (max, resolution) => Math.max(max, resolution.sequence ?? 0),
        0
      ) + 1;
    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setSequence(String(state.item?.sequence ?? nextSequence));
  }, [resolutions, state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }

    const cleanName = name.trim();
    const parsedSequence = Number(sequence);
    if (!cleanName || !Number.isInteger(parsedSequence) || parsedSequence < 0) {
      toast.error('Name and a non-negative order are required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      sequence: parsedSequence,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit resolution' : 'Add resolution'}
            </DialogTitle>
            <DialogDescription>
              Configure the outcome label used when work items are closed.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-[1fr_120px]'>
              <div className='space-y-2'>
                <Label htmlFor='resolution-name'>Name</Label>
                <Input
                  id='resolution-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='Done'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='resolution-sequence'>Order</Label>
                <Input
                  id='resolution-sequence'
                  type='number'
                  min={0}
                  value={sequence}
                  onChange={(event) => setSequence(event.target.value)}
                />
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='resolution-description'>Description</Label>
              <Textarea
                id='resolution-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function PrioritySchemeDialog({
  state,
  priorities,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: PrioritySchemeDialogState | null;
  priorities: PMPrioritySettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: PrioritySchemeDialogState['mode'],
    id: number | undefined,
    values: {
      name: string;
      description: string | null;
      defaultPriorityId: number;
      priorityIds: number[];
    }
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [defaultPriorityId, setDefaultPriorityId] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    if (!state) {
      return;
    }
    const initialIds =
      state.mode === 'edit'
        ? state.item.priorities.map((priority) => priority.id)
        : priorities.map((priority) => priority.id);
    const initialDefault =
      state.mode === 'edit'
        ? state.item.defaultPriorityId
        : (initialIds[0] ?? priorities[0]?.id);

    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setSelectedIds(initialIds);
    setDefaultPriorityId(initialDefault ? String(initialDefault) : '');
  }, [priorities, state]);

  const togglePriority = (id: number, checked: boolean) => {
    setSelectedIds((current) => {
      if (checked) {
        return current.includes(id) ? current : [...current, id];
      }
      return current.filter((currentId) => currentId !== id);
    });
    if (!checked && defaultPriorityId === String(id)) {
      setDefaultPriorityId('');
    }
  };

  const handleDefaultChange = (value: string) => {
    const id = Number(value);
    setDefaultPriorityId(value);
    setSelectedIds((current) =>
      current.includes(id) ? current : [...current, id]
    );
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    const defaultId = Number(defaultPriorityId);
    if (!cleanName || !Number.isFinite(defaultId) || selectedIds.length === 0) {
      toast.error(
        'Name, default priority, and at least one priority are required.'
      );
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      defaultPriorityId: defaultId,
      priorityIds: selectedIds,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit'
                ? 'Edit priority scheme'
                : 'Add priority scheme'}
            </DialogTitle>
            <DialogDescription>
              Select the priorities available to projects using this scheme.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='priority-scheme-name'>Name</Label>
                <Input
                  id='priority-scheme-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label>Default priority</Label>
                <Select
                  value={defaultPriorityId}
                  onValueChange={handleDefaultChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select default' />
                  </SelectTrigger>
                  <SelectContent>
                    {priorities.map((priority) => (
                      <SelectItem key={priority.id} value={String(priority.id)}>
                        {priority.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='priority-scheme-description'>Description</Label>
              <Textarea
                id='priority-scheme-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='space-y-2'>
              <Label>Priorities</Label>
              <ScrollArea className='h-56 rounded-md border'>
                <div className='divide-y'>
                  {priorities.map((priority) => {
                    const checked = selectedIds.includes(priority.id);
                    return (
                      <label
                        key={priority.id}
                        className='flex cursor-pointer items-start gap-3 px-3 py-2'
                      >
                        <Checkbox
                          checked={checked}
                          onCheckedChange={(value) =>
                            togglePriority(priority.id, value === true)
                          }
                        />
                        <span className='flex min-w-0 items-start gap-2'>
                          <PriorityGlyph priority={priority} />
                          <span className='min-w-0'>
                            <span className='block text-sm font-medium'>
                              {priority.name}
                            </span>
                            <span className='block text-xs text-muted-foreground'>
                              {priority.description || priority.priorityKey}
                            </span>
                          </span>
                        </span>
                      </label>
                    );
                  })}
                </div>
              </ScrollArea>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              disabled={isSubmitting || priorities.length === 0}
            >
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function WorkflowDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: WorkflowDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: WorkflowDialogState['mode'],
    id: number | undefined,
    values: PMCreateWorkflowRequest
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (!state) {
      return;
    }
    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
  }, [state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    if (!cleanName) {
      toast.error('Workflow name is required.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit workflow' : 'Add workflow'}
            </DialogTitle>
            <DialogDescription>
              Configure the workflow label shown in PM settings and schemes.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='workflow-name'>Name</Label>
              <Input
                id='workflow-name'
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder='Software Simplified Workflow'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='workflow-description'>Description</Label>
              <Textarea
                id='workflow-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function WorkflowSchemeDialog({
  state,
  workTypes,
  workflows,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: WorkflowSchemeDialogState | null;
  workTypes: PMWorkTypeSettingsApi[];
  workflows: PMWorkflowSettingsApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: WorkflowSchemeDialogState['mode'],
    id: number | undefined,
    values: {
      name: string;
      description: string | null;
      defaultWorkflowId: number;
      workflowByWorkTypeId: Record<number, number | undefined>;
    }
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [defaultWorkflowId, setDefaultWorkflowId] = useState('');
  const [workflowByWorkTypeId, setWorkflowByWorkTypeId] = useState<
    Record<number, number | undefined>
  >({});

  useEffect(() => {
    if (!state) {
      return;
    }

    const initialDefault =
      state.mode === 'edit'
        ? state.item.defaultWorkflowId
        : (workflows.find((workflow) => workflow.lifecycleState === 'ACTIVE')
            ?.id ?? workflows[0]?.id);
    const initialMappings =
      state.mode === 'edit'
        ? Object.fromEntries(
            state.item.items.map((item) => [item.issueTypeId, item.workflowId])
          )
        : Object.fromEntries(
            workTypes.map((workType) => [workType.id, initialDefault])
          );

    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setDefaultWorkflowId(initialDefault ? String(initialDefault) : '');
    setWorkflowByWorkTypeId(initialMappings);
  }, [state, workflows, workTypes]);

  const handleMappingChange = (issueTypeId: number, workflowId: string) => {
    setWorkflowByWorkTypeId((current) => ({
      ...current,
      [issueTypeId]: Number(workflowId),
    }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }
    const cleanName = name.trim();
    const defaultId = Number(defaultWorkflowId);
    const mappedCount = Object.values(workflowByWorkTypeId).filter(
      (workflowId): workflowId is number => typeof workflowId === 'number'
    ).length;

    if (!cleanName || !Number.isFinite(defaultId) || mappedCount === 0) {
      toast.error(
        'Name, default workflow, and at least one work type mapping are required.'
      );
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      name: cleanName,
      description: normalizeOptionalText(description),
      defaultWorkflowId: defaultId,
      workflowByWorkTypeId,
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit'
                ? 'Edit workflow scheme'
                : 'Add workflow scheme'}
            </DialogTitle>
            <DialogDescription>
              Choose the default workflow and map work types to workflows.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='workflow-scheme-name'>Name</Label>
                <Input
                  id='workflow-scheme-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label>Default workflow</Label>
                <Select
                  value={defaultWorkflowId}
                  onValueChange={setDefaultWorkflowId}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select default' />
                  </SelectTrigger>
                  <SelectContent>
                    {workflows.map((workflow) => (
                      <SelectItem key={workflow.id} value={String(workflow.id)}>
                        {workflow.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='workflow-scheme-description'>Description</Label>
              <Textarea
                id='workflow-scheme-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <div className='space-y-2'>
              <Label>Work type mappings</Label>
              <ScrollArea className='h-72 rounded-md border'>
                <div className='divide-y'>
                  {workTypes.map((workType) => (
                    <div
                      key={workType.id}
                      className='grid gap-3 px-3 py-3 sm:grid-cols-[minmax(0,1fr)_minmax(220px,280px)] sm:items-center'
                    >
                      <div className='flex min-w-0 items-start gap-2'>
                        <WorkTypeGlyph workType={workType} />
                        <span className='min-w-0'>
                          <span className='block text-sm font-medium'>
                            {workType.name}
                          </span>
                          <span className='block text-xs text-muted-foreground'>
                            {workType.description || workType.typeKey}
                          </span>
                        </span>
                      </div>
                      <Select
                        value={
                          workflowByWorkTypeId[workType.id]
                            ? String(workflowByWorkTypeId[workType.id])
                            : ''
                        }
                        onValueChange={(value) =>
                          handleMappingChange(workType.id, value)
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder='Select workflow' />
                        </SelectTrigger>
                        <SelectContent>
                          {workflows.map((workflow) => (
                            <SelectItem
                              key={workflow.id}
                              value={String(workflow.id)}
                            >
                              {workflow.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              disabled={
                isSubmitting || workTypes.length === 0 || workflows.length === 0
              }
            >
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
