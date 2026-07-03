# Register Form UX/UI Improvement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the UX/UI of the registration form by adding dynamic width transitions and arranging the fields in a 2-column layout.

**Architecture:** Update `AuthLayout.tsx` to dynamically switch the Card container's max-width based on mode with transitions, and update `RegisterForm.tsx` to pair inputs using responsive Tailwind grid grids.

**Tech Stack:** React 19, Tailwind CSS, TypeScript

---

### Task 1: Update AuthLayout Container Width & Transition

**Files:**
- Modify: `serp_web/src/modules/account/components/AuthLayout.tsx:93-131`

- [ ] **Step 1: Modify AuthLayout to support dynamic container max-width with CSS transitions**

Modify [AuthLayout.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/AuthLayout.tsx) to set dynamic max-width classes and a smooth transition.

Code to replace in `AuthLayout.tsx`:
```tsx
      <div className='relative flex flex-1 items-center justify-center px-4 py-10 lg:py-14'>
        <div className={`w-full transition-all duration-300 ease-in-out ${mode === 'login' ? 'max-w-md' : 'max-w-2xl'}`}>
          <Card className='overflow-hidden rounded-[2rem] border-white/70 bg-white/88 py-0 shadow-[0_40px_120px_-48px_rgba(15,23,42,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/78 transition-all duration-300 ease-in-out'>
            <div className='border-b border-border/60 px-6 py-5 sm:px-8'>
              <div className='space-y-2'>
                <h2 className='text-2xl font-semibold tracking-[-0.03em] text-slate-950 dark:text-slate-50'>
                  {mode === 'login' ? 'Sign in' : 'Create account'}
                </h2>
                <p className='text-sm leading-6 text-muted-foreground'>
                  {mode === 'login'
                    ? 'Continue where your team left off with a focused sign-in flow.'
                    : 'Start your organization with a polished onboarding experience.'}
                </p>
              </div>
            </div>

            <CardContent className='space-y-6 px-6 py-6 sm:px-8 sm:py-8'>
              {mode === 'login' ? (
                <LoginForm
                  onSuccess={handleAuthSuccess}
                  onSwitchToRegister={() => handleModeChange('register')}
                />
              ) : (
                <RegisterForm
                  onSuccess={() => handleModeChange('login')}
                  onSwitchToLogin={() => handleModeChange('login')}
                />
              )}

              <Separator />

              <p className='text-center text-xs leading-6 text-muted-foreground'>
                By continuing, you agree to SERP&apos;s terms of service and
                privacy policy.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
```

- [ ] **Step 2: Commit changes**

Run:
```bash
git add serp_web/src/modules/account/components/AuthLayout.tsx
git commit -m "feat: make auth card container dynamically wider for register form"
```

---

### Task 2: Restructure RegisterForm Grid Layout

**Files:**
- Modify: `serp_web/src/modules/account/components/RegisterForm.tsx:148-197`

- [ ] **Step 1: Modify RegisterForm to group email/organization and password/confirm password into 2-column grids**

Update [RegisterForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/RegisterForm.tsx) to pair the input fields.

Code to replace in `RegisterForm.tsx`:
```tsx
      <div className='grid gap-4 sm:grid-cols-2'>
        <AuthInputField
          id='register-email'
          type='email'
          label='Work email'
          placeholder='founder@company.com'
          autoComplete='email'
          leadingIcon={Mail}
          error={errors.email?.message}
          disabled={isBusy}
          {...register('email', { onChange: handleFieldInteraction })}
        />

        <AuthInputField
          id='register-organization'
          label='Organization name'
          placeholder='Northwind Operations'
          autoComplete='organization'
          leadingIcon={Building2}
          error={errors.organizationName?.message}
          disabled={isBusy}
          {...register('organizationName', {
            onChange: handleFieldInteraction,
          })}
        />
      </div>

      <div className='grid gap-4 sm:grid-cols-2'>
        <AuthPasswordField
          id='register-password'
          label='Create password'
          placeholder='Create a strong password'
          autoComplete='new-password'
          hint='8+ chars, upper/lowercase, number'
          error={errors.password?.message}
          disabled={isBusy}
          toggleLabel='Toggle password visibility'
          {...register('password', { onChange: handleFieldInteraction })}
        />

        <AuthPasswordField
          id='register-confirm-password'
          label='Confirm password'
          placeholder='Repeat your password'
          autoComplete='new-password'
          error={errors.confirmPassword?.message}
          disabled={isBusy}
          toggleLabel='Toggle confirm password visibility'
          {...register('confirmPassword', { onChange: handleFieldInteraction })}
        />
      </div>
```

- [ ] **Step 2: Commit changes**

Run:
```bash
git add serp_web/src/modules/account/components/RegisterForm.tsx
git commit -m "feat: restructure register form fields into horizontal grid pairs"
```

---

### Task 3: Build & Quality Verification

- [ ] **Step 1: Run linter checks**

Run command in `serp_web` directory:
```bash
npm run lint
```
Expected output: No linting errors.

- [ ] **Step 2: Run TypeScript type-check**

Run command in `serp_web` directory:
```bash
npm run type-check
```
Expected output: No type errors.

- [ ] **Step 3: Run production build to ensure bundle correctness**

Run command in `serp_web` directory:
```bash
npm run build
```
Expected output: Production build completes successfully.
