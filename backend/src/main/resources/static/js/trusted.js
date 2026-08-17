/* trusted.js — Trusted devices whitelist page */
document.addEventListener('DOMContentLoaded', () => {
  loadTrusted();
  ['DEVICE_TRUSTED','DEVICE_UNTRUSTED'].forEach(e => onSseEvent(e, loadTrusted));
});

async function loadTrusted() {
  try {
    const list = await API.get('/api/devices/trusted');
    const tbody = document.getElementById('trusted-tbody');
    if (!list.length) {
      tbody.innerHTML=`<tr><td colspan="8" class="empty-state">${Icon('shield')}<br>No trusted devices yet. Go to <a href="/devices.html" style="color:var(--accent-green)">Devices</a> to add one.</td></tr>`;
      return;
    }
    tbody.innerHTML = list.map(t => {
      const d = t.device || {};
      return `<tr>
        <td>${t.label ? escHtml(t.label) : deviceName(d)}</td>
        <td>${na(d.deviceName)}</td>
        <td>${na(d.manufacturer)}</td>
        <td>${na(d.vendorId)}</td>
        <td>${na(d.productId)}</td>
        <td>${na(d.serialNumber)}</td>
        <td>${fmtDateTime(t.createdAt)}</td>
        <td><button class="btn-sm-dark danger" onclick="removeTrust(${t.id})">Remove</button></td></tr>`;
    }).join('');
  } catch(e) { console.error(e); }
}

async function removeTrust(trustedId) {
  if (!confirm('Remove this device from the trusted list?')) return;
  try {
    await API.del(`/api/devices/trusted/${trustedId}`);
    showToast('Trust Removed','Device removed from trusted list.','medium');
    loadTrusted();
  } catch(e) { showToast('Error', e.message, 'high'); }
}
