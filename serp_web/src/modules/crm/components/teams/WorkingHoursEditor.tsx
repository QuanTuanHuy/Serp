/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project — weekly working hours editor for CRM team members
 */

'use client';

import { useEffect, useRef, useState } from 'react';
import { Input, Label, Switch } from '@/shared/components/ui';
import type { CrmDayOfWeek, WorkingHoursItem } from '../../types';
import {
  CRM_DAYS_ORDER,
  crmDayLabel,
  endMinuteToLabel,
  minutesToLabel,
  parseTimeToMinutes,
} from '../../utils/working-hours-time';

const DEFAULT_START = 9 * 60;
const DEFAULT_END = 17 * 60;

type DayTimeDraft = { start: string; end: string };

function buildDraftsFromValue(
  rows: WorkingHoursItem[]
): Record<CrmDayOfWeek, DayTimeDraft> {
  const out = {} as Record<CrmDayOfWeek, DayTimeDraft>;
  for (const day of CRM_DAYS_ORDER) {
    const row = rows.find((r) => r.dayOfWeek === day);
    if (!row || !row.workingDay) {
      out[day] = { start: '', end: '' };
    } else {
      out[day] = {
        start:
          row.startMinute !== undefined ? minutesToLabel(row.startMinute) : '',
        end: row.endMinute !== undefined ? endMinuteToLabel(row.endMinute) : '',
      };
    }
  }
  return out;
}

interface WorkingHoursEditorProps {
  value: WorkingHoursItem[];
  onChange: (next: WorkingHoursItem[]) => void;
  disabled?: boolean;
}

export function WorkingHoursEditor({
  value,
  onChange,
  disabled,
}: WorkingHoursEditorProps) {
  type HoursPatch = Partial<WorkingHoursItem>;

  const editingDayRef = useRef<CrmDayOfWeek | null>(null);
  const [drafts, setDrafts] = useState<Record<CrmDayOfWeek, DayTimeDraft>>(() =>
    buildDraftsFromValue(value)
  );

  useEffect(() => {
    setDrafts((prev) => {
      const built = buildDraftsFromValue(value);
      const editing = editingDayRef.current;
      if (editing) {
        return { ...built, [editing]: prev[editing] };
      }
      return built;
    });
  }, [value]);

  const updateRow = (dayOfWeek: CrmDayOfWeek, patch: HoursPatch) => {
    const next = value.map((row) =>
      row.dayOfWeek === dayOfWeek ? { ...row, ...patch, dayOfWeek } : row
    );
    onChange(next);
  };

  const handleWorkingDayChange = (args: {
    row: WorkingHoursItem;
    checked: boolean;
  }) => {
    const { row, checked } = args;
    if (checked) {
      updateRow(row.dayOfWeek, {
        workingDay: true,
        startMinute: row.startMinute ?? DEFAULT_START,
        endMinute: row.endMinute ?? DEFAULT_END,
      });
    } else {
      updateRow(row.dayOfWeek, {
        workingDay: false,
        startMinute: undefined,
        endMinute: undefined,
      });
    }
  };

  const commitStart = (day: CrmDayOfWeek, raw: string) => {
    const trimmed = raw.trim();
    if (trimmed === '') {
      updateRow(day, { startMinute: undefined });
      setDrafts((d) => ({
        ...d,
        [day]: { ...d[day], start: '' },
      }));
      return;
    }
    const m = parseTimeToMinutes(trimmed);
    if (m !== null) {
      updateRow(day, { startMinute: m });
      setDrafts((d) => ({
        ...d,
        [day]: { ...d[day], start: minutesToLabel(m) },
      }));
    } else {
      const row = value.find((r) => r.dayOfWeek === day);
      setDrafts((d) => ({
        ...d,
        [day]: {
          ...d[day],
          start:
            row?.startMinute !== undefined
              ? minutesToLabel(row.startMinute)
              : '',
        },
      }));
    }
  };

  const commitEnd = (day: CrmDayOfWeek, raw: string) => {
    const trimmed = raw.trim();
    if (trimmed === '') {
      updateRow(day, { endMinute: undefined });
      setDrafts((d) => ({
        ...d,
        [day]: { ...d[day], end: '' },
      }));
      return;
    }
    const m = parseTimeToMinutes(trimmed, { allowEndOfDay: true });
    if (m !== null) {
      updateRow(day, { endMinute: m });
      setDrafts((d) => ({
        ...d,
        [day]: { ...d[day], end: endMinuteToLabel(m) },
      }));
    } else {
      const row = value.find((r) => r.dayOfWeek === day);
      setDrafts((d) => ({
        ...d,
        [day]: {
          ...d[day],
          end:
            row?.endMinute !== undefined ? endMinuteToLabel(row.endMinute) : '',
        },
      }));
    }
  };

  return (
    <div className='space-y-3 rounded-md border p-3'>
      <p className='text-sm font-medium'>Working hours</p>
      <p className='text-xs text-muted-foreground'>
        Leave all days off to use server defaults. Times use 24h format (end may
        be 24:00).
      </p>
      <div className='space-y-2'>
        {CRM_DAYS_ORDER.map((day) => {
          const row = value.find((r) => r.dayOfWeek === day);
          if (!row) return null;
          const draft = drafts[day] ?? { start: '', end: '' };
          return (
            <div
              key={day}
              className='flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-4'
            >
              <div className='flex w-full min-w-0 items-center justify-between gap-2 sm:w-40'>
                <Label className='text-sm'>{crmDayLabel(day)}</Label>
                <Switch
                  checked={row.workingDay}
                  disabled={disabled}
                  onCheckedChange={(checked) =>
                    handleWorkingDayChange({ row, checked })
                  }
                />
              </div>
              {row.workingDay ? (
                <div className='flex flex-1 flex-wrap items-center gap-2'>
                  <Input
                    type='text'
                    inputMode='numeric'
                    className='w-28'
                    disabled={disabled}
                    placeholder='09:00'
                    aria-label={`${crmDayLabel(day)} start`}
                    value={draft.start}
                    onFocus={() => {
                      editingDayRef.current = day;
                    }}
                    onChange={(e) => {
                      setDrafts((d) => ({
                        ...d,
                        [day]: { ...d[day], start: e.target.value },
                      }));
                    }}
                    onBlur={(e) => {
                      editingDayRef.current = null;
                      commitStart(day, e.target.value);
                    }}
                  />
                  <span className='text-muted-foreground'>–</span>
                  <Input
                    type='text'
                    className='w-28'
                    disabled={disabled}
                    placeholder='17:00'
                    aria-label={`${crmDayLabel(day)} end`}
                    value={draft.end}
                    onFocus={() => {
                      editingDayRef.current = day;
                    }}
                    onChange={(e) => {
                      setDrafts((d) => ({
                        ...d,
                        [day]: { ...d[day], end: e.target.value },
                      }));
                    }}
                    onBlur={(e) => {
                      editingDayRef.current = null;
                      commitEnd(day, e.target.value);
                    }}
                  />
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </div>
  );
}
