# Modular-Based Architecture for SERP Web

## Current Structure Analysis

### ✅ **Existing Strengths**

- **Module separation**: `src/modules/` với account, crm, settings, purchase, logistics, ptm, notifications
- **Modern tech stack**: Next.js 15, Redux Toolkit, TypeScript, Shadcn/ui
- **Clean exports**: Modules có barrel exports pattern (`index.ts`)
- **Type safety**: TypeScript với proper type definitions
- **RTK Query**: API endpoints với `api.injectEndpoints()` pattern
- **Shared layer**: `shared/components/ui/` với Shadcn components

### ⚠️ **Areas for Improvement**

- **Module isolation**: Ensure no cross-module imports (communicate via Redux)
- **Consistent API pattern**: All modules should use `extraOptions: { service: 'moduleName' }`
- **Lazy loading**: Implement module-level code splitting

## **Current Modular Structure**

```
src/
├── app/                    # Next.js App Router
│   ├── crm/               # CRM module pages
│   ├── ptm/               # PTM module pages
│   ├── settings/          # Settings pages
│   ├── purchase/          # Purchase pages
│   ├── logistics/         # Logistics pages
│   └── layout.tsx
├── modules/               # 🎯 Business Logic Modules
│   ├── crm/
│   │   ├── api/           # RTK Query endpoints (crmApi.ts)
│   │   ├── components/    # CRM-specific UI
│   │   ├── store/         # CRM Redux slices
│   │   ├── types/         # CRM TypeScript types
│   │   └── index.ts       # ✅ Barrel exports
│   ├── account/           # Auth, users
│   ├── settings/          # Organization, departments
│   ├── purchase/          # Orders, suppliers
│   ├── logistics/         # Inventory, shipping
│   ├── ptm/               # Tasks, projects
│   └── notifications/     # Push notifications
├── shared/                # 🔄 Cross-Module Resources
│   ├── components/ui/     # Shadcn UI components
│   ├── hooks/             # Common hooks
│   └── utils/             # Helper functions
└── lib/                   # 🔧 Core Configuration
    └── store/api/         # apiSlice.ts (base RTK Query config)
```

## **Key Principles**

### 1. **Module Independence**

- Each module is self-contained
- No direct imports between modules
- Communication via shared state or events

### 2. **RTK Query API Pattern** ✅

```typescript
// modules/crm/api/crmApi.ts
import { api } from '@/lib/store/api';

export const crmApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCustomers: builder.query<APIResponse<Customer[]>, Filters>({
      query: (filters) => ({ url: '/customers', params: filters }),
      extraOptions: { service: 'crm' }, // Routes to /crm/api/v1/customers
      providesTags: ['Customer'],
    }),
  }),
});

export const { useGetCustomersQuery } = crmApi;
```

### 3. **Barrel Exports Pattern** ✅

```typescript
// modules/crm/index.ts
export * from './api/crmApi';
export * from './components';
export * from './types';
```

### 3. **Feature-Based Routing**

```
/dashboard/crm           → CRM module
/dashboard/accounting    → Accounting module
/dashboard/inventory     → Inventory module
```

### 4. **Shared Resources Strategy**

- **UI Components**: Button, Table, Modal trong `shared/components`
- **Business Logic**: Module-specific trong `modules/*/`
- **API Layer**: Shared utilities trong `shared/services`

## **Benefits for ERP System**

- **Scalability**: Dễ thêm modules mới (HR, Sales, etc.)
- **Team Development**: Nhiều team làm việc song song
- **Code Reusability**: Shared components across modules
- **Maintainability**: Tách biệt business logic theo domain
- **Performance**: Module lazy loading, code splitting

---

_Authors: QuanTuanHuy - Part of Serp Project_
