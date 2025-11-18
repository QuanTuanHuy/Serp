/**
 * PTM v2 - Novel Rich Text Note Editor
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Notion-style WYSIWYG editor using Novel
 */

'use client';

import { useMemo, useState } from 'react';
import { Save, X, Pin } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Label } from '@/shared/components/ui/label';
import { cn } from '@/shared/utils';
import {
  EditorRoot,
  EditorContent,
  type JSONContent,
  EditorInstance,
  StarterKit,
  TiptapImage,
  TiptapLink,
  TiptapUnderline,
  TextStyle,
  Color,
} from 'novel';
import { useDebouncedCallback } from 'use-debounce';

interface NoteEditorNovelProps {
  initialContent?: string;
  initialPinned?: boolean;
  onSave: (content: string, isPinned: boolean) => void | Promise<void>;
  onCancel?: () => void;
  placeholder?: string;
}

// Default extensions for the editor
const extensions = [
  StarterKit.configure({
    bulletList: {
      keepMarks: true,
      keepAttributes: false,
    },
    orderedList: {
      keepMarks: true,
      keepAttributes: false,
    },
  }),
  TiptapImage,
  TiptapLink,
  TiptapUnderline,
  TextStyle,
  Color,
];

export function NoteEditorNovel({
  initialContent = '',
  initialPinned = false,
  onSave,
  onCancel,
  placeholder = 'Press / for commands...',
}: NoteEditorNovelProps) {
  // Prepare a valid initial JSON string so saving works even before first update
  const initialJson = useMemo(() => {
    try {
      return JSON.stringify(
        initialContent && initialContent.trim() !== ''
          ? JSON.parse(initialContent)
          : {
              type: 'doc',
              content: [
                {
                  type: 'paragraph',
                },
              ],
            }
      );
    } catch {
      // Fallback: treat as plain text wrapped into a paragraph
      return JSON.stringify({
        type: 'doc',
        content: [
          {
            type: 'paragraph',
            content: [{ type: 'text', text: initialContent }],
          },
        ],
      });
    }
  }, [initialContent]);

  const [editorContent, setEditorContent] = useState(initialJson);
  const [isPinned, setIsPinned] = useState(initialPinned);
  const [isSaving, setIsSaving] = useState(false);

  // Parse initial content
  const getInitialContent = (): JSONContent => {
    if (!initialContent || initialContent.trim() === '') {
      return {
        type: 'doc',
        content: [
          {
            type: 'paragraph',
          },
        ],
      };
    }

    try {
      return JSON.parse(initialContent);
    } catch {
      // If not JSON, treat as plain text
      return {
        type: 'doc',
        content: [
          {
            type: 'paragraph',
            content: [{ type: 'text', text: initialContent }],
          },
        ],
      };
    }
  };

  const debouncedUpdates = useDebouncedCallback(
    async (editor: EditorInstance) => {
      const json = editor.getJSON();
      setEditorContent(JSON.stringify(json));
    },
    500
  );

  const handleSave = async () => {
    if (!editorContent.trim()) return;

    setIsSaving(true);
    try {
      await onSave(editorContent, isPinned);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Card className='border-2'>
      <CardHeader className='pb-3'>
        <div className='flex items-center justify-between'>
          <h3 className='text-sm font-medium text-muted-foreground'>
            Note Editor
          </h3>
          <div className='flex items-center gap-2 text-xs text-muted-foreground'>
            <kbd className='px-2 py-1 bg-muted rounded text-xs font-mono'>
              /
            </kbd>
            <span>for commands</span>
          </div>
        </div>
      </CardHeader>

      <CardContent className='space-y-4'>
        {/* Novel Editor */}
        <EditorRoot>
          <EditorContent
            initialContent={getInitialContent()}
            extensions={extensions}
            className='border rounded-lg'
            onUpdate={({ editor }) => {
              debouncedUpdates(editor);
            }}
            editorProps={{
              attributes: {
                class:
                  'prose prose-sm dark:prose-invert max-w-none min-h-[300px] p-4 focus:outline-none',
              },
            }}
          />
        </EditorRoot>

        {/* Controls */}
        <div className='flex items-center justify-between pt-2 border-t'>
          <div className='flex items-center gap-2'>
            <Checkbox
              id='pin-note'
              checked={isPinned}
              onCheckedChange={(checked) => setIsPinned(checked as boolean)}
            />
            <Label
              htmlFor='pin-note'
              className='flex items-center gap-1 text-sm cursor-pointer'
            >
              <Pin
                className={cn(
                  'h-3.5 w-3.5',
                  isPinned && 'fill-yellow-600 text-yellow-600'
                )}
              />
              Pin this note
            </Label>
          </div>

          <div className='flex items-center gap-2'>
            {onCancel && (
              <Button variant='outline' onClick={onCancel} disabled={isSaving}>
                <X className='h-4 w-4 mr-2' />
                Cancel
              </Button>
            )}
            <Button
              onClick={handleSave}
              disabled={!editorContent.trim() || isSaving}
            >
              <Save className='h-4 w-4 mr-2' />
              {isSaving ? 'Saving...' : 'Save Note'}
            </Button>
          </div>
        </div>

        {/* Help Text */}
        <div className='text-xs text-muted-foreground bg-muted/30 p-3 rounded-lg'>
          <p className='font-medium mb-1.5'>✨ Novel Editor Features:</p>
          <ul className='space-y-0.5 ml-4 grid grid-cols-2 gap-x-4'>
            <li>• Type / for slash commands</li>
            <li>• Select text for formatting</li>
            <li>• Drag & drop images</li>
            <li>• Markdown shortcuts</li>
            <li>• Real-time preview</li>
            <li>• Rich text formatting</li>
          </ul>
        </div>
      </CardContent>
    </Card>
  );
}
