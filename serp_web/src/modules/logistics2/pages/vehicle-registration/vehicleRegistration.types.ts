/*
Author: QuanTuanHuy
Description: Part of Serp Project - Vehicle registration page types
*/

import type { VehicleShipper } from '../../types';

export interface VehicleRegistrationDay {
  date: Date;
  dateKey: string;
  isCurrentMonth: boolean;
  isPast: boolean;
  canRegister: boolean;
  registrations: VehicleShipper[];
}
