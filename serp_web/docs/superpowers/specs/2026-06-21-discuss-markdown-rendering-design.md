# Discuss Module Markdown Rendering Design Spec

**Author:** Antigravity (Advanced Agentic Coding team)  
**Date:** 2026-06-21  
**Status:** Approved  

---

## 1. Goal

Enable Markdown rendering by default for all chat messages in the `discuss` module of `serp_web`. This includes formatting syntax such as headers, lists, code (inline and block), bold, italic, and auto-linking raw URLs (e.g. `https://example.com`), while ensuring absolute security against XSS.

---

## 2. Requirements

- **Markdown Support:** Enable basic Markdown syntax (Bold, Italic, Links, Lists, Code blocks, Headers) for all text messages.
- **Autolink:** Automatically detect raw URLs in text nodes and convert them to clickable anchor tags, except when they are inside code elements.
- **Styling:** Custom CSS styling optimized for both own messages (gradient violet-fuchsia background) and other users' messages (white/dark-slate background) to guarantee high legibility and professional aesthetics.
- **Security:** Standard HTML tags typed by users must be rendered as text and not interpreted as HTML, preventing XSS vectors.

---

## 3. Architecture & Components

We will integrate `react-markdown` to parse the message content and render JSX elements. To achieve autolinking without importing heavy GFM plugins, we will perform post-processing on the parsed React text nodes.

### 3.1 Autolink Helper
A utility component `AutolinkText` will parse plain text strings, matching URLs using a regex, and splitting them into normal text segments and anchor tags:

```tsx
const URL_REGEX = /(https?:\/\/[^\s]+)/g;

export const AutolinkText: React.FC<{ text: string; isOwn: boolean }> = ({ text, isOwn }) => {
  if (!text) return null;
  const parts = text.split(URL_REGEX);
  
  return (
    <>
      {parts.map((part, index) => {
        if (part.match(URL_REGEX)) {
          return (
            <a
              key={index}
              href={part}
              target="_blank"
              rel="noopener noreferrer"
              className={cn(
                "underline font-medium hover:opacity-90 transition-opacity break-all",
                isOwn ? "text-white" : "text-violet-600 dark:text-violet-400"
              )}
            >
              {part}
            </a>
          );
        }
        return <span key={index}>{part}</span>;
      })}
    </>
  );
};
```

### 3.2 Recursive Children Processor
A recursive function `linkifyChildren` will walk through parsed React children from `react-markdown` and wrap leaf text nodes with `<AutolinkText>` if they are string types:

```tsx
const linkifyChildren = (children: React.ReactNode, isOwn: boolean): React.ReactNode => {
  return React.Children.map(children, (child) => {
    if (typeof child === 'string') {
      return <AutolinkText text={child} isOwn={isOwn} />;
    }
    return child;
  });
};
```

---

## 4. UI Rendering & Styling System

The `<ReactMarkdown>` component in [MessageItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/MessageItem.tsx) will be configured with a custom mapping for elements:

```tsx
components={{
  p: ({ children }) => (
    <span className="block text-sm leading-relaxed break-words whitespace-pre-wrap">
      {linkifyChildren(children, isOwn)}
    </span>
  ),
  strong: ({ children }) => <strong className="font-semibold">{children}</strong>,
  em: ({ children }) => <em className="italic">{children}</em>,
  code: ({ node, className, children, ...props }) => {
    const isBlock = className || (typeof children === 'string' && children.includes('\n'));
    return isBlock ? (
      <pre className="bg-slate-950 text-slate-50 p-3 rounded-xl border border-slate-800 dark:border-slate-700/50 font-mono text-xs overflow-x-auto my-2">
        <code>{children}</code>
      </pre>
    ) : (
      <code
        className={cn(
          "px-1.5 py-0.5 rounded text-[13px] font-mono",
          isOwn
            ? "bg-white/20 text-white border border-white/10"
            : "bg-slate-100 dark:bg-slate-900 text-rose-600 dark:text-rose-400 border border-slate-200 dark:border-slate-800"
        )}
      >
        {children}
      </code>
    );
  },
  a: ({ href, children }) => (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className={cn(
        "underline font-medium hover:opacity-90 transition-opacity break-all",
        isOwn ? "text-white" : "text-violet-600 dark:text-violet-400"
      )}
    >
      {children}
    </a>
  ),
  ul: ({ children }) => (
    <ul className={cn(
      "list-disc pl-5 space-y-1 my-1.5",
      isOwn ? "marker:text-white" : "marker:text-slate-500"
    )}>
      {children}
    </ul>
  ),
  ol: ({ children }) => (
    <ol className={cn(
      "list-decimal pl-5 space-y-1 my-1.5",
      isOwn ? "marker:text-white" : "marker:text-slate-500"
    )}>
      {children}
    </ol>
  ),
  li: ({ children }) => (
    <li className="text-sm">
      {linkifyChildren(children, isOwn)}
    </li>
  ),
}}
```

---

## 5. Security & Edge Cases

1. **XSS Protection:** `react-markdown` uses virtual DOM structure creation and does not execute scripts. Custom elements are directly instantiated as React nodes, which keeps it safe.
2. **Missing closing markdown tokens:** If raw unclosed tokens are present (e.g. `**bold text`), they fallback gracefully to plain strings.
3. **URLs in code elements:** Because `AutolinkText` is only invoked inside custom renderers for tags that can contain prose (e.g., `p`, `li`) and explicitly skips code components, raw URLs within code blocks or inline code tags are rendered as plain code strings.
