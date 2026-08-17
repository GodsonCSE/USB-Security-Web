package com.usbsecurity.monitor;

public interface UsbMonitorListener {
    void onDeviceConnected(DeviceInfo device);
    void onDeviceDisconnected(DeviceInfo device);
    void onMonitoringError(String message, Throwable cause);
}
