package com.usbsecurity.repository;

import com.usbsecurity.model.AlertStatus;
import com.usbsecurity.model.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<SecurityAlert> findAllByOrderByCreatedAtDesc();
    long countByStatus(AlertStatus status);
}
