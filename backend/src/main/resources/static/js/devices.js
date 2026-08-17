/* devices.js — All USB devices page */
document.addEventListener('DOMContentLoaded', () => {
  loadDevices();
  ['DEVICE_CONNECTED','DEVICE_DISCONNECTED','DEVICE_TRUSTED','DEVICE_UNTRUSTED'].forEach(e => onSseEvent(e, loadDevices));
});

async function loadDevices() {
  try {
    const devices = await API.get('/api/devices');
    const tbody = document.getElementById('devices-tbody');
    if (!devices.length) {
      tbody.innerHTML=`<tr><td colspan="9" class="empty-state">${Icon('usb')}<br>No USB devices detected yet.</td></tr>`;
      return;
    }
    tbody.innerHTML = devices.map(d => `<tr>
      <td>${deviceName(d)}</td>
      <td>${na(d.manufacturer)}</td>
      <td>${na(d.vendorId)}</td>
      <td>${na(d.productId)}</td>
      <td>${na(d.serialNumber)}</td>
      <td>${na(d.deviceType)}</td>
      <td>${statusBadge(d.trusted)}</td>
      <td>${riskBadge(d.riskLevel)}</td>
      <td>${actionBtn(d)}</td></tr>`).join('');
  } catch(e) { console.error(e); }
}

function actionBtn(d) {
  if (d.trusted) {
    return `<button class="btn-sm-dark danger" onclick="untrust(${d.id})">Remove Trust</button>`;
  }
  return `<button class="btn-sm-dark primary" onclick="trust(${d.id})">Add to Trusted</button>`;
}

async function trust(deviceId) {
  try {
    await API.post(`/api/devices/${deviceId}/trust`, {label: ''});
    showToast('Device Trusted', 'Device added to trusted list.', 'low');
    loadDevices();
  } catch(e) { showToast('Error', e.message, 'high'); }
}

async function untrust(deviceId) {
  try {
    const trusted = await API.get('/api/devices/trusted');
    const entry = trusted.find(t => t.device?.id === deviceId);
    if (!entry) { showToast('Error','Trusted entry not found','high'); return; }
    await API.del(`/api/devices/trusted/${entry.id}`);
    showToast('Trust Removed', 'Device removed from trusted list.', 'medium');
    loadDevices();
  } catch(e) { showToast('Error', e.message, 'high'); }
}
