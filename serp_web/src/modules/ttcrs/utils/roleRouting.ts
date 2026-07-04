const TTCRS_ROLE_PRIORITY = [
  { role: 'TTCRS_CUSTOMER', route: '/ttcrs/customer/requests' },
  { role: 'TTCRS_DRIVER', route: '/ttcrs/driver/routes' },
  { role: 'TTCRS_DISPATCHER', route: '/ttcrs/dispatcher/requests' },
] as const;

/**
 * Determines the target TTCRS route based on the user's roles.
 * Priority order: CUSTOMER > DRIVER > DISPATCHER (fallback).
 */
export function getTtcrsEntryRoute(roles?: string[]): string {
  if (!roles || roles.length === 0) {
    return '/ttcrs/dispatcher/requests';
  }

  const matched = TTCRS_ROLE_PRIORITY.find(({ role }) => roles.includes(role));
  return matched?.route ?? '/ttcrs/dispatcher/requests';
}
