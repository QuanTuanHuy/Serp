/**
 * Author: SERP Project
 * Description: Part of Serp Project - Delivery manifests legacy route page
 */

import { redirect } from 'next/navigation';

export default function FirstMileDeliveryManifestsPage() {
  redirect('/first-mile/dispatchers/last-mile');
}
