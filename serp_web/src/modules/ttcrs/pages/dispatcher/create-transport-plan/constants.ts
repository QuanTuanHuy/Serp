import type { RequestType, VehicleStatus } from '../../../types';

export const PAGE_SIZE = 20;

export const ROUTE_COLORS = [
  '#ef4444',
  '#3b82f6',
  '#22c55e',
  '#f59e0b',
  '#8b5cf6',
  '#ec4899',
  '#14b8a6',
  '#f97316',
  '#64748b',
  '#dc2626',
  '#0ea5e9',
  '#84cc16',
];

export const TYPE_CLASS: Record<RequestType, string> = {
  OF: 'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  IF: 'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  OE: 'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  IE: 'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
};

export const STATUS_ORDER: Record<VehicleStatus, number> = {
  AVAILABLE: 0,
  IN_USE: 1,
  MAINTENANCE: 2,
};

export const STEPS = [
  { n: 1, label: 'Select Requests' },
  { n: 2, label: 'Plan Setup' },
  { n: 3, label: 'Review & Assign' },
] as const;
