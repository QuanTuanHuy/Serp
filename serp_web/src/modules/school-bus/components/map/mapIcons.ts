'use client';

import { divIcon } from 'leaflet';

type MarkerKind =
  | 'school'
  | 'depot'
  | 'pickup'
  | 'dropoff'
  | 'bus'
  | 'student';

/**
 * SVG path data for each marker kind (sourced from lucide icon set).
 * Rendered inside a 24×24 viewBox.
 */
const markerSvgPaths: Record<MarkerKind, string> = {
  // lucide: GraduationCap
  school:
    '<path d="M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z"/><path d="M22 10v6"/><path d="M6 12.5V16a6 3 0 0 0 12 0v-3.5"/>',
  // lucide: Warehouse
  depot:
    '<path d="M22 8.35V20a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8.35A2 2 0 0 1 3.26 6.5l8-3.2a2 2 0 0 1 1.48 0l8 3.2A2 2 0 0 1 22 8.35Z"/><path d="M6 18h12"/><path d="M6 14h12"/><rect width="12" height="12" x="6" y="10"/>',
  // lucide: MapPin
  pickup:
    '<path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/>',
  // lucide: MapPinCheck (pin with check)
  dropoff:
    '<path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><path d="m9 10 2 2 4-4"/>',

  // lucide: Bus
  bus:
    '<path d="M8 6v6"/><path d="M15 6v6"/><path d="M2 12h19.6"/><path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.4-.1-.8-.2-1.2l-1.4-5C20.1 6.8 19.1 6 18 6H4a2 2 0 0 0-2 2v10h3"/><circle cx="7" cy="18" r="2"/><path d="M9 18h5"/><circle cx="16" cy="18" r="2"/>',
  // lucide: User
  student:
    '<path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
};

const markerColors: Record<
  MarkerKind,
  { bg: string; border: string; stroke: string }
> = {
  school: { bg: '#fff1f2', border: '#be123c', stroke: '#be123c' },
  depot: { bg: '#fffbeb', border: '#b45309', stroke: '#b45309' },
  pickup: { bg: '#eff6ff', border: '#0369a1', stroke: '#0369a1' },
  dropoff: { bg: '#ecfdf5', border: '#047857', stroke: '#047857' },

  bus: { bg: '#eef2ff', border: '#4338ca', stroke: '#4338ca' },
  student: { bg: '#faf5ff', border: '#7c3aed', stroke: '#7c3aed' },
};

export type { MarkerKind };

export { markerColors };

export function createSchoolBusMarkerIcon(kind: MarkerKind, size = 32) {
  const color = markerColors[kind];
  const svgPaths = markerSvgPaths[kind];
  const iconPad = 6;
  const svgSize = size - iconPad;

  return divIcon({
    className: 'school-bus-map-marker',
    html: `
      <div style="
        width:${size}px;
        height:${size}px;
        border-radius:9999px;
        border:2.5px solid ${color.border};
        background:${color.bg};
        display:flex;
        align-items:center;
        justify-content:center;
        box-shadow:0 8px 18px rgba(15,23,42,0.22);
      ">
        <svg xmlns="http://www.w3.org/2000/svg" width="${svgSize}" height="${svgSize}"
          viewBox="0 0 24 24" fill="none" stroke="${color.stroke}"
          stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          ${svgPaths}
        </svg>
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    popupAnchor: [0, -size / 2],
  });
}

export function createDirectionArrowIcon(color: string, angleDeg: number) {
  return divIcon({
    className: 'school-bus-map-arrow',
    html: `
      <div style="
        width:20px;
        height:20px;
        display:flex;
        align-items:center;
        justify-content:center;
        color:${color};
        font-size:12px;
        font-weight:800;
        transform:rotate(${angleDeg}deg);
        text-shadow:0 2px 6px rgba(15,23,42,0.35);
      ">&#9650;</div>
    `,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  });
}

/**
 * Pickup marker with a student-count badge in the top-right corner.
 */
export function createPickupWithCountIcon(count: number, size = 28) {
  const color = markerColors['pickup'];
  const svgPaths = markerSvgPaths['pickup'];
  const iconPad = 8;
  const svgSize = size - iconPad;
  const countText = count > 9 ? '9+' : String(count);

  const badge =
    count > 0
      ? `<div style="
          position:absolute;
          top:-5px;
          right:-5px;
          min-width:14px;
          height:14px;
          padding:0 3px;
          border-radius:9999px;
          background:#0369a1;
          color:#fff;
          font-size:8px;
          font-weight:800;
          display:flex;
          align-items:center;
          justify-content:center;
          border:1.5px solid white;
          line-height:1;
        ">${countText}</div>`
      : '';

  return divIcon({
    className: 'school-bus-map-marker',
    html: `
      <div style="position:relative; width:${size}px; height:${size}px;">
        <div style="
          width:${size}px;
          height:${size}px;
          border-radius:9999px;
          border:2.5px solid ${color.border};
          background:${color.bg};
          display:flex;
          align-items:center;
          justify-content:center;
          box-shadow:0 8px 18px rgba(15,23,42,0.22);
        ">
          <svg xmlns="http://www.w3.org/2000/svg" width="${svgSize}" height="${svgSize}"
            viewBox="0 0 24 24" fill="none" stroke="${color.stroke}"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            ${svgPaths}
          </svg>
        </div>
        ${badge}
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    popupAnchor: [0, -size / 2],
  });
}

/**
 * Numbered circle marker for route stops (shows stop order).
 * Uses indigo to visually separate from sky-blue pickup markers.
 */
export function createStopNumberIcon(stopNum: number, size = 24) {
  const label = stopNum > 99 ? '99+' : String(stopNum);
  const fontSize = stopNum > 9 ? 9 : 11;
  return divIcon({
    className: 'school-bus-map-marker',
    html: `
      <div style="
        width:${size}px;
        height:${size}px;
        border-radius:9999px;
        background:#4338ca;
        color:#fff;
        font-size:${fontSize}px;
        font-weight:800;
        display:flex;
        align-items:center;
        justify-content:center;
        box-shadow:0 4px 12px rgba(67,56,202,0.55);
        border:2.5px solid white;
        line-height:1;
      ">${label}</div>
    `,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    popupAnchor: [0, -size / 2],
  });
}
