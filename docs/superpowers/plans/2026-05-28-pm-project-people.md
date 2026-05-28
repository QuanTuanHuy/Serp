# PM Project People Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build user-only project People management for `pm_core` and integrate it as a `People` tab in `serp_web` PM project detail.

**Architecture:** Backend adds aggregate People command/query handlers over existing `ProjectRoleActor` primitives, leaving existing role actor APIs unchanged. Frontend adds PM RTK Query endpoints and a hybrid people table that uses PM aggregate APIs plus existing settings org user search.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js App Router, React 19, TypeScript, RTK Query, Tailwind, shared Shadcn UI.

---

## File Map

Backend create:
- `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ListProjectPeopleQuery.java` - query input.
- `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ProjectPeopleView.java` - aggregate response view.
- `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ListProjectPeopleQueryHandler.java` - read aggregation and permission check.
- `pm_core/src/main/java/serp/project/pmcore/application/project/command/people/replace/ReplaceProjectPersonRolesCommand.java` - replace roles command input.
- `pm_core/src/main/java/serp/project/pmcore/application/project/command/people/replace/ReplaceProjectPersonRolesCommandHandler.java` - replace user role memberships.
- `pm_core/src/main/java/serp/project/pmcore/application/project/command/people/remove/RemoveProjectPersonCommand.java` - remove person command input.
- `pm_core/src/main/java/serp/project/pmcore/application/project/command/people/remove/RemoveProjectPersonCommandHandler.java` - remove all user role memberships.
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/people/ProjectPeopleController.java` - REST endpoints.
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/people/dto/request/ReplaceProjectPersonRolesRequest.java` - `roleIds` request.
- `pm_core/src/test/java/serp/project/pmcore/application/project/people/ProjectPeopleHandlersTest.java` - handler tests.

Backend modify:
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java` - add `PROJECT_PEOPLE` path.
- `pm_core/src/main/java/serp/project/pmcore/domain/project/service/IProjectRoleActorService.java` - add list-by-project and remove-by-project-user methods.
- `pm_core/src/main/java/serp/project/pmcore/domain/project/service/impl/ProjectRoleActorService.java` - delegate new methods.
- `pm_core/src/main/java/serp/project/pmcore/domain/project/port/IProjectRoleActorPort.java` - add port methods.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ProjectRoleActorAdapter.java` - implement port methods.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IProjectRoleActorRepository.java` - add repository queries.

Frontend create:
- `serp_web/src/app/pm/projects/[projectId]/(detail)/people/page.tsx` - route wrapper.
- `serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx` - page UI.

Frontend modify:
- `serp_web/src/modules/pm/components/projects/PMProjectsTopTabs.tsx` - add `People` tab.
- `serp_web/src/modules/pm/api/projectApi.ts` - add people endpoints.
- `serp_web/src/modules/pm/types/project-api.types.ts` - add people API types.
- `serp_web/src/lib/store/api/apiSlice.ts` - add `pm/ProjectPeople` tag.

---

### Task 1: Backend Role Actor Bulk Support

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/project/service/IProjectRoleActorService.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/project/service/impl/ProjectRoleActorService.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/project/port/IProjectRoleActorPort.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ProjectRoleActorAdapter.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IProjectRoleActorRepository.java`

- [ ] **Step 1: Add service methods**

In `IProjectRoleActorService`, add:

```java
List<ProjectRoleActorEntity> getActorsByProject(Long projectId, Long tenantId);

void removeUserActorsByProject(Long tenantId,
                               Long projectId,
                               String subjectId,
                               Long userId);
```

- [ ] **Step 2: Add port methods**

In `IProjectRoleActorPort`, add matching methods:

```java
List<ProjectRoleActorEntity> getProjectRoleActorsByProjectId(Long projectId, Long tenantId);

void softDeleteActiveUserAssignmentsByProject(Long tenantId,
                                              Long projectId,
                                              String subjectId,
                                              Long userId);
```

- [ ] **Step 3: Add repository methods**

In `IProjectRoleActorRepository`, add:

