'use client';

import { useEffect, useRef, useState } from 'react';
import { Loader2 } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/shared/components/ui';
import type { TransportPlanStopDetail } from '../../../types';

declare global {
  interface Window {
    L: any;
  }
}

const LEAFLET_CSS_ID = 'leaflet-css';
const LEAFLET_JS_ID = 'leaflet-js';
const DEFAULT_CENTER: [number, number] = [10.8231, 106.6297];

async function fetchOsrmPolyline(
  waypoints: [number, number][],
): Promise<[number, number][] | null> {
  if (waypoints.length < 2) return null;
  const coords = waypoints.map(([lat, lng]) => `${lng},${lat}`).join(';');
  const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(8000) });
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code !== 'Ok' || !data.routes?.[0]) return null;
    return (data.routes[0].geometry.coordinates as [number, number][]).map(([lng, lat]) => [
      lat,
      lng,
    ]);
  } catch {
    return null;
  }
}

interface Props {
  open: boolean;
  onClose: () => void;
  stops: TransportPlanStopDetail[];
  currentStopSeq: number | null;
}

export function RouteMapModal({ open, onClose, stops, currentStopSeq }: Props) {
  // Callback ref instead of useRef — fires when Dialog portal mounts/unmounts the div,
  // triggering the map-init effect with the actual DOM element.
  const [containerEl, setContainerEl] = useState<HTMLDivElement | null>(null);
  const mapRef = useRef<any>(null);
  const [leafletReady, setLeafletReady] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (window.L) {
      setLeafletReady(true);
      return;
    }
    if (!document.getElementById(LEAFLET_CSS_ID)) {
      const link = document.createElement('link');
      link.id = LEAFLET_CSS_ID;
      link.rel = 'stylesheet';
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
      const iv = setInterval(() => {
        if (window.L) {
          clearInterval(iv);
          setLeafletReady(true);
        }
      }, 100);
      return () => clearInterval(iv);
    }
  }, []);

  useEffect(() => {
    if (!open || !leafletReady || !containerEl) return;
    if (mapRef.current) {
      mapRef.current.remove();
      mapRef.current = null;
    }

    const L = window.L;
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });
    const map = L.map(containerEl).setView(DEFAULT_CENTER, 10);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);
    mapRef.current = map;

    const validStops = [...stops]
      .filter((s) => s.lat != null && s.lng != null)
      .sort((a, b) => a.sequence - b.sequence);

    const bounds: [number, number][] = [];

    // Determine current stop: arrived but not completed
    const atStopSeq = stops.find((s) => s.actualArrivalTime != null && !s.isCompleted)?.sequence;
    const nextUnvisitedSeq = [...stops]
      .sort((a, b) => a.sequence - b.sequence)
      .find((s) => !s.actualArrivalTime)?.sequence;
    const highlightSeq = atStopSeq ?? nextUnvisitedSeq ?? currentStopSeq;

    validStops.forEach((stop) => {
      const pos: [number, number] = [stop.lat!, stop.lng!];
      bounds.push(pos);
      const isCurrent = stop.sequence === highlightSeq;

      if (isCurrent) {
        const icon = L.divIcon({
          className: '',
          html: `<div style="width:30px;height:30px;border-radius:50%;background:#f97316;color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:12px;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.4)">${stop.sequence}</div>`,
          iconSize: [30, 30],
          iconAnchor: [15, 15],
        });
        L.marker(pos, { icon })
          .bindPopup(`<b>${stop.locationCode}</b><br>${stop.action.replace(/_/g, ' ')}`)
          .addTo(map);
      } else if (stop.isCompleted) {
        L.circleMarker(pos, {
          radius: 8,
          color: '#22c55e',
          fillColor: '#22c55e',
          fillOpacity: 1,
          weight: 2,
        })
          .bindPopup(`<b>${stop.locationCode}</b><br>${stop.action.replace(/_/g, ' ')}`)
          .addTo(map);
      } else {
        L.circleMarker(pos, {
          radius: 6,
          color: '#94a3b8',
          fillColor: '#94a3b8',
          fillOpacity: 0.7,
          weight: 1,
        })
          .bindPopup(`<b>${stop.locationCode}</b><br>${stop.action.replace(/_/g, ' ')}`)
          .addTo(map);
      }
    });

    if (bounds.length >= 2) {
      map.fitBounds(L.latLngBounds(bounds), { padding: [40, 40], maxZoom: 13 });
    } else if (bounds.length === 1) {
      map.setView(bounds[0], 13);
    }

    fetchOsrmPolyline(validStops.map((s) => [s.lat!, s.lng!])).then((coords) => {
      if (coords && mapRef.current) {
        L.polyline(coords, { color: '#3b82f6', weight: 4, opacity: 0.8 }).addTo(mapRef.current);
      }
    });

    return () => {
      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, [open, leafletReady, containerEl]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
      <DialogContent className="max-w-3xl p-0 gap-0">
        <DialogHeader className="px-6 pt-4 pb-3">
          <DialogTitle>Full Route Map</DialogTitle>
        </DialogHeader>
        <div className="relative h-[500px]">
          {!leafletReady && (
            <div className="absolute inset-0 flex items-center justify-center bg-muted">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          )}
          <div ref={setContainerEl} className="h-full w-full rounded-b-lg" />
        </div>
      </DialogContent>
    </Dialog>
  );
}
