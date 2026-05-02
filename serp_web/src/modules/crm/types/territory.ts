// Territory Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity } from './base';

export type TerritoryLevel = 'PROVINCE_CITY';

export interface Territory extends BaseEntity {
  id: string;
  territoryCode: string;
  territoryName: string;
  territoryLevel: TerritoryLevel;
  countryCode: string;
  parentTerritoryCode?: string;
  active: boolean;
  source?: string;
}

export interface TeamTerritory extends BaseEntity {
  teamId: string;
  territoryCode: string;
  assignedBy?: number;
  active: boolean;
  territory?: Territory;
}

export interface TeamTerritoryResponse {
  teamId: string;
  territories: Territory[];
}

// Filter types
export interface TerritoryFilters {
  keyword?: string;
  active?: boolean;
  parentTerritoryCode?: string;
  source?: string;
}

// Request types
export interface CreateTerritoryRequest {
  territoryCode: string;
  territoryName: string;
  territoryLevel?: TerritoryLevel;
  countryCode?: string;
  parentTerritoryCode?: string;
  active?: boolean;
}

export interface UpdateTerritoryRequest {
  territoryName?: string;
  territoryLevel?: TerritoryLevel;
  countryCode?: string;
  parentTerritoryCode?: string;
}

export interface AssignTerritoriesRequest {
  territoryCodes: string[];
}