```java
List<ProjectRoleActorModel> findByProjectIdAndTenantId(Long projectId, Long tenantId);

@Modifying
@Query("""
        UPDATE ProjectRoleActorModel actor
        SET actor.deletedAt = CURRENT_TIMESTAMP,
            actor.updatedAt = CURRENT_TIMESTAMP,
            actor.updatedBy = :userId
        WHERE actor.tenantId = :tenantId
          AND actor.projectId = :projectId
          AND actor.subjectType = 'USER'
          AND actor.subjectId = :subjectId
          AND actor.deletedAt IS NULL
        """)
void softDeleteActiveUserAssignmentsByProject(@Param("tenantId") Long tenantId,
                                              @Param("projectId") Long projectId,
                                              @Param("subjectId") String subjectId,
                                              @Param("userId") Long userId);
```

- [ ] **Step 4: Implement adapter/service delegation**

In adapter, map repository models using existing mapper. In service, delegate:

```java
@Override
public List<ProjectRoleActorEntity> getActorsByProject(Long projectId, Long tenantId) {
    return projectRoleActorPort.getProjectRoleActorsByProjectId(projectId, tenantId);
}

@Override
public void removeUserActorsByProject(Long tenantId, Long projectId, String subjectId, Long userId) {
    projectRoleActorPort.softDeleteActiveUserAssignmentsByProject(tenantId, projectId, subjectId, userId);
}
```

- [ ] **Step 5: Run compile check**

Run from `pm_core`: `./mvnw.cmd -DskipTests compile`

Expected: compile passes or only reveals missing imports from edited files.

---

### Task 2: Backend People Query

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ListProjectPeopleQuery.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ProjectPeopleView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/people/list/ListProjectPeopleQueryHandler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/project/people/ProjectPeopleHandlersTest.java`

- [ ] **Step 1: Write failing tests**

Create test class with tests:

```java
@Test
void listPeopleShouldGroupUserActorsAndIncludeLead() {
    // Arrange project lead 55, user 99 has two roles, lead has no role actor.
    // Mock role actors, roles, user profiles.
    // Act handler.handle(new ListProjectPeopleQuery(PROJECT_ID, TENANT_ID, USER_ID, Set.of("admins"))).
    // Assert BROWSE_PROJECTS checked, result has user 99 with two role chips, result has lead 55 with empty roles and isProjectLead true.
}

@Test
void listPeopleShouldUseEarliestActorCreatedAtAsAddedAt() {
    // Arrange same USER subject with actor createdAt 2000 and 1000.
    // Assert addedAt is 1000.
}
```

- [ ] **Step 2: Create query record**

```java
public record ListProjectPeopleQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) {
}
```

- [ ] **Step 3: Create view record**

```java
public record ProjectPeopleView(
        Long userId,
        String name,
        String email,
        String avatarUrl,
        boolean projectLead,
        List<RoleView> roles,
        Long addedAt
) {
    public record RoleView(Long id, String name, boolean system) {
    }
}
```

- [ ] **Step 4: Implement handler**

Implement `ListProjectPeopleQueryHandler` with constructor deps: `IProjectService`, `IProjectRoleActorService`, `IProjectRoleService`, `IProjectPermissionEvaluationService`, `IUserService`.

Core behavior:

```java
ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
projectPermissionEvaluationService.checkPermission(
        ProjectPermissionSubject.from(project),
        buildContext(query.userId(), query.groupKeys()),
        ProjectPermissionKeys.BROWSE_PROJECTS
);
List<ProjectRoleActorEntity> userActors = projectRoleActorService.getActorsByProject(query.projectId(), query.tenantId())
        .stream()
        .filter(actor -> "USER".equalsIgnoreCase(actor.getSubjectType()))
        .toList();
```

Group by numeric `subjectId`, fetch user profiles with `userService.getUserProfilesByIds`, fetch roles with `projectRoleService.getProjectRoleByIdIncludingSystem`, include lead id even with no actors, sort by name/email/user id.

- [ ] **Step 5: Run focused tests**

Run from `pm_core`: `./mvnw.cmd -Dtest=ProjectPeopleHandlersTest test`

Expected: query tests pass.

---

### Task 3: Backend People Mutations And Controller

**Files:**
- Create: command records/handlers under `application/project/command/people/replace` and `remove`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/people/ProjectPeopleController.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/people/dto/request/ReplaceProjectPersonRolesRequest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/project/people/ProjectPeopleHandlersTest.java`

