import type { Lead } from '../../../../types';

interface LeadProfileSidebarProps {
  lead: Lead;
  isUpdating: boolean;
  onUpdateLead: (data: Partial<Lead>) => Promise<void>;
}

export function LeadProfileSidebar({ lead, isUpdating, onUpdateLead }: LeadProfileSidebarProps) {
  return <div className="p-4 border rounded-xl bg-card text-card-foreground">Lead Profile Sidebar Skeleton</div>;
}
