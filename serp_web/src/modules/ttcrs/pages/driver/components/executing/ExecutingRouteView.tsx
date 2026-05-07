'use client';

import { useEffect, useRef, useState } from 'react';
import { Clock, Loader2, MapPin, Navigation } from 'lucide-react';
import { Button, Card } from '@/shared/components/ui';
import type { TransportPlanStopDetail } from '../../../../types';
import { ArriveConfirmDialog } from './ArriveConfirmDialog';

declare global { interface Window { L: any } }

const LEAFLET_CSS_ID = 'leaflet-css';
const LEAFLET_JS_ID = 'leaflet-js';
const DEFAULT_CENTER: [number, number] = [10.8231, 106.6297];

async function fetchOsrmData(
  from: [number, number],
  to: [number, number],
): Promise<{ coords: [number, number][]; distanceKm: number; durationSec: number } | null> {
  const url = `https://router.project-osrm.org/route/v1/driving/${from[1]},${from[0]};${to[1]},${to[0]}?overview=full&geometries=geojson`;
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(8000) });
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code !== 'Ok' || !data.routes?.[0]) return null;
    const route = data.routes[0];
    const coords: [number, number][] = (route.geometry.coordinates as [number, number][]).map(
      ([lon, lat]) => [lat, lon],
    );
    return { coords, distanceKm: route.distance / 1000, durationSec: route.duration };
  } catch { return null; }
}

interface ExecutingRouteViewProps {
  fromStop: TransportPlanStopDetail;
  toStop: TransportPlanStopDetail;
  onArrived: () => void;
  isArrivePending: boolean;
}

