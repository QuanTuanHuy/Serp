'use client';

import type { CSSProperties } from 'react';

export const schoolBusBrand = {
  rose: '#e11d48',
  roseDark: '#be123c',
  roseSoft: '#fff1f2',
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
  '--primary': schoolBusBrand.rose,
  '--primary-foreground': schoolBusBrand.white,
  '--secondary': schoolBusBrand.slateSoft,
  '--secondary-foreground': schoolBusBrand.ink,
  '--muted': '#f1f5f9',
  '--muted-foreground': '#64748b',
  '--accent': schoolBusBrand.roseSoft,
  '--accent-foreground': schoolBusBrand.roseDark,
  '--border': schoolBusBrand.border,
  '--input': '#cbd5e1',
  '--ring': schoolBusBrand.rose,
  '--sidebar-accent': schoolBusBrand.roseSoft,
  '--sidebar-accent-foreground': schoolBusBrand.roseDark,
  '--sidebar-primary': schoolBusBrand.rose,
  '--sidebar-primary-foreground': schoolBusBrand.white,
} as CSSProperties;

export const schoolBusUi = {
  pageGradient:
    'bg-[radial-gradient(circle_at_top_left,rgba(225,29,72,0.10),transparent_34%),linear-gradient(180deg,#ffffff_0%,#f8fafc_48%,#ffffff_100%)] text-slate-950',
  header:
    'border-b border-slate-200/80 bg-white/90 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur-xl',
  hero:
    'relative overflow-hidden rounded-[32px] border border-rose-100 bg-white p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)]',
  section:
    'rounded-[28px] border border-slate-200/80 bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  card:
    'rounded-[28px] border border-slate-200/80 bg-white shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  interactiveCard:
    'rounded-2xl border border-slate-200 bg-white p-4 shadow-[0_12px_32px_rgba(15,23,42,0.04)] transition hover:border-rose-200 hover:bg-rose-50/35 hover:shadow-[0_18px_44px_rgba(225,29,72,0.10)]',
  primaryButton:
    'rounded-full bg-rose-600 text-white shadow-[0_12px_30px_rgba(225,29,72,0.24)] hover:bg-rose-700',
  outlineButton:
    'rounded-full border-rose-200 bg-white text-slate-900 shadow-sm hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700',
  ghostButton:
    'rounded-full text-slate-700 hover:bg-rose-50 hover:text-rose-700',
  iconButton:
    'rounded-full border-slate-200 bg-white text-slate-700 shadow-sm hover:border-rose-200 hover:bg-rose-50 hover:text-rose-700',
  dangerButton:
    'rounded-full bg-rose-600 text-white shadow-[0_12px_30px_rgba(225,29,72,0.24)] hover:bg-rose-700',
  eyebrow:
    'text-xs font-bold uppercase tracking-[0.28em] text-rose-600',
  heading: 'font-semibold tracking-tight text-slate-950',
  mutedText: 'text-slate-500',
  tableWrap:
    'overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-[0_14px_36px_rgba(15,23,42,0.05)]',
  mapFrame:
    'overflow-hidden rounded-[26px] border border-rose-100 bg-white shadow-[0_20px_54px_rgba(15,23,42,0.08)]',
  subtlePanel:
    'rounded-2xl border border-slate-200 bg-slate-50/80 p-4',
} as const;
