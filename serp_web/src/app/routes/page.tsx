/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Routes entry redirect
 */

import { redirect } from 'next/navigation';

export default function RoutesEntryPage() {
  redirect('/first-mile/network/route');
}
