/**
 * Declares TypeScript module types for Leaflet png image assets to prevent compilation errors.
 */
declare module 'leaflet/dist/images/*.png' {
  const content: any;
  export default content;
}
