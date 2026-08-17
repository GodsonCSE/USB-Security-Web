/* ============================================================
   icons.js — Tiny self-contained icon set (no external fonts/CDNs)
   Usage: Icon('bell', 'stat-icon', 'color:var(--risk-high)')
   ============================================================ */
const ICONS = {
  shield:      '<path d="M12 3 5 6v5c0 5 3 8.5 7 10 4-1.5 7-5 7-10V6z"/>',
  gauge:       '<path d="M4 15a8 8 0 1 1 16 0"/><line x1="12" y1="13" x2="16" y2="8"/><circle cx="12" cy="13" r="1"/>',
  usb:         '<rect x="7" y="9" width="10" height="12" rx="2"/><rect x="10" y="3" width="4" height="6" rx="1"/><line x1="9.5" y1="15" x2="14.5" y2="15"/>',
  bell:        '<path d="M6 9a6 6 0 0 1 12 0c0 5.5 2 7.5 2 7.5H4S6 14.5 6 9Z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
  list:        '<line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><circle cx="4.5" cy="6" r="1"/><circle cx="4.5" cy="12" r="1"/><circle cx="4.5" cy="18" r="1"/>',
  sync:        '<path d="M4 12a8 8 0 0 1 13.6-5.7L20 9"/><polyline points="20 4 20 9 15 9"/><path d="M20 12a8 8 0 0 1-13.6 5.7L4 15"/><polyline points="4 20 4 15 9 15"/>',
  chartPie:    '<circle cx="12" cy="12" r="9"/><line x1="12" y1="12" x2="12" y2="3"/><line x1="12" y1="12" x2="18.5" y2="7"/>',
  plug:        '<path d="M9 2v6"/><path d="M15 2v6"/><path d="M6 8h12v3a6 6 0 0 1-6 6 6 6 0 0 1-6-6Z"/><path d="M12 17v5"/>',
  question:    '<circle cx="12" cy="12" r="9"/><path d="M9.5 9.3a2.5 2.5 0 0 1 4.9.7c0 1.6-2.4 2-2.4 3.5"/><circle cx="12" cy="17" r="0.6" fill="currentColor" stroke="none"/>',
  satellite:   '<line x1="12" y1="12" x2="12" y2="20"/><circle cx="12" cy="12" r="1.4" fill="currentColor" stroke="none"/><path d="M8 12a4 4 0 0 1 8 0"/><path d="M5 12a7 7 0 0 1 14 0"/>',
  wave:        '<polyline points="3 12 7 12 7 6 11 6 11 18 15 18 15 8 19 8 19 12 21 12"/>',
  history:     '<circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 16 14"/>',
  search:      '<circle cx="10.5" cy="10.5" r="6.5"/><line x1="20" y1="20" x2="15.5" y2="15.5"/>',
  file:        '<path d="M6 2h9l5 5v15H6Z"/><polyline points="15 2 15 7 20 7"/>',
  checkCircle: '<circle cx="12" cy="12" r="9"/><polyline points="8 12.5 11 15.5 16 9"/>',
  info:        '<circle cx="12" cy="12" r="9"/><line x1="12" y1="10.5" x2="12" y2="16"/><circle cx="12" cy="7.5" r="1" fill="currentColor" stroke="none"/>'
};

function Icon(name, extraClass, style) {
  const body = ICONS[name] || '';
  const cls  = extraClass ? ' ' + extraClass : '';
  const sty  = style ? ` style="${style}"` : '';
  return `<svg class="icon${cls}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"${sty} aria-hidden="true">${body}</svg>`;
}

// Auto-render any static markup like <span data-icon="bell"></span>.
// Color/size are inherited from the element's own style/class (font-size, color, opacity).
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-icon]').forEach(el => {
    el.innerHTML = Icon(el.getAttribute('data-icon'));
  });
});
