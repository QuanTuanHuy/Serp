/**
 * Sales Layout - Sales Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Sales layout with sidebar navigation
 */

import React from 'react';
import { SalesLayout } from '@/modules/sales/components/layout';

interface SalesLayoutPageProps {
  children: React.ReactNode;
}

export default function SalesLayoutPage({ children }: SalesLayoutPageProps) {
  return <SalesLayout>{children}</SalesLayout>;
}
