/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item skill requirement panel
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { ChevronDown, ChevronRight, Loader2, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Skeleton,
} from '@/shared/components/ui';

import {
  useGetPmSkillsQuery,
  useGetPmWorkItemSkillsQuery,
  useReplacePmWorkItemSkillsMutation,
} from '../../api';
import type {
  PMSkillProficiency,
  PMSkillRequirementType,
  PMWorkItemSkillApi,
} from '../../types/api';
import {
  buildSkillMap,
  getProficiencyLabel,
  getRequirementLabel,
  getSkillLabel,
  SKILL_PROFICIENCY_OPTIONS,
  SKILL_REQUIREMENT_OPTIONS,
} from './skill-ui.utils';

interface PMWorkItemSkillPanelProps {
  projectId: number;
  workItemId?: number;
}

interface WorkItemSkillRow {
  clientId: string;
  skillId: string;
  requirementType: PMSkillRequirementType;
  minProficiency: PMSkillProficiency;
  weight: string;
}

export function PMWorkItemSkillPanel({
  projectId,
  workItemId,
}: PMWorkItemSkillPanelProps) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [panelOpen, setPanelOpen] = useState(true);
  const skillsQuery = useGetPmSkillsQuery();
  const workItemSkillsQuery = useGetPmWorkItemSkillsQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !workItemId }
  );
  const skillsById = useMemo(
    () => buildSkillMap(skillsQuery.data),
    [skillsQuery.data]
  );
  const requirements = workItemSkillsQuery.data ?? [];

  return (
    <section className='rounded-lg border bg-background'>
      <div className='flex items-center justify-between gap-3 border-b px-4 py-3'>
        <button
          type='button'
          className='flex min-w-0 items-center gap-2 text-left font-semibold'
          aria-expanded={panelOpen}
          onClick={() => setPanelOpen((current) => !current)}
        >
          {panelOpen ? (
            <ChevronDown className='h-4 w-4 shrink-0 text-muted-foreground' />
          ) : (
            <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
          )}
          <span>Skills</span>
        </button>
        <Button
          type='button'
          variant='ghost'
          size='sm'
          disabled={!workItemId}
          onClick={() => setDialogOpen(true)}
        >
          Edit
        </Button>
      </div>
      {panelOpen ? (
        <div className='space-y-2 p-4'>
          {workItemSkillsQuery.isLoading ? (
            <>
              <Skeleton className='h-10 rounded-md' />
              <Skeleton className='h-10 rounded-md' />
            </>
          ) : requirements.length ? (
            requirements.map((item) => (
              <div key={item.id} className='rounded-md border px-3 py-2'>
                <div className='flex flex-wrap items-center gap-2'>
                  <span className='text-sm font-medium'>
                    {getSkillLabel(item.skillId, skillsById)}
                  </span>
                  <Badge variant='secondary'>
                    {getRequirementLabel(item.requirementType)}
                  </Badge>
                </div>
                <div className='mt-1 text-xs text-muted-foreground'>
                  Min {getProficiencyLabel(item.minProficiency)} - Weight{' '}
                  {item.weight}
                </div>
              </div>
            ))
          ) : (
            <div className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
              No skill requirements.
            </div>
          )}
        </div>
      ) : null}
      <PMWorkItemSkillDialog
        open={dialogOpen}
        projectId={projectId}
        workItemId={workItemId}
        skills={skillsQuery.data ?? []}
        existingSkills={requirements}
        isInitialLoading={
          workItemSkillsQuery.isLoading || skillsQuery.isLoading
        }
        onOpenChange={setDialogOpen}
      />
    </section>
  );
}

