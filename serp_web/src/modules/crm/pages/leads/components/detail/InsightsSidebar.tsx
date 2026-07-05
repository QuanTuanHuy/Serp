import type { Lead } from '../../../../types';

interface InsightsSidebarProps {
  lead: Lead;
  leadScore: number;
  onAssignLead: (assignedToId: number) => Promise<void>;
  onUpdateStatus: (toStatus: any, payload?: any) => Promise<void>;
  onOpenConvert: () => void;
  onOpenQualify: () => void;
  onOpenDisqualify: () => void;
  onOpenMeetingRequest: () => void;
}

export function InsightsSidebar({ lead, leadScore }: InsightsSidebarProps) {
  return <div className="p-4 border rounded-xl bg-card text-card-foreground">Insights Sidebar Skeleton</div>;
}
