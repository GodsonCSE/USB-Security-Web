/* dashboard.js */
document.addEventListener('DOMContentLoaded', async () => {
  await Promise.all([loadStats(), loadConnectedDevices(), loadRecentEvents()]);
  onSseEvent('STATS_UPDATE', s => updateStatCards(s));
  onSseEvent('DEVICE_CONNECTED', data => {
    loadStats(); loadConnectedDevices();
    prependActivity(data.event, data.device);
    const lvl = (data.riskLevel || 'MEDIUM').toLowerCase();
    showToast(lvl === 'high' ? '⚠ HIGH RISK DEVICE' : 'USB Connected',
      (data.device?.deviceName || 'Unknown') + (data.trusted ? ' — Trusted' : ' — Unknown'), lvl);
  });
  onSseEvent('DEVICE_DISCONNECTED', data => { loadStats(); loadConnectedDevices(); prependActivity(data.event, data.device); });
  onSseEvent('DEVICE_TRUSTED',   () => { loadStats(); loadConnectedDevices(); });
  onSseEvent('DEVICE_UNTRUSTED', () => { loadStats(); loadConnectedDevices(); });
});

async function loadStats() {
  try { updateStatCards(await API.get('/api/statistics')); } catch(e) { console.error(e); }
}

function updateStatCards(s) {
  ['connected','trusted','unknown','alerts'].forEach((k,i) => {
    const el = document.getElementById('stat-'+k);
    if (el) el.textContent = [s.connectedCount, s.trustedCount, s.unknownCount, s.openAlertCount][i] ?? '-';
  });
  updateChart(s);
}

function updateChart(s) {
  const data = [+(s.trustedCount||0), +(s.unknownCount||0),
                Math.max(0,+(s.totalDeviceCount||0)-+(s.connectedCount||0))];
  renderDonut('deviceChart', ['Trusted','Unknown','Offline'], data, ['#2ecc71','#f0b429','#30363d']);
}

// Small dependency-free donut chart, drawn as plain SVG (no Chart.js / no network).
function renderDonut(elId, labels, data, colors) {
  const el = document.getElementById(elId);
  if (!el) return;
  const total = data.reduce((a,b) => a + b, 0);
  const r = 52, cx = 60, cy = 60, sw = 16, circumference = 2 * Math.PI * r;
  let offset = 0, arcs = '';
  if (total <= 0) {
    arcs = `<circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="var(--border-color)" stroke-width="${sw}"/>`;
  } else {
    data.forEach((v, i) => {
      if (v <= 0) return;
      const len = (v / total) * circumference;
      arcs += `<circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="${colors[i]}" stroke-width="${sw}"
                 stroke-dasharray="${len} ${circumference - len}" stroke-dashoffset="${-offset}"
                 transform="rotate(-90 ${cx} ${cy})"/>`;
      offset += len;
    });
  }
  const legend = labels.map((l, i) => `
    <span style="display:inline-flex;align-items:center;gap:6px;margin:0 8px 4px;font-size:12px;color:var(--text-muted)">
      <span style="width:9px;height:9px;border-radius:50%;background:${colors[i]};display:inline-block"></span>${l} (${data[i]||0})
    </span>`).join('');
  el.innerHTML = `<svg viewBox="0 0 120 120" width="180" height="180">${arcs}</svg>
    <div style="display:flex;flex-wrap:wrap;justify-content:center;margin-top:6px">${legend}</div>`;
}

async function loadConnectedDevices() {
  try {
    const devices = await API.get('/api/devices/connected');
    const tbody = document.getElementById('connected-tbody');
    if (!tbody) return;
    if (!devices.length) {
      tbody.innerHTML = `<tr><td colspan="7" class="empty-state">${Icon('plug')}<br>No USB devices currently connected.</td></tr>`;
      return;
    }
    tbody.innerHTML = devices.map(d => `<tr>
      <td>${deviceName(d)}</td><td>${na(d.vendorId)}</td><td>${na(d.productId)}</td>
      <td>${na(d.serialNumber)}</td><td>${na(d.deviceType)}</td>
      <td>${statusBadge(d.trusted)}</td><td>${riskBadge(d.riskLevel)}</td></tr>`).join('');
  } catch(e) { console.error(e); }
}

async function loadRecentEvents() {
  try {
    const events = await API.get('/api/events/recent');
    const feed = document.getElementById('activity-feed');
    if (!feed) return;
    if (!events.length) { feed.innerHTML=`<div class="empty-state">${Icon('history')}<br>No activity yet.</div>`; return; }
    feed.innerHTML='';
    events.forEach(ev => prependActivity(ev, ev.device, false));
  } catch(e) { console.error(e); }
}

function prependActivity(ev, device, prepend=true) {
  const feed = document.getElementById('activity-feed');
  if (!feed) return;
  const isConn = ev?.eventType==='CONNECTED';
  const risk = (ev?.riskLevel||'low').toLowerCase();
  const el = document.createElement('div');
  el.className='activity-item';
  el.innerHTML=`<div class="activity-dot ${isConn?(risk==='high'?'high':'connected'):'disconnected'}"></div>
    <div style="flex:1;min-width:0"><div style="font-size:13px">${deviceName(device)}</div>
    <div style="font-size:11px;color:var(--text-muted)">${ev?.eventType||''}</div></div>
    <div class="activity-time">${fmtTime(ev?.timestamp)}</div>${riskBadge(ev?.riskLevel)}`;
  if (prepend && feed.firstChild) { feed.insertBefore(el,feed.firstChild); if(feed.children.length>30) feed.removeChild(feed.lastChild); }
  else feed.appendChild(el);
}
