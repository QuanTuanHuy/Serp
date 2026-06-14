/**
 * School Bus Module - Role-Based Access Control Helper
 *
 * Provides pure utility functions for role checking and route access control.
 * These functions are UX helpers only — backend authorization is the source of truth.
 *
 * Usage:
 *   const { user } = useUser();
 *   const roles = user?.roles ?? [];
 *   if (isSchoolBusAdmin(roles)) { ... }
 */

// ─── Role Constants ────────────────────────────────────────────────────────────

export const SCHOOL_BUS_ROLES = {
  ADMIN: 'SCHOOL_BUS_ADMIN',
  DISPATCHER: 'SCHOOL_BUS_DISPATCHER',
  DRIVER: 'SCHOOL_BUS_DRIVER',
  ATTENDANT: 'SCHOOL_BUS_ATTENDANT',
  PARENT: 'SCHOOL_BUS_PARENT',
} as const;

export type SchoolBusRole = (typeof SCHOOL_BUS_ROLES)[keyof typeof SCHOOL_BUS_ROLES];

// ─── Role Predicates ───────────────────────────────────────────────────────────

export function hasSchoolBusRole(roles: string[], role: SchoolBusRole): boolean {
  return roles.includes(role);
}

export function hasAnySchoolBusRole(roles: string[], check: SchoolBusRole[]): boolean {
  return check.some((r) => roles.includes(r));
}

export function isSchoolBusAdmin(roles: string[]): boolean {
  return roles.includes(SCHOOL_BUS_ROLES.ADMIN);
}

export function isSchoolBusDispatcher(roles: string[]): boolean {
  return roles.includes(SCHOOL_BUS_ROLES.DISPATCHER);
}

export function isSchoolBusDriver(roles: string[]): boolean {
  return roles.includes(SCHOOL_BUS_ROLES.DRIVER);
}

export function isSchoolBusAttendant(roles: string[]): boolean {
  return roles.includes(SCHOOL_BUS_ROLES.ATTENDANT);
}

export function isSchoolBusParent(roles: string[]): boolean {
  return roles.includes(SCHOOL_BUS_ROLES.PARENT);
}

/**
 * Returns true if the user has full operational authority (Admin or Dispatcher).
 */
export function isSchoolBusOperator(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER]);
}

/**
 * Returns true only when the user has the Parent role but does NOT have Admin or Dispatcher.
 * Use this for data-scope / UI-mode decisions. Admin+Parent should behave as operator, not parent.
 */
export function isSchoolBusParentOnly(roles: string[]): boolean {
  return isSchoolBusParent(roles) && !isSchoolBusOperator(roles);
}

/**
 * Returns true if the user has any recognized School Bus role.
 */
export function hasAnySchoolBusAccess(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [
    SCHOOL_BUS_ROLES.ADMIN,
    SCHOOL_BUS_ROLES.DISPATCHER,
    SCHOOL_BUS_ROLES.DRIVER,
    SCHOOL_BUS_ROLES.ATTENDANT,
    SCHOOL_BUS_ROLES.PARENT,
  ]);
}

// ─── Route Access ──────────────────────────────────────────────────────────────

/**
 * Route-to-allowed-roles mapping for the School Bus module.
 * Keys are path prefixes (exact or prefix match).
 */
