/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile hubs route redirect
 */

import { redirect } from 'next/navigation';

export default function HubsPage() {
  redirect('/first-mile/network/hub');
}
