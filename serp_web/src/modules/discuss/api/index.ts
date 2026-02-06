/*
Author: QuanTuanHuy
Description: Part of Serp Project - API barrel exports for Discuss module
*/

// Export all API endpoints
export * from './channels.api';
export * from './messages.api';
export * from './attachments.api';
export * from './presence.api';

// Export transformers
export * from './transformers';

// Re-export the main discussApi for backwards compatibility
export { discussApi } from './discussApi';
