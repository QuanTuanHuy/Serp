/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module Layout
*/

import { ReactNode } from 'react';
import { LogisticsLayout } from '@/modules/logistics/components';

export default function Layout({ children }: { children: ReactNode }) {
  return <LogisticsLayout>{children}</LogisticsLayout>;
}
