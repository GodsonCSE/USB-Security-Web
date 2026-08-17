package com.usbsecurity.repository;

import com.usbsecurity.model.UsbDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsbDeviceRepository extends JpaRepository<UsbDevice, Long> {
    Optional<UsbDevice> findByDeviceKey(String deviceKey);
    List<UsbDevice> findByCurrentlyConnectedTrue();
    List<UsbDevice> findAllByOrderByLastSeenAtDesc();
}
