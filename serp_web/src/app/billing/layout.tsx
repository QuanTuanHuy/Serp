/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing layout route
 */

import { FirstMileLayout } from '@/modules/first-mile';

export default function BillingLayoutRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  return <FirstMileLayout>{children}</FirstMileLayout>;
}
