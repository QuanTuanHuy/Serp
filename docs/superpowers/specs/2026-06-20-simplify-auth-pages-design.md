# Design Spec: Simplify Register & Login Pages and Unify Background Style

**Author:** Antigravity (AI Coding Assistant)  
**Date:** 2026-06-20  
**Status:** Proposed  
**Topic:** Simplifying standalone authentication views (`LoginForm`, `RegisterForm`, `AuthLayout`) and aligning background/aesthetic design with the Main Home Page (`src/app/home/page.tsx`).

---

## 1. Objectives & Context

Currently, the authentication page (`/auth`) uses a split layout:
- **Left Column:** Product values, trust marks, and secondary illustrations/pillars.
- **Right Column:** A large card enclosing a `<Tabs>` component to switch between Sign in and Create Account.

We want to simplify the user experience (UX) to follow modern SaaS best practices (such as Stripe, Vercel, and GitHub):
1. **Simplified Centered Layout:** Restructure the layout to a single-column, centered card model, removing the secondary marketing pillars on the left.
2. **Link-based Navigation:** Replace the tab bar with subtle text links below the form actions (e.g., *"Don't have an account? Sign up"* or *"Already have an account? Sign in"*).
3. **Unified Aesthetics:** Make the background container, gradients, and blur filters exactly match the Main Home Page (`src/app/home/page.tsx`).

---

## 2. Architectural & Component Changes

The structural edits will be isolated to the following components in `@/modules/account` inside `serp_web`:

### Modified Components

1. **`AuthLayout.tsx` (file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/AuthLayout.tsx)**
   - Remove the split grid layout (`lg:grid-cols-[1.08fr_0.92fr]`).
   - Remove the left-side section containing `TRUST_MARKERS` and `VALUE_PILLARS`.
   - Update the root `main` styling and replace the background decoration with the identical gradient and blur circles used in `src/app/home/page.tsx`:
     - Base classes: `min-h-screen bg-background relative overflow-hidden flex flex-col justify-between`
     - Gradient overlay: `<div className="absolute inset-x-0 top-0 -z-10 h-[28rem] bg-gradient-to-b from-primary/10 via-background to-background" />`
     - Blurry circles:
       - Left: `<div className="absolute left-0 top-24 -z-10 h-64 w-64 rounded-full bg-primary/10 blur-3xl" />`
       - Right: `<div className="absolute right-0 top-16 -z-10 h-72 w-72 rounded-full bg-sky-500/10 blur-3xl" />`
   - Center the Card (`max-w-md w-full mx-auto shadow-xl`).
   - Remove the `<Tabs>` control. Render `<LoginForm>` or `<RegisterForm>` directly depending on `mode` (using conditional rendering).
   - Streamline the Card header to only show "Sign in" or "Create account" and a short description.
   - Clean up the custom header (top logo and ThemeToggle).

2. **`LoginForm.tsx` (file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/LoginForm.tsx)**
   - Remove internal redundant headers.
   - Integrate/ensure clean styling for inputs.
   - Use the `onSwitchToRegister` callback at the bottom as a link: *"Need a brand new workspace? **Create account**"*.

3. **`RegisterForm.tsx` (file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/RegisterForm.tsx)**
   - Remove internal redundant headers.
   - Use the `onSwitchToLogin` callback at the bottom as a link: *"Already have access to a workspace? **Sign in**"*.

---

## 3. Data Flow & State Management

The authentication logic (using the `useAuth` hook and its `login`/`register` actions) remains identical. The routing behavior using `router.replace` to append `?mode=register` also stays intact, preserving all query params, session validations, and redirect hooks.

---

## 4. Verification & Testing

### Automated Checks
Run the linting, formatting, and type checks from `serp_web/` directory:
- `npm run lint`
- `npm run type-check`
- `npm run build` (Ensures that Next.js client-side/server-side code builds perfectly with no missing dependencies).

### Manual Verification
- Access `/auth` locally. Verify that the login card is perfectly centered, with the identical background aesthetics as `/home`.
- Click the "Create account" link at the bottom of the Card and verify smooth toggle to the Register Form without layout shifts.
- Click "Back to sign in" to switch back.
- Test Theme Toggle (Dark/Light Mode) on the authentication page.
