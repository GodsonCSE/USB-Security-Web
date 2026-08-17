/* ============================================================
   app.js — Shared utilities & zero-friction data adapter
   Supports live Spring Boot backend + standalone preview mode
   ============================================================ */

// ── In-memory Fallback / Standalone Mock Store (file:// preview only)
const MockStore = {
  devices: [
    { id: 1, deviceKey: "046D:C31C:LOGI-KB-98124", deviceName: "Logitech K120 Keyboard", manufacturer: "Logitech", vendorId: "046D", productId: "C31C", serialNumber: "LOGI-KB-98124", deviceType: "HID / Keyboard", currentlyConnected: true, trusted: true, trustedId: 101, riskLevel: "LOW", firstSeenAt: new Date(Date.now() - 5*86400000).toISOString(), lastSeenAt: new Date().toISOString() },
    { id: 2, deviceKey: "046D:C077:LOGI-M100-5521", deviceName: "Logitech Optical Mouse M100", manufacturer: "Logitech", vendorId: "046D", productId: "C077", serialNumber: "LOGI-M100-5521", deviceType: "HID / Mouse", currentlyConnected: true, trusted: true, trustedId: 102, riskLevel: "LOW", firstSeenAt: new Date(Date.now() - 4*86400000).toISOString(), lastSeenAt: new Date().toISOString() },
    { id: 3, deviceKey: "0781:5583:SANDISK-ULTRA-8831", deviceName: "SanDisk Ultra USB 3.0", manufacturer: "SanDisk", vendorId: "0781", productId: "5583", serialNumber: "SANDISK-ULTRA-8831", deviceType: "Mass Storage", currentlyConnected: true, trusted: false, trustedId: null, riskLevel: "MEDIUM", firstSeenAt: new Date(Date.now() - 2*3600000).toISOString(), lastSeenAt: new Date().toISOString() },
    { id: 4, deviceKey: "1209:0001:NOSERIAL-RUBBERDUCKY", deviceName: "Generic USB Composite Device", manufacturer: "Unknown", vendorId: "1209", productId: "0001", serialNumber: "Unknown", deviceType: "Composite / HID", currentlyConnected: false, trusted: false, trustedId: null, riskLevel: "HIGH", firstSeenAt: new Date(Date.now() - 45*60000).toISOString(), lastSeenAt: new Date(Date.now() - 10*60000).toISOString() }
  ],
  trusted: [
    { id: 101, device: { id: 1, deviceName: "Logitech K120 Keyboard", manufacturer: "Logitech", vendorId: "046D", productId: "C31C", serialNumber: "LOGI-KB-98124" }, label: "Corporate Standard Keyboard", createdAt: new Date(Date.now() - 5*86400000).toISOString() },
    { id: 102, device: { id: 2, deviceName: "Logitech Optical Mouse M100", manufacturer: "Logitech", vendorId: "046D", productId: "C077", serialNumber: "LOGI-M100-5521" }, label: "Office Optical Mouse", createdAt: new Date(Date.now() - 4*86400000).toISOString() }
  ],
  alerts: [
    { id: 201, device: { id: 3, deviceName: "SanDisk Ultra USB 3.0", vendorId: "0781", productId: "5583", serialNumber: "SANDISK-ULTRA-8831" }, severity: "MEDIUM", message: "Unknown USB Mass Storage detected: SanDisk Ultra USB 3.0 (0781:5583)", status: "OPEN", createdAt: new Date(Date.now() - 2*3600000).toISOString() },
    { id: 202, device: { id: 4, deviceName: "Generic USB Composite Device", vendorId: "1209", productId: "0001", serialNumber: "Unknown" }, severity: "HIGH", message: "High-Risk USB device: Missing serial number and unknown vendor signature", status: "OPEN", createdAt: new Date(Date.now() - 45*60000).toISOString() }
  ],
  events: [
    { id: 301, device: { id: 4, deviceName: "Generic USB Composite Device", manufacturer: "Unknown" }, eventType: "DISCONNECTED", riskLevel: "HIGH", reason: "Suspicious device unplugged", timestamp: new Date(Date.now() - 10*60000).toISOString() },
    { id: 302, device: { id: 4, deviceName: "Generic USB Composite Device", manufacturer: "Unknown" }, eventType: "CONNECTED", riskLevel: "HIGH", reason: "Unregistered composite device without valid serial number", timestamp: new Date(Date.now() - 45*60000).toISOString() },
    { id: 303, device: { id: 3, deviceName: "SanDisk Ultra USB 3.0", manufacturer: "SanDisk" }, eventType: "CONNECTED", riskLevel: "MEDIUM", reason: "Unknown mass storage device attached", timestamp: new Date(Date.now() - 2*3600000).toISOString() },
    { id: 304, device: { id: 2, deviceName: "Logitech Optical Mouse M100", manufacturer: "Logitech" }, eventType: "CONNECTED", riskLevel: "LOW", reason: "Whitelisted device connected", timestamp: new Date(Date.now() - 4*86400000).toISOString() },
    { id: 305, device: { id: 1, deviceName: "Logitech K120 Keyboard", manufacturer: "Logitech" }, eventType: "TRUSTED", riskLevel: "LOW", reason: "Added to trusted whitelist", timestamp: new Date(Date.now() - 5*86400000).toISOString() },
    { id: 306, device: { id: 1, deviceName: "Logitech K120 Keyboard", manufacturer: "Logitech" }, eventType: "CONNECTED", riskLevel: "LOW", reason: "Whitelisted device connected", timestamp: new Date(Date.now() - 5*86400000 - 900000).toISOString() }
  ]
};

