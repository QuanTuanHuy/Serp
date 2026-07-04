/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Legacy second-mile vehicles redirect
 */

import { redirect } from 'next/navigation';

export default function SecondMileVehiclesRoutePage() {
  redirect('/first-mile/settings/vehicles?scope=second-mile');
}
