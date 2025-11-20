/**
 * PTM v2 - Rich Text Preview Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Render Tiptap JSON as HTML preview
 */

'use client';

import type { JSONContent } from 'novel';

interface RichTextPreviewProps {
  content: string; // JSON string
  maxLength?: number;
  className?: string;
}

export function RichTextPreview({
  content,
  maxLength = 150,
  className = '',
}: RichTextPreviewProps) {
  const renderNode = (
    node: JSONContent,
    charCount: { count: number }
  ): string => {
    if (charCount.count >= maxLength) return '';

    // Text node
    if (node.type === 'text') {
      const text = node.text || '';
      const remainingChars = maxLength - charCount.count;

      if (text.length > remainingChars) {
        charCount.count = maxLength;
        return applyMarks(
          text.substring(0, remainingChars) + '...',
          node.marks
        );
      }

      charCount.count += text.length;
      return applyMarks(text, node.marks);
    }

    // Hard break
    if (node.type === 'hardBreak') {
      return '<br/>';
    }

    // Nodes with content
    if (node.content && Array.isArray(node.content)) {
      const children = node.content
        .map((child) => renderNode(child, charCount))
        .join('');

      if (charCount.count >= maxLength) return children;

      switch (node.type) {
        case 'doc':
          return children;

        case 'paragraph':
          return children ? `<p class="mb-2">${children}</p>` : '';

        case 'heading':
          const level = node.attrs?.level || 1;
          const headingClasses = {
            1: 'text-lg font-bold mb-2',
            2: 'text-base font-semibold mb-2',
            3: 'text-sm font-semibold mb-1',
          };
          return `<h${level} class="${headingClasses[level as keyof typeof headingClasses]}">${children}</h${level}>`;

        case 'bulletList':
          return `<ul class="list-disc ml-4 mb-2">${children}</ul>`;

        case 'orderedList':
          return `<ol class="list-decimal ml-4 mb-2">${children}</ol>`;

        case 'listItem':
          return `<li class="mb-1">${children}</li>`;

        case 'blockquote':
          return `<blockquote class="border-l-4 border-primary pl-3 italic mb-2">${children}</blockquote>`;

        case 'codeBlock':
          return `<pre class="bg-muted p-2 rounded text-xs font-mono mb-2 overflow-x-auto">${children}</pre>`;

        case 'taskList':
          return `<ul class="space-y-1 mb-2">${children}</ul>`;

        case 'taskItem':
          const checked = node.attrs?.checked || false;
          const checkbox = checked
            ? '<input type="checkbox" checked disabled class="mr-2 align-middle"/>'
            : '<input type="checkbox" disabled class="mr-2 align-middle"/>';
          return `<li class="flex items-start">${checkbox}<span>${children}</span></li>`;

        case 'horizontalRule':
          return '<hr class="border-t my-2"/>';

        default:
          return children;
      }
    }

    return '';
  };

  const applyMarks = (text: string, marks?: any[]): string => {
    if (!marks || marks.length === 0) return text;

    let result = text;
    marks.forEach((mark) => {
      switch (mark.type) {
        case 'bold':
          result = `<strong class="font-semibold">${result}</strong>`;
          break;
        case 'italic':
          result = `<em class="italic">${result}</em>`;
          break;
        case 'underline':
          result = `<u class="underline">${result}</u>`;
          break;
        case 'code':
          result = `<code class="bg-muted px-1 py-0.5 rounded text-xs font-mono">${result}</code>`;
          break;
        case 'link':
          const href = mark.attrs?.href || '#';
          result = `<a href="${href}" class="text-primary underline" target="_blank">${result}</a>`;
          break;
        case 'strike':
          result = `<s class="line-through">${result}</s>`;
          break;
      }
    });

    return result;
  };

  const renderContent = (): string => {
    try {
      const json: JSONContent = JSON.parse(content);
      const charCount = { count: 0 };
      return renderNode(json, charCount);
    } catch (error) {
      // Fallback to plain text if JSON parsing fails
      const plainText = content.substring(0, maxLength);
      return plainText.length < content.length
        ? `<p>${plainText}...</p>`
        : `<p>${plainText}</p>`;
    }
  };

  return (
    <div
      className={`prose prose-sm dark:prose-invert max-w-none ${className}`}
      dangerouslySetInnerHTML={{ __html: renderContent() }}
    />
  );
}
