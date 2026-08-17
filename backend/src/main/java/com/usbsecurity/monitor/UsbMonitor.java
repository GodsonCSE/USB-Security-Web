package com.usbsecurity.monitor;

import java.util.List;

public interface UsbMonitor {
    void start(UsbMonitorListener listener);
    void stop();
    List<DeviceInfo> scanCurrentDevices();
    boolean isRunning();
}
