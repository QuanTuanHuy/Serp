'use client';

import type { CSSProperties } from 'react';

export const schoolBusBrand = {
  ink: '#0f172a',
  slate: '#475569',
  slateSoft: '#f8fafc',
  border: '#e2e8f0',
  white: '#ffffff',
  sky: '#0284c7',
  amber: '#d97706',
  emerald: '#059669',
} as const;

export const schoolBusThemeStyle = {
  '--background': schoolBusBrand.white,
  '--foreground': schoolBusBrand.ink,
  '--card': schoolBusBrand.white,
  '--card-foreground': schoolBusBrand.ink,
  '--popover': schoolBusBrand.white,
  '--popover-foreground': schoolBusBrand.ink,
  '--primary': schoolBusBrand.ink,
  '--primary-foreground': schoolBusBrand.white,
  '--secondary': schoolBusBrand.slateSoft,
  '--secondary-foreground': schoolBusBrand.ink,
  '--muted': '#f1f5f9',
  '--muted-foreground': '#64748b',
  '--accent': '#f1f5f9',
  '--accent-foreground': schoolBusBrand.ink,
  '--border': schoolBusBrand.border,
  '--input': '#cbd5e1',
  '--ring': schoolBusBrand.ink,
  '--sidebar-accent': '#f1f5f9',
  '--sidebar-accent-foreground': schoolBusBrand.ink,
  '--sidebar-primary': schoolBusBrand.ink,
  '--sidebar-primary-foreground': schoolBusBrand.white,
} as CSSProperties;

export const schoolBusUi = {
  pageGradient:
    'bg-[radial-gradient(circle_at_top_left,rgba(148,163,184,0.06),transparent_34%),linear-gradient(180deg,#ffffff_0%,#f8fafc_48%,#ffffff_100%)] text-slate-950',
  header:
    'border-b border-slate-200/80 bg-white/90 shadow-[0_4px_20px_rgba(15,23,42,0.04)] backdrop-blur-xl',
  hero:
    'relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm',
  section:
    'rounded-[28px] border border-slate-200/80 bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  card:
    'rounded-[28px] border border-slate-200/80 bg-white shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  interactiveCard:
    'rounded-2xl border border-slate-200 bg-white p-4 shadow-[0_12px_32px_rgba(15,23,42,0.04)] transition hover:border-slate-300 hover:bg-slate-50/50 hover:shadow-sm',
  primaryButton:
    'rounded-lg bg-slate-900 text-white shadow-sm hover:bg-slate-800 transition',
  outlineButton:
    'rounded-lg border border-slate-200 bg-white text-slate-900 shadow-sm hover:bg-slate-50 transition',
  ghostButton:
    'rounded-lg text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition',
  iconButton:
    'rounded-lg border border-slate-200 bg-white text-slate-700 shadow-sm hover:bg-slate-50 transition',
  dangerButton:
    'rounded-lg bg-red-600 text-white shadow-sm hover:bg-red-700 transition',
  eyebrow:
    'text-[10px] font-bold uppercase tracking-[0.28em] text-slate-500',
  heading: 'font-semibold tracking-tight text-slate-950',
  mutedText: 'text-slate-500',
  tableWrap:
    'overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-[0_14px_36px_rgba(15,23,42,0.05)]',
  mapFrame:
    'overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm',
  subtlePanel:
    'rounded-2xl border border-slate-200 bg-slate-50/80 p-4',
} as const;
