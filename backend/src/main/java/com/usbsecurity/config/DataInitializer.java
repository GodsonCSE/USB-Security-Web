package com.usbsecurity.config;

import com.usbsecurity.model.*;
import com.usbsecurity.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsbDeviceRepository deviceRepo;
    private final TrustedDeviceRepository trustedRepo;
    private final UsbEventRepository eventRepo;
    private final SecurityAlertRepository alertRepo;

    @Value("${usb.monitor.simulate:false}")
    private boolean simulate;

    @Value("${usb.monitor.seed-sample-data:false}")
    private boolean seedSampleData;

    public DataInitializer(UsbDeviceRepository deviceRepo,
                           TrustedDeviceRepository trustedRepo,
                           UsbEventRepository eventRepo,
                           SecurityAlertRepository alertRepo) {
        this.deviceRepo = deviceRepo;
        this.trustedRepo = trustedRepo;
        this.eventRepo = eventRepo;
        this.alertRepo = alertRepo;
    }

    @Override
    public void run(String... args) {
        // Only seed sample data if in simulation/demo mode or explicitly enabled
        if (!simulate && !seedSampleData) {
            return;
        }

        if (deviceRepo.count() > 0) {
            return;
        }

        log.info("Simulation mode active: populating initial USB Security sample data...");

        LocalDateTime now = LocalDateTime.now();

        // 1. Trusted keyboard
        UsbDevice kbd = new UsbDevice();
        kbd.setDeviceKey("046D:C31C:LOGI-KB-98124");
        kbd.setDeviceName("Logitech K120 Keyboard");
        kbd.setManufacturer("Logitech");
        kbd.setVendorId("046D");
        kbd.setProductId("C31C");
        kbd.setSerialNumber("LOGI-KB-98124");
        kbd.setDeviceType("HID / Keyboard");
        kbd.setCurrentlyConnected(true);
        kbd.setFirstSeenAt(now.minusDays(5));
        kbd.setLastSeenAt(now);
        deviceRepo.save(kbd);

        TrustedDevice trustedKbd = new TrustedDevice(kbd, "Corporate Standard Keyboard");
        trustedKbd.setCreatedAt(now.minusDays(5));
        trustedRepo.save(trustedKbd);

        UsbEvent evKbd1 = new UsbEvent(kbd, EventType.CONNECTED, RiskLevel.LOW, "Whitelisted device connected");
        evKbd1.setTimestamp(now.minusDays(5));
        eventRepo.save(evKbd1);

        UsbEvent evKbd2 = new UsbEvent(kbd, EventType.TRUSTED, RiskLevel.LOW, "Added to trusted whitelist");
        evKbd2.setTimestamp(now.minusDays(5));
        eventRepo.save(evKbd2);

        // 2. Trusted mouse
        UsbDevice mouse = new UsbDevice();
        mouse.setDeviceKey("046D:C077:LOGI-M100-5521");
        mouse.setDeviceName("Logitech Optical Mouse M100");
        mouse.setManufacturer("Logitech");
        mouse.setVendorId("046D");
        mouse.setProductId("C077");
        mouse.setSerialNumber("LOGI-M100-5521");
        mouse.setDeviceType("HID / Mouse");
        mouse.setCurrentlyConnected(true);
        mouse.setFirstSeenAt(now.minusDays(4));
        mouse.setLastSeenAt(now);
        deviceRepo.save(mouse);

        TrustedDevice trustedMouse = new TrustedDevice(mouse, "Office Optical Mouse");
        trustedMouse.setCreatedAt(now.minusDays(4));
        trustedRepo.save(trustedMouse);

        UsbEvent evMouse = new UsbEvent(mouse, EventType.CONNECTED, RiskLevel.LOW, "Whitelisted device connected");
        evMouse.setTimestamp(now.minusDays(4));
        eventRepo.save(evMouse);

        // 3. Unknown flash drive (Medium risk)
        UsbDevice drive = new UsbDevice();
        drive.setDeviceKey("0781:5583:SANDISK-ULTRA-8831");
        drive.setDeviceName("SanDisk Ultra USB 3.0");
        drive.setManufacturer("SanDisk");
        drive.setVendorId("0781");
        drive.setProductId("5583");
        drive.setSerialNumber("SANDISK-ULTRA-8831");
        drive.setDeviceType("Mass Storage");
        drive.setCurrentlyConnected(true);
        drive.setFirstSeenAt(now.minusHours(2));
        drive.setLastSeenAt(now);
        deviceRepo.save(drive);

        UsbEvent evDrive = new UsbEvent(drive, EventType.CONNECTED, RiskLevel.MEDIUM, "Unknown mass storage device attached");
        evDrive.setTimestamp(now.minusHours(2));
        eventRepo.save(evDrive);

        SecurityAlert alertDrive = new SecurityAlert(drive, RiskLevel.MEDIUM, "Unknown USB Mass Storage detected: SanDisk Ultra USB 3.0 (0781:5583)");
        alertDrive.setCreatedAt(now.minusHours(2));
        alertRepo.save(alertDrive);

        // 4. Suspicious unknown device (High risk - missing serial / unrecognized HID)
        UsbDevice suspicious = new UsbDevice();
        suspicious.setDeviceKey("1209:0001:NOSERIAL-RUBBERDUCKY");
        suspicious.setDeviceName("Generic USB Composite Device");
        suspicious.setManufacturer("Unknown");
        suspicious.setVendorId("1209");
        suspicious.setProductId("0001");
        suspicious.setSerialNumber("Unknown");
        suspicious.setDeviceType("Composite / HID");
        suspicious.setCurrentlyConnected(false);
        suspicious.setFirstSeenAt(now.minusMinutes(45));
        suspicious.setLastSeenAt(now.minusMinutes(10));
        deviceRepo.save(suspicious);

        UsbEvent evSusp1 = new UsbEvent(suspicious, EventType.CONNECTED, RiskLevel.HIGH, "Unregistered composite device without valid serial number");
        evSusp1.setTimestamp(now.minusMinutes(45));
        eventRepo.save(evSusp1);

        UsbEvent evSusp2 = new UsbEvent(suspicious, EventType.DISCONNECTED, RiskLevel.HIGH, "Suspicious device unplugged");
        evSusp2.setTimestamp(now.minusMinutes(10));
        eventRepo.save(evSusp2);

        SecurityAlert alertSusp = new SecurityAlert(suspicious, RiskLevel.HIGH, "High-Risk USB device: Missing serial number and unknown vendor signature");
        alertSusp.setCreatedAt(now.minusMinutes(45));
        alertRepo.save(alertSusp);

        log.info("Initial sample data loaded successfully.");
    }
}
