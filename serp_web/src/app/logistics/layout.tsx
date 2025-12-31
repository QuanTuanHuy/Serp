/**
 * Logistics Layout - Logistics Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Logistics layout with sidebar navigation
 */

import React from 'react';
import { LogisticsLayout } from '@/modules/logistics/components/layout';

interface LogisticsLayoutPageProps {
  children: React.ReactNode;
}

export default function LogisticsLayoutPage({
  children,
}: LogisticsLayoutPageProps) {
  return <LogisticsLayout>{children}</LogisticsLayout>;
}
