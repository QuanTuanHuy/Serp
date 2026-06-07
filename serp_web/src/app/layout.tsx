import type { Metadata } from 'next';
import { GeistSans } from 'geist/font/sans';
import { GeistMono } from 'geist/font/mono';

import {
  ThemeProvider,
  NotificationProvider,
  StoreProvider,
} from '@/shared/providers';
import {
  NotificationToastProvider,
  NotificationUnreadSync,
} from '@/modules/notifications';

import './globals.css';

export const metadata: Metadata = {
  title: 'Smart Enterprise Resource Planning | SERP',
  description: 'Modern ERP system built with Next.js and TypeScript',
  keywords: ['ERP', 'CRM', 'Accounting', 'Inventory', 'Business Management'],
  authors: [{ name: 'QuanTuanHuy' }],
  icons: {
    icon: '/icon.svg',
  },
};

export const viewport = 'width=device-width, initial-scale=1';

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang='en' suppressHydrationWarning>
      <body
        className={`${GeistSans.variable} ${GeistMono.variable} antialiased`}
      >
        <StoreProvider>
          <ThemeProvider>
            <NotificationProvider>
              <NotificationUnreadSync />
              <NotificationToastProvider />
              {children}
            </NotificationProvider>
          </ThemeProvider>
        </StoreProvider>
      </body>
    </html>
  );
}
