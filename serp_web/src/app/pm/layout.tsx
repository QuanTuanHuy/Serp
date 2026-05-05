/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM module layout wrapper
 */

import { PMLayout as Layout } from '@/modules/pm';

export default function PMLayoutRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  return <Layout>{children}</Layout>;
}
