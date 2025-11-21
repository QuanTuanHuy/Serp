/**
 * PTM v2 - Note Content Helper
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Helper functions for note content processing
 */

import type { JSONContent } from 'novel';

/**
 * Extract plain text from Tiptap JSON content for search and preview
 */
export function extractPlainTextFromJSON(jsonContent: string): string {
  try {
    const json: JSONContent = JSON.parse(jsonContent);

    const getText = (node: JSONContent): string => {
      // Text node
      if (node.type === 'text') {
        return node.text || '';
      }

      // Node with children
      if (node.content && Array.isArray(node.content)) {
        const texts = node.content.map(getText).filter(Boolean);

        // Add line breaks for block elements
        if (
          node.type === 'paragraph' ||
          node.type === 'heading' ||
          node.type === 'codeBlock'
        ) {
          return texts.join(' ') + '\n';
        }

        return texts.join(' ');
      }

      return '';
    };

    const plainText = getText(json).trim();

    // Limit length for preview (200 chars)
    if (plainText.length > 200) {
      return plainText.substring(0, 200) + '...';
    }

    return plainText;
  } catch (error) {
    // If parsing fails, treat as plain text
    console.warn('Failed to parse note content as JSON:', error);
    const cleaned = jsonContent.replace(/[#*`_\[\]()]/g, '');
    return cleaned.length > 200 ? cleaned.substring(0, 200) + '...' : cleaned;
  }
}

/**
 * Check if content is valid Tiptap JSON
 */
export function isValidTiptapJSON(content: string): boolean {
  try {
    const json = JSON.parse(content);
    return json.type === 'doc' && Array.isArray(json.content);
  } catch {
    return false;
  }
}

/**
 * Convert markdown to Tiptap JSON (basic conversion)
 */
export function markdownToTiptapJSON(markdown: string): string {
  const lines = markdown.split('\n');
  const content: any[] = [];

  for (const line of lines) {
    if (!line.trim()) {
      content.push({ type: 'paragraph' });
      continue;
    }

    // Heading
    if (line.startsWith('### ')) {
      content.push({
        type: 'heading',
        attrs: { level: 3 },
        content: [{ type: 'text', text: line.slice(4) }],
      });
    } else if (line.startsWith('## ')) {
      content.push({
        type: 'heading',
        attrs: { level: 2 },
        content: [{ type: 'text', text: line.slice(3) }],
      });
    } else if (line.startsWith('# ')) {
      content.push({
        type: 'heading',
        attrs: { level: 1 },
        content: [{ type: 'text', text: line.slice(2) }],
      });
    } else {
      // Regular paragraph
      content.push({
        type: 'paragraph',
        content: [{ type: 'text', text: line }],
      });
    }
  }

  return JSON.stringify({
    type: 'doc',
    content: content.length > 0 ? content : [{ type: 'paragraph' }],
  });
}
