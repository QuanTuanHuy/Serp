/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list inline editors
 */

'use client';

import { useMemo, useState, type KeyboardEvent, type ReactNode } from 'react';
import {
  CalendarDays,
  Check,
  ChevronDown,
  Loader2,
  Pencil,
  Save,
  X,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
} from '@/shared/components/ui';
import { Combobox, type ComboboxItem } from '@/shared/components/ui/combobox';
import { cn } from '@/shared/utils';
import { PMDatePicker } from '../../shared';
import {
  fromLocalDateInputValue,
  parseLocalDateValue,
  toLocalDateInputValue,
} from '../../../utils/date';
import type {
  PMWorkItemSearchApi,
  PMWorkItemTransitionApi,
} from '../../../types/api';

export type WorkItemListTransitionLoader = (
  item: PMWorkItemSearchApi
) => Promise<PMWorkItemTransitionApi[]>;

interface InlineEditorShellProps {
  children: ReactNode;
  className?: string;
}

export function InlineEditorShell({
  children,
  className,
}: InlineEditorShellProps) {
  return (
    <div
      className={className}
      onClick={(event) => event.stopPropagation()}
      onKeyDown={(event) => event.stopPropagation()}
    >
      {children}
    </div>
  );
}

export function WorkItemListSummaryEditor({
  item,
  disabled,
  onSave,
}: {
  item: PMWorkItemSearchApi;
  disabled?: boolean;
  onSave: (item: PMWorkItemSearchApi, summary: string) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(item.summary);

  const cancel = () => {
    setDraft(item.summary);
    setEditing(false);
  };

  const save = async () => {
    const nextValue = draft.trim();
    if (!nextValue) {
      toast.error('Summary is required.');
      return;
    }
    if (nextValue === item.summary) {
      setEditing(false);
      return;
    }

    try {
      await onSave(item, nextValue);
      setEditing(false);
    } catch {
      return;
    }
  };

  const handleKeyDown = async (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      await save();
    }
    if (event.key === 'Escape') {
      event.preventDefault();
      cancel();
    }
  };

  if (!editing) {
    return (
      <button
        type='button'
        className='group flex w-full min-w-0 items-center gap-1.5 rounded px-1 py-0.5 text-left hover:bg-muted'
        onClick={() => {
          setDraft(item.summary);
          setEditing(true);
        }}
      >
        <span className='min-w-0 truncate font-medium text-foreground'>
          {item.summary}
        </span>
        <Pencil className='h-3 w-3 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100' />
      </button>
    );
  }

  return (
    <div className='flex w-full min-w-0 items-center gap-1.5'>
      <Input
        autoFocus
        value={draft}
        disabled={disabled}
        className='h-8 min-w-0 flex-1'
        onChange={(event) => setDraft(event.target.value)}
        onKeyDown={handleKeyDown}
      />
      <IconAction
        label='Save summary'
        disabled={disabled}
        onClick={() => {
          void save();
        }}
      >
        <Check className='h-3.5 w-3.5' />
      </IconAction>
      <IconAction
        label='Cancel summary edit'
        disabled={disabled}
        onClick={cancel}
      >
        <X className='h-3.5 w-3.5' />
      </IconAction>
    </div>
  );
}

export function WorkItemListComboboxEditor({
  value,
  display,
  options,
  currentLabel,
  placeholder,
  loading,
  disabled,
  onSave,
}: {
  value?: number | null;
  display: ReactNode;
  options: ComboboxItem[];
  currentLabel?: string | null;
  placeholder: string;
  loading?: boolean;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState<string | number | undefined>(
    value ?? undefined
  );

  const editorOptions = useMemo(() => {
    if (
      value === null ||
      value === undefined ||
      !currentLabel ||
      options.some((option) => String(option.value) === String(value))
    ) {
      return options;
    }

    return [{ value, label: currentLabel }, ...options];
  }, [currentLabel, options, value]);

  const cancel = () => {
    setDraft(value ?? undefined);
    setEditing(false);
  };

  const save = async () => {
    const nextValue = draft === undefined ? null : Number(draft);
    if ((value ?? null) === nextValue) {
      setEditing(false);
      return;
    }

    try {
      await onSave(nextValue);
      setEditing(false);
    } catch {
      return;
    }
  };

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex max-w-full min-w-0 items-center gap-1.5 rounded px-1 py-0.5 text-left hover:bg-muted'
        onClick={() => {
          setDraft(value ?? undefined);
          setEditing(true);
        }}
      >
        {display}
        <Pencil className='h-3 w-3 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100' />
      </button>
    );
  }

  return (
    <div className='w-full min-w-0 space-y-1.5'>
      <Combobox
        value={draft}
        onChange={setDraft}
        items={editorOptions}
        placeholder={placeholder}
        emptyText='No options found'
        loading={loading}
        disabled={disabled || loading}
        className='h-8 min-w-0'
      />
      <InlineActions
        disabled={disabled || loading}
        onCancel={cancel}
        onSave={() => {
          void save();
        }}
      />
    </div>
  );
}

