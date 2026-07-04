'use client';

import { MapLocationPicker } from '@/shared/components';

interface CoordinatePickerMapProps {
  latitude?: number;
  longitude?: number;
  className?: string;
  disabled?: boolean;
  onChange: (latitude: number, longitude: number) => void;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

export const CoordinatePickerMap = ({
  latitude,
  longitude,
  className,
  disabled = false,
  onChange,
}: CoordinatePickerMapProps) => {
  return (
    <MapLocationPicker
      initialLat={latitude ?? DEFAULT_CENTER.latitude}
      initialLng={longitude ?? DEFAULT_CENTER.longitude}
      className={className}
      disabled={disabled}
      onLocationSelect={onChange}
    />
  );
};

export default CoordinatePickerMap;
