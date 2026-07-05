# CRM Opportunity UX/UI Redesign Design Specification

**Author:** Antigravity & QuanTuanHuy  
**Date:** 2026-07-05  
**Description:** Specification for modernizing the CRM Opportunity detail workspace and creation/edit forms.

---

## 1. Goal & Context

The CRM Opportunity detail page (`OpportunityDetailPage.tsx`) and form (`OpportunityForm.tsx`) currently follow a traditional layout that leads to information fragmentation and a high density of non-interactive elements.

This specification outlines the redesign of the Opportunity workspace into a modern, 3-column asymmetric layout (similar to the Lead Detail Workspace), while preserving all business rules, backend mappings, and database integrity.

---

## 2. Page & Layout Redesign

The new page will employ a 3-column responsive layout (`grid grid-cols-1 lg:grid-cols-4` where the center column spans 2 grid spaces).

```
+-----------------------------------------------------------------------------------+
|  Header: Back Button | Opportunity Name | Edit, Delete, Reopen, Won, Lost Actions |
+-----------------------------------------------------------------------------------+
|  Pipeline Progress Bar (Progression Stage Indicator - Flat Status Indicator)     |
+-----------------------------------------------------------------------------------+
| [Column 1: Profile Sidebar] | [Column 2 & 3: Interaction Hub] | [Column 4: Insights] |
|                             |                                 |                      |
| - Name                      | - QuickComposer:                | - Win Probability    |
| - Associated Account        |   * Log Activity Tab            |   Radial Arc Gauge   |
| - Source Lead (optional)    |   * Add Note Tab (Real API)     |                      |
| - Estimated Value (Inline)  |                                 | - Live Forecast Value|
| - Expected Close Date (Pick)| - Unified Timeline:             |   (Weighted Value)   |
| - Description (Inline Edit) |   * Chronological feed          |                      |
| - Static Notes              |   * Mapped Creator Names        | - Vertical Pipeline  |
|                             |   * Inline Edit/Delete Notes    |   Stage Stepper      |
|                             |                                 |                      |
|                             |                                 | - Assigned Rep       |
|                             |                                 | - Quick Action Hub   |
+-----------------------------+---------------------------------+----------------------+
```

### Component Details

#### A. Left Column: `OpportunityProfileSidebar`
- **Purpose:** Focuses on the core properties of the commercial transaction.
- **Interactions (Click-to-Edit):**
  - **Opportunity Name:** Click to edit text input.
  - **Associated Account:** Click to reveal account select dropdown (populated by `useGetAccountsQuery`).
  - **Source Lead (Optional):** Click to select associated lead (populated by `useGetLeadsQuery`).
  - **Estimated Value:** Click to edit numeric input, formatted as currency on blur.
  - **Expected Close Date:** Click to edit using `CRMDatePicker`.
  - **Description:** Textarea editor with auto-save or save/cancel actions.

#### B. Center Column: `QuickComposer` & `UnifiedTimeline`
- **QuickComposer:** 
  - Allows logging activities (Calls, Emails, Meetings) and adding notes.
  - Add Note invokes `useCreateCrmNoteMutation` to save notes using the real backend polymorphic notes system.
- **UnifiedTimeline:**
  - Integrates notes and activities chronologically.
  - Resolves creator IDs (`createdBy`) and assignee IDs (`assignedTo`) to real names using `useGetOrganizationUsersQuery`.
  - Integrates inline update (`useUpdateCrmNoteMutation`) and delete (`useDeleteCrmNoteMutation`) options directly on note cards.

#### C. Right Column: `OpportunityInsightsSidebar`
- **Win Probability Gauge:**
  - A glow-enhanced SVG radial arc gauge representing the win probability percentage (10% to 100%).
  - Inside the gauge: Displays the dynamically calculated `Weighted Value` (Estimated Value $\times$ Probability %).
- **Vertical Pipeline Stage Stepper:**
  - Displays the active deal path: `Prospecting (10%)` $\rightarrow$ `Qualification (25%)` $\rightarrow$ `Proposal (50%)` $\rightarrow$ `Negotiation (75%)` $\rightarrow$ `Closed (Won 100% / Lost 0%)`.
  - **Interactive Selection:** Clicking any stage item immediately triggers the Stage Transition dialog/action.
- **Assign Rep:** Dropdown selecting from organization users list.
- **Action Hub:** Group of quick buttons: *Request Meeting*, *Mark as Won*, *Mark as Lost*, and *Reopen*.

---

## 3. Form Redesign: `OpportunityForm`

The creation and editing forms are restructured to feature a two-pane layout:
- **Left Pane (2/3 width):** Input fields structured in clean cards (Basic Details, Pipeline Settings, Additional Notes).
- **Right Pane (1/3 width):** A sticky **Live Deal Calculator Widget**:
  - Displays a visual real-time calculation card.
  - As the user types `Estimated Value` and changes `Stage`, it immediately updates the calculated `Win Probability %` and `Weighted Value` using a smooth number transition.
  - Displays helper alerts if `Expected Close Date` is in the past or unusually far in the future.

### Data Validation & Backend Sync
- Form fields are validated via React Hook Form and Zod schema.
- The calculations in the frontend widget are strictly aligned with the Java backend logic in `OpportunityEntity.java`.

---

## 4. Verification Plan

### Automated Build & Checks
1. Run static type checking to verify types:
   ```bash
   npm run type-check
   ```
2. Run ESLint to verify style guide adherence:
   ```bash
   npm run lint
   ```
3. Run compilation check to verify the production build:
   ```bash
   npm run build
   ```

### Manual Verification Flow
1. Navigate to `/crm/opportunities/create` and verify that typing an estimated value and selecting a stage dynamically updates the Live Calculator.
2. Save the opportunity and verify that the detail page loads in the new 3-column layout.
3. Verify that click-to-edit fields in the left sidebar successfully update the opportunity properties.
4. Verify that clicking on stages in the vertical pipeline stepper updates the stage and refreshes the timeline.