- [ ] **Step 1: Add mutation tests**

Add tests:

```java
@Test
void replaceRolesShouldRemoveExistingUserActorsAndAssignRequestedRoles() {
    // Arrange existing roles 20,21 and request 21,22.
    // Assert ADMINISTER_PROJECTS checked, removeUserActorsByProject called, assignActorIfAbsent called for 21 and 22.
}

@Test
void replaceRolesShouldRejectEmptyRoleIds() {
    // Assert DomainValidationException with ROLE_ACTOR_SUBJECT_INVALID or validation error code.
}

@Test
void removePersonShouldRemoveAllUserActors() {
    // Assert ADMINISTER_PROJECTS checked and removeUserActorsByProject called.
}
```

- [ ] **Step 2: Add request DTO**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceProjectPersonRolesRequest {
    @NotEmpty(message = "roleIds is required")
    private List<@NotNull Long> roleIds;
}
```

- [ ] **Step 3: Add path constant**

```java
public static final String PROJECT_PEOPLE = PROJECTS + "/{projectId}/people";
```

- [ ] **Step 4: Implement replace command/handler**

Command:

```java
public record ReplaceProjectPersonRolesCommand(Long projectId, Long personUserId, List<Long> roleIds, Long tenantId, Long userId, Set<String> groupKeys) {
}
```

Handler validates project writable, `ADMINISTER_PROJECTS`, user exists via `userService.getUserById`, each role exists, removes all existing user actors, assigns `USER` actor for each requested role with subject id `String.valueOf(personUserId)`.

- [ ] **Step 5: Implement remove command/handler**

Command:

```java
public record RemoveProjectPersonCommand(Long projectId, Long personUserId, Long tenantId, Long userId, Set<String> groupKeys) {
}
```

Handler validates project writable and `ADMINISTER_PROJECTS`, then calls `removeUserActorsByProject(tenantId, projectId, String.valueOf(personUserId), userId)`.

- [ ] **Step 6: Implement controller**

Controller methods:

```java
@GetMapping
public ResponseEntity<GeneralResponse<List<ProjectPeopleView>>> listProjectPeople(@PathVariable Long projectId) { ... }

@PutMapping("/{userId}/roles")
public ResponseEntity<GeneralResponse<?>> replaceProjectPersonRoles(@PathVariable Long projectId, @PathVariable Long userId, @Valid @RequestBody ReplaceProjectPersonRolesRequest request) { ... }

@DeleteMapping("/{userId}")
public ResponseEntity<GeneralResponse<?>> removeProjectPerson(@PathVariable Long projectId, @PathVariable Long userId) { ... }
```

- [ ] **Step 7: Run backend verification**

Run from `pm_core`: `./mvnw.cmd -Dtest=ProjectPeopleHandlersTest test`

Run from `pm_core`: `./mvnw.cmd clean compile`

Expected: tests and compile pass.

---

### Task 4: Frontend API And Routing

**Files:**
- Modify: `serp_web/src/lib/store/api/apiSlice.ts`
- Modify: `serp_web/src/modules/pm/types/project-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/projectApi.ts`
- Modify: `serp_web/src/modules/pm/components/projects/PMProjectsTopTabs.tsx`
- Create: `serp_web/src/app/pm/projects/[projectId]/(detail)/people/page.tsx`

- [ ] **Step 1: Add types**

Add to `project-api.types.ts`:

```ts
export interface PMProjectPersonRoleApi {
  id: number;
  name: string;
  system: boolean;
}

export interface PMProjectPersonApi {
  userId: number;
  name?: string | null;
  email?: string | null;
  avatarUrl?: string | null;
  projectLead: boolean;
  roles: PMProjectPersonRoleApi[];
  addedAt?: number | null;
}

