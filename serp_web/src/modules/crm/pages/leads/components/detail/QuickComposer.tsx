interface QuickComposerProps {
  leadId: string;
  onAddNote: (content: string) => Promise<void>;
  onAddActivity: (data: { subject: string; type: string; notes?: string }) => Promise<void>;
}

export function QuickComposer({ leadId, onAddNote, onAddActivity }: QuickComposerProps) {
  return <div className="p-4 border rounded-xl bg-card text-card-foreground">Quick Composer Skeleton</div>;
}
