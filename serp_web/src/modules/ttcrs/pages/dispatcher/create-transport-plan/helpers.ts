import type { VehicleStatus } from '../../../types';
import { STATUS_ORDER } from './constants';

export function formatId(id: number): string {
  return `REQ-${String(id).padStart(5, '0')}`;
}

export function sortByStatus<T extends { status: VehicleStatus }>(items: T[]): T[] {
  return [...items].sort(
    (a, b) => (STATUS_ORDER[a.status] ?? 99) - (STATUS_ORDER[b.status] ?? 99)
  );
}
