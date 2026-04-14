'use client';

import { divIcon } from 'leaflet';

type MarkerKind =
  | 'school'
  | 'depot'
  | 'pickup'
  | 'dropoff'
  | 'start'
  | 'end'
  | 'bus';

const markerStyles: Record<
  MarkerKind,
  { bg: string; border: string; text: string; label: string }
> = {
  school: {
    bg: '#fff1f2',
    border: '#be123c',
    text: '#be123c',
    label: 'S',
  },
  depot: {
    bg: '#fffbeb',
    border: '#b45309',
    text: '#b45309',
    label: 'D',
  },
  pickup: {
    bg: '#eff6ff',
    border: '#0369a1',
    text: '#0369a1',
    label: 'P',
  },
  dropoff: {
    bg: '#ecfdf5',
    border: '#047857',
    text: '#047857',
    label: 'R',
  },
  start: {
    bg: '#fff1f2',
    border: '#9f1239',
    text: '#9f1239',
    label: 'A',
  },
  end: {
    bg: '#ffe4e6',
    border: '#e11d48',
    text: '#e11d48',
    label: 'Z',
  },
  bus: {
    bg: '#eef2ff',
    border: '#4338ca',
    text: '#4338ca',
    label: 'B',
  },
};

export function createSchoolBusMarkerIcon(kind: MarkerKind, size = 28) {
  const style = markerStyles[kind];
  return divIcon({
    className: 'school-bus-map-marker',
    html: `
      <div style="
        width:${size}px;
        height:${size}px;
        border-radius:9999px;
        border:2px solid ${style.border};
        background:${style.bg};
        color:${style.text};
        display:flex;
        align-items:center;
        justify-content:center;
        font-size:12px;
        font-weight:700;
        box-shadow:0 8px 18px rgba(15,23,42,0.22);
      ">${style.label}</div>
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