const ROUTE_ACCESS_MAP: { path: string; roles: SchoolBusRole[] }[] = [
  {
    path: '/school-bus/schools',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER],
  },
  {
    path: '/school-bus/parents',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER],
  },
  {
    path: '/school-bus/fleet',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER],
  },
  {
    path: '/school-bus/dispatch',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER],
  },
  {
    path: '/school-bus/reports',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER],
  },
  {
    path: '/school-bus/students',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER, SCHOOL_BUS_ROLES.PARENT],
  },
  {
    path: '/school-bus/requests',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER, SCHOOL_BUS_ROLES.PARENT],
  },
  {
    path: '/school-bus/subscriptions',
    roles: [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER, SCHOOL_BUS_ROLES.PARENT],
  },
  {
    path: '/school-bus/trips',
    roles: [
      SCHOOL_BUS_ROLES.ADMIN,
      SCHOOL_BUS_ROLES.DISPATCHER,
      SCHOOL_BUS_ROLES.DRIVER,
      SCHOOL_BUS_ROLES.ATTENDANT,
      SCHOOL_BUS_ROLES.PARENT,
    ],
  },
  {
    path: '/school-bus/attendance',
    roles: [
      SCHOOL_BUS_ROLES.ADMIN,
      SCHOOL_BUS_ROLES.DISPATCHER,
      SCHOOL_BUS_ROLES.ATTENDANT,
      SCHOOL_BUS_ROLES.PARENT,
    ],
  },
  {
    path: '/school-bus/dashboard',
    roles: [
      SCHOOL_BUS_ROLES.ADMIN,
      SCHOOL_BUS_ROLES.DISPATCHER,
      SCHOOL_BUS_ROLES.DRIVER,
      SCHOOL_BUS_ROLES.ATTENDANT,
      SCHOOL_BUS_ROLES.PARENT,
    ],
  },
];

/**
 * Check if the user (by roles) is allowed to visit a specific school-bus path.
 * Falls back to true if no explicit rule is found (backend will enforce).
 */
export function canAccessSchoolBusRoute(roles: string[], pathname: string): boolean {
  const normalized = pathname.split('?')[0]; // strip query params

  for (const rule of ROUTE_ACCESS_MAP) {
    if (normalized === rule.path || normalized.startsWith(rule.path + '/')) {
      return hasAnySchoolBusRole(roles, rule.roles);
    }
  }

  // Root /school-bus — allow any school bus user
  if (normalized === '/school-bus') {
    return hasAnySchoolBusAccess(roles);
  }

  return true; // unknown paths: let backend decide
}

// ─── Landing Path ─────────────────────────────────────────────────────────────

/**
 * Role priority order (higher index = lower priority).
 * When user has multiple roles, highest-priority role wins.
 */
const ROLE_PRIORITY: SchoolBusRole[] = [
  SCHOOL_BUS_ROLES.ADMIN,
  SCHOOL_BUS_ROLES.DISPATCHER,
  SCHOOL_BUS_ROLES.ATTENDANT,
  SCHOOL_BUS_ROLES.DRIVER,
  SCHOOL_BUS_ROLES.PARENT,
];

/**
 * Get the effective (highest-priority) School Bus role for a user.
 */
export function getEffectiveSchoolBusRole(roles: string[]): SchoolBusRole | null {
  for (const role of ROLE_PRIORITY) {
    if (roles.includes(role)) return role;
  }
  return null;
}

/**
 * Get the appropriate landing path for the user's highest-priority School Bus role.
 *
 * - ADMIN / DISPATCHER → /school-bus/dashboard (full ops cockpit)
 * - DRIVER             → /school-bus/trips      (assigned trips)
 * - ATTENDANT          → /school-bus/attendance  (attendance workspace)
 * - PARENT             → /school-bus/dashboard  (child status dashboard)
 */
export function getSchoolBusLandingPath(roles: string[]): string {
  const role = getEffectiveSchoolBusRole(roles);

  switch (role) {
    case SCHOOL_BUS_ROLES.ADMIN:
    case SCHOOL_BUS_ROLES.DISPATCHER:
      return '/school-bus/dashboard';

    case SCHOOL_BUS_ROLES.DRIVER:
      return '/school-bus/trips';

    case SCHOOL_BUS_ROLES.ATTENDANT:
      return '/school-bus/attendance';

    case SCHOOL_BUS_ROLES.PARENT:
      return '/school-bus/dashboard';

    default:
      return '/school-bus/dashboard';
  }
}

// ─── Action Visibility Helpers ─────────────────────────────────────────────────

/**
 * Can the user perform trip lifecycle operations (Start, Arrive, Depart, Complete).
 * Backend permission: school-bus.trip.operate
 */
