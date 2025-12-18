import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';

import {
  ThemeProvider,
  NotificationProvider,
  StoreProvider,
} from '@/shared/providers';
import { NotificationToastProvider } from '@/modules/notifications';

import './globals.css';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

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
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <StoreProvider>
          <ThemeProvider>
            <NotificationProvider>
              <NotificationToastProvider />
              {children}
            </NotificationProvider>
          </ThemeProvider>
        </StoreProvider>
      </body>
    </html>
  );
}
