'use client';

import * as React from 'react';
import type { SchoolBusListParams } from '../types';

export function useSchoolBusPagination(
  initial: SchoolBusListParams = {
    page: 0,
    size: 10,
    sortDirection: 'DESC',
  }
) {
  const [params, setParams] = React.useState<SchoolBusListParams>(initial);

  const setPage = React.useCallback((page: number) => {
    setParams((current) => ({ ...current, page: Math.max(page, 0) }));
  }, []);

  const setKeyword = React.useCallback((keyword: string) => {
    setParams((current) => ({ ...current, keyword, page: 0 }));
  }, []);

  return { params, setParams, setPage, setKeyword };
}
