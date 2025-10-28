/**
 * CRM Layout - Customer Relationship Management Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM layout with sidebar navigation
 */

import React from 'react';
import { CRMLayout } from '@/modules/crm/components/layout';

interface CRMLayoutPageProps {
  children: React.ReactNode;
}

export default function CRMLayoutPage({ children }: CRMLayoutPageProps) {
  return <CRMLayout>{children}</CRMLayout>;
}
