/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification preference API types
 */

export interface QuietHoursDto {
  enabled: boolean;
  start?: string;
  end?: string;
}

export interface PreferenceResponse {
  enableInApp: boolean;
  enableEmail: boolean;
  enablePush: boolean;
  quietHours: QuietHoursDto;
}

export interface UpdatePreferenceRequest {
  enableInApp?: boolean;
  enableEmail?: boolean;
  enablePush?: boolean;
  quietHoursEnabled?: boolean;
  quietHoursStart?: number;
  quietHoursEnd?: number;
}