const isFileProtocol = window.location.protocol === 'file:';

// ── API helpers ───────────────────────────────────────────────
const API = {
  async get(path) {
    if (isFileProtocol) {
      return API.handleMockGet(path);
    }
    const r = await fetch(path);
    if (!r.ok) {
      const errText = await r.text().catch(() => '');
      throw new Error(`GET ${path} failed (${r.status}): ${errText || r.statusText}`);
    }
    return r.json();
  },

  async post(path, body) {
    if (isFileProtocol) {
      return API.handleMockPost(path, body);
    }
    const r = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!r.ok) {
      const errText = await r.text().catch(() => '');
      throw new Error(`POST ${path} failed (${r.status}): ${errText || r.statusText}`);
    }
    return r.json();
  },

  async del(path) {
    if (isFileProtocol) {
      return API.handleMockDel(path);
    }
    const r = await fetch(path, { method: 'DELETE' });
    if (!r.ok) {
      const errText = await r.text().catch(() => '');
      throw new Error(`DELETE ${path} failed (${r.status}): ${errText || r.statusText}`);
    }
    return r.status === 204 ? null : r.json();
  },

  handleMockGet(path) {
    const url = new URL(path, 'http://localhost');
    const pathname = url.pathname;

    if (pathname === '/api/statistics') {
      const conn = MockStore.devices.filter(d => d.currentlyConnected).length;
      const tr = MockStore.devices.filter(d => d.trusted).length;
      const unk = MockStore.devices.filter(d => d.currentlyConnected && !d.trusted).length;
      const al = MockStore.alerts.filter(a => a.status === 'OPEN').length;
      return {
        connectedCount: conn,
        trustedCount: tr,
        unknownCount: unk,
        openAlertCount: al,
        totalDeviceCount: MockStore.devices.length
      };
    }
    if (pathname === '/api/devices/connected') {
      return MockStore.devices.filter(d => d.currentlyConnected);
    }
    if (pathname === '/api/devices/trusted') {
      return MockStore.trusted;
    }
    if (pathname === '/api/devices') {
      return MockStore.devices;
    }
    if (pathname === '/api/alerts/open') {
      return MockStore.alerts.filter(a => a.status === 'OPEN');
    }
    if (pathname === '/api/alerts') {
      return MockStore.alerts;
    }
    if (pathname === '/api/events/recent' || pathname === '/api/events') {
      let events = [...MockStore.events];
      const type = url.searchParams.get('type');
      const risk = url.searchParams.get('risk');
      const device = url.searchParams.get('device');
      if (type) events = events.filter(e => e.eventType === type);
      if (risk) events = events.filter(e => e.riskLevel === risk);
      if (device) events = events.filter(e => (e.device?.deviceName || '').toLowerCase().includes(device.toLowerCase()));
      return events;
    }
    return [];
  },

  handleMockPost(path, body) {
    if (path.includes('/trust')) {
      const match = path.match(/\/api\/devices\/(\d+)\/trust/);
      if (match) {
        const id = parseInt(match[1], 10);
        const d = MockStore.devices.find(x => x.id === id);
        if (d) {
          d.trusted = true;
          d.riskLevel = 'LOW';
          const trustedId = Date.now();
          d.trustedId = trustedId;
          MockStore.trusted.push({
            id: trustedId,
            device: { ...d },
            label: body?.label || d.deviceName,
            createdAt: new Date().toISOString()
          });
          MockStore.events.unshift({
            id: Date.now(),
            device: { ...d },
            eventType: 'TRUSTED',
            riskLevel: 'LOW',
            reason: 'Added to trusted whitelist',
            timestamp: new Date().toISOString()
          });
        }
      }
      return { success: true };
    }
    if (path.includes('/resolve')) {
      const match = path.match(/\/api\/alerts\/(\d+)\/resolve/);
      if (match) {
        const id = parseInt(match[1], 10);
        const a = MockStore.alerts.find(x => x.id === id);
        if (a) {
          a.status = body?.status || 'DISMISSED';
          a.resolvedAt = new Date().toISOString();
        }
      }
      return { success: true };
    }
    return { success: true };
  },

  handleMockDel(path) {
    const match = path.match(/\/api\/devices\/trusted\/(\d+)/);
    if (match) {
      const tid = parseInt(match[1], 10);
      const entryIdx = MockStore.trusted.findIndex(t => t.id === tid);
      if (entryIdx !== -1) {
        const entry = MockStore.trusted[entryIdx];
        const d = MockStore.devices.find(x => x.id === entry.device?.id);
        if (d) {
          d.trusted = false;
          d.trustedId = null;
          d.riskLevel = 'MEDIUM';
        }
        MockStore.trusted.splice(entryIdx, 1);
        MockStore.events.unshift({
          id: Date.now(),
          device: d || entry.device,
          eventType: 'UNTRUSTED',
          riskLevel: 'MEDIUM',
          reason: 'Removed from trusted whitelist',
          timestamp: new Date().toISOString()
        });
      }
    }
    return { success: true };
  }
};

