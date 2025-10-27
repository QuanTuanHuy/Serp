/**
 * PTM Layout - Personal Task Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM layout with sidebar navigation
 */

import React from 'react';
import { PTMLayout } from '@/modules/ptm/components';

interface PTMLayoutPageProps {
  children: React.ReactNode;
}

export default function PTMLayoutPage({ children }: PTMLayoutPageProps) {
  return <PTMLayout>{children}</PTMLayout>;
}
