/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS combobox adapter
 */

'use client';

import { Combobox, type ComboboxItem } from './ui/combobox';

export type TmsComboboxOption = ComboboxItem & {
  value: string;
  label: string;
};

interface TmsComboboxProps {
  id?: string;
  value?: string;
  onValueChange: (value: string) => void;
  options: TmsComboboxOption[];
  placeholder?: string;
  emptyText?: string;
  disabled?: boolean;
  loading?: boolean;
  clearable?: boolean;
  clearText?: string;
  className?: string;
}

export function TmsCombobox({
  id,
  value,
  onValueChange,
  options,
  placeholder,
  emptyText,
  disabled,
  loading,
  clearable = false,
  clearText,
  className,
}: TmsComboboxProps) {
  return (
    <Combobox
      id={id}
      value={value === '' ? undefined : value}
      onChange={(nextValue) => onValueChange(String(nextValue ?? ''))}
      items={options}
      placeholder={placeholder}
      emptyText={emptyText}
      disabled={disabled}
      loading={loading}
      clearable={clearable}
      clearText={clearText}
      className={className}
    />
  );
}
