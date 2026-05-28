/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM user skill profile dialog
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import {
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
  useGetPmUserSkillsQuery,
  useReplacePmUserSkillsMutation,
} from '../../api';
import type { PMSkillProficiency, PMUserSkillApi } from '../../types/api';
import {
  buildSkillMap,
  getSkillLabel,
  SKILL_PROFICIENCY_OPTIONS,
} from './skill-ui.utils';

interface PMUserSkillDialogProps {
  open: boolean;
  userId?: number | null;
  userName?: string | null;
  onOpenChange: (open: boolean) => void;
}

interface UserSkillRow {
  clientId: string;
  skillId: string;
  proficiency: PMSkillProficiency;
  confidence: string;
}

export function PMUserSkillDialog({
  open,
  userId,
  userName,
  onOpenChange,
}: PMUserSkillDialogProps) {
  const [rows, setRows] = useState<UserSkillRow[]>([]);
  const skillsQuery = useGetPmSkillsQuery();
  const userSkillsQuery = useGetPmUserSkillsQuery(userId ?? 0, {
    skip: !open || !userId,
  });
  const [replaceSkills, replaceState] = useReplacePmUserSkillsMutation();

  const skillsById = useMemo(
    () => buildSkillMap(skillsQuery.data),
    [skillsQuery.data]
  );
  const skills = skillsQuery.data ?? [];

  useEffect(() => {
    if (!open) {
      setRows([]);
      return;
    }
    if (!userSkillsQuery.data) {
      return;
    }
    setRows(userSkillsQuery.data.map(toUserSkillRow));
  }, [open, userSkillsQuery.data]);

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
        proficiency: 'WORKING',
        confidence: '',
      },
    ]);
  };

  const updateRow = (clientId: string, patch: Partial<UserSkillRow>) => {
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
    if (!userId) {
      return;
    }

    const skillIds = rows.map((row) => row.skillId).filter(Boolean);
    if (new Set(skillIds).size !== skillIds.length) {
      toast.error('Each skill can only be selected once.');
      return;
    }

    try {
      await replaceSkills({
        userId,
        body: {
          items: rows
            .filter((row) => row.skillId)
            .map((row) => ({
              skillId: Number(row.skillId),
              proficiency: row.proficiency,
              confidence: row.confidence ? Number(row.confidence) : null,
              source: 'MANUAL',
            })),
        },
      }).unwrap();
      toast.success('User skills updated.');
      onOpenChange(false);
    } catch (error) {
      toast.error('Unable to save user skills', {
        description: getErrorMessage(error),
      });
    }
  };

  const isLoading =
    userSkillsQuery.isLoading || (skillsQuery.isLoading && skills.length === 0);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            Skills for {userName || (userId ? `User #${userId}` : 'user')}
          </DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className='space-y-3'>
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className='h-12 rounded-md' />
            ))}
          </div>
        ) : (
          <div className='space-y-3'>
            <div className='grid gap-2'>
              {rows.map((row) => (
                <div
                  key={row.clientId}
                  className='grid gap-2 rounded-md border p-3 md:grid-cols-[minmax(0,1fr)_160px_120px_40px]'
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
                    <Label>Proficiency</Label>
                    <Select
                      value={row.proficiency}
                      onValueChange={(value) =>
                        updateRow(row.clientId, {
                          proficiency: value as PMSkillProficiency,
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
                    <Label>Confidence</Label>
                    <Input
                      type='number'
                      min='0'
                      max='100'
                      value={row.confidence}
                      onChange={(event) =>
                        updateRow(row.clientId, {
                          confidence: event.target.value,
                        })
                      }
                      placeholder='0-100'
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
                  No skills assigned.
                </div>
              ) : null}
            </div>
            <Button type='button' variant='outline' onClick={addRow}>
              <Plus className='mr-2 h-4 w-4' />
              Add skill
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
            disabled={!userId || replaceState.isLoading}
          >
            {replaceState.isLoading ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Save skills
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function toUserSkillRow(skill: PMUserSkillApi): UserSkillRow {
  return {
    clientId: createClientId(),
    skillId: String(skill.skillId),
    proficiency: skill.proficiency,
    confidence: skill.confidence?.toString() ?? '',
  };
}

function createClientId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}
