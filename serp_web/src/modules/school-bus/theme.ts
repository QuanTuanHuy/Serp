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
  primary: '#C81E3A',
  primaryHover: '#B31B34',
  primaryActive: '#99182D',
  primarySoft: '#FDECEF',
  primaryBorder: '#F6CDD5',
} as const;

export const schoolBusThemeStyle = {
  '--primary': schoolBusBrand.primary,
  '--primary-foreground': schoolBusBrand.white,
  '--ring': schoolBusBrand.primary,
  '--sidebar-primary': schoolBusBrand.primary,
  '--sidebar-primary-foreground': schoolBusBrand.white,
} as CSSProperties;

export const schoolBusUi = {
  pageGradient:
    'bg-background text-foreground dark:bg-[radial-gradient(circle_at_top_left,rgba(200,30,58,0.06),transparent_34%)]',
  header:
    'border-b border-border bg-background/90 shadow-[0_4px_20px_rgba(15,23,42,0.04)] backdrop-blur-xl',
  hero:
    'relative overflow-hidden rounded-2xl border border-border bg-card p-6 text-card-foreground shadow-sm',
  section:
    'rounded-[28px] border border-border bg-card p-5 text-card-foreground shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  card:
    'rounded-[28px] border border-border bg-card text-card-foreground shadow-[0_18px_50px_rgba(15,23,42,0.06)]',
  interactiveCard:
    'rounded-2xl border border-border bg-card p-4 text-card-foreground shadow-[0_12px_32px_rgba(15,23,42,0.04)] transition hover:bg-muted/40 hover:shadow-sm',
  primaryButton:
    'rounded-lg bg-[#C81E3A] text-white shadow-sm hover:bg-[#B31B34] active:bg-[#99182D] transition',
  outlineButton:
    'rounded-lg border border-border bg-background text-foreground shadow-sm hover:bg-muted transition',
  ghostButton:
    'rounded-lg text-muted-foreground hover:bg-muted hover:text-foreground transition',
  iconButton:
    'rounded-lg border border-border bg-background text-muted-foreground shadow-sm hover:bg-muted hover:text-foreground transition',
  dangerButton:
    'rounded-lg bg-red-600 text-white shadow-sm hover:bg-red-700 transition',
  eyebrow:
    'text-[10px] font-bold uppercase tracking-[0.28em] text-muted-foreground',
  heading: 'font-semibold tracking-tight text-foreground',
  mutedText: 'text-muted-foreground',
  tableWrap:
    'overflow-hidden rounded-[24px] border border-border bg-card text-card-foreground shadow-[0_14px_36px_rgba(15,23,42,0.05)]',
  mapFrame:
    'overflow-hidden rounded-2xl border border-border bg-card text-card-foreground shadow-sm',
  subtlePanel:
    'rounded-2xl border border-border bg-muted/40 p-4',
} as const;