export function WorkItemListDateEditor({
  value,
  disabled,
  onSave,
}: {
  value?: number | string | null;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(toLocalDateInputValue(value));

  const cancel = () => {
    setDraft(toLocalDateInputValue(value));
    setEditing(false);
  };

  const save = async () => {
    const nextValue = fromLocalDateInputValue(draft) ?? null;
    try {
      await onSave(nextValue);
      setEditing(false);
    } catch {
      return;
    }
  };

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex max-w-full min-w-0 items-center gap-1.5 rounded px-1 py-0.5 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground'
        onClick={() => {
          setDraft(toLocalDateInputValue(value));
          setEditing(true);
        }}
      >
        <CalendarDays className='h-3.5 w-3.5 shrink-0' />
        <span className='truncate'>{formatShortDate(value)}</span>
        <Pencil className='h-3 w-3 shrink-0 opacity-0 transition-opacity group-hover:opacity-100' />
      </button>
    );
  }

  return (
    <div className='w-full min-w-0 space-y-1.5'>
      <PMDatePicker
        value={draft}
        onChange={(date) => setDraft(date ? toLocalDateInputValue(date) : '')}
        disabled={disabled}
        showClear={false}
        className='w-full'
        buttonClassName='h-8 flex-1'
      />
      <div className='flex items-center gap-1'>
        <Button
          type='button'
          size='sm'
          variant='ghost'
          className='h-7 px-2 text-xs'
          disabled={disabled}
          onClick={() => setDraft('')}
        >
          Clear
        </Button>
        <InlineActions
          disabled={disabled}
          onCancel={cancel}
          onSave={() => {
            void save();
          }}
        />
      </div>
    </div>
  );
}

export function WorkItemListStatusEditor({
  item,
  loading,
  disabled,
  onLoadTransitions,
  onSave,
}: {
  item: PMWorkItemSearchApi;
  loading?: boolean;
  disabled?: boolean;
  onLoadTransitions: WorkItemListTransitionLoader;
  onSave: (item: PMWorkItemSearchApi, transitionId: number) => Promise<void>;
}) {
  const [open, setOpen] = useState(false);
  const [isLoadingTransitions, setIsLoadingTransitions] = useState(false);
  const [transitions, setTransitions] = useState<PMWorkItemTransitionApi[]>([]);

  const loadTransitions = async () => {
    setIsLoadingTransitions(true);
    try {
      setTransitions(await onLoadTransitions(item));
    } catch {
      setTransitions([]);
    } finally {
      setIsLoadingTransitions(false);
    }
  };

  const handleOpenChange = (nextOpen: boolean) => {
    setOpen(nextOpen);
    if (nextOpen) {
      void loadTransitions();
    }
  };

  return (
    <DropdownMenu open={open} onOpenChange={handleOpenChange}>
      <DropdownMenuTrigger asChild>
        <Button
          type='button'
          variant='ghost'
          size='sm'
          className={cn(
            'h-7 justify-start gap-1.5 px-1.5 text-xs font-semibold',
            getStatusTone(item.statusCategoryKey)
          )}
          disabled={disabled || loading}
        >
          {loading ? <Loader2 className='h-3 w-3 animate-spin' /> : null}
          <span className='max-w-28 truncate'>
            {item.statusName ?? `Status ${item.statusId}`}
          </span>
          <ChevronDown className='h-3 w-3 opacity-70' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='start' className='w-64'>
        <DropdownMenuLabel>Move to</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {isLoadingTransitions ? (
          <DropdownMenuItem disabled>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading transitions
          </DropdownMenuItem>
        ) : null}
        {!isLoadingTransitions && transitions.length === 0 ? (
          <DropdownMenuItem disabled>No transitions available</DropdownMenuItem>
        ) : null}
        {!isLoadingTransitions
          ? transitions.map((transition) => (
              <DropdownMenuItem
                key={transition.id}
                className='flex items-center justify-between gap-3'
                onSelect={() => {
                  void onSave(item, transition.id).catch(() => undefined);
                }}
              >
                <span className='min-w-0 truncate'>
                  {transition.targetStatus?.name ?? transition.name}
                </span>
                <span className='shrink-0 text-xs text-muted-foreground'>
                  {transition.name}
                </span>
              </DropdownMenuItem>
            ))
          : null}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function InlineActions({
  disabled,
  onCancel,
  onSave,
}: {
  disabled?: boolean;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <div className='flex items-center justify-end gap-1'>
      <IconAction label='Save' disabled={disabled} onClick={onSave}>
        <Save className='h-3.5 w-3.5' />
      </IconAction>
      <IconAction label='Cancel' disabled={disabled} onClick={onCancel}>
        <X className='h-3.5 w-3.5' />
      </IconAction>
    </div>
  );
}

function IconAction({
  label,
  disabled,
  children,
  onClick,
}: {
  label: string;
  disabled?: boolean;
  children: ReactNode;
  onClick: () => void;
}) {
  return (
    <Button
      type='button'
      variant='ghost'
      size='icon'
      className='h-7 w-7'
      aria-label={label}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </Button>
  );
}

function formatShortDate(value?: number | string | null): string {
  const date = parseLocalDateValue(value);
  if (!date) return 'None';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
}

function getStatusTone(statusCategoryKey?: string | null): string {
  const key = statusCategoryKey?.toLowerCase();
  if (key?.includes('done')) {
    return 'bg-emerald-100 text-emerald-800 hover:bg-emerald-100 dark:bg-emerald-500/20 dark:text-emerald-200';
  }
  if (key?.includes('progress')) {
    return 'bg-blue-100 text-blue-800 hover:bg-blue-100 dark:bg-blue-500/20 dark:text-blue-200';
  }
  return 'bg-muted text-muted-foreground hover:bg-muted';
}
