# AGENTS.md - Guide for AI Coding Agents in `serp_web`

This guide is for agentic coding tools working inside `serp_web/`; it complements the repository-root `AGENTS.md` with frontend-specific commands and conventions.

## Module Snapshot

- Next.js 15 App Router frontend with React 19 and TypeScript.
- Styling uses Tailwind CSS 4, Radix UI, and Shadcn-style shared primitives.
- State and data use Redux Toolkit, RTK Query, redux-persist, and shared store middleware.
- Feature code lives in `src/modules/`; route entry points live in `src/app/`.
- Shared UI, hooks, providers, and helpers live in `src/shared/`; store setup and the RTK Query base slice live in `src/lib/store/`.

## Key Directories

```text
src/app/                # App Router pages, layouts, route wrappers
src/modules/            # Feature modules: account, admin, crm, logistics, ptm, sales, settings, subscription
src/shared/             # Shared UI, hooks, providers, utils, types
src/lib/store/          # Redux store, middleware, RTK Query base API
package.json            # Scripts and dependencies
components.json         # Shadcn generator config
```

## Install and Run

Run all commands from `serp_web/`.

```bash
npm install
npm run dev
npm run build
npm run start
```

## Lint, Format, and Type Check

```bash
npm run lint
npm run lint:fix
npm run format
npm run format:check
npm run type-check
```

```bash
npx eslint src/modules/settings/hooks/useUsers.ts
npx eslint src/modules/settings/hooks/useUsers.ts --fix
npx prettier --check src/modules/account/components/LoginForm.tsx
npx prettier --write src/modules/account/components/LoginForm.tsx
```

- `npm run lint` covers `.ts`, `.tsx`, `.js`, and `.jsx`; `npm run type-check` runs `tsc --noEmit` for the whole app.
- `npm run build` uses `next build --turbopack`.
- Husky pre-commit runs `npx lint-staged`; it does not replace full verification.

## Test Status

- There is no `test` script in `package.json`, no checked-in Jest/Vitest/Playwright/Cypress config, and no `*.test.*` or `*.spec.*` files under `src/` today.
- There is therefore no supported single-test command right now.
- If a task asks for one test, first confirm whether the current branch added a test framework.
- If not, say clearly that frontend tests are not configured yet.

## Recommended Verification Before Handoff

For most changes, run:

```bash
npm run lint
npm run type-check
npm run format:check
```

- Also run `npm run build` for routing, provider, config, environment, or bundle-sensitive changes.

## Architecture Rules

- Keep `src/app/` files thin; they should usually compose or render module pages.
- Put business logic, feature hooks, slices, forms, dialogs, and RTK Query endpoints in `src/modules/<feature>/`.
- Put reusable primitives in `src/shared/components/ui/`.
- Put cross-feature hooks, providers, common types, and helpers in `src/shared/`; keep base Redux and API infrastructure in `src/lib/store/`.
- Prefer barrel exports (`index.ts`) at module boundaries.

## Module Boundaries

- Treat each folder in `src/modules/` as a feature boundary.
- Avoid adding new cross-module imports between business modules.
- If something is shared, move it to `src/shared/` or expose it through a stable module barrel.
- Some legacy cross-module type imports exist; do not expand that pattern without a strong reason.

## Import Conventions

Observed import order in this codebase:

1. Next.js and React
2. Third-party libraries
3. Internal alias imports from `@/`
4. Relative imports from the same module

- Use the `@/*` alias for `src/*`.
- Prefer alias imports for shared code and distant folders.
- Prefer relative imports for nearby files in the same module subtree.
- Use `import type { ... }` for type-only imports when practical.

## Formatting

- Prettier is the source of truth.
- Use semicolons, single quotes, trailing commas `es5`, and print width `80`.
- Use 2 spaces, not tabs, and keep line endings as `lf`.
- ESLint extends `next/core-web-vitals`, `next/typescript`, and `prettier`.

## TypeScript Guidelines

- TypeScript is configured with `strict: true`.
- Prefer explicit props, request, and response types.
- Prefer narrow unions over loose strings where the domain is known.
- Avoid introducing new `any` even though the lint rule is disabled.
- Keep types near the feature that owns them.
- `ReturnType<typeof useX>` is fine for exported hook return types.

## React and Next.js Guidelines

- Add `'use client';` only to interactive components, hooks, and providers.
- Leave route wrappers server-side when they only compose module UI.
- Wire global providers through `src/app/layout.tsx`.
- Prefer composition over large route files that hold business logic.
- Keep local UI state local unless Redux is actually needed.

## Forms and Validation

- The common pattern is `react-hook-form` + `zod` + `zodResolver`.
- Define form schemas near the form when they are form-specific.
- Use `z.infer<typeof schema>` for form value types.
- Keep default values typed and extracted if that improves readability.

## API and State Management

- Extend the shared base slice with `api.injectEndpoints()`.
- Always set `extraOptions: { service: 'serviceName' }` for service-routed endpoints.
- Use tag types consistently for cache invalidation.
- Prefer RTK Query hooks over manual `fetch` calls.
- Keep Redux slices inside the owning module unless the state is genuinely shared.
- Reuse the centralized auth header and token refresh logic in `src/lib/store/api/apiSlice.ts`.

## Error Handling

- Use `.unwrap()` on RTK Query mutations inside `try/catch`.
- Normalize user-facing errors with `getErrorMessage(...)` when applicable.
- Show success and failure feedback with the shared notification/toast helpers.
- Do not reimplement auth or retry logic inside feature modules.

## UI and Styling

- Use `@/shared/components/ui/` before creating new primitives.
- Use the shared `cn()` helper for conditional classes.
- Follow the Tailwind + Radix + Shadcn patterns already present in the module.
- Prefer semantic theme tokens from `src/app/globals.css` over ad-hoc colors.
- Preserve accessibility, keyboard support, focus states, and responsive behavior.

## Naming Conventions

- Route files: `page.tsx`, `layout.tsx`.
- React components: `PascalCase`.
- Page components: `SomethingPage.tsx`.
- Forms/dialogs/layouts: `SomethingForm.tsx`, `SomethingDialog.tsx`, `SomethingLayout.tsx`.
- Hooks: `useSomething.ts`.
- Redux slices: `somethingSlice.ts`.
- Type files: `something.types.ts`.
- Shared UI primitive files: kebab-case such as `button.tsx` and `alert-dialog.tsx`.
- API files should follow the local module convention (`salesApi.ts` or legacy `users.api.ts`).

## Comments and File Headers

- Many source files include an author/description header; match the surrounding pattern when adding new source files.

## Practical Agent Rules

- Do not invent unsupported test commands.
- Do not move business logic into `src/app/` if it belongs in a feature module.
- Do not add new cross-module dependencies when `src/shared/` is the better home.
- Do not bypass shared UI, store, or API infrastructure unless there is a clear reason.
- When unsure, inspect nearby files in the same module and follow the local pattern first.
