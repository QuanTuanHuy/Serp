# UI/UX Redesign for CRM Lead Detail Page

- **Date:** 2026-07-05
- **Status:** Approved (Text-only Brainstorming)
- **Author:** Antigravity
- **Target File:** [LeadDetailPageEnhanced.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/leads/LeadDetailPageEnhanced.tsx)

## 1. Goal Description
Modernize the UX/UI of the Lead Detail Page to shift from a traditional, blocky tabbed view to an action-oriented 3-column workspace. The new design simplifies lead management, reduces user clicks, and provides a clean, unified workspace for sales representatives.

---

## 2. Existing API Assessment & Constraints
We will utilize the existing CRM API endpoints exposed in `crmApi.ts`.

- **Lead Profile & Info:**
  - Retrieve details via `useGetLeadQuery(leadId)`.
  - Update details via `useUpdateLeadMutation()`. Supports updating fields inline (Email, Phone, Company, Job Title, Estimated Value, Follow Up Date, Notes).
- **Assigned To:**
  - Update assignee via `useAssignLeadMutation()`.
- **Status Transitions (Pipeline):**
  - Update status via `useUpdateLeadStatusMutation()`. Handles transitions to `QUALIFIED`, `DISQUALIFIED`, and `CONVERTED`.
- **Unified Timeline (Merged Data):**
  - **Notes:** Retrieve via `useGetNotesQuery({ entityType: 'LEAD', entityId })`. Add notes via `useCreateNoteMutation()`.
  - **Activities:** Retrieve via `useGetLeadActivitiesQuery({ leadId })`. Add activities via `useCreateActivityMutation()`.
- **Meeting Requests:**
  - Handled via `RequestMeetingDialog` using the local `/meeting-requests` endpoints (`useCreateMeetingRequestMutation`).
  - > [!IMPORTANT]
    > **Integration Limitation:** There is currently NO synchronization or integration with Google Calendar or Outlook. All meetings are managed locally within the CRM system.

---

## 3. Proposed Layout & Architecture (3-Column Workspace)

```
+---------------------------------------------------------------------------------------+
|  <- Back   Lead Name  [Status Badges]                                                 |
+---------------------------------------------------------------------------------------+
|  [COL 1: Lead Profile]    |  [COL 2: Interaction Hub]     |  [COL 3: Insights & Actions]  |
|  - Avatar & Basic Info    |                               |                               |
|  - Separator Line         |  +-------------------------+  |  - Lead Score Gauge           |
|  - Email (Inline Edit)    |  | Quick Composer:         |  |    (Modern minimal ring)      |
|  - Phone (Inline Edit)    |  | [Log Activity] [Add Note]|  |  - Quick Assign Dropdown      |
|  - Company (Inline Edit)  |  +-------------------------+  |  - Actions:                   |
|  - Value (Inline Edit)    |                               |    * Convert to Account       |
|  - Follow-up (Inline Edit)|  - Unified Timeline:          |    * Qualify / Disqualify     |
|                           |    * Call log (Activity)      |    * Request Meeting          |
|                           |    * Ghi chú (Note)           |  - Collapsible Metadata       |
|                           |    * Status Changed (System)  |                               |
+---------------------------------------------------------------------------------------+
```

### Component Breakdown

### Left Column: `LeadProfileSidebar` (25% width)
- **Profile Header:** Clean gradient avatar fallback, large lead name, and flat badges for Lead Status & Lead Source.
- **Inline Editing Fields:** Contact details list with light gray borders. Hovering reveals a edit icon. Clicking triggers input mode. Saving updates via `useUpdateLeadMutation` with automatic loading indicators.

### Center Column: `InteractionHub` (50% width)
- **Quick Composer:** Minimally styled box with two tabs: *Log Activity* (log Call/Email/Meeting) and *Add Note* (add text note). Clicking expands the composer to show action buttons.
- **Unified Activity Timeline:** Chronological vertical feed (`border-l-2`) merging results from `useGetLeadActivitiesQuery` and `useGetNotesQuery`.
  - Notes show a yellow/orange `MessageSquare` badge.
  - Activities show blue/purple `Phone`/`Mail` badges.
  - Status updates, assignments, and creations are styled as compact inline system logs.

### Right Column: `InsightsSidebar` (25% width)
- **Lead Score Gauge:** A slim gradient circular arc showing score progress. Middle number text is styled with a subtle drop-shadow glow matching the score color. An expandable popover explains the score breakdown.
- **Reassign Dropdown:** Avatar and name of current assignee. Clicking opens a dropdown of active CRM members to reassign immediately using `useAssignLeadMutation`.
- **Conversion Actions:** Structured action list:
  - **Convert to Account:** Enabled/visible only if lead is `QUALIFIED`.
  - **Qualify & Disqualify:** Primary buttons to toggle status dialogs.
  - **Request Meeting:** Action button that opens `RequestMeetingDialog` (explicitly documented as local-only).
- **Metadata Accordion:** A collapsible section at the bottom for Lead ID, creation date, and last update date.

---

## 4. Verification Plan

### Automated Checks
- Verify typescript types compile correctly: `npm run type-check` (run from `serp_web`).
- Lint checks: `npm run lint` or `npx eslint` on modified files.

### Manual Verification
- Verify lead details update inline correctly and trigger cache invalidation.
- Verify creating notes/activities via the Quick Composer immediately reflects on the Unified Timeline.
- Verify status changes (Qualify, Disqualify, Convert) work and trigger proper system logs in the timeline.
