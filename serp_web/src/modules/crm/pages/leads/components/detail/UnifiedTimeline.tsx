import type { Activity } from '../../../../types';
import type { Note } from '../../../../api/noteApi';

interface UnifiedTimelineProps {
  activities: Activity[];
  notes: Note[];
  isLoading: boolean;
}

export function UnifiedTimeline({ activities, notes, isLoading }: UnifiedTimelineProps) {
  return <div className="p-4 border rounded-xl bg-card text-card-foreground">Unified Timeline Skeleton</div>;
}