export interface PMReplaceProjectPersonRolesRequest {
  roleIds: number[];
}
```

- [ ] **Step 2: Add cache tag**

Add `'pm/ProjectPeople'` to `apiSlice.ts` tagTypes.

- [ ] **Step 3: Add project API endpoints**

Add to `projectApi.ts`:

```ts
getPmProjectPeople: builder.query<PMProjectPersonApi[], number>({
  query: (projectId) => ({ url: `/projects/${projectId}/people`, method: 'GET' }),
  extraOptions: { service: 'pm' },
  transformResponse: createDataTransform<PMProjectPersonApi[]>(),
  providesTags: (_result, _error, projectId) => [
    { type: 'pm/ProjectPeople' as const, id: projectId },
  ],
}),
replacePmProjectPersonRoles: builder.mutation<
  void,
  { projectId: number; userId: number; body: PMReplaceProjectPersonRolesRequest }
>({
  query: ({ projectId, userId, body }) => ({
    url: `/projects/${projectId}/people/${userId}/roles`,
    method: 'PUT',
    body,
  }),
  extraOptions: { service: 'pm' },
  invalidatesTags: (_result, _error, { projectId }) => [
    { type: 'pm/ProjectPeople' as const, id: projectId },
  ],
}),
removePmProjectPerson: builder.mutation<void, { projectId: number; userId: number }>({
  query: ({ projectId, userId }) => ({
    url: `/projects/${projectId}/people/${userId}`,
    method: 'DELETE',
  }),
  extraOptions: { service: 'pm' },
  invalidatesTags: (_result, _error, { projectId }) => [
    { type: 'pm/ProjectPeople' as const, id: projectId },
  ],
}),
```

- [ ] **Step 4: Add tab**

In `PMProjectsTopTabs.tsx`, insert after `Components`:

```ts
{
  key: 'people',
  label: 'People',
  href: (projectId) => `/pm/projects/${projectId}/people`,
},
```

- [ ] **Step 5: Add route wrapper**

Create page:

```tsx
import { PMProjectPeoplePage } from '@/modules/pm/pages/PMProjectPeoplePage';

export default async function ProjectPeopleRoute({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  return <PMProjectPeoplePage projectId={projectId} />;
}
```

---

### Task 5: Frontend People Page UI

**Files:**
- Create: `serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx`

- [ ] **Step 1: Implement page shell and data loading**

Use client component with `useDeferredValue`, local states for search/dialogs, `useGetPmProjectPeopleQuery`, `useGetPmProjectByIdQuery`, `useGetProjectRolesQuery`, `useGetOrganizationUsersQuery`, and mutations.

- [ ] **Step 2: Implement local filtering**

Filter people by lower-cased `name`, `email`, and role names.

- [ ] **Step 3: Implement table**

Render avatar/name/email, lead badge when `person.projectLead`, role chips, added date using timestamp formatter or fallback `-`, actions dropdown with `Edit roles` and `Remove`.

- [ ] **Step 4: Implement Add User dialog**

Search org users, exclude existing people by `userId`, select one user and one or more roles, submit `replacePmProjectPersonRoles({ projectId, userId, body: { roleIds } }).unwrap()`.

- [ ] **Step 5: Implement Edit Roles dialog**

Prefill selected role ids from row, require at least one role, submit same replace mutation.

- [ ] **Step 6: Implement Remove confirmation**

Use `AlertDialog`; on confirm call `removePmProjectPerson({ projectId, userId }).unwrap()` and show toast.

- [ ] **Step 7: Run frontend checks**

Run from `serp_web`: `npm run lint`

Run from `serp_web`: `npm run type-check`

Run from `serp_web`: `npm run format:check`

Expected: all pass.

---

### Task 6: Final Verification

**Files:** all touched files.

- [ ] **Step 1: Backend compile**

Run from `pm_core`: `./mvnw.cmd clean compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Backend focused tests**

Run from `pm_core`: `./mvnw.cmd -Dtest=ProjectPeopleHandlersTest test`

Expected: tests pass.

- [ ] **Step 3: Frontend checks**

Run from `serp_web`: `npm run lint`

Run from `serp_web`: `npm run type-check`

Run from `serp_web`: `npm run format:check`

Expected: all pass.

- [ ] **Step 4: Inspect diff**

Run from repo root: `git diff -- pm_core serp_web docs/superpowers`

Expected: only People feature and plan/spec docs changed.
