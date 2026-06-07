# Settings General API Integration Design

## Context

`serp_web/src/app/settings/page.tsx` currently renders the Settings General page with hardcoded organization data. The `account` service already exposes `GET /api/v1/organizations/me`, but it only returns existing organization fields and does not support updating the full settings form.

The goal is to make Settings General data-driven with a complete backend data model for profile, branding, and regional preferences.

## Scope

This design covers Settings General for the current user's organization:

- Profile data: organization identity and contact details.
- Address details: structured address fields.
- Branding data: logo, favicon, primary color, secondary color.
- Preferences: timezone, currency, language, date format, time format, week start day.
- Read-only summary fields for page cards where available.

This design does not include binary file upload. Logo and favicon are stored as URL strings. If a file service exists later, this page can consume uploaded file URLs without changing the settings contract.

## Chosen Approach

Add first-class columns to the existing `organizations` table.

This is preferred over a separate `organization_settings` table because these fields are direct properties of the organization, the page needs a single settings object, and the current code already maps `OrganizationModel` to `OrganizationEntity` for similar fields such as `timezone`, `currency`, `language`, `logoUrl`, `primaryColor`, `website`, `phoneNumber`, and `email`.

## Database Changes

Add a Flyway migration under `account/src/main/resources/db/migration`.

New nullable columns on `organizations`:

- `city` varchar
- `state` varchar
- `country` varchar
- `zip_code` varchar
- `tax_id` varchar
- `secondary_color` varchar
- `favicon_url` varchar
- `date_format` varchar
- `time_format` varchar
- `week_starts_on` varchar

Existing fields reused:

- `name`
- `code`
- `description`
- `address`
- `industry`
- `employee_count`
- `timezone`
- `currency`
- `language`
- `logo_url`
- `primary_color`
- `website`
- `phone_number`
- `email`

## Backend API

Use a single read endpoint and a single update endpoint:

- `GET /api/v1/organizations/me/settings`
- `PUT /api/v1/organizations/me/settings`

Both endpoints resolve organization id from `AuthUtils.getCurrentTenantId()` and must enforce tenant access through existing auth utilities and core checks.

### GET Response

Return `GeneralResponse<OrganizationSettingsResponse>`.

`OrganizationSettingsResponse` shape:

```json
{
  "id": 1,
  "name": "Acme Corporation",
  "code": "acme-corp",
  "email": "contact@acme.com",
  "phoneNumber": "+1 (555) 123-4567",
  "website": "https://acme.com",
  "address": "123 Business Street",
  "city": "San Francisco",
  "state": "CA",
  "country": "United States",
  "zipCode": "94102",
  "taxId": "TAX-123456",
  "industry": "Technology",
  "employeeCount": 120,
  "description": "Leading technology solutions provider",
  "logoUrl": "https://example.com/logo.png",
  "faviconUrl": "https://example.com/favicon.ico",
  "primaryColor": "#7c3aed",
  "secondaryColor": "#a78bfa",
  "timezone": "Asia/Ho_Chi_Minh",
  "dateFormat": "DD/MM/YYYY",
  "timeFormat": "24h",
  "weekStartsOn": "monday",
  "currency": "VND",
  "language": "vi",
  "createdAt": 1715731200000,
  "updatedAt": 1715731200000,
  "summary": {
    "totalUsers": 45,
    "totalDepartments": 8,
    "subscriptionPlan": "Professional"
  }
}
```

Summary values are read-only. If any count or subscription value is not available without introducing cross-service risk, return `null` for that field and let FE hide the card value or show `Not available`.

### PUT Request

Use one request DTO for all editable settings. All fields are optional except validation constraints on supplied values.

```json
{
  "name": "Acme Corporation",
  "email": "contact@acme.com",
  "phoneNumber": "+1 (555) 123-4567",
  "website": "https://acme.com",
  "address": "123 Business Street",
  "city": "San Francisco",
  "state": "CA",
  "country": "United States",
  "zipCode": "94102",
  "taxId": "TAX-123456",
  "industry": "Technology",
  "employeeCount": 120,
  "description": "Leading technology solutions provider",
  "logoUrl": "https://example.com/logo.png",
  "faviconUrl": "https://example.com/favicon.ico",
  "primaryColor": "#7c3aed",
  "secondaryColor": "#a78bfa",
  "timezone": "Asia/Ho_Chi_Minh",
  "dateFormat": "DD/MM/YYYY",
  "timeFormat": "24h",
  "weekStartsOn": "monday",
  "currency": "VND",
  "language": "vi"
}
```

