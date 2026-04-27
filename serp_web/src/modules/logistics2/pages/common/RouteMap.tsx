'use client';

import { Fragment, useMemo } from 'react';
import {
  MapContainer,
  TileLayer,
  Polyline,
  Marker,
  Popup,
} from 'react-leaflet';
import L from 'leaflet';
import { decodePolyline } from '@/shared/utils/polyline';
import { Truck, MapPin, Warehouse, XCircle } from 'lucide-react';
import { renderToString } from 'react-dom/server';
import type { Route, RouteStop } from '../../types';

// Định nghĩa giao diện từ API của bạn
interface RouteMapProps {
  route: Route;
}

type RouteMapSegment = {
  stop: RouteStop;
  path: [number, number][];
  location: [number, number];
  isPassed: boolean;
};

// Helper tạo custom icon từ Lucide
const createCustomIcon = (
  IconComponent: any,
  colorClass: string,
  bgClass: string
) => {
  const iconHtml = renderToString(
    <div
      className={`flex h-8 w-8 items-center justify-center rounded-full border-2 border-white shadow-md ${bgClass} ${colorClass}`}
    >
      <IconComponent size={16} />
    </div>
  );

  return L.divIcon({
    html: iconHtml,
    className: 'custom-leaflet-icon',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -16],
  });
};

const ICONS = {
  WAREHOUSE: createCustomIcon(Warehouse, 'text-slate-700', 'bg-slate-200'),
  WAITING: createCustomIcon(MapPin, 'text-amber-700', 'bg-amber-100'),
  ARRIVED: createCustomIcon(MapPin, 'text-emerald-700', 'bg-emerald-100'),
  FAILED: createCustomIcon(XCircle, 'text-rose-700', 'bg-rose-100'),
  TRUCK: createCustomIcon(Truck, 'text-blue-700', 'bg-blue-100'),
};

export default function RouteMap({ route }: RouteMapProps) {
  // Xử lý dữ liệu polylines và vị trí
  const mapData = useMemo(() => {
    if (!route?.routeStops?.length) return null;

    const stops = [...route.routeStops].sort((a, b) => a.sequence - b.sequence);

    let allPoints: [number, number][] = [];
    let warehouseLocation: [number, number] | null = null;
    let latestArrivedIndex = -1;
    const segments: RouteMapSegment[] = [];

    stops.forEach((stop) => {
      const path = stop.encodedPolyline
        ? decodePolyline(stop.encodedPolyline)
        : [];

      if (path.length === 0) {
        return;
      }

      allPoints = [...allPoints, ...path];

      // Điểm bắt đầu của polyline đầu tiên chính là Kho (Warehouse)
      if (!warehouseLocation) {
        warehouseLocation = path[0];
      }

      const location = path[path.length - 1];
      const isPassed = stop.status === 'ARRIVED';

      segments.push({
        stop,
        path,
        // Điểm cuối của polyline là vị trí điểm giao hàng
        location,
        isPassed,
      });

      if (isPassed) {
        latestArrivedIndex = segments.length - 1;
      }
    });

    if (!warehouseLocation) return null;

    if (allPoints.length === 0) {
      allPoints = [warehouseLocation];
    }

    // Xác định vị trí xe tải (Truck)
    let currentTruckLocation: [number, number] | null = warehouseLocation;
    if (route.status === 'IN_PROGRESS') {
      if (latestArrivedIndex >= 0 && segments[latestArrivedIndex]) {
        currentTruckLocation = segments[latestArrivedIndex].location;
      }
    } else {
      currentTruckLocation = null; // Không hiện xe tải nếu chưa chạy hoặc đã xong
    }

    return { segments, warehouseLocation, currentTruckLocation, allPoints };
  }, [route]);

  if (!mapData || !mapData.warehouseLocation)
    return <div>Không có dữ liệu bản đồ</div>;

  // Tính toán bounds để tự động zoom vừa vặn tất cả các điểm
  const bounds = L.latLngBounds(mapData.allPoints);

  return (
    <div className='h-[500px] w-full overflow-hidden rounded-xl border z-0 relative'>
      <MapContainer bounds={bounds} className='h-full w-full'>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url='https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        />

        {/* Marker Kho (Warehouse) */}
        <Marker position={mapData.warehouseLocation} icon={ICONS.WAREHOUSE}>
          <Popup>
            <div className='p-1'>
              <p className='font-semibold text-sm'>Kho xuất phát</p>
              <p className='text-xs text-muted-foreground'>
                Điểm bắt đầu lộ trình
              </p>
            </div>
          </Popup>
        </Marker>

        {/* Vẽ Polylines và Markers cho từng điểm giao */}
        {mapData.segments.map((segment) => (
          <Fragment key={segment.stop.id}>
            <Polyline
              positions={segment.path}
              color={segment.isPassed ? '#10b981' : '#94a3b8'} // Xanh ngọc nếu đã đi qua, Xám nếu chưa
              weight={4}
              opacity={0.8}
              dashArray={segment.isPassed ? undefined : '5, 10'} // Nét đứt nếu chưa đi qua
            />

            <Marker
              position={segment.location}
              icon={
                ICONS[segment.stop.status as keyof typeof ICONS] ||
                ICONS.WAITING
              }
            >
              <Popup>
                <div className='flex flex-col gap-1 min-w-[200px] p-1'>
                  <span className='text-xs font-semibold uppercase text-blue-600'>
                    Điểm giao #{segment.stop.sequence}
                  </span>
                  <p className='font-bold'>
                    {segment.stop.deliverySlip?.customerName ||
                      'Khách hàng chưa xác định'}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    {segment.stop.deliverySlip?.customerAddress?.fullAddress ||
                      'Chưa có địa chỉ'}
                  </p>
                  <div className='mt-2 text-xs border-t pt-2'>
                    <p>
                      Mã đơn:{' '}
                      <strong>{segment.stop.deliverySlip?.code || '--'}</strong>
                    </p>
                    <p>
                      Trạng thái: <strong>{segment.stop.status}</strong>
                    </p>
                  </div>
                </div>
              </Popup>
            </Marker>
          </Fragment>
        ))}

        {/* Marker Xe tải (Nếu lộ trình đang chạy) */}
        {mapData.currentTruckLocation && (
          <Marker
            position={mapData.currentTruckLocation}
            icon={ICONS.TRUCK}
            zIndexOffset={1000}
          >
            <Popup>
              <p className='font-semibold text-sm'>Vị trí hiện tại</p>
            </Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
}
