/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Legacy TMS vehicles redirect
 */

import { redirect } from 'next/navigation';

export default function VehiclesScopePage() {
  redirect('/first-mile/settings/vehicles');
}
