/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS network route redirect
 */

import { redirect } from 'next/navigation';

export default function FirstMileNetworkPage() {
  redirect('/first-mile/network/post-office');
}
