import type { ReactNode } from 'react';
import { SchoolBusLayout } from '@/modules/school-bus/components/layout/SchoolBusLayout';

export default function Layout({ children }: { children: ReactNode }) {
  return <SchoolBusLayout>{children}</SchoolBusLayout>;
}
