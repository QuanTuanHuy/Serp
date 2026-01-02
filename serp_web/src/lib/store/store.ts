/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Central Redux store setup with RTK Query and persistence
 */

import { configureStore, combineReducers } from '@reduxjs/toolkit';
import {
  persistStore,
  persistReducer,
  FLUSH,
  REHYDRATE,
  PAUSE,
  PERSIST,
  PURGE,
  REGISTER,
} from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import { api } from './api';
import { websocketMiddleware } from './middleware/websocketMiddleware';

// Import feature slices
import { authSlice, userSlice } from '@/modules/account/store';
import { crmReducer } from '@/modules/crm/store';
import { adminReducer } from '@/modules/admin/store';
import { ptmReducer } from '@/modules/ptm';
import { salesReducer } from '@/modules/sales/store';
import { purchaseReducer } from '@/modules/purchase/store';
import { logisticsReducer } from '@/modules/logistics/store';
import { notificationReducer } from '@/modules/notifications';

// Persist configuration
const accountPersistConfig = {
  key: 'account',
  storage,
  whitelist: ['auth', 'user'],
};

// reducer
const accountReducer = combineReducers({
  auth: authSlice,
  user: userSlice,
});

// Root reducer
const rootReducer = combineReducers({
  [api.reducerPath]: api.reducer,
  account: persistReducer(accountPersistConfig, accountReducer),
  crm: crmReducer,
  admin: adminReducer,
  ptm: ptmReducer,
  sales: salesReducer,
  purchase: purchaseReducer,
  logistics: logisticsReducer,
  notifications: notificationReducer,
});

// Configure store
export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    })
      // Add RTK Query middleware
      .concat(api.middleware)
      .concat(websocketMiddleware),

  // Enable Redux DevTools in development
  devTools: process.env.NODE_ENV !== 'production',
});

// Create persistor
export const persistor = persistStore(store);

// Export types for TypeScript
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export type AppStore = typeof store;

// Reset store action for logout
export const resetStore = () => {
  persistor.purge();
  return { type: 'RESET_STORE' };
};
