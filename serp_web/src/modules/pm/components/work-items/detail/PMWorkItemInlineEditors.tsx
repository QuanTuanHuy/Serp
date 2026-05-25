/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item inline editors
 */

import { useState, type ReactNode } from 'react';
import { CalendarDays, Clock3, Pencil, Save } from 'lucide-react';
import { toast } from 'sonner';
import { Button, Input, Textarea } from '@/shared/components/ui';
import { Combobox, type ComboboxItem } from '@/shared/components/ui/combobox';
import { formatDetailDate, toDateInputValue } from './pmWorkItemDetail.utils';

export function InlineSummaryEditor({
  value,
  disabled,
  onSave,
}: {
  value: string;
  disabled?: boolean;
  onSave: (value: string) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value);

  if (!editing) {
    return (
      <button
        type='button'
        className='group flex w-full min-w-0 items-start gap-2 text-left'
        onClick={() => {
          setDraft(value);
          setEditing(true);
        }}
      >
        <h1 className='min-w-0 flex-1 break-words text-2xl font-semibold tracking-tight sm:text-3xl'>
          {value}
        </h1>
        <Pencil className='mt-2 h-4 w-4 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    const nextValue = draft.trim();
    if (!nextValue) {
      toast.error('Summary is required.');
      return;
    }
    await onSave(nextValue);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Textarea
        value={draft}
        rows={2}
        className='text-2xl font-semibold sm:text-3xl'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

export function InlineDescriptionEditor({
  value,
  disabled,
  onSave,
}: {
  value?: string | null;
  disabled?: boolean;
  onSave: (value: string | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value ?? '');

  if (!editing) {
    return (
      <button
        type='button'
        className='w-full text-left text-sm text-muted-foreground hover:text-foreground'
        onClick={() => {
          setDraft(value ?? '');
          setEditing(true);
        }}
      >
        {value ? (
          <span className='whitespace-pre-wrap leading-6'>{value}</span>
        ) : (
          'Add a description...'
        )}
      </button>
    );
  }

  const save = async () => {
    await onSave(draft.trim() || null);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Textarea
        value={draft}
        rows={8}
        placeholder='Add context, scope, acceptance notes, or delivery details.'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

export function InlineComboboxField({
  value,
  display,
  items,
  placeholder,
  loading,
  disabled,
  onSave,
}: {
  value?: number | null;
  display: ReactNode;
  items: ComboboxItem[];
  placeholder: string;
  loading?: boolean;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState<string | number | undefined>(
    value ?? undefined
  );

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex min-w-0 items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(value ?? undefined);
          setEditing(true);
        }}
      >
        {display}
        <Pencil className='h-3.5 w-3.5 shrink-0 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    await onSave(draft === undefined ? null : Number(draft));
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Combobox
        value={draft}
        onChange={setDraft}
        items={items}
        placeholder={placeholder}
        emptyText='No options found'
        loading={loading}
      />
      <InlineEditorActions
        disabled={disabled || loading}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

export function InlineDateField({
  value,
  disabled,
  onSave,
}: {
  value?: number | string | null;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(toDateInputValue(value));

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(toDateInputValue(value));
          setEditing(true);
        }}
      >
        <CalendarDays className='h-4 w-4 text-muted-foreground' />
        {formatDetailDate(value)}
        <Pencil className='h-3.5 w-3.5 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    await onSave(draft ? new Date(`${draft}T00:00:00`).getTime() : null);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Input
        type='date'
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

export function InlineNumberField({
  value,
  disabled,
  onSave,
}: {
  value?: number | null;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(
    value === null || value === undefined ? '' : String(value)
  );

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(value === null || value === undefined ? '' : String(value));
          setEditing(true);
        }}
      >
        <Clock3 className='h-4 w-4 text-muted-foreground' />
        {value === null || value === undefined ? 'None' : `${value} min`}
        <Pencil className='h-3.5 w-3.5 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    const trimmed = draft.trim();
    if (!trimmed) {
      await onSave(null);
      setEditing(false);
      return;
    }
    const nextValue = Number(trimmed);
    if (!Number.isFinite(nextValue) || nextValue < 0) {
      toast.error('Estimate must be zero or greater.');
      return;
    }
    await onSave(nextValue);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Input
        type='number'
        min='0'
        step='1'
        value={draft}
        placeholder='Minutes'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

export function InlineEditorActions({
  disabled,
  onCancel,
  onSave,
}: {
  disabled?: boolean;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <div className='flex items-center gap-2'>
      <Button
        size='sm'
        className='h-8 gap-1'
        disabled={disabled}
        onClick={onSave}
      >
        <Save className='h-3.5 w-3.5' />
        Save
      </Button>
      <Button
        size='sm'
        variant='ghost'
        className='h-8'
        disabled={disabled}
        onClick={onCancel}
      >
        Cancel
      </Button>
    </div>
  );
}
