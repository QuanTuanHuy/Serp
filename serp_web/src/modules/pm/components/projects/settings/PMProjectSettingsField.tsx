/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings field
 */

interface PMProjectSettingsFieldProps {
  label: string;
  value: string;
}

export function PMProjectSettingsField({
  label,
  value,
}: PMProjectSettingsFieldProps) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 text-sm font-medium'>{value}</p>
    </div>
  );
}
