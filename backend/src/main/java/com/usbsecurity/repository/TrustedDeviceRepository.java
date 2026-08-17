package com.usbsecurity.repository;

import com.usbsecurity.model.TrustedDevice;
import com.usbsecurity.model.UsbDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {
    Optional<TrustedDevice> findByDevice(UsbDevice device);
    boolean existsByDevice(UsbDevice device);
    Optional<TrustedDevice> findByDeviceId(Long deviceId);
    List<TrustedDevice> findAllByOrderByCreatedAtDesc();
}
