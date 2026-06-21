# Discuss Module Markdown Rendering Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable Markdown rendering and raw URL autolinking for all messages in the discuss module using `react-markdown`.

**Architecture:** We will explicitly add `react-markdown` to the project dependencies, implement a recursive text node autolinking post-processor, and integrate `<ReactMarkdown>` with custom renderers inside `MessageItem.tsx` for optimal styling under both light/dark modes and own/others' bubble states.

**Tech Stack:** React 19, Next.js 15, Tailwind CSS, react-markdown (v9.0.3)

---

### Task 1: Add `react-markdown` Dependency

**Files:**
- Modify: [package.json](file:///d:/User2/open_source/serp/serp_web/package.json)

- [ ] **Step 1: Add package to dependencies**

Modify [package.json](file:///d:/User2/open_source/serp/serp_web/package.json) to add `react-markdown` alphabetically in `dependencies`:
```json
    "react-hot-toast": "^2.6.0",
    "react-leaflet": "^5.0.0",
    "react-markdown": "^9.0.3",
    "react-redux": "^9.2.0",
```

- [ ] **Step 2: Install dependencies**

Run:
```bash
npm install
```
Expected: Installation completes successfully and `package-lock.json` is updated.

- [ ] **Step 3: Verify the build compiles**

Run:
```bash
npm run type-check
```
Expected: No TypeScript compilation errors.

- [ ] **Step 4: Commit changes**

Run:
```bash
git add package.json package-lock.json
git commit -m "chore: add react-markdown to dependencies"
```

---

### Task 2: Implement Autolink and Recursive Linkify Helpers

**Files:**
- Modify: [MessageItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/MessageItem.tsx)

- [ ] **Step 1: Add import and helpers at the top of the file**

Add the imports, the `AutolinkText` component, and the `linkifyChildren` helper function in [MessageItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/MessageItem.tsx) below the existing imports (around line 20):

```tsx
import ReactMarkdown from 'react-markdown';

const URL_REGEX = /(https?:\/\/[^\s]+)/g;

const AutolinkText: React.FC<{ text: string; isOwn: boolean }> = ({ text, isOwn }) => {
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

const linkifyChildren = (children: React.ReactNode, isOwn: boolean): React.ReactNode => {
  return React.Children.map(children, (child) => {
    if (typeof child === 'string') {
      return <AutolinkText text={child} isOwn={isOwn} />;
    }
    return child;
  });
};
```

- [ ] **Step 2: Verify type-check**

Run:
```bash
npm run type-check
```
Expected: Success.

- [ ] **Step 3: Commit changes**

Run:
```bash
git add src/modules/discuss/components/MessageItem.tsx
git commit -m "feat: implement AutolinkText and linkifyChildren helpers"
```

---

### Task 3: Render Messages Using `ReactMarkdown`

**Files:**
- Modify: [MessageItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/MessageItem.tsx)

- [ ] **Step 1: Replace raw message content renderer with ReactMarkdown**

In [MessageItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/MessageItem.tsx), replace lines 167-170:
```tsx
            {/* Content */}
            <p className='text-sm leading-relaxed break-words whitespace-pre-wrap'>
              {message.content}
            </p>
```

With the following configured `<ReactMarkdown>` component:
```tsx
            {/* Content */}
            <ReactMarkdown
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
            >
              {message.content}
            </ReactMarkdown>
```

- [ ] **Step 2: Verify build and lint**

Run:
```bash
npm run build && npm run lint
```
Expected: The project builds successfully and passes eslint checks with no errors.

- [ ] **Step 3: Commit changes**

Run:
```bash
git add src/modules/discuss/components/MessageItem.tsx
git commit -m "feat: render discuss messages using react-markdown with custom styles"
```
