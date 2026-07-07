'use client';

import * as React from 'react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusThemeStyle, schoolBusUi } from '../theme';

// -- Base confirm dialog -------------------------------------------------------

export interface SchoolBusConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Dialog heading */
  title: string;
  /** Supporting text below the heading */
  description?: React.ReactNode;
  /** Label for the confirm button (default: "Confirm") */
  confirmLabel?: string;
  /** Label for the cancel button (default: "Cancel") */
  cancelLabel?: string;
  /**
   * Visual variant of the confirm button.
   * - "danger"  -> red / destructive (default for delete actions)
   * - "primary" -> slate / primary (default for non-destructive confirmations)
   */
  variant?: 'danger' | 'primary';
  /** Extra content rendered between description and footer (e.g. an input or checklist) */
  children?: React.ReactNode;
  onConfirm: () => void | Promise<void>;
  isLoading?: boolean;
}

/**
 * SchoolBusConfirmDialog
 *
 * Base confirmation dialog that all school-bus confirm/delete popovers should
 * use.  Applies the module's rounded-card aesthetic and neutral colour theme so
 * every confirmation modal looks consistent.
 *
 * Usage:
 * ```tsx
 * <SchoolBusConfirmDialog
 *   open={open}
 *   onOpenChange={setOpen}
 *   title="Remove student?"
 *   description="The student will be unassigned from this route."
 *   variant="danger"
 *   confirmLabel="Remove"
 *   onConfirm={handleConfirm}
 *   isLoading={removing}
 * />
 * ```
 */
export function SchoolBusConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel = 'Xác nhận',
  cancelLabel = 'Hủy',
  variant = 'danger',
  children,
  onConfirm,
  isLoading = false,
}: SchoolBusConfirmDialogProps) {
  const confirmCls =
    variant === 'danger' ? schoolBusUi.dangerButton : schoolBusUi.primaryButton;

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent
        className='school-bus-shell rounded-[28px] border-border bg-background text-foreground shadow-[0_30px_90px_rgba(15,23,42,0.18)]'
        style={schoolBusThemeStyle}
      >
        <AlertDialogHeader>
          <AlertDialogTitle className='text-xl font-semibold tracking-tight text-foreground'>
            {title}
          </AlertDialogTitle>
          {description && (
            <AlertDialogDescription asChild={typeof description !== 'string'}>
              {typeof description === 'string' ? (
                description
              ) : (
                <div className='text-sm text-muted-foreground'>
                  {description}
                </div>
              )}
            </AlertDialogDescription>
          )}
        </AlertDialogHeader>

        {children && <div className='px-1 pb-2'>{children}</div>}

        <AlertDialogFooter>
          <AlertDialogCancel
            className={schoolBusUi.outlineButton}
            disabled={isLoading}
          >
            {cancelLabel}
          </AlertDialogCancel>
          <AlertDialogAction
            className={cn(confirmCls)}
            disabled={isLoading}
            onClick={async (e) => {
              e.preventDefault();
              try {
                await onConfirm();
                onOpenChange(false);
              } catch {
                // stay open so user can retry or cancel
              }
            }}
          >
            {isLoading ? `${confirmLabel}...` : confirmLabel}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

// -- Convenience hook ----------------------------------------------------------

interface UseConfirmOptions {
  onConfirm: () => void | Promise<void>;
  isLoading?: boolean;
}

/**
 * useSchoolBusConfirm
 *
 * Lightweight state hook so a component can open a single SchoolBusConfirmDialog
 * without manually tracking `open` + pending-item state.
 *
 * ```tsx
 * const { confirmState, requestConfirm, closeConfirm } = useSchoolBusConfirm({
 *   onConfirm: () => doDelete(confirmState.payload),
 * });
 *
 * <SchoolBusConfirmDialog {...confirmState} onOpenChange={closeConfirm} />
 * <Button onClick={() => requestConfirm({ payload: id, title: 'Delete?', ... })} />
 * ```
 */
export interface ConfirmRequest
  extends Pick<
    SchoolBusConfirmDialogProps,
    'title' | 'description' | 'confirmLabel' | 'cancelLabel' | 'variant'
  > {
  /** Arbitrary data the calling component needs back in `onConfirm`. */
  payload?: unknown;
}

export interface ConfirmState extends ConfirmRequest {
  open: boolean;
  payload?: unknown;
}

export function useSchoolBusConfirm({
  onConfirm,
  isLoading,
}: UseConfirmOptions) {
  const [state, setState] = React.useState<ConfirmState>({
    open: false,
    title: '',
  });

  const requestConfirm = React.useCallback((req: ConfirmRequest) => {
    setState({ ...req, open: true });
  }, []);

  const closeConfirm = React.useCallback((open: boolean) => {
    if (!open) setState((prev) => ({ ...prev, open: false }));
  }, []);

  const dialogProps: SchoolBusConfirmDialogProps = {
    ...state,
    onOpenChange: closeConfirm,
    onConfirm,
    isLoading,
  };

  return { confirmState: state, dialogProps, requestConfirm, closeConfirm };
}