`code`, `createdAt`, `updatedAt`, and `summary` are not editable through this endpoint.

### Validation

- `name`: required when present, non-blank, max 255.
- `email`: valid email when present.
- `website`, `logoUrl`, `faviconUrl`: valid URL when present.
- `employeeCount`: non-negative when present.
- `primaryColor`, `secondaryColor`: hex color format `#RRGGBB` when present.
- `dateFormat`: one of `MM/DD/YYYY`, `DD/MM/YYYY`, `YYYY-MM-DD`.
- `timeFormat`: one of `12h`, `24h`.
- `weekStartsOn`: one of `sunday`, `monday`.

Backend should preserve unspecified fields. Empty strings from FE should be normalized consistently: trim all strings, then store `null` for blank optional fields.

## Backend Components

Update existing organization layers:

- `OrganizationModel`: add new JPA columns.
- `OrganizationEntity`: add new fields.
- `OrganizationMapper`: map new fields both directions.
- `IOrganizationPort` / `OrganizationAdapter`: existing `save` is enough for update persistence.
- `IOrganizationService` / `OrganizationService`: add `updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request)`.
- `OrganizationUseCase`: add `getMyOrganizationSettings` and `updateMyOrganizationSettings` orchestration methods.
- `OrganizationController`: add GET and PUT endpoints under `/api/v1/organizations/me/settings`.

Controller remains thin: resolve tenant id, validate request with `@Valid`, call use case, return `GeneralResponse<?>` through `ResponseEntity.status(response.getCode()).body(response)`.

## Frontend Design

Move page logic out of app route:

- Keep `serp_web/src/app/settings/page.tsx` thin.
- Create module-owned `SettingsGeneralPage` under `serp_web/src/modules/settings`.

Add API integration:

- Create or enable general settings RTK Query endpoint in `src/modules/settings/services/general`.
- Export hooks through `src/modules/settings/services/settingsApi.ts`.
- Use `extraOptions: { service: 'account' }` for explicit account-service routing. The base API also defaults to account behavior when `service` is omitted, but this endpoint should set it for consistency with the module guide.

Expected hooks:

- `useGetOrganizationSettingsQuery()`
- `useUpdateOrganizationSettingsMutation()`

Page behavior:

- Load settings through RTK Query.
- Populate three tabs from the same settings object.
- Save via one PUT endpoint with the current full form state.
- Use `.unwrap()` in mutation `try/catch`.
- Show loading, error, success, and saving states.
- Keep `code` read-only.
- Keep logo/favicon as URL inputs, not file upload controls, until upload service exists.

## UI Sections

Profile tab:

- Organization name
- Code read-only
- Email
- Phone number
- Website
- Industry
- Employee count
- Tax id
- Address
- City
- State/province
- Country
- ZIP/postal code
- Description

Branding tab:

- Logo URL
- Favicon URL
- Primary color
- Secondary color

Preferences tab:

- Timezone
- Date format
- Time format
- Week starts on
- Currency
- Language

Stats cards:

- Total users
- Departments
- Subscription plan
- Member since

Stats cards should degrade gracefully when values are missing.

## Error Handling

Backend:

- Return not found when tenant organization does not exist.
- Return validation errors through existing global exception flow.
- Return forbidden when current user cannot access resolved organization.
- Keep `GeneralResponse<?>` shape stable.

Frontend:

- Use existing shared notification or toast utilities if available in Settings module.
- Show field-level validation where practical.
- Show page-level error if GET fails.
- Disable Save button while mutation is in flight.

## Verification Plan

Backend:

- Run focused compile/test for `account` after changes.
- Add or update unit test for organization settings update preserving unspecified values and validating enum-like fields if existing test patterns support it.
- Run `mvnw.cmd clean compile` from `account` on Windows.

Frontend:

- Run `npm run lint` from `serp_web`.
- Run `npm run type-check` from `serp_web`.
- Run `npm run format:check` from `serp_web`.

## Implementation Notes

- Use migration file `V9__add_organization_settings_fields.sql` unless another migration is added before implementation starts.
- If summary counts require too much cross-module work, implement settings fields first and return nullable summary fields.
