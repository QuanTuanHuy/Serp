/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Logistics2 app layout route
 */

import { Logistics2Layout } from '@/modules/logistics2';

export default function Logistics2LayoutRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  return <Logistics2Layout>{children}</Logistics2Layout>;
}
