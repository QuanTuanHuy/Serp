# PTM v2 - Novel Rich Text Editor Implementation

## Overview

This implementation integrates [Novel](https://novel.sh) - a Notion-style WYSIWYG editor built on top of Tiptap and TailwindCSS - into the PTM v2 note management system.

## Features

### ✨ Rich Text Editing

- **Slash Commands**: Type `/` to open a command palette with formatting options
- **Task Lists**: Create interactive to-do lists with checkboxes
- **Code Blocks**: Syntax-highlighted code blocks with support for multiple languages
- **Images**: Drag and drop image support with resizing capabilities
- **Links**: Auto-detected URLs with custom link formatting
- **Typography**: Bold, italic, underline, headings (H1-H3), quotes, and more

### 🎨 Styled Components

All Tiptap extensions are configured with Tailwind CSS classes for consistent dark mode support:

- Bullet and numbered lists with proper indentation
- Styled blockquotes with left border accent
- Code blocks with background and border styling
- Task items with proper checkbox alignment
- Horizontal rules for content separation

### ⌨️ Keyboard Shortcuts

- `/` - Open slash command palette
- `Ctrl/Cmd + B` - Bold
- `Ctrl/Cmd + I` - Italic
- `Ctrl/Cmd + U` - Underline
- `Ctrl/Cmd + K` - Add link
- `#` + `Space` - Heading 1
- `##` + `Space` - Heading 2
- `###` + `Space` - Heading 3
- `-` + `Space` - Bullet list
- `1.` + `Space` - Numbered list
- `[]` + `Space` - Task list
- `` ` `` - Inline code
- ` ``` ` - Code block

## File Structure

```
notes/
├── extensions.ts              # Tiptap extension configurations
├── slash-command.tsx          # Slash command menu items
├── prosemirror.css           # Prosemirror editor styles
├── NoteEditorNovel.tsx       # Main Novel editor component
├── NoteEditor.tsx            # Legacy markdown editor (fallback)
├── NoteCard.tsx              # Note display card
├── NoteList.tsx              # Notes list with search
└── index.ts                  # Barrel exports
```

## Components

### NoteEditorNovel

The main editor component with Novel integration.

**Props:**

- `initialContent?: string` - JSON content from previous save
- `initialPinned?: boolean` - Whether note is pinned
- `onSave: (content: string, isPinned: boolean) => void` - Save callback
- `onCancel?: () => void` - Cancel callback
- `placeholder?: string` - Placeholder text

**Features:**

- Autosave with 500ms debounce
- JSON content storage (Tiptap JSONContent format)
- Pin/unpin notes
- Slash command palette
- Full keyboard navigation

### Extensions Configuration (`extensions.ts`)

Configures all Tiptap extensions with Tailwind styling:

- `StarterKit` - Basic editor functionality (bold, italic, lists, etc.)
- `Placeholder` - Dynamic placeholder text
- `TiptapLink` - Link handling with styled attributes
- `TiptapImage` - Image upload and display
- `UpdatedImage` - Enhanced image with resize handles
- `TaskList` & `TaskItem` - Interactive checkboxes
- `HorizontalRule` - Divider lines

### Slash Commands (`slash-command.tsx`)

Defines available slash command menu items:

1. **Text** - Plain paragraph
2. **To-do List** - Task list with checkboxes
3. **Heading 1-3** - Different heading levels
4. **Bullet List** - Unordered list
5. **Numbered List** - Ordered list
6. **Quote** - Blockquote
7. **Code** - Code block
8. **Divider** - Horizontal rule

## Usage

### Basic Implementation

```tsx
import { NoteEditorNovel } from '@/modules/ptmv2/components/notes';

function MyComponent() {
  const handleSave = async (content: string, isPinned: boolean) => {
    // Save content (JSON format) to backend
    await saveNote({ content, isPinned });
  };

  return (
    <NoteEditorNovel
      initialContent={existingNote?.content}
      initialPinned={existingNote?.isPinned}
      onSave={handleSave}
      onCancel={() => setEditing(false)}
    />
  );
}
```

### Content Format

Novel stores content in Tiptap's JSONContent format:

```json
{
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": { "level": 1 },
      "content": [{ "type": "text", "text": "My Note" }]
    },
    {
      "type": "paragraph",
      "content": [{ "type": "text", "text": "Some content..." }]
    },
    {
      "type": "taskList",
      "content": [
        {
          "type": "taskItem",
          "attrs": { "checked": false },
          "content": [
            {
              "type": "paragraph",
              "content": [{ "type": "text", "text": "Task 1" }]
            }
          ]
        }
      ]
    }
  ]
}
```

### Retrieving Plain Text

To display note previews (like in NoteCard), convert JSON to plain text:

```tsx
// Backend should provide contentPlain alongside content
const plainText = editor.getText(); // Gets all text without formatting
```

## Styling

### TailwindCSS Configuration

The editor uses Tailwind's typography plugin for prose styling. Ensure your `tailwind.config.ts` includes:

```typescript
plugins: [
  require('@tailwindcss/typography'),
  // ... other plugins
];
```

### Dark Mode Support

All extensions are configured with `dark:` variants for seamless dark mode:

- Code blocks: `dark:bg-muted`
- Text: `dark:prose-invert`
- Borders: `dark:border-muted`

### Custom Styling

Modify `prosemirror.css` for custom Prosemirror styles:

- Placeholder colors
- Selection highlight
- Task checkbox appearance
- Image border radius
- Drag handle visibility

## Backend Integration

### Recommended API Structure

```typescript
// Note entity (backend)
interface Note {
  id: string;
  content: string;          // JSON string (Tiptap JSONContent)
  contentPlain: string;     // Plain text for search/preview
  isPinned: boolean;
  taskId?: string;
  projectId?: string;
  createdAt: Date;
  updatedAt: Date;
}

// Create note endpoint
POST /api/notes
{
  "content": "{ \"type\": \"doc\", ... }",
  "contentPlain": "Plain text version",
  "isPinned": false,
  "taskId": "task-123"
}
```

### Content Processing

When saving, process both JSON and plain text:

```typescript
import { extractPlainTextFromJSON } from '@/modules/ptmv2/components/notes';

const handleSave = async (content: string, isPinned: boolean) => {
  // Content is already JSON string from Novel editor
  const plainText = extractPlainTextFromJSON(content);

  await createNote({
    content, // Store JSON for full editing capability
    contentPlain: plainText, // Plain text generated on backend or frontend
    isPinned,
  });
};
```

**Important:** The `content` field stores the full Tiptap JSONContent, while `contentPlain` is used for:

- Search functionality
- Preview in note cards
- Quick display without parsing JSON

The backend should generate `contentPlain` from `content` automatically, but you can also use the `extractPlainTextFromJSON` helper on the frontend.

## Migration from NoteEditor

The legacy `NoteEditor` component (markdown-based) is still available as a fallback. To migrate existing markdown notes:

1. **Keep both editors available** - Let users choose their preference
2. **Convert on first edit** - When user opens old note in Novel editor, convert markdown to JSON
3. **Provide export** - Allow exporting Novel notes back to markdown if needed

## Troubleshooting

### Slash Command Not Working

- Ensure `handleCommandNavigation` is passed to `editorProps.handleDOMEvents.keydown`
- Check that `slashCommand` extension is included in extensions array

### Styles Not Applied

- Import `prosemirror.css` in component
- Verify Tailwind typography plugin is installed
- Check prose classes are applied to editor

### Content Not Saving

- Ensure `onUpdate` callback uses debounced version
- Verify JSON.stringify doesn't throw on editor.getJSON()
- Check autosave state updates correctly

### Images Not Loading

- Configure image upload handler (not implemented yet)
- Set proper CORS headers for image URLs
- Use UpdatedImage extension for resize handles

## Future Enhancements

- [ ] Image upload to cloud storage (S3, Cloudinary)
- [ ] Collaborative editing with Y.js
- [ ] Comments and mentions (@user)
- [ ] Custom keyboard shortcuts
- [ ] Export to PDF/Markdown
- [ ] Version history
- [ ] Templates for common note types
- [ ] AI-powered autocomplete
- [ ] Math equations (KaTeX)
- [ ] Embedded videos (YouTube)

## References

- [Novel Documentation](https://novel.sh/docs)
- [Tiptap Documentation](https://tiptap.dev)
- [Novel GitHub Repository](https://github.com/steven-tey/novel)
- [Example Implementation](https://github.com/steven-tey/novel/blob/main/apps/web/components/tailwind/advanced-editor.tsx)

## Author

**QuanTuanHuy**  
Part of SERP Project - Smart ERP System
