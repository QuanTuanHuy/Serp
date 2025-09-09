/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project
 */

'use client';

import React from 'react';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from '@/lib/store';

// Loading component for persistence rehydration
const PersistenceLoader: React.FC = () => (
  <div className='flex items-center justify-center min-h-screen'>
    <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary'></div>
  </div>
);

interface StoreProviderProps {
  children: React.ReactNode;
}

/**
 * Redux Store Provider with persistence
 * Wraps the app with Redux store and persistence gate
 */
export const StoreProvider: React.FC<StoreProviderProps> = ({ children }) => {
  return (
    <Provider store={store}>
      <PersistGate loading={<PersistenceLoader />} persistor={persistor}>
        {children}
      </PersistGate>
    </Provider>
  );
};

export default StoreProvider;
