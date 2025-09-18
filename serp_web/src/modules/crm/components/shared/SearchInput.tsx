// SearchInput Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Input } from '@/shared/components/ui';
import { cn, debounce } from '@/shared/utils';
import { useState, useEffect, useMemo } from 'react';

interface SearchInputProps {
  placeholder?: string;
  value?: string;
  onSearch: (query: string) => void;
  debounceMs?: number;
  className?: string;
  disabled?: boolean;
  icon?: React.ReactNode;
}

export const SearchInput: React.FC<SearchInputProps> = ({
  placeholder = 'Search...',
  value = '',
  onSearch,
  debounceMs = 300,
  className,
  disabled = false,
  icon,
}) => {
  const [localValue, setLocalValue] = useState(value);

  // Create debounced search function
  const debouncedSearch = useMemo(
    () => debounce((query: string) => onSearch(query), debounceMs),
    [onSearch, debounceMs]
  );

  // Handle input change
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setLocalValue(newValue);
    debouncedSearch(newValue);
  };

  // Clear search
  const handleClear = () => {
    setLocalValue('');
    onSearch('');
  };

  // Sync with external value changes
  useEffect(() => {
    setLocalValue(value);
  }, [value]);

  return (
    <div className={cn('relative', className)}>
      <div className='absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none'>
        {icon || (
          <svg
            className='h-4 w-4 text-gray-400'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth='2'
              d='M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'
            />
          </svg>
        )}
      </div>

      <Input
        type='text'
        placeholder={placeholder}
        value={localValue}
        onChange={handleChange}
        disabled={disabled}
        className={cn(
          'pl-10 pr-10',
          localValue && 'pr-10' // Make room for clear button
        )}
      />

      {localValue && !disabled && (
        <button
          onClick={handleClear}
          className='absolute inset-y-0 right-0 pr-3 flex items-center hover:text-gray-600'
          title='Clear search'
        >
          <svg
            className='h-4 w-4 text-gray-400 hover:text-gray-600'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth='2'
              d='M6 18L18 6M6 6l12 12'
            />
          </svg>
        </button>
      )}
    </div>
  );
};

export default SearchInput;
