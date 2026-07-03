# Register Form UX/UI Design (Dynamic Width & Grid Layout)

**Author**: Antigravity
**Date**: 2026-07-03
**Status**: Proposal (Approved by User)

## Goal

Improve the UX/UI of the registration form in the `serp_web` frontend. The current single-column layout on a narrow `max-w-md` container feels cluttered and tight due to the amount of information required for organization onboarding. We want a cleaner, simpler, and more modern layout.

## Design Decisions

1. **Dynamic Container Width**:
   - The auth container Card in `AuthLayout.tsx` will dynamically adjust its maximum width based on whether the user is in `login` or `register` mode.
   - Login mode: `max-w-md` (448px), optimized for a focused, 2-field form.
   - Register mode: `max-w-2xl` (672px), optimized for a spacious, multi-column form.
   - Animation: Use `transition-all duration-300 ease-in-out` on the card to smoothly animate width changes.

2. **Balanced 2-Column Grid Layout for Register Form**:
   - To reduce the vertical height of the registration form, pair input fields side-by-side on screens `sm` (640px) and up.
   - **Row 1**: First Name & Last Name (already 2 columns, will keep).
   - **Row 2**: Work Email & Organization Name (changed from 1 column to 2 columns).
   - **Row 3**: Create Password & Confirm Password (changed from 1 column to 2 columns).
   - **Full Width Elements**: Password strength meter, error alert, "Create account" submit button, and the sign-in transition footer link will span across both columns (`w-full` / `sm:col-span-2`).

## Proposed Changes

### Component: `serp_web` Account Module

#### [MODIFY] [AuthLayout.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/AuthLayout.tsx)
- Dynamically toggle the container class between `max-w-md` and `max-w-2xl` based on `mode`.
- Add `transition-all duration-300 ease-in-out` to the outer `Card` element to ensure a smooth transition.

#### [MODIFY] [RegisterForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/account/components/RegisterForm.tsx)
- Reorganize input fields into horizontal grid rows:
  - First Name & Last Name: `<div className="grid gap-4 sm:grid-cols-2">` (no change needed here).
  - Work Email & Organization Name: Move them inside a single `<div className="grid gap-4 sm:grid-cols-2">` block.
  - Create Password & Confirm Password: Move them inside a single `<div className="grid gap-4 sm:grid-cols-2">` block.
- Ensure the password strength indicator, submit button, and footer links scale properly to full width.

## Verification Plan

### Automated Tests
- Run `npm run lint` and `npm run type-check` inside `serp_web` to verify code correctness and formatting.

### Manual Verification
- Launch the development server (`npm run dev` in `serp_web`).
- Navigate to the Authentication page (`/auth`).
- Toggle between Login ("Sign in") and Register ("Create account") modes:
  - Verify that the card width changes smoothly with transition animation.
  - Verify that Register mode displays fields in a neat 2-column layout on desktop viewports.
  - Verify that the layout falls back gracefully to a single-column stacked layout on mobile viewports.
  - Verify that password validation and form submission work correctly.
