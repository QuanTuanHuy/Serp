# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## SERP web

Next.js 15 App Router frontend with React 19 and TypeScript. Styling uses Tailwind CSS 4, Radix UI, and Shadcn-style shared primitives. State/data use Redux Toolkit, RTK Query, redux-persist, and shared store middleware.

## Commands

Run from `serp_web/`.

```bash
npm install
npm run dev
npm run build
npm run start
npm run lint
npm run lint:fix
npm run format
npm run format:check
npm run type-check
npx eslint src/modules/settings/hooks/useUsers.ts
npx eslint src/modules/settings/hooks/useUsers.ts --fix
npx prettier --check src/modules/account/components/LoginForm.tsx
npx prettier --write src/modules/account/components/LoginForm.tsx
```

There is no `test` script, checked-in Jest/Vitest/Playwright/Cypress config, or `*.test.*` / `*.spec.*` files under `src/`. No supported single-test command exists unless current branch adds test framework.

Recommended verification for most changes:

```bash
npm run lint
npm run type-check
npm run format:check
```

Run `npm run build` for routing, provider, config, environment, or bundle-sensitive changes.

## Architecture

- `src/app/`: App Router pages, layouts, route wrappers. Keep thin; compose module UI.
- `src/modules/`: feature modules such as account, admin, crm, logistics, ptm, sales, settings, subscription.
- `src/shared/`: shared UI, hooks, providers, utils, types.
- `src/lib/store/`: Redux store, middleware, RTK Query base API.
- `components.json`: Shadcn generator config.

Treat each folder in `src/modules/` as a feature boundary. Avoid new cross-module business imports. Move shared code to `src/shared/` or expose it through a stable module barrel.

## Conventions

- Add `'use client';` only to interactive components, hooks, and providers.
- Prefer barrel exports (`index.ts`) at module boundaries.
- Use import order: Next/React, third-party, `@/` alias, then relative imports.
- Use `@/*` alias for `src/*`; prefer relative imports for nearby same-module files.
- Use `import type` for type-only imports when practical.
- Prettier is source of truth: 2 spaces, semicolons, single quotes, trailing commas `es5`, print width `80`, LF endings.
- TypeScript is strict; prefer explicit props/request/response types and avoid new `any`.
- Forms usually use `react-hook-form` + `zod` + `zodResolver`.
- Extend shared RTK Query slice with `api.injectEndpoints()` and set `extraOptions: { service: 'serviceName' }` for service-routed endpoints.
- Use RTK Query hooks over manual `fetch`; use `.unwrap()` inside `try/catch` for mutations.
- Reuse shared auth header and token refresh logic in `src/lib/store/api/apiSlice.ts`.
- Use `@/shared/components/ui/` primitives and shared `cn()` helper before creating new UI primitives.
- Prefer semantic theme tokens from `src/app/globals.css` over ad-hoc colors.
- Preserve accessibility, keyboard support, focus states, and responsive behavior.

## Naming

- Route files: `page.tsx`, `layout.tsx`.
- Components: `PascalCase`.
- Page components: `SomethingPage.tsx`.
- Forms/dialogs/layouts: `SomethingForm.tsx`, `SomethingDialog.tsx`, `SomethingLayout.tsx`.
- Hooks: `useSomething.ts`.
- Redux slices: `somethingSlice.ts`.
- Type files: `something.types.ts`.
- Shared UI primitive files: kebab-case.
- API files follow local module convention, such as `salesApi.ts` or legacy `users.api.ts`.