function PMWorkItemSkillDialog({
  open,
  projectId,
  workItemId,
  skills,
  existingSkills,
  isInitialLoading,
  onOpenChange,
}: {
  open: boolean;
  projectId: number;
  workItemId?: number;
  skills: Array<{ id: number; code: string; name: string }>;
  existingSkills: PMWorkItemSkillApi[];
  isInitialLoading: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const [rows, setRows] = useState<WorkItemSkillRow[]>([]);
  const [replaceSkills, replaceState] = useReplacePmWorkItemSkillsMutation();
  const skillsById = useMemo(() => buildSkillMap(skills), [skills]);

  useEffect(() => {
    if (!open) {
      setRows([]);
      return;
    }
    setRows(existingSkills.map(toWorkItemSkillRow));
  }, [existingSkills, open]);

  const addRow = () => {
    const selected = new Set(rows.map((row) => row.skillId));
    const firstAvailable = skills.find(
      (skill) => !selected.has(String(skill.id))
    );
    if (!firstAvailable) {
      toast.error('No available skills to add.');
      return;
    }
    setRows((current) => [
      ...current,
      {
        clientId: createClientId(),
        skillId: String(firstAvailable.id),
        requirementType: 'REQUIRED',
        minProficiency: 'WORKING',
        weight: '1',
      },
    ]);
  };

  const updateRow = (clientId: string, patch: Partial<WorkItemSkillRow>) => {
    setRows((current) =>
      current.map((row) =>
        row.clientId === clientId ? { ...row, ...patch } : row
      )
    );
  };

  const removeRow = (clientId: string) => {
    setRows((current) => current.filter((row) => row.clientId !== clientId));
  };

  const handleSave = async () => {
    if (!workItemId) {
      return;
    }

    const skillIds = rows.map((row) => row.skillId).filter(Boolean);
    if (new Set(skillIds).size !== skillIds.length) {
      toast.error('Each skill can only be selected once.');
      return;
    }

    try {
      await replaceSkills({
        projectId,
        workItemId,
        body: {
          items: rows
            .filter((row) => row.skillId)
            .map((row) => ({
              skillId: Number(row.skillId),
              requirementType: row.requirementType,
              minProficiency: row.minProficiency,
              weight: Math.max(1, Math.min(100, Number(row.weight) || 1)),
              source: 'MANUAL',
            })),
        },
      }).unwrap();
      toast.success('Work item skill requirements updated.');
      onOpenChange(false);
    } catch (error) {
      toast.error('Unable to save skill requirements', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Skill requirements</DialogTitle>
        </DialogHeader>
        {isInitialLoading ? (
          <div className='space-y-3'>
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className='h-12 rounded-md' />
            ))}
          </div>
        ) : (
          <div className='space-y-3'>
            {rows.map((row) => (
              <div
                key={row.clientId}
                className='grid gap-2 rounded-md border p-3 md:grid-cols-[minmax(0,1fr)_140px_150px_96px_40px]'
              >
                <label className='space-y-1'>
                  <Label>Skill</Label>
                  <Select
                    value={row.skillId}
                    onValueChange={(value) =>
                      updateRow(row.clientId, { skillId: value })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select skill' />
                    </SelectTrigger>
                    <SelectContent>
                      {skills.map((skill) => (
                        <SelectItem key={skill.id} value={String(skill.id)}>
                          {getSkillLabel(skill.id, skillsById)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </label>
                <label className='space-y-1'>
                  <Label>Type</Label>
                  <Select
                    value={row.requirementType}
                    onValueChange={(value) =>
                      updateRow(row.clientId, {
                        requirementType: value as PMSkillRequirementType,
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {SKILL_REQUIREMENT_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </label>
                <label className='space-y-1'>
                  <Label>Min level</Label>
                  <Select
                    value={row.minProficiency}
                    onValueChange={(value) =>
                      updateRow(row.clientId, {
                        minProficiency: value as PMSkillProficiency,
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {SKILL_PROFICIENCY_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </label>
                <label className='space-y-1'>
                  <Label>Weight</Label>
                  <Input
                    type='number'
                    min='1'
                    max='100'
                    value={row.weight}
                    onChange={(event) =>
                      updateRow(row.clientId, { weight: event.target.value })
                    }
                  />
                </label>
                <div className='flex items-end'>
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    onClick={() => removeRow(row.clientId)}
                  >
                    <Trash2 className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            ))}
            {rows.length === 0 ? (
              <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                No skill requirements.
              </div>
            ) : null}
            <Button type='button' variant='outline' onClick={addRow}>
              <Plus className='mr-2 h-4 w-4' />
              Add requirement
            </Button>
          </div>
        )}
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
            onClick={handleSave}
            disabled={!workItemId || replaceState.isLoading}
          >
            {replaceState.isLoading ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Save requirements
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function toWorkItemSkillRow(skill: PMWorkItemSkillApi): WorkItemSkillRow {
  return {
    clientId: createClientId(),
    skillId: String(skill.skillId),
    requirementType: skill.requirementType,
    minProficiency: skill.minProficiency,
    weight: String(skill.weight),
  };
}

function createClientId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}
