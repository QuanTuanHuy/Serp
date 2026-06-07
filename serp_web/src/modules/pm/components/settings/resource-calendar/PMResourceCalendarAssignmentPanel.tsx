/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar assignments
 */

import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';

import type {
  PMReplaceResourceCalendarAssignmentsRequest,
  PMResourceCalendarAssignmentApi,
  PMResourceCalendarProfileApi,
} from '../../../types/api';

type AssignmentDraft = {
  key: string;
  userId: string;
  effectiveFrom: string;
  effectiveTo: string;
};

export function PMResourceCalendarAssignmentPanel({
  profiles,
  assignments,
  selectedProfileId,
  isSubmitting,
  onProfileChange,
  onSubmit,
}: {
  profiles: PMResourceCalendarProfileApi[];
  assignments: PMResourceCalendarAssignmentApi[];
  selectedProfileId: number | null;
  isSubmitting: boolean;
  onProfileChange: (profileId: number | null) => void;
  onSubmit: (
    profileId: number,
    body: PMReplaceResourceCalendarAssignmentsRequest
  ) => Promise<void>;
}) {
  const [rows, setRows] = useState<AssignmentDraft[]>([]);
  const selectedProfile = useMemo(
    () =>
      profiles.find((profile) => profile.id === selectedProfileId) ??
      profiles[0],
    [profiles, selectedProfileId]
  );

  useEffect(() => {
    if (!selectedProfileId && profiles[0]) {
      onProfileChange(profiles[0].id);
    }
  }, [onProfileChange, profiles, selectedProfileId]);

  useEffect(() => {
    if (!selectedProfile) {
      setRows([]);
      return;
    }

    setRows(
      assignments
        .filter((assignment) => assignment.profileId === selectedProfile.id)
        .map((assignment) => ({
          key: String(assignment.id),
          userId: String(assignment.userId),
          effectiveFrom: assignment.effectiveFrom,
          effectiveTo: assignment.effectiveTo ?? '',
        }))
    );
  }, [assignments, selectedProfile]);

  const updateRow = (
    key: string,
    field: keyof Omit<AssignmentDraft, 'key'>,
    value: string
  ) => {
    setRows((current) =>
      current.map((row) => (row.key === key ? { ...row, [field]: value } : row))
    );
  };

  const addRow = () => {
    setRows((current) => [
      ...current,
      {
        key: crypto.randomUUID(),
        userId: '',
        effectiveFrom: new Date().toISOString().slice(0, 10),
        effectiveTo: '',
      },
    ]);
  };

  const removeRow = (key: string) => {
    setRows((current) => current.filter((row) => row.key !== key));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedProfile) {
      return;
    }

    const parsedRows = rows.map((row) => ({
      userId: Number(row.userId),
      effectiveFrom: row.effectiveFrom,
      effectiveTo: row.effectiveTo.trim() || null,
    }));
    const invalidRow = parsedRows.some(
      (row) =>
        !Number.isInteger(row.userId) ||
        row.userId <= 0 ||
        !row.effectiveFrom ||
        (row.effectiveTo !== null && row.effectiveFrom > row.effectiveTo)
    );
    if (invalidRow) {
      toast.error('Assignments need valid user IDs and date ranges.');
      return;
    }

    await onSubmit(selectedProfile.id, { assignments: parsedRows });
  };

  return (
    <Card className='border-border/60 bg-background/90 shadow-sm'>
      <CardHeader className='border-b py-4'>
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <CardTitle className='text-sm'>Assignments</CardTitle>
          <div className='w-full md:w-72'>
            <Select
              value={selectedProfile ? String(selectedProfile.id) : ''}
              onValueChange={(value) => onProfileChange(Number(value))}
            >
              <SelectTrigger>
                <SelectValue placeholder='Select profile' />
              </SelectTrigger>
              <SelectContent>
                {profiles.map((profile) => (
                  <SelectItem key={profile.id} value={String(profile.id)}>
                    {profile.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </CardHeader>
      <CardContent className='p-4'>
        {!selectedProfile ? (
          <div className='text-sm text-muted-foreground'>
            Create a profile before assigning users.
          </div>
        ) : (
          <form onSubmit={handleSubmit} className='space-y-3'>
            <div className='space-y-2'>
              {rows.map((row) => (
                <div
                  key={row.key}
                  className='grid gap-2 rounded-md border p-3 md:grid-cols-[120px_1fr_1fr_40px] md:items-end'
                >
                  <div className='space-y-2'>
                    <Label>User ID</Label>
                    <Input
                      type='number'
                      min={1}
                      value={row.userId}
                      onChange={(event) =>
                        updateRow(row.key, 'userId', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label>Effective from</Label>
                    <Input
                      type='date'
                      value={row.effectiveFrom}
                      onChange={(event) =>
                        updateRow(row.key, 'effectiveFrom', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label>Effective to</Label>
                    <Input
                      type='date'
                      value={row.effectiveTo}
                      onChange={(event) =>
                        updateRow(row.key, 'effectiveTo', event.target.value)
                      }
                    />
                  </div>
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    aria-label='Remove assignment'
                    onClick={() => removeRow(row.key)}
                  >
                    <Trash2 className='h-4 w-4 text-destructive' />
                  </Button>
                </div>
              ))}
            </div>
            <div className='flex flex-wrap gap-2'>
              <Button type='button' variant='outline' onClick={addRow}>
                <Plus className='mr-2 h-4 w-4' />
                Add user
              </Button>
              <Button type='submit' disabled={isSubmitting}>
                {isSubmitting ? 'Saving...' : 'Save assignments'}
              </Button>
            </div>
          </form>
        )}
      </CardContent>
    </Card>
  );
}
