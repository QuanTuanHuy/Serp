# First-Time Login Pricing Guide Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modify the welcome card on the `/home` dashboard page to display a beautiful onboarding banner for organization admins who haven't subscribed to any plan yet, guiding them to `/subscription`.

**Architecture:** We will compute a boolean `hasBusinessModules` based on active user modules. If the user is an Organization Admin, has no active business modules, and has not dismissed the guide (stored in LocalStorage), we render the onboarding version of the welcome Hero Card. Clicking "Dismiss" or "Skip for now" persists the choice and updates the UI immediately.

**Tech Stack:** React 19, Next.js 15, Lucide Icons, LocalStorage.

---

### Task 1: Update Imports and State in HomePage

**Files:**
- Modify: [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx#L1-L36) (Add imports and state)

- [ ] **Step 1: Add `useState` to React import and import Lucide icons**

Update lines 8-10 of [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx#L8-L10) to import `useState`.
Update lines 26-35 of [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx#L26-L35) to import `Sparkles` and `X` from `lucide-react`.

Code change to apply:
```typescript
import { useEffect, useMemo, useState } from 'react';
```
```typescript
import {
  AlertTriangle,
  ArrowRight,
  Compass,
  Loader2,
  type LucideIcon,
  Settings2,
  Shield,
  Sparkles,
  UserRound,
  X,
} from 'lucide-react';
```

- [ ] **Step 2: Commit**

```bash
git add src/app/home/page.tsx
git commit -m "feat(home): add react useState and lucide-react icon imports for onboarding"
```

---

### Task 2: Implement Logic for Onboarding Detection

**Files:**
- Modify: [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx#L169-L215) (Add state and computed properties)

- [ ] **Step 1: Add `isDismissed` state and computed properties**

Initialize state `isDismissed` inside `HomePage()`.
Read initial value in `useEffect` to avoid SSR mismatch.
Define `hasBusinessModules` using `useMemo` over `modules`.
Implement `handleDismiss` to store the value in `localStorage`.

Code to inject inside `HomePage` components:
```typescript
  const [isDismissed, setIsDismissed] = useState(false);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const skipOnboarding = localStorage.getItem('serp_skip_pricing_onboarding');
      if (skipOnboarding === 'true') {
        setIsDismissed(true);
      }
    }
  }, []);

  const hasBusinessModules = useMemo(() => {
    return modules.some((m) => !m.isAdmin);
  }, [modules]);

  const handleDismiss = () => {
    localStorage.setItem('serp_skip_pricing_onboarding', 'true');
    setIsDismissed(true);
  };
```

- [ ] **Step 2: Commit**

```bash
git add src/app/home/page.tsx
git commit -m "feat(home): implement state and logic to detect unsubscribed admins"
```

---

### Task 3: Conditionally Render Onboarding Welcome Card

**Files:**
- Modify: [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx#L292-L320) (Update Hero card rendering)

- [ ] **Step 1: Conditional rendering of CardContent**

Update the `<section>` that renders the main welcome card so that it shows the onboarding guide if:
`roleFlags.isOrgAdmin && !hasBusinessModules && !isDismissed`
Otherwise, it renders the standard card content.

Code modification:
```typescript
          <section>
            <Card className='relative overflow-hidden border-border/60 bg-background/90 shadow-lg'>
              {roleFlags.isOrgAdmin && !hasBusinessModules && !isDismissed ? (
                <>
                  <Button
                    variant='ghost'
                    size='icon'
                    className='absolute top-5 right-5 h-8 w-8 rounded-full hover:bg-muted/50'
                    onClick={handleDismiss}
                  >
                    <X className='h-4 w-4 text-muted-foreground' />
                    <span className='sr-only'>Dismiss guide</span>
                  </Button>
                  <CardContent className='p-6 md:p-8'>
                    <div className='max-w-3xl'>
                      <div className='inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary mb-3'>
                        <Sparkles className='h-3 w-3 animate-pulse' />
                        Get Started
                      </div>
                      <h1 className='text-3xl font-semibold tracking-tight sm:text-4xl'>
                        Hi {overview.greetingName}, let&apos;s activate your workspace.
                      </h1>
                      <p className='mt-3 text-base leading-6 text-muted-foreground max-w-2xl'>
                        Your organization doesn&apos;t have an active subscription plan yet. Choose a plan now to enable modules like CRM, Sales, Logistics, and Project Management for your team.
                      </p>
                    </div>

                    <div className='mt-8 flex flex-col gap-3 sm:flex-row'>
                      <Button asChild size='lg' className='gap-2 bg-slate-950 text-white hover:bg-slate-800 dark:bg-slate-50 dark:text-slate-950 dark:hover:bg-slate-200'>
                        <Link href='/subscription'>
                          View Pricing Plans
                          <ArrowRight className='h-4 w-4' />
                        </Link>
                      </Button>
                      <Button asChild variant='outline' size='lg'>
                        <Link href='/apps'>Explore App Catalog</Link>
                      </Button>
                    </div>
                  </CardContent>
                </>
              ) : (
                <CardContent className='p-6 md:p-8'>
                  <div className='max-w-3xl'>
                    <p className='text-sm font-medium text-primary'>
                      Welcome back
                    </p>
                    <h1 className='mt-2 text-3xl font-semibold tracking-tight sm:text-4xl'>
                      Hi {overview.greetingName}, your modules are now the center
                      of the workspace.
                    </h1>
                  </div>

                  <div className='mt-8 flex flex-col gap-3 sm:flex-row'>
                    <Button asChild size='lg' className='gap-2'>
                      <Link href={overview.firstAvailableModule?.href ?? '/apps'}>
                        {overview.firstAvailableModule
                          ? `Open ${overview.firstAvailableModule.name}`
                          : 'Explore applications'}
                        <ArrowRight className='h-4 w-4' />
                      </Link>
                    </Button>
                    <Button asChild variant='outline' size='lg'>
                      <Link href='/apps'>Browse all apps</Link>
                    </Button>
                  </div>
                </CardContent>
              )}
            </Card>
          </section>
```

- [ ] **Step 2: Commit**

```bash
git add src/app/home/page.tsx
git commit -m "feat(home): render onboarding guide when unsubscribed admin is on first login"
```

---

### Task 4: Linting & Build Verification

- [ ] **Step 1: Run type checking**

Run in `serp_web`: `npm run type-check` (or `npx tsc --noEmit`)
Verify no compilation errors.

- [ ] **Step 2: Run linter**

Run in `serp_web`: `npm run lint`
Verify no linting errors.

- [ ] **Step 3: Run production build**

Run in `serp_web`: `npm run build`
Verify build succeeds.
