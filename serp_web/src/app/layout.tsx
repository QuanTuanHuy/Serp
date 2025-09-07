import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';

import { Header } from '@/components/layout/header';
import { MainLayout } from '@/components/layout/main-layout';
import { ThemeProvider } from '@/components/theme-provider';
import { Toaster } from '@/components/ui/sonner';
import { ReduxProvider } from '@/lib/redux-provider';

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
  title: 'SERP Web - Enterprise Resource Planning',
  description: 'Modern ERP system built with Next.js and TypeScript',
  keywords: ['ERP', 'CRM', 'Accounting', 'Inventory', 'Business Management'],
  authors: [{ name: 'QuanTuanHuy' }],
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
        <ThemeProvider
          attribute='class'
          defaultTheme='system'
          enableSystem
          disableTransitionOnChange
        >
          <ReduxProvider>
            <MainLayout>
              <Header />
              {children}
            </MainLayout>
            <Toaster />
          </ReduxProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
