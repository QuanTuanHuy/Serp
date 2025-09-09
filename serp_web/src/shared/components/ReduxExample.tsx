/**
 * Redux Example Component
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Example component to test Redux setup
 */

'use client';

import React from 'react';
import {
  useAppSelector,
  useAppDispatch,
  useAppLoading,
  useAppErrors,
} from '@/shared/hooks';

export const ReduxExample: React.FC = () => {
  const dispatch = useAppDispatch();
  const isLoading = useAppLoading();
  const errors = useAppErrors();

  // Example selector (will be useful when we have actual slices)
  const exampleState = useAppSelector((state) => {
    // This is just for demonstration
    return {
      hasApiState: !!(state as any).api,
      queriesCount: Object.keys((state as any).api?.queries || {}).length,
      mutationsCount: Object.keys((state as any).api?.mutations || {}).length,
    };
  });

  return (
    <div className='p-6 bg-card rounded-lg shadow-sm border'>
      <h3 className='text-lg font-semibold mb-4'>Redux Store Status</h3>

      <div className='space-y-2 text-sm'>
        <div className='flex justify-between'>
          <span>API State Available:</span>
          <span
            className={
              exampleState.hasApiState ? 'text-green-600' : 'text-red-600'
            }
          >
            {exampleState.hasApiState ? '✓ Yes' : '✗ No'}
          </span>
        </div>

        <div className='flex justify-between'>
          <span>Active Queries:</span>
          <span className='font-mono'>{exampleState.queriesCount}</span>
        </div>

        <div className='flex justify-between'>
          <span>Active Mutations:</span>
          <span className='font-mono'>{exampleState.mutationsCount}</span>
        </div>

        <div className='flex justify-between'>
          <span>Loading State:</span>
          <span className={isLoading ? 'text-blue-600' : 'text-gray-600'}>
            {isLoading ? '⏳ Loading' : '✓ Idle'}
          </span>
        </div>

        <div className='flex justify-between'>
          <span>Errors Count:</span>
          <span
            className={errors.length > 0 ? 'text-red-600' : 'text-green-600'}
          >
            {errors.length}
          </span>
        </div>
      </div>

      {errors.length > 0 && (
        <div className='mt-4 p-3 bg-red-50 border border-red-200 rounded'>
          <h4 className='text-red-800 font-medium'>Current Errors:</h4>
          <ul className='text-red-700 text-sm mt-1'>
            {errors.map((error, index) => (
              <li key={index}>• {JSON.stringify(error)}</li>
            ))}
          </ul>
        </div>
      )}

      <div className='mt-4 text-xs text-muted-foreground'>
        Redux store is successfully configured with RTK Query and persistence.
      </div>
    </div>
  );
};