export function canOperateTrip(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [
    SCHOOL_BUS_ROLES.ADMIN,
    SCHOOL_BUS_ROLES.DISPATCHER,
    SCHOOL_BUS_ROLES.DRIVER,
  ]);
}

/**
 * Can the user mark attendance (Board, Absent, No-show, Drop-off, Not served).
 * Backend permission: school-bus.attendance.mark
 */
export function canMarkAttendance(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [
    SCHOOL_BUS_ROLES.ADMIN,
    SCHOOL_BUS_ROLES.DISPATCHER,
    SCHOOL_BUS_ROLES.ATTENDANT,
  ]);
}

/**
 * Can the user approve or reject transport requests / subscriptions.
 * Backend permission: school-bus.request.approve / school-bus.subscription.approve
 */
export function canApproveRequests(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER]);
}

/**
 * Can the user write (create/edit/delete) master data such as schools, fleet, depots.
 * Backend permission: school-bus.school.write / school-bus.fleet.write
 */
export function canWriteMasterData(roles: string[]): boolean {
  return isSchoolBusAdmin(roles);
}

/**
 * Can the user create or manage route planning sessions.
 * Backend permission: school-bus.planning.write / school-bus.dispatch.assign
 */
export function canManageDispatching(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER]);
}

/**
 * Can the user view and export reports.
 * Backend permission: school-bus.report.read / school-bus.report.export
 */
export function canAccessReports(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [SCHOOL_BUS_ROLES.ADMIN, SCHOOL_BUS_ROLES.DISPATCHER]);
}

/**
 * Can the user write student profiles and manage parent data.
 * Backend permission: school-bus.student.write / school-bus.parent.write
 */
export function canWriteStudentData(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [
    SCHOOL_BUS_ROLES.ADMIN,
    SCHOOL_BUS_ROLES.DISPATCHER,
    SCHOOL_BUS_ROLES.PARENT,
  ]);
}

/**
 * Can the user create or cancel transport requests.
 * Backend permission: school-bus.request.write
 */
export function canWriteRequest(roles: string[]): boolean {
  return hasAnySchoolBusRole(roles, [
    SCHOOL_BUS_ROLES.ADMIN,
    SCHOOL_BUS_ROLES.DISPATCHER,
    SCHOOL_BUS_ROLES.PARENT,
  ]);
}

// ─── useSchoolBusAccess Hook ───────────────────────────────────────────────────

/**
 * Convenience hook that wraps all access checks with the current user's roles.
 *
 * @example
 * ```tsx
 * const access = useSchoolBusAccess();
 * if (access.isOperator) { ... }
 * if (access.canOperateTrip) { ... }
 * ```
 */
import { useMemo } from 'react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';

export function useSchoolBusAccess() {
  const user = useAppSelector(selectUserProfile);
  const roles: string[] = user?.roles ?? [];

  return useMemo(
    () => ({
      roles,
      effectiveRole: getEffectiveSchoolBusRole(roles),
      landingPath: getSchoolBusLandingPath(roles),

      // Role predicates
      isAdmin: isSchoolBusAdmin(roles),
      isDispatcher: isSchoolBusDispatcher(roles),
      isDriver: isSchoolBusDriver(roles),
      isAttendant: isSchoolBusAttendant(roles),
      isParent: isSchoolBusParent(roles),
      isOperator: isSchoolBusOperator(roles),
      isParentOnly: isSchoolBusParentOnly(roles),

      // Action flags
      canOperateTrip: canOperateTrip(roles),
      canMarkAttendance: canMarkAttendance(roles),
      canApproveRequests: canApproveRequests(roles),
      canWriteMasterData: canWriteMasterData(roles),
      canManageDispatching: canManageDispatching(roles),
      canAccessReports: canAccessReports(roles),
      canWriteStudentData: canWriteStudentData(roles),
      canWriteRequest: canWriteRequest(roles),

      // Route access
      canAccessRoute: (pathname: string) => canAccessSchoolBusRoute(roles, pathname),
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [roles.join(',')]
  );
}
