/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Transit summary item
 */

interface SummaryItemProps {
  label: string;
  value: string;
}

export function SummaryItem({ label, value }: SummaryItemProps) {
  return (
    <div className='rounded-md border p-3'>
      <p className='text-xs text-muted-foreground'>{label}</p>
      <p className='mt-1 font-medium'>{value}</p>
    </div>
  );
}
