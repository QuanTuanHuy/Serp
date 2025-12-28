/**
 * PTM - Keyboard Shortcuts Hook (OPTIONAL IMPLEMENTATION)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Centralized keyboard shortcut management
 *
 * NOTE: This is an OPTIONAL enhancement. Current inline approach also works well.
 * Implement this when:
 * - You have >20 shortcuts across the module
 * - You need conflict detection
 * - You want auto-generated documentation
 * - You add shortcut customization feature
 */

'use client';

import { useEffect, useMemo } from 'react';

export interface KeyboardShortcut {
  /** Keyboard key (e.g., 'k', 'Enter', 'Escape') */
  key: string;

  /** Require Ctrl key (Windows/Linux) */
  ctrl?: boolean;

  /** Require Cmd key (macOS) */
  meta?: boolean;

  /** Require Shift key */
  shift?: boolean;

  /** Require Alt/Option key */
  alt?: boolean;

  /** Handler function */
  handler: (e: KeyboardEvent) => void;

  /** Human-readable description for documentation */
  description: string;

  /** Prevent default browser behavior */
  preventDefault?: boolean;

  /** Stop event propagation */
  stopPropagation?: boolean;
}

interface UseKeyboardShortcutsOptions {
  /** Enable/disable all shortcuts */
  enabled?: boolean;

  /** Disable when input/textarea is focused */
  disableOnInput?: boolean;

  /** Throw error if duplicate shortcuts found */
  throwOnConflict?: boolean;
}

/**
 * Centralized keyboard shortcut management hook
 *
 * @example
 * // Basic usage
 * useKeyboardShortcuts([
 *   { key: 'k', meta: true, handler: focusSearch, description: 'Focus search' },
 *   { key: 'Escape', handler: closePanel, description: 'Close panel' },
 * ]);
 *
 * @example
 * // With conditions
 * useKeyboardShortcuts([
 *   { key: 'l', meta: true, handler: copyLink, description: 'Copy link' },
 * ], { enabled: isPanelOpen && !isEditing });
 *
 * @example
 * // In component
 * function TaskDetail({ open, isEditing }: Props) {
 *   useKeyboardShortcuts([
 *     { key: 'Escape', handler: () => onClose(), description: 'Close' },
 *     { key: 'e', meta: true, handler: handleEdit, description: 'Edit' },
 *     { key: 'd', meta: true, handler: handleDelete, description: 'Delete' },
 *     { key: 'l', meta: true, handler: handleCopyLink, description: 'Copy link' },
 *   ], { enabled: open && !isEditing });
 * }
 */
export function useKeyboardShortcuts(
  shortcuts: KeyboardShortcut[],
  options: UseKeyboardShortcutsOptions = {}
) {
  const {
    enabled = true,
    disableOnInput = true,
    throwOnConflict = process.env.NODE_ENV === 'development',
  } = options;

  // Detect conflicts in development
  const shortcutMap = useMemo(() => {
    if (!throwOnConflict) return new Map();

    const map = new Map<string, KeyboardShortcut>();

    shortcuts.forEach((shortcut) => {
      const key = getShortcutKey(shortcut);
      const existing = map.get(key);

      if (existing) {
        console.error('Keyboard shortcut conflict detected:', {
          key,
          existing: existing.description,
          new: shortcut.description,
        });

        if (throwOnConflict) {
          throw new Error(
            `Keyboard shortcut conflict: "${key}" is used by both "${existing.description}" and "${shortcut.description}"`
          );
        }
      }

      map.set(key, shortcut);
    });

    return map;
  }, [shortcuts, throwOnConflict]);

  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      // Skip if input/textarea focused (optional)
      if (disableOnInput) {
        const activeElement = document.activeElement;
        if (
          activeElement?.tagName === 'INPUT' ||
          activeElement?.tagName === 'TEXTAREA' ||
          activeElement?.hasAttribute('contenteditable')
        ) {
          return;
        }
      }

      // Check each shortcut
      shortcuts.forEach((shortcut) => {
        const {
          key,
          ctrl,
          meta,
          shift,
          alt,
          handler,
          preventDefault = true,
          stopPropagation = false,
        } = shortcut;

        // Match key
        if (e.key.toLowerCase() !== key.toLowerCase()) return;

        // Match all modifiers (strict)
        const modifiersMatch =
          (ctrl === true ? e.ctrlKey : !e.ctrlKey) &&
          (meta === true ? e.metaKey : !e.metaKey) &&
          (shift === true ? e.shiftKey : !e.shiftKey) &&
          (alt === true ? e.altKey : !e.altKey);

        if (modifiersMatch) {
          if (preventDefault) e.preventDefault();
          if (stopPropagation) e.stopPropagation();
          handler(e);
        }
      });
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [shortcuts, enabled, disableOnInput]);

  // Return shortcuts for documentation
  return {
    shortcuts,
    shortcutMap,
    getDocumentation: () => generateDocumentation(shortcuts),
  };
}

/**
 * Generate unique key for shortcut (for conflict detection)
 */
function getShortcutKey(shortcut: KeyboardShortcut): string {
  const { key, ctrl, meta, shift, alt } = shortcut;
  const modifiers = [
    ctrl && 'Ctrl',
    meta && 'Cmd',
    shift && 'Shift',
    alt && 'Alt',
  ]
    .filter(Boolean)
    .join('+');

  return modifiers ? `${modifiers}+${key}` : key;
}

/**
 * Generate markdown documentation for shortcuts
 */
function generateDocumentation(shortcuts: KeyboardShortcut[]): string {
  return shortcuts
    .map((shortcut) => {
      const key = getShortcutKey(shortcut);
      return `- **${key}**: ${shortcut.description}`;
    })
    .join('\n');
}

/**
 * Helper: Create shortcut string for display
 *
 * @example
 * formatShortcut({ key: 'k', meta: true }) // "⌘K"
 * formatShortcut({ key: 'Delete', shift: true, meta: true }) // "⇧⌘Delete"
 */
export function formatShortcut(shortcut: KeyboardShortcut): string {
  const { key, ctrl, meta, shift, alt } = shortcut;
  const isMac =
    typeof navigator !== 'undefined' && navigator.platform.includes('Mac');

  const modifiers = [
    ctrl && (isMac ? '⌃' : 'Ctrl'),
    alt && (isMac ? '⌥' : 'Alt'),
    shift && (isMac ? '⇧' : 'Shift'),
    meta && (isMac ? '⌘' : 'Cmd'),
  ]
    .filter(Boolean)
    .join('');

  const keyDisplay = key === ' ' ? 'Space' : key;
  return `${modifiers}${keyDisplay}`;
}

/**
 * Hook for global shortcuts (always active)
 *
 * @example
 * // In layout or root component
 * useGlobalShortcuts([
 *   { key: 'k', meta: true, handler: openCommandPalette, description: 'Command palette' },
 *   { key: '/', handler: focusSearch, description: 'Search' },
 * ]);
 */
export function useGlobalShortcuts(shortcuts: KeyboardShortcut[]) {
  return useKeyboardShortcuts(shortcuts, {
    enabled: true,
    disableOnInput: true,
  });
}

/**
 * Hook for context-specific shortcuts (conditional)
 *
 * @example
 * // In modal/panel component
 * useConditionalShortcuts([
 *   { key: 'Escape', handler: close, description: 'Close' },
 * ], open);
 */
export function useConditionalShortcuts(
  shortcuts: KeyboardShortcut[],
  condition: boolean
) {
  return useKeyboardShortcuts(shortcuts, { enabled: condition });
}
