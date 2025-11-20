/**
 * PTM v2 - Note Editor Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Markdown note editor
 */

'use client';

import { useState } from 'react';
import { Save, X, Eye, Edit3, Pin } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Textarea } from '@/shared/components/ui/textarea';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Label } from '@/shared/components/ui/label';
import { cn } from '@/shared/utils';

interface NoteEditorProps {
  initialContent?: string;
  initialPinned?: boolean;
  onSave: (content: string, isPinned: boolean) => void | Promise<void>;
  onCancel?: () => void;
  placeholder?: string;
}

export function NoteEditor({
  initialContent = '',
  initialPinned = false,
  onSave,
  onCancel,
  placeholder = 'Write your note here... (Markdown supported)',
}: NoteEditorProps) {
  const [content, setContent] = useState(initialContent);
  const [isPinned, setIsPinned] = useState(initialPinned);
  const [activeTab, setActiveTab] = useState<'write' | 'preview'>('write');
  const [isSaving, setIsSaving] = useState(false);

  const handleSave = async () => {
    if (!content.trim()) return;

    setIsSaving(true);
    try {
      await onSave(content, isPinned);
    } finally {
      setIsSaving(false);
    }
  };

  // Simple markdown preview (basic rendering)
  const renderMarkdownPreview = (text: string) => {
    let html = text;

    // Headers
    html = html.replace(
      /^### (.*$)/gim,
      '<h3 class="text-lg font-semibold mt-4 mb-2">$1</h3>'
    );
    html = html.replace(
      /^## (.*$)/gim,
      '<h2 class="text-xl font-semibold mt-4 mb-2">$1</h2>'
    );
    html = html.replace(
      /^# (.*$)/gim,
      '<h1 class="text-2xl font-bold mt-4 mb-2">$1</h1>'
    );

    // Bold
    html = html.replace(
      /\*\*(.*?)\*\*/gim,
      '<strong class="font-semibold">$1</strong>'
    );

    // Italic
    html = html.replace(/\*(.*?)\*/gim, '<em class="italic">$1</em>');

    // Code blocks
    html = html.replace(
      /```(.*?)```/gim,
      '<pre class="bg-muted p-3 rounded-md my-2 overflow-x-auto"><code>$1</code></pre>'
    );

    // Inline code
    html = html.replace(
      /`(.*?)`/gim,
      '<code class="bg-muted px-1.5 py-0.5 rounded text-sm">$1</code>'
    );

    // Links
    html = html.replace(
      /\[(.*?)\]\((.*?)\)/gim,
      '<a href="$2" class="text-primary underline" target="_blank">$1</a>'
    );

    // Unordered lists
    html = html.replace(/^\* (.*$)/gim, '<li class="ml-4">• $1</li>');
    html = html.replace(/^- (.*$)/gim, '<li class="ml-4">• $1</li>');

    // Line breaks
    html = html.replace(/\n/gim, '<br />');

    return html;
  };

  return (
    <Card className='border-2'>
      <CardHeader className='pb-3'>
        <Tabs
          value={activeTab}
          onValueChange={(v) => setActiveTab(v as 'write' | 'preview')}
        >
          <TabsList className='grid w-full grid-cols-2'>
            <TabsTrigger value='write' className='flex items-center gap-2'>
              <Edit3 className='h-4 w-4' />
              Write
            </TabsTrigger>
            <TabsTrigger value='preview' className='flex items-center gap-2'>
              <Eye className='h-4 w-4' />
              Preview
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </CardHeader>

      <CardContent className='space-y-4'>
        {activeTab === 'write' ? (
          <Textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder={placeholder}
            rows={10}
            className='font-mono text-sm'
          />
        ) : (
          <div
            className='prose prose-sm dark:prose-invert max-w-none min-h-[240px] p-4 border rounded-md'
            dangerouslySetInnerHTML={{
              __html: content
                ? renderMarkdownPreview(content)
                : '<p class="text-muted-foreground italic">Nothing to preview</p>',
            }}
          />
        )}

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
            <Button onClick={handleSave} disabled={!content.trim() || isSaving}>
              <Save className='h-4 w-4 mr-2' />
              {isSaving ? 'Saving...' : 'Save Note'}
            </Button>
          </div>
        </div>

        <div className='text-xs text-muted-foreground'>
          <p className='font-medium mb-1'>Markdown tips:</p>
          <ul className='space-y-0.5 ml-4'>
            <li>
              • <code className='bg-muted px-1 rounded text-xs'># Heading</code>{' '}
              for headers
            </li>
            <li>
              • <code className='bg-muted px-1 rounded text-xs'>**bold**</code>{' '}
              and{' '}
              <code className='bg-muted px-1 rounded text-xs'>*italic*</code>
            </li>
            <li>
              • <code className='bg-muted px-1 rounded text-xs'>`code`</code>{' '}
              for inline code
            </li>
            <li>
              • <code className='bg-muted px-1 rounded text-xs'>- item</code>{' '}
              for lists
            </li>
          </ul>
        </div>
      </CardContent>
    </Card>
  );
}
