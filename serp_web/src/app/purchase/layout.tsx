/**
 * Purchase Layout - Purchase Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Purchase layout with sidebar navigation
 */

import React from 'react';
import { PurchaseLayout } from '@/modules/purchase/components/layout';

interface PurchaseLayoutPageProps {
  children: React.ReactNode;
}

export default function PurchaseLayoutPage({
  children,
}: PurchaseLayoutPageProps) {
  return <PurchaseLayout>{children}</PurchaseLayout>;
}
