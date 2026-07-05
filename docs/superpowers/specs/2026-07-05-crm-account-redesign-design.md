# CRM Account (Customer) UX/UI Redesign Design Specification

**Author:** Antigravity & QuanTuanHuy  
**Date:** 2026-07-05  
**Description:** Specification for modernizing the CRM Account detail workspace (CustomerDetailPageEnhanced.tsx) and input forms (AccountForm).

---

## 1. Goal & Context

The CRM Account detail page (`CustomerDetailPageEnhanced.tsx`) manages complex client profiles with large datasets, including financial indicators, contacts list, communication preferences, and activity logs.

This specification redesigns the workspace into a modern, 3-column asymmetric layout, keeping structural consistency with the Lead and Opportunity detail pages, and refactors the creation/edit form into a grid layout with a toggle-based preference widget.

---

## 2. Page & Layout Redesign

The new page will employ a 3-column responsive layout (`grid grid-cols-1 lg:grid-cols-4` where the center column spans 2 grid spaces).

```
+-----------------------------------------------------------------------------------+
|  Header: Back Button | Account Name & Status Badges | Quick Action Hub Buttons   |
+-----------------------------------------------------------------------------------+
|  Metric Strip: Total Value | Opportunities count | Won Deals | Credit Limit       |
+-----------------------------------------------------------------------------------+
| [Column 1: Profile Sidebar] | [Column 2 & 3: Tabs Container]  | [Column 4: Insights] |
|                             |                                 |                      |
| - Account Name (Inline)     | - Tab 1: Timeline               | - Credit Utilization |
| - Email & Phone (Inline)    |   * Log Activity / Add Notes    |   Progress Bar Gauge |
| - Website (Inline)          |   * Chronological History       |                      |
| - Company Size / Industry   |                                 | - Primary Contact    |
| - Tax Number                | - Tab 2: Contacts List          |   Avatar Card        |
| - Timezone & Language       |   * Direct Add/Edit/Delete      |                      |
| - Business Address (Inline) |                                 | - Communication      |
|                             | - Tab 3: Opportunities Feed     |   Preferences Badges |
|                             |   * Mini Opportunity Cards      |                      |
|                             |                                 | - Quick Action list  |
+-----------------------------+---------------------------------+----------------------+
```

### Component Details

#### A. Left Column: `AccountProfileSidebar`
- **Purpose:** Manages core business profile properties.
- **Interactions (Click-to-Edit):**
  - **Account Name:** Click to edit text input.
  - **Email & Phone:** Click to edit contact details.
  - **Website:** Click to edit URL input.
  - **Industry / Company Size:** Click to select from dropdown lists.
  - **Timezone / Language:** Click to select options.
  - **Address:** Click to edit text input.

#### B. Center Column: Tabs Container
- **Tab 1: Timeline:**
  - Integrates notes and activities chronologically.
  - Reuses `QuickComposer` and `UnifiedTimeline` specialized for Accounts.
  - Resolves creator IDs to actual user names using `useGetOrganizationUsersQuery`.
- **Tab 2: Contacts:**
  - Integrates the existing `ContactList` component to manage multiple account contacts (add, edit, delete, set primary).
- **Tab 3: Opportunities:**
  - Displays mini horizontal opportunity cards (estimated value, win probability, stage badge, assigned rep) using `useGetOpportunitiesQuery` filtered by account `customerId`.

#### C. Right Column: `AccountInsightsSidebar`
- **Credit Utilization Gauge:**
  - Renders a progress bar showing credit utilization: `Total Value / Credit Limit`.
  - Automatically turns red if utilization exceeds 90%.
  - Includes a quick action button to open the Credit Limit edit dialog.
- **Primary Contact Card:** Displays avatar, name, and role of the primary contact.
- **Communication Preferences:** Displays preferred days of the week and time slots in clean badges.
- **Quick Action list:** *Request Meeting*, *Deactivate/Activate Account*, *Delete Account*.

---

## 3. Form Redesign: `AccountForm`

The creation and editing form is refactored into a two-pane layout:
- **Left Pane (2/3 width):** Grouped cards for Basic Company Details (name, tax number, size, industry) and Contact/Location Details.
- **Right Pane (1/3 width):** **Tags & Preferences Widget**:
  - **Tag Cloud:** Click badges to add/remove tags (VIP, Partner, Target, Key, New, Inactive).
  - **Communication Preferences:** Replaces checkboxes with Toggle Badges for Days of the week (Monday $\rightarrow$ Sunday) and Time slots (Morning, Afternoon, Evening).

---

## 4. Verification Plan

### Automated Checks
1. Run static type checking:
   ```bash
   npm run type-check
   ```
2. Run ESLint:
   ```bash
   npm run lint
   ```
3. Run Next.js compilation:
   ```bash
   npm run build
   ```

### Manual Verification Flow
1. Navigate to `/crm/accounts/create` and verify that the Tag Cloud and Preference Toggle Badges update form state correctly.
2. Save the account and verify that the detail page loads in the new 3-column layout.
3. Verify that click-to-edit fields in the left sidebar successfully update the account properties.
4. Verify that the Opportunities tab correctly displays active deals associated with this account.
