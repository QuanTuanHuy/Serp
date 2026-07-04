/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Legacy first-mile vehicles redirect
 */

import { redirect } from 'next/navigation';

export default function FirstMileVehiclesFirstMileRoutePage() {
  redirect('/first-mile/settings/vehicles?scope=first-mile');
}
