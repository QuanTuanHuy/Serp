/**
 * Theme Provider - Dark/Light Mode Support
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import { ThemeProvider as NextThemesProvider } from 'next-themes';
import type { Attribute } from 'next-themes';
import * as React from 'react';

type ThemeProviderProps = {
  children: React.ReactNode;
  attribute?: Attribute;
  defaultTheme?: string;
  enableSystem?: boolean;
  disableTransitionOnChange?: boolean;
};

export function ThemeProvider({
  children,
  attribute = 'class',
  defaultTheme = 'system',
  enableSystem = true,
  disableTransitionOnChange = true,
  ...props
}: ThemeProviderProps) {
  return (
    <NextThemesProvider
      attribute={attribute}
      defaultTheme={defaultTheme}
      enableSystem={enableSystem}
      disableTransitionOnChange={disableTransitionOnChange}
      {...props}
    >
      {children}
    </NextThemesProvider>
  );
}