// ── Toast notifications ───────────────────────────────────────
function showToast(title, body, level = 'low') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const el = document.createElement('div');
  el.className = `toast-msg ${level.toLowerCase()}`;
  el.innerHTML = `<div class="toast-title">${escHtml(title)}</div>
                  <div class="toast-body">${escHtml(body)}</div>`;
  container.appendChild(el);
  setTimeout(() => el.remove(), 6000);
}

// ── Formatters ────────────────────────────────────────────────
function fmtDateTime(iso) {
  if (!iso) return '<span class="na-text">N/A</span>';
  const d = new Date(iso);
  return d.toLocaleString(undefined, {
    month: 'short', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
}

function fmtTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function riskBadge(level) {
  const l = (level || 'MEDIUM').toUpperCase();
  return `<span class="risk-badge risk-${l}">${l}</span>`;
}

function statusBadge(trusted) {
  return trusted
    ? '<span class="badge-trusted">✔ Trusted</span>'
    : '<span class="badge-unknown">? Unknown</span>';
}

function alertStatusBadge(status) {
  const colours = { OPEN: 'badge-open', ALLOWED: 'badge-trusted', TRUSTED: 'badge-trusted',
                    BLOCK_SIMULATED: 'badge-unknown', DISMISSED: 'text-muted-custom' };
  return `<span class="${colours[status] || ''}">${status}</span>`;
}

function deviceName(d) {
  return d?.deviceName && d.deviceName !== 'Unknown' ? escHtml(d.deviceName) : '<span class="na-text">Unknown device</span>';
}

function na(v) {
  return (v && v !== 'Unknown') ? escHtml(v) : '<span class="na-text">N/A</span>';
}

function escHtml(s) {
  if (!s) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ── SSE connection ────────────────────────────────────────────
let _sse = null;
const _sseHandlers = {};

function onSseEvent(eventName, handler) {
  _sseHandlers[eventName] = _sseHandlers[eventName] || [];
  _sseHandlers[eventName].push(handler);
}

function connectSse() {
  if (isFileProtocol) {
    updateMonitorStatus(true, 'Standalone Preview');
    return;
  }
  if (_sse) return;

  try {
    _sse = new EventSource('/sse/events');

    _sse.addEventListener('CONNECTED', () => {
      updateMonitorStatus(true, 'Monitoring active');
    });

    ['DEVICE_CONNECTED','DEVICE_DISCONNECTED','DEVICE_TRUSTED','DEVICE_UNTRUSTED',
     'STATS_UPDATE','ALERT_UPDATED'].forEach(name => {
      _sse.addEventListener(name, e => {
        let data;
        try { data = JSON.parse(e.data); } catch { return; }
        (_sseHandlers[name] || []).forEach(h => h(data));
      });
    });

    _sse.onerror = () => {
      updateMonitorStatus(false, 'Disconnected');
    };
  } catch (e) {
    updateMonitorStatus(false, 'Disconnected');
  }
}

function updateMonitorStatus(ok, label) {
  const dot  = document.getElementById('status-dot');
  const text = document.getElementById('status-text');
  if (dot)  { dot.className  = 'status-dot' + (ok ? ' active' : ''); }
  if (text) { text.textContent = label || (ok ? 'Monitoring active' : 'Reconnecting…'); }
}

// ── Active nav link ───────────────────────────────────────────
function setActiveNav() {
  const current = (window.location.pathname.split('/').pop() || 'index.html').toLowerCase();
  document.querySelectorAll('.nav-link').forEach(a => {
    a.classList.remove('active');
    const href = a.getAttribute('href') || '';
    const linkTarget = (href.split('/').pop() || 'index.html').toLowerCase();
    if (linkTarget === current ||
        (current === '' && linkTarget === 'index.html') ||
        (current === 'index.html' && (href === '/' || linkTarget === 'index.html'))) {
      a.classList.add('active');
    }
  });
}

// ── Init on every page ────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  setActiveNav();
  connectSse();
});
