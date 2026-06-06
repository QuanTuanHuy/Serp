/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail page
 */

'use client';

import { useRouter } from 'next/navigation';
import { Alert, AlertDescription, AlertTitle } from '@/shared/components/ui';
import { PMWorkItemDetailContent } from '../components/work-items/detail';

interface PMWorkItemDetailPageProps {
  projectId: string;
  workItemId: string;
}

function toRouteNumber(value: string): number | null {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function PMWorkItemDetailPage({
  projectId,
  workItemId,
}: PMWorkItemDetailPageProps) {
  const router = useRouter();
  const numericProjectId = toRouteNumber(projectId);
  const numericWorkItemId = toRouteNumber(workItemId);

  if (!numericProjectId || !numericWorkItemId) {
    return (
      <div className='p-6'>
        <Alert variant='destructive'>
          <AlertTitle>Invalid work item route</AlertTitle>
          <AlertDescription>
            Project and work item identifiers must be positive numbers.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className='h-[calc(100vh-9rem)] min-h-[720px] overflow-hidden rounded-lg border bg-background'>
      <PMWorkItemDetailContent
        projectId={numericProjectId}
        workItemId={numericWorkItemId}
        onClose={() => router.back()}
      />
    </div>
  );
}
