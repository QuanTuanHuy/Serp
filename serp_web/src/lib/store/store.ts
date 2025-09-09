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
import { api } from './api/apiSlice';

// Import feature slices
import authSlice from '@/modules/auth/store/authSlice';
// import ptmSlice from '@/modules/ptm/store/ptmSlice';

// Persist configuration
const persistConfig = {
  key: 'serp-root',
  version: 1,
  storage,
  // Whitelist - only persist specific reducers
  whitelist: [
    'auth', // Persist auth state
    // 'user', // Persist user preferences
  ],
  // Blacklist - never persist these reducers
  blacklist: [
    'api', // Never persist API cache
  ],
};

// Root reducer combining all feature reducers
const rootReducer = combineReducers({
  // RTK Query API slice
  [api.reducerPath]: api.reducer,

  // Feature slices
  auth: authSlice,
  // crm: crmSlice,
  // ptm: ptmSlice,
  // accounting: accountingSlice,
  // inventory: inventorySlice,
});

// Persisted reducer
const persistedReducer = persistReducer(persistConfig, rootReducer);

// Configure store
export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these action types from redux-persist
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    })
      // Add RTK Query middleware
      .concat(api.middleware),

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
