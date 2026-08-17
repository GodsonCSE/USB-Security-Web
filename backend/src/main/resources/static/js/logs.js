/* logs.js — Security event log with search, filter, export */
let currentEvents = [];

document.addEventListener('DOMContentLoaded', () => {
  loadLogs();
  document.getElementById('btn-search').addEventListener('click', loadLogs);
  document.getElementById('btn-clear').addEventListener('click', () => {
    ['filter-from','filter-to','filter-device'].forEach(id => document.getElementById(id).value='');
    ['filter-type','filter-risk','filter-sort'].forEach(id => document.getElementById(id).value='');
    loadLogs();
  });
  document.getElementById('btn-csv').addEventListener('click', () => exportLogs('csv'));
  document.getElementById('btn-pdf').addEventListener('click', () => exportLogs('pdf'));
});

async function loadLogs() {
  const from   = document.getElementById('filter-from')?.value;
  const to     = document.getElementById('filter-to')?.value;
  const type   = document.getElementById('filter-type')?.value;
  const risk   = document.getElementById('filter-risk')?.value;
  const device = document.getElementById('filter-device')?.value;
  const sort   = document.getElementById('filter-sort')?.value || 'desc';

  const params = new URLSearchParams();
  if (from)   params.append('from', from);
  if (to)     params.append('to',   to);
  if (type)   params.append('type', type);
  if (risk)   params.append('risk', risk);
  if (device) params.append('device', device);

  try {
    let events = await API.get('/api/events?' + params.toString());
    if (sort === 'asc') events = events.reverse();
    currentEvents = events;

    const count = document.getElementById('result-count');
    if (count) count.textContent = `${events.length} result(s)`;

    const tbody = document.getElementById('logs-tbody');
    if (!events.length) {
      tbody.innerHTML=`<tr><td colspan="6" class="empty-state">${Icon('search')}<br>No matching log entries.</td></tr>`;
      return;
    }
    tbody.innerHTML = events.map(e => `<tr>
      <td style="white-space:nowrap">${fmtDateTime(e.timestamp)}</td>
      <td>${escHtml(e.eventType||'')}</td>
      <td>${deviceName(e.device)}</td>
      <td>${na(e.device?.manufacturer)}</td>
      <td>${riskBadge(e.riskLevel)}</td>
      <td style="max-width:300px;font-size:12px;color:var(--text-muted)">${escHtml(e.reason||'')}</td></tr>`).join('');
  } catch(err) { console.error(err); }
}

function sanitizeCsvValue(val) {
  if (val == null) return '';
  const s = String(val).trim();
  if (/^[=+\-@\t\r]/.test(s)) {
    return "'" + s;
  }
  return s;
}

function exportLogs(format) {
  if (window.location.protocol === 'file:') {
    if (format === 'csv') {
      let content = "Timestamp,Event,Device,Manufacturer,VID,PID,Serial,Risk,Reason\n";
      currentEvents.forEach(e => {
        const row = [
          sanitizeCsvValue(e.timestamp),
          sanitizeCsvValue(e.eventType),
          sanitizeCsvValue(e.device?.deviceName),
          sanitizeCsvValue(e.device?.manufacturer),
          sanitizeCsvValue(e.device?.vendorId),
          sanitizeCsvValue(e.device?.productId),
          sanitizeCsvValue(e.device?.serialNumber),
          sanitizeCsvValue(e.riskLevel),
          sanitizeCsvValue(e.reason)
        ];
        content += row.map(v => `"${v.replace(/"/g, '""')}"`).join(',') + "\n";
      });
      const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `usb_security_logs_${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      showToast('Export Successful', 'CSV exported successfully in preview mode.', 'low');
    } else {
      window.print();
    }
    return;
  }

  const from   = document.getElementById('filter-from')?.value;
  const to     = document.getElementById('filter-to')?.value;
  const type   = document.getElementById('filter-type')?.value;
  const risk   = document.getElementById('filter-risk')?.value;
  const device = document.getElementById('filter-device')?.value;

  const params = new URLSearchParams();
  if (from)   params.append('from', from);
  if (to)     params.append('to',   to);
  if (type)   params.append('type', type);
  if (risk)   params.append('risk', risk);
  if (device) params.append('device', device);

  window.location.href = `/api/export/${format}?${params.toString()}`;
}
