# PM Duration Estimate Input Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let PM work item estimate fields accept Jira-style duration strings while still sending minutes to backend.

**Architecture:** Add one PM-local duration helper that parses and formats estimates using Jira defaults (`1w = 5d`, `1d = 8h`, `1h = 60m`). Use it from create form request building and detail inline estimate editor.

**Tech Stack:** Next.js 15, React 19, TypeScript, react-hook-form, zod, RTK Query.

---

### Task 1: Add duration helper

**Files:**

- Create: `serp_web/src/modules/pm/utils/durationEstimate.ts`

- [ ] **Step 1: Create parser/formatter**

```ts
export const ESTIMATE_MINUTES_PER_HOUR = 60;
export const ESTIMATE_HOURS_PER_DAY = 8;
export const ESTIMATE_DAYS_PER_WEEK = 5;

const UNIT_TO_MINUTES = {
  w:
    ESTIMATE_DAYS_PER_WEEK * ESTIMATE_HOURS_PER_DAY * ESTIMATE_MINUTES_PER_HOUR,
  d: ESTIMATE_HOURS_PER_DAY * ESTIMATE_MINUTES_PER_HOUR,
  h: ESTIMATE_MINUTES_PER_HOUR,
  m: 1,
} as const;

export function parseDurationEstimate(value: string): number | null {
  const trimmed = value.trim().toLowerCase();
  if (!trimmed) return null;

  if (/^\d+$/.test(trimmed)) {
    return Number(trimmed);
  }

  const compact = trimmed.replace(/\s+/g, "");
  const tokenPattern = /(\d+)([wdhm])/g;
  let total = 0;
  let consumed = "";
  let match: RegExpExecArray | null;

  while ((match = tokenPattern.exec(compact)) !== null) {
    const amount = Number(match[1]);
    const unit = match[2] as keyof typeof UNIT_TO_MINUTES;
    total += amount * UNIT_TO_MINUTES[unit];
    consumed += match[0];
  }

  if (consumed !== compact) return Number.NaN;
  return total;
}

export function formatDurationEstimate(value?: number | null): string {
  if (value === null || value === undefined) return "None";

  let remaining = Math.round(value);
  const weeks = Math.floor(remaining / UNIT_TO_MINUTES.w);
  remaining -= weeks * UNIT_TO_MINUTES.w;
  const days = Math.floor(remaining / UNIT_TO_MINUTES.d);
  remaining -= days * UNIT_TO_MINUTES.d;
  const hours = Math.floor(remaining / UNIT_TO_MINUTES.h);
  const minutes = remaining - hours * UNIT_TO_MINUTES.h;

  const parts = [
    weeks ? `${weeks}w` : "",
    days ? `${days}d` : "",
    hours ? `${hours}h` : "",
    minutes ? `${minutes}m` : "",
  ].filter(Boolean);

  return parts.length ? parts.join(" ") : "0m";
}
```

- [ ] **Step 2: Verify helper compiles indirectly**

Run after integration: `npm run type-check` from `serp_web/`.

### Task 2: Use duration input in create dialog

**Files:**

- Modify: `serp_web/src/modules/pm/components/work-items/createWorkItemForm.ts`
- Modify: `serp_web/src/modules/pm/components/work-items/CreateWorkItemDialog.tsx`

- [ ] **Step 1: Parse form value before request**

Import `parseDurationEstimate` from `../../utils/durationEstimate` and change `timeOriginalEstimate` request mapping to:

```ts
timeOriginalEstimate: values.timeOriginalEstimate
  ? parseDurationEstimate(values.timeOriginalEstimate) ?? undefined
  : undefined,
```

- [ ] **Step 2: Change input from number to text**

Change create dialog estimate input to:

```tsx
<Input placeholder="2w 4d 6h 45m" {...field} />
```

Change description to:

```tsx
Use Jira-style duration. Example: 2w 4d 6h 45m.
```

### Task 3: Use duration input in detail inline editor

**Files:**

- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemInlineEditors.tsx`

- [ ] **Step 1: Import helper**

```ts
import {
  formatDurationEstimate,
  parseDurationEstimate,
} from "../../../utils/durationEstimate";
```

- [ ] **Step 2: Display compact duration**

Replace display text:

```tsx
{
  formatDurationEstimate(value);
}
```

- [ ] **Step 3: Edit as text duration**

Initialize draft with `formatDurationEstimate(value)` except null/undefined as empty string. Parse with `parseDurationEstimate`, reject `Number.NaN` or negative values, and use placeholder `2w 4d 6h 45m`.

### Task 4: Verify

**Files:**

- No source changes.

- [ ] **Step 1: Run lint**

Run from `serp_web/`: `npm run lint`

- [ ] **Step 2: Run type check**

Run from `serp_web/`: `npm run type-check`

- [ ] **Step 3: Run format check**

Run from `serp_web/`: `npm run format:check`

Expected: all commands pass.
