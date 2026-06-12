/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile post offices route redirect
 */

import { redirect } from 'next/navigation';

interface FirstMilePostOfficesRoutePageProps {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}

export default async function FirstMilePostOfficesRoutePage({
  searchParams,
}: FirstMilePostOfficesRoutePageProps) {
  const params = await searchParams;
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => query.append(key, item));
      return;
    }

    if (value !== undefined) {
      query.set(key, value);
    }
  });

  const queryString = query.toString();

  redirect(
    queryString
      ? `/first-mile/network/post-offices?${queryString}`
      : '/first-mile/network/post-offices'
  );
}