export function ExecutingRouteView({
  fromStop,
  toStop,
  onArrived,
  isArrivePending,
}: ExecutingRouteViewProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<any>(null);
  const layerRef = useRef<any>(null);
  const [leafletReady, setLeafletReady] = useState(false);
  const [routeInfo, setRouteInfo] = useState<{
    coords: [number, number][];
    distanceKm: number;
    durationSec: number;
  } | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const hasFrom = fromStop.lat != null && fromStop.lng != null;
  const hasTo = toStop.lat != null && toStop.lng != null;

  // Computed ETA = now + OSRM duration - check lại xem có nên đổi Date.now() thành thời điểm xe rời đi không (tức là từ khi tài xế bấm "I have arrived" ở stop trước hay từ khi tài xế bấm "Start route" ở stop đầu tiên)
  const computedEta = routeInfo
    ? new Date(Date.now() + routeInfo.durationSec * 1000)
    : null;

  const fmtTime = (d: Date) =>
    d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const fmtPlanned = (dt: string | null) =>
    dt ? dt.replace('T', ' ').slice(11, 16) : '—';

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (window.L) { setLeafletReady(true); return; }
    if (!document.getElementById(LEAFLET_CSS_ID)) {
      const link = document.createElement('link');
      link.id = LEAFLET_CSS_ID; link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }
    if (!document.getElementById(LEAFLET_JS_ID)) {
      const script = document.createElement('script');
      script.id = LEAFLET_JS_ID;
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setLeafletReady(true);
      document.head.appendChild(script);
    } else {
      const iv = setInterval(() => { if (window.L) { clearInterval(iv); setLeafletReady(true); } }, 100);
      return () => clearInterval(iv);
    }
  }, []);

  useEffect(() => {
    if (!leafletReady || !containerRef.current || mapRef.current) return;
    const L = window.L;
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });
    mapRef.current = window.L.map(containerRef.current).setView(DEFAULT_CENTER, 10);
    window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(mapRef.current);
    return () => { mapRef.current?.remove(); mapRef.current = null; };
  }, [leafletReady]);

  // Fetch OSRM route between fromStop and toStop
  useEffect(() => {
    if (!hasFrom || !hasTo) return;
    fetchOsrmData(
      [fromStop.lat!, fromStop.lng!],
      [toStop.lat!, toStop.lng!],
    ).then(setRouteInfo);
  }, [fromStop, toStop, hasFrom, hasTo]);

  // Render polyline + markers
  useEffect(() => {
    if (!leafletReady || !mapRef.current) return;
    const L = window.L;
    if (layerRef.current) layerRef.current.remove();
    layerRef.current = L.layerGroup().addTo(mapRef.current);
    const group = layerRef.current;
    const bounds: [number, number][] = [];

    if (routeInfo) {
      L.polyline(routeInfo.coords, { color: '#f97316', weight: 5, opacity: 0.9 }).addTo(group);
    }

    if (hasFrom) {
      const pos: [number, number] = [fromStop.lat!, fromStop.lng!];
      bounds.push(pos);
      L.circleMarker(pos, { radius: 8, color: '#3b82f6', fillColor: '#3b82f6', fillOpacity: 1, weight: 2 })
        .bindPopup(`<b>${fromStop.locationCode}</b><br><span style="color:#888">Current position</span>`)
        .addTo(group);
    }
    if (hasTo) {
      const pos: [number, number] = [toStop.lat!, toStop.lng!];
      bounds.push(pos);
      const svgIcon = L.divIcon({
        className: '',
        html: `<div style="width:32px;height:32px;border-radius:50%;background:#f97316;color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:14px;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.4)">▼</div>`,
        iconSize: [32, 32], iconAnchor: [16, 16],
      });
      L.marker(pos, { icon: svgIcon })
        .bindPopup(`<b>${toStop.locationCode}</b><br><span style="color:#888">${toStop.action}</span>`)
        .addTo(group);
    }

    if (bounds.length >= 2) {
      mapRef.current.fitBounds(L.latLngBounds(bounds), { padding: [60, 60], maxZoom: 14 });
    } else if (bounds.length === 1) {
      mapRef.current.setView(bounds[0], 13);
    }
  }, [routeInfo, leafletReady, hasFrom, hasTo]);

  return (
    // `isolate` creates a stacking context so the floating Card's z-[1000]
    // does NOT escape to <body> level and obscure the AlertDialog (z-50, rendered via Portal).
    <div className="relative h-screen w-full isolate">
      {/* Full-screen map */}
      {!leafletReady && (
        <div className="absolute inset-0 z-10 flex items-center justify-center bg-muted">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}
      <div ref={containerRef} className="h-full w-full" />

      {/* Floating info overlay */}
      <div className="absolute bottom-6 left-1/2 z-[1000] w-full max-w-sm -translate-x-1/2 px-4">
        <Card className="shadow-2xl border-2 border-primary/20">
          <div className="p-4 space-y-3">
            {/* Destination */}
            <div className="flex items-start gap-2">
              <MapPin className="h-5 w-5 text-orange-500 mt-0.5 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Next Stop</p>
                <p className="text-lg font-bold font-mono">{toStop.locationCode}</p>
                <p className="text-xs text-muted-foreground">{toStop.action.replace(/_/g, ' ')}</p>
              </div>
            </div>

            {/* Distance & ETA */}
            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="rounded-lg bg-muted p-2">
                <Navigation className="h-4 w-4 mx-auto text-blue-500 mb-1" />
                <p className="text-xs text-muted-foreground">Distance</p>
                <p className="text-sm font-semibold">
                  {routeInfo ? `${routeInfo.distanceKm.toFixed(1)} km` : '—'}
                </p>
              </div>
              <div className="rounded-lg bg-muted p-2">
                <Clock className="h-4 w-4 mx-auto text-orange-500 mb-1" />
                <p className="text-xs text-muted-foreground">Live ETA</p>
                <p className="text-sm font-semibold">
                  {computedEta ? fmtTime(computedEta) : '—'}
                </p>
              </div>
              <div className="rounded-lg bg-muted p-2">
                <Clock className="h-4 w-4 mx-auto text-green-500 mb-1" />
                <p className="text-xs text-muted-foreground">Planned ETA</p>
                <p className="text-sm font-semibold">{fmtPlanned(toStop.plannedArrivalTime)}</p>
              </div>
            </div>

            {/* I have arrived button */}
            <Button
              className="w-full bg-orange-500 hover:bg-orange-600 text-white"
              size="lg"
              onClick={() => setConfirmOpen(true)}
              disabled={isArrivePending}
            >
              {isArrivePending ? (
                <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Confirming…</>
              ) : (
                'I Have Arrived'
              )}
            </Button>
          </div>
        </Card>
      </div>

      <ArriveConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        onConfirm={() => { setConfirmOpen(false); onArrived(); }}
        locationCode={toStop.locationCode}
        isPending={isArrivePending}
      />
    </div>
  );
}
