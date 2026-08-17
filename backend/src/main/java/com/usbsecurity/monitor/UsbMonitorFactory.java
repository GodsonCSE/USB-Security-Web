package com.usbsecurity.monitor;

public final class UsbMonitorFactory {

    private UsbMonitorFactory() {}

    public static UsbMonitor create(int pollMs, boolean simulate) {
        if (simulate) {
            System.out.println("[UsbMonitorFactory] Simulation mode active.");
            return new SimulatedUsbMonitor(pollMs);
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win"))                                    return new WindowsUsbMonitor(pollMs);
        if (os.contains("mac") || os.contains("darwin"))          return new MacOsUsbMonitor(pollMs);
        if (os.contains("nux") || os.contains("nix") || os.contains("bsd")) return new LinuxUsbMonitor(pollMs);

        System.err.println("[UsbMonitorFactory] Unknown OS '" + os + "' — using simulation.");
        return new SimulatedUsbMonitor(pollMs);
    }
}
