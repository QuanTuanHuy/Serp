/*
Author: QuanTuanHuy
Description: Part of Serp Project - Main Discuss API (re-exports all endpoints)
*/

import { api } from '@/lib/store/api';

// This file serves as the main entry point for all discuss API endpoints
// Individual endpoints are organized in separate files for better maintainability

// Import and re-export all endpoint slices
import './channels.api';
import './messages.api';
import './attachments.api';

// Export the combined API
export const discussApi = api;

// Re-export all hooks from individual API files
export * from './channels.api';
export * from './messages.api';
export * from './attachments.api';
