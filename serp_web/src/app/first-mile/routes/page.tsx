/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile routes route redirect
 */

import { redirect } from 'next/navigation';

export default function FirstMileRoutesPage() {
  redirect('/first-mile/network/routes');
}
