/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar profile dialog
 */

import { FormEvent, useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { cn } from '@/shared/utils';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Textarea } from '@/shared/components/ui/textarea';

import type {
  PMCreateResourceCalendarProfileRequest,
  PMReplaceResourceCalendarBlocksRequest,
  PMResourceCalendarProfileApi,
} from '../../../types/api';
import { normalizeOptionalText } from '../settings-page.types';

const DAY_OPTIONS = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' },
];

const TIMEZONE_OPTIONS = [
  'Asia/Ho_Chi_Minh',
  'UTC',
  'Asia/Singapore',
  'Asia/Bangkok',
  'Asia/Tokyo',
  'Europe/London',
  'America/New_York',
];

type BlockDraft = {
  key: string;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  capacityFactor: string;
};

export type ResourceCalendarProfileDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMResourceCalendarProfileApi };

export function PMResourceCalendarProfileDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: ResourceCalendarProfileDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: ResourceCalendarProfileDialogState['mode'],
    id: number | undefined,
    profile: PMCreateResourceCalendarProfileRequest,
    blocks: PMReplaceResourceCalendarBlocksRequest
  ) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [timezone, setTimezone] = useState('Asia/Ho_Chi_Minh');
  const [isDefault, setIsDefault] = useState(false);
  const [blocks, setBlocks] = useState<BlockDraft[]>([]);

  useEffect(() => {
    if (!state) {
      return;
    }

    setName(state.item?.name ?? '');
    setDescription(state.item?.description ?? '');
    setTimezone(state.item?.timezone ?? 'Asia/Ho_Chi_Minh');
    setIsDefault(state.item?.isDefault ?? false);
    setBlocks(
      state.mode === 'edit'
        ? [...state.item.blocks]
            .sort((a, b) => a.dayOfWeek - b.dayOfWeek)
            .map((block) => ({
                key: String(block.id),
                dayOfWeek: block.dayOfWeek,
                startTime: block.startTime,
                endTime: block.endTime,
                capacityFactor: String(block.capacityFactor),
              }))
        : defaultBlocks()
    );
  }, [state]);

  const updateBlock = (
    key: string,
    field: keyof Omit<BlockDraft, 'key'>,
    value: string | number
  ) => {
    setBlocks((current) =>
      current.map((block) =>
        block.key === key ? { ...block, [field]: value } : block
      )
    );
  };

  const addBlock = () => {
    setBlocks((current) => [
      ...current,
      {
        key: crypto.randomUUID(),
        dayOfWeek: 1,
        startTime: '09:00',
        endTime: '17:00',
        capacityFactor: '1',
      },
    ]);
  };

  const removeBlock = (key: string) => {
    setBlocks((current) => current.filter((block) => block.key !== key));
  };

  const handleCopyMondayToWeekdays = () => {
    const mondayBlocks = blocks.filter((b) => b.dayOfWeek === 1);
    if (mondayBlocks.length === 0) {
      toast.error("Please configure Monday's hours first.");
      return;
    }

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

    setBlocks((current) => {
      const nonWeekdayBlocks = current.filter(
        (b) => b.dayOfWeek === 1 || b.dayOfWeek === 6 || b.dayOfWeek === 7
      );
      return [...nonWeekdayBlocks, ...duplicatedBlocks].sort(
        (a, b) => a.dayOfWeek - b.dayOfWeek
      );
    });
    toast.success("Copied Monday's schedule to weekdays (Tue-Fri).");
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }

    const cleanName = name.trim();
    if (!cleanName) {
      toast.error('Profile name is required.');
      return;
    }

    const parsedBlocks = blocks.map((block) => ({
      dayOfWeek: block.dayOfWeek,
      startTime: block.startTime,
      endTime: block.endTime,
      capacityFactor: Number(block.capacityFactor),
    }));
    const invalidBlock = parsedBlocks.some(
      (block) =>
        !block.startTime ||
        !block.endTime ||
        block.startTime >= block.endTime ||
        !Number.isFinite(block.capacityFactor) ||
        block.capacityFactor <= 0 ||
        block.capacityFactor > 1
    );
    if (parsedBlocks.length === 0 || invalidBlock) {
      toast.error('Each weekly block needs a valid time range and factor.');
      return;
    }

    await onSubmit(
      state.mode,
      state.item?.id,
      {
        name: cleanName,
        description: normalizeOptionalText(description),
        timezone,
        isDefault,
      },
      { blocks: parsedBlocks }
    );
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit'
                ? 'Edit resource calendar'
                : 'Add resource calendar'}
            </DialogTitle>
            <DialogDescription>
              Configure reusable weekly capacity for PM optimization.
            </DialogDescription>
          </DialogHeader>

          <div className='max-h-[60vh] overflow-y-auto pr-2.5 space-y-4 scrollbar-thin'>
            <div className='grid gap-4'>
            <div className='grid gap-4 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='resource-calendar-name'>Name</Label>
                <Input
                  id='resource-calendar-name'
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='VN Full-time'
                />
              </div>
              <div className='space-y-2'>
                <Label>Timezone</Label>
                <Select value={timezone} onValueChange={setTimezone}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TIMEZONE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={option}>
                        {option}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='resource-calendar-description'>Description</Label>
              <Textarea
                id='resource-calendar-description'
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />
            </div>
            <label className='flex items-center gap-2 rounded-md border px-3 py-2 text-sm'>
              <Checkbox
                checked={isDefault}
                onCheckedChange={(value) => setIsDefault(value === true)}
              />
              Use as the default resource calendar
            </label>
          </div>

          <div className='space-y-3 border-t pt-4'>
            <div className='flex items-center justify-between gap-3'>
              <div className='space-y-0.5'>
                <Label className='text-base font-semibold'>Weekly blocks</Label>
                <p className='text-xs text-muted-foreground'>Configure working blocks per day of the week.</p>
              </div>
              <div className='flex gap-2'>
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  onClick={handleCopyMondayToWeekdays}
                >
                  Copy Monday to Weekdays (Tue-Fri)
                </Button>
                <Button type='button' variant='outline' size='sm' onClick={addBlock}>
                  <Plus className='mr-2 h-4 w-4' />
                  Add block
                </Button>
              </div>
            </div>

            {/* Visual 7-day grid */}
            <div className='overflow-x-auto pb-1 scrollbar-none'>
              <div className='grid grid-cols-7 gap-1.5 border rounded-md p-3 bg-muted/40 min-w-[500px]'>
                {[1, 2, 3, 4, 5, 6, 7].map((day) => {
                  const dayBlocks = blocks.filter(b => b.dayOfWeek === day);
                  const dayName = DAY_OPTIONS.find(d => d.value === day)?.label.substring(0, 3) ?? '';
                  return (
                    <div
                      key={day}
                      className={cn(
                        'flex flex-col items-center justify-center p-1.5 rounded-md border text-center text-xs transition-colors',
                        dayBlocks.length > 0
                          ? 'bg-primary/5 border-primary/20 text-primary font-medium'
                          : 'bg-muted border-border text-muted-foreground'
                      )}
                    >
                      <span className='font-semibold'>{dayName}</span>
                      {dayBlocks.length > 0 ? (
                        dayBlocks.map((b, i) => (
                          <span key={i} className='text-[10px] mt-1 block opacity-90'>
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
            </div>
            <div className='space-y-2'>
              {blocks.map((block) => (
                <div
                  key={block.key}
                  className='grid gap-2 rounded-md border p-3 md:grid-cols-[minmax(150px,1fr)_120px_120px_130px_40px] md:items-end'
                >
                  <div className='space-y-2'>
                    <Label>Day</Label>
                    <Select
                      value={String(block.dayOfWeek)}
                      onValueChange={(value) =>
                        updateBlock(block.key, 'dayOfWeek', Number(value))
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {DAY_OPTIONS.map((option) => (
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
                  <div className='space-y-2'>
                    <Label>Start</Label>
                    <Input
                      type='time'
                      value={block.startTime}
                      onChange={(event) =>
                        updateBlock(block.key, 'startTime', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label>End</Label>
                    <Input
                      type='time'
                      value={block.endTime}
                      onChange={(event) =>
                        updateBlock(block.key, 'endTime', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label>Factor</Label>
                    <Input
                      type='number'
                      min='0.01'
                      max='1'
                      step='0.01'
                      value={block.capacityFactor}
                      onChange={(event) =>
                        updateBlock(
                          block.key,
                          'capacityFactor',
                          event.target.value
                        )
                      }
                    />
                  </div>
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    aria-label='Remove block'
                    onClick={() => removeBlock(block.key)}
                  >
                    <Trash2 className='h-4 w-4 text-destructive' />
                  </Button>
                </div>
              ))}
            </div>
          </div>
          </div>

          <DialogFooter className='border-t pt-3'>
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

function defaultBlocks(): BlockDraft[] {
  return [1, 2, 3, 4, 5].map((dayOfWeek) => ({
    key: crypto.randomUUID(),
    dayOfWeek,
    startTime: '09:00',
    endTime: '17:00',
    capacityFactor: '1',
  }));
}
