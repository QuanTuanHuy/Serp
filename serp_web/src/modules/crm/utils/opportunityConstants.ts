// Author: QuanTuanHuy, Description: Part of Serp Project

import type { OpportunityStage } from '../types';

export const PIPELINE_STAGES: {
  stage: OpportunityStage;
  label: string;
  color: string;
  bgColor: string;
  probability: number;
}[] = [
  {
    stage: 'PROSPECTING',
    label: 'Prospecting',
    color: 'text-blue-700 dark:text-blue-300',
    bgColor: 'bg-blue-100 dark:bg-blue-900/50',
    probability: 10,
  },
  {
    stage: 'QUALIFICATION',
    label: 'Qualification',
    color: 'text-cyan-700 dark:text-cyan-300',
    bgColor: 'bg-cyan-100 dark:bg-cyan-900/50',
    probability: 25,
  },
  {
    stage: 'PROPOSAL',
    label: 'Proposal',
    color: 'text-yellow-700 dark:text-yellow-300',
    bgColor: 'bg-yellow-100 dark:bg-yellow-900/50',
    probability: 50,
  },
  {
    stage: 'NEGOTIATION',
    label: 'Negotiation',
    color: 'text-orange-700 dark:text-orange-300',
    bgColor: 'bg-orange-100 dark:bg-orange-900/50',
    probability: 75,
  },
  {
    stage: 'CLOSED_WON',
    label: 'Closed Won',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
    probability: 100,
  },
  {
    stage: 'CLOSED_LOST',
    label: 'Closed Lost',
    color: 'text-red-700 dark:text-red-300',
    bgColor: 'bg-red-100 dark:bg-red-900/50',
    probability: 0,
  },
];

export const ACTIVE_PIPELINE_STAGES = PIPELINE_STAGES.filter(
  ({ stage }) => stage !== 'CLOSED_WON' && stage !== 'CLOSED_LOST'
);

export const PIPELINE_STAGES_FOR_LIST = [
  { stage: 'PROSPECTING', label: 'Prospecting', color: 'bg-blue-500' },
  { stage: 'QUALIFICATION', label: 'Qualification', color: 'bg-cyan-500' },
  { stage: 'PROPOSAL', label: 'Proposal', color: 'bg-yellow-500' },
  { stage: 'NEGOTIATION', label: 'Negotiation', color: 'bg-orange-500' },
  { stage: 'CLOSED_WON', label: 'Won', color: 'bg-green-500' },
  { stage: 'CLOSED_LOST', label: 'Lost', color: 'bg-red-500' },
] as const;

export const OPPORTUNITY_TYPES = {
  NEW_BUSINESS: {
    label: 'New Business',
    color:
      'bg-purple-100 dark:bg-purple-900/50 text-purple-700 dark:text-purple-300',
  },
  EXISTING_BUSINESS: {
    label: 'Existing Business',
    color: 'bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-300',
  },
  RENEWAL: {
    label: 'Renewal',
    color:
      'bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-300',
  },
} as const;
