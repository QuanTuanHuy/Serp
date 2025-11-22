/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase Module Redirect
*/

import { redirect } from 'next/navigation';

export default function PurchasePage() {
  redirect('/purchase/products');
}
