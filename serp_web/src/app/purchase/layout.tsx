/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase Module Layout
*/

import { ReactNode } from 'react';
import { PurchaseLayout } from '@/modules/purchase/components';

export default function Layout({ children }: { children: ReactNode }) {
  return <PurchaseLayout>{children}</PurchaseLayout>;
}
