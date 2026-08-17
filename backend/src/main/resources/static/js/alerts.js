/* alerts.js — Security alerts page */
let showOnlyOpen = true;

document.addEventListener('DOMContentLoaded', () => {
  loadAlerts();
  document.getElementById('filter-open').addEventListener('change', e => {
    showOnlyOpen = e.target.checked;
    loadAlerts();
  });
  onSseEvent('DEVICE_CONNECTED', loadAlerts);
  onSseEvent('ALERT_UPDATED', loadAlerts);
});

async function loadAlerts() {
  try {
    const url = showOnlyOpen ? '/api/alerts/open' : '/api/alerts';
    const alerts = await API.get(url);
    const container = document.getElementById('alerts-container');

    // Update count badge
    const badge = document.getElementById('open-count');
    if (badge) badge.textContent = alerts.filter(a => a.status==='OPEN').length;

    if (!alerts.length) {
      container.innerHTML=`<div class="empty-state">${Icon('checkCircle','','color:var(--risk-low)')}<br>No alerts. All monitored connections are accounted for.</div>`;
      return;
    }

    container.innerHTML = alerts.map(a => {
      const d = a.device || {};
      return `<div class="alert-item">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:8px">
          <div>
            <div class="alert-title">⚠ ${a.status==='OPEN'?'OPEN ALERT':'RESOLVED'} — ${riskBadge(a.severity)}</div>
            <div class="alert-device" style="margin-top:6px"><strong>${deviceName(d)}</strong></div>
            <div style="font-size:12px;color:var(--text-muted);margin-top:2px">
              VID: ${na(d.vendorId)} &nbsp;|&nbsp; PID: ${na(d.productId)} &nbsp;|&nbsp; Serial: ${na(d.serialNumber)}
            </div>
            <div class="alert-reason" style="margin-top:6px">${escHtml(a.message||'')}</div>
            <div style="font-size:11px;color:var(--text-muted);margin-top:4px">${fmtDateTime(a.createdAt)}</div>
          </div>
          <div>${alertStatusBadge(a.status)}</div>
        </div>
        ${a.status==='OPEN' ? `
        <div class="alert-actions">
          <button class="btn-sm-dark" onclick="resolve(${a.id},'ALLOWED')">✔ Allow Once</button>
          <button class="btn-sm-dark primary" onclick="resolveAndTrust(${a.id},${d.id})">🛡 Mark Trusted</button>
          <button class="btn-sm-dark" onclick="resolve(${a.id},'BLOCK_SIMULATED')" title="Simulation only — no real blocking performed">🚫 Simulate Block</button>
          <button class="btn-sm-dark" onclick="resolve(${a.id},'DISMISSED')">✕ Dismiss</button>
        </div>` : ''}
      </div>`;
    }).join('');
  } catch(e) { console.error(e); }
}

async function resolve(alertId, status) {
  try {
    await API.post(`/api/alerts/${alertId}/resolve`, {status});
    const label = {ALLOWED:'Allowed',BLOCK_SIMULATED:'Block simulated (no actual blocking performed)',DISMISSED:'Dismissed'}[status]||status;
    showToast('Alert Resolved', label, status==='BLOCK_SIMULATED'?'medium':'low');
    loadAlerts();
  } catch(e) { showToast('Error', e.message, 'high'); }
}

async function resolveAndTrust(alertId, deviceId) {
  try {
    await API.post(`/api/devices/${deviceId}/trust`, {label:''});
    await API.post(`/api/alerts/${alertId}/resolve`, {status:'TRUSTED'});
    showToast('Device Trusted','Device added to trusted list and alert resolved.','low');
    loadAlerts();
  } catch(e) { showToast('Error', e.message, 'high'); }
}
