# Spec: First-Time Login Pricing Guide for Org Admins

Date: 2026-07-04
Author: Antigravity (AI Coding Assistant)
Status: Approved by User

## Background & Goal

When a new user registers an organization and logs in for the first time in `serp_web`, they land on the Home page (`/home`). Since they do not have any active subscriptions or functional modules yet, they need to be guided to the **Pricing** page (`/subscription`) to choose a plan and activate their workspace.

This feature modifies the main welcome Hero Card on `/home` to show an onboarding guide specifically for Organization Admins who have no active functional modules yet.

---

## Design Details

### 1. Visibility Logic

The onboarding variant of the Hero Card is displayed if **all** of the following conditions are met:
1. **User Role**: The user has the `isOrgAdmin` role flag set to `true`.
2. **No Business Modules**: The organization has no active business/functional modules. That is, the modules returned by `useModules()` do not contain any items where `isAdmin` is `false` (e.g., no CRM, Sales, Logistics, PM modules are active yet; only `SETTINGS` is active).
   * Logic: `const hasBusinessModules = modules.some(m => !m.isAdmin);`
   * Target state: `!hasBusinessModules`
3. **Not Dismissed**: The user has not previously dismissed this guide.
   * State check: `localStorage.getItem('serp_skip_pricing_onboarding') !== 'true'`

### 2. UI Component Changes

The main Welcome Card in `src/app/home/page.tsx` will be conditionally updated:

* **Header Tag**: Displays "Get Started" with a `Sparkles` icon next to it, instead of "Welcome back".
* **Heading text**: `"Hi {Name}, let's activate your workspace."`
* **Description text**: `"Your organization doesn't have an active subscription plan yet. Choose a plan now to enable modules like CRM, Sales, Logistics, and Project Management for your team."`
* **CTAs**:
  * **Primary Button**: `"View Pricing Plans"` (solid button pointing to `/subscription`).
  * **Secondary Button**: `"Explore App Catalog"` (outline/ghost button pointing to `/apps`).
* **Dismiss Button**: An `x` icon in the top-right corner of the Card (`absolute top-5 right-5`). Clicking this:
  * Sets the LocalStorage key `serp_skip_pricing_onboarding` to `'true'`.
  * Triggers a state update `setIsDismissed(true)` to transition the UI back to the standard welcome layout immediately.

---

## Proposed Changes

### [MODIFY] [page.tsx](file:///d:/User2/open_source/serp/serp_web/src/app/home/page.tsx)
* Add `isDismissed` state, initialized using a `useEffect` reading from `localStorage`.
* Extract `hasBusinessModules` check from `modules`.
* Add custom render conditions inside the Hero Card section to display the onboarding text and buttons when:
  `roleFlags.isOrgAdmin && !hasBusinessModules && !isDismissed`
* Add an absolute-positioned Close/Dismiss button in the top-right corner of the Welcome Card when the onboarding mode is active.
* Implement the click handler for the Dismiss action (updates state and sets `localStorage`).

---

## Verification Plan

### Automated Verification
* Run linter: `npm run lint` inside `serp_web`.
* Run build check: `npm run build` inside `serp_web`.

### Manual Verification
1. Log in with an admin account that has active business modules. Verify that the welcome card displays standard text and no onboarding elements.
2. Log in with a new admin account (having no business modules active). Verify that the card displays the onboarding state ("let's activate your workspace", "View Pricing Plans", Close button).
3. Click the Close button. Verify that the card transitions to the standard workspace welcome card instantly.
4. Refresh the page and verify that the onboarding state remains hidden (the dismiss preference is persisted).
