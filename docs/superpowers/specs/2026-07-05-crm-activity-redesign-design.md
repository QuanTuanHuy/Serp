# CRM Activity UX/UI Redesign Design Specification

**Author:** Antigravity & QuanTuanHuy  
**Date:** 2026-07-05  
**Description:** Specification for modernizing the CRM Activity detail page (ActivityDetailPage.tsx), list page (ActivityListPage.tsx), and edit forms (ActivityForm.tsx).

---

## 1. Goal & Context

The CRM Activity pages manage tasks, phone calls, emails, and meeting records.

This specification modernizes:
1. The **Activity Detail Page** into a workflow-centric 2-column asymmetric layout.
2. The **Activity List Page** (list view mode) by grouping activities dynamically by urgency (Overdue, Today, Upcoming, Completed/Cancelled).
3. The **Activity Form** into a dynamic 2-column layout that hides/shows fields depending on the selected activity type (e.g. Location only for Meetings) and status (e.g. Outcome only for Completed).

---

## 2. Activity Detail Page Layout

The page utilizes a 2-column layout (`grid grid-cols-1 lg:grid-cols-3`):

```
+-----------------------------------------------------------------------------+
| Header: Back Button | Activity Subject & Type/Priority Badges | Actions Hub |
+-----------------------------------------------------------------------------+
| [Column 1: Profile & Workspace (2/3)]  | [Column 2: Metadata Sidebar (1/3)] |
|                                        |                                    |
| - Description Card                     | - Linked Entity Card               |
| - Outcome Card (Only if Completed)     |   (Link to Lead/Account/Opp)       |
| - Follow-up Card (Only if Required)    |                                    |
| - Notes Tab Component                  | - Assignee Profile Card            |
|                                        | - Schedule Info Card               |
|                                        | - Quick Actions List               |
+----------------------------------------+------------------------------------+
```

### Component Details

#### A. Left Column: `ActivityProfile`
- **Description:** Renders the main task description, outcome notes, and categorization tags.
- **Notes Tab:** A tab container allowing the rep to add internal remarks or logs for this specific activity.

#### B. Right Column: `ActivityMetadataSidebar`
- **Linked Entity Card:** Displays parent metadata (type, name) and click-to-navigate links with distinct icons (Building2 for Customer/Account, User for Lead, Target for Opportunity).
- **Schedule Card:** Displays the start time, calculated duration, location, and reminder schedule.
- **Quick Actions:** Complete, Reschedule, Edit, and Delete triggers.

---

## 3. Activity List Page: Urgency Time Groups

In list view mode, activities are sorted and grouped dynamically:
1. **Overdue:** Activities scheduled before `now` with `status === 'PLANNED'`. Highlighted in red.
2. **Today:** Activities scheduled for today. Highlighted in blue.
3. **Upcoming:** Activities scheduled for future days. Highlighted in green.
4. **Completed / Cancelled:** Historical activities. Highlighted in gray.

Each group header shows the total item count and supports collapse/expand interactions.

---

## 4. Activity Form: Dynamic Layout

`ActivityForm` employs a 2-column grid layout:
- **Left Pane (2/3 width):** Core Details (Subject, Type, Status, Priority, Description).
  - *Dynamic location:* Renders only if `activityType === 'MEETING'`.
  - *Dynamic outcome:* Renders only if `status === 'COMPLETED'`.
- **Right Pane (1/3 width):** Meta & Notes (Assignee selector, duration, category tags).

---

## 5. Verification Plan

### Automated Checks
1. Run static type checks:
   ```bash
   npm run type-check
   ```
2. Run ESLint:
   ```bash
   npm run lint
   ```
3. Run compilation build:
   ```bash
   npm run build
   ```

### Manual Verification Flow
1. Navigate to `/crm/activities` and verify that activities in the list view are grouped into Overdue, Today, Upcoming, and Completed sections.
2. Click "Quick Add" or "Edit" to open `ActivityForm`. Verify that the Location input hides/shows when toggling Meeting, and the Outcome text area hides/shows when toggling Completed status.
3. Open a detailed activity page and verify that it loads in the asymmetric 2-column layout.
