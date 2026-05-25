/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project detail view model types
 */

import type { PMProjectVisibility } from './project-create.types';
import type { PMProjectListItem } from './project-list.types';

export interface PMProjectDetail extends PMProjectListItem {
  visibility: PMProjectVisibility;
  startDate: string;
  targetDate: string;
}
