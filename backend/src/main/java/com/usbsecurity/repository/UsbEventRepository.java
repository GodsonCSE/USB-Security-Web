package com.usbsecurity.repository;

import com.usbsecurity.model.EventType;
import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.model.UsbEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsbEventRepository extends JpaRepository<UsbEvent, Long> {

    List<UsbEvent> findTop20ByOrderByTimestampDesc();

    @Query("""
        SELECT e FROM UsbEvent e
        WHERE (:from IS NULL OR e.timestamp >= :from)
          AND (:to   IS NULL OR e.timestamp <= :to)
          AND (:type IS NULL OR e.eventType = :type)
          AND (:risk IS NULL OR e.riskLevel = :risk)
          AND (:name IS NULL OR LOWER(e.device.deviceName) LIKE LOWER(CONCAT('%',:name,'%')))
        ORDER BY e.timestamp DESC
        LIMIT 2000
    """)
    List<UsbEvent> search(
        @Param("from") LocalDateTime from,
        @Param("to")   LocalDateTime to,
        @Param("type") EventType type,
        @Param("risk") RiskLevel risk,
        @Param("name") String name
    );
}
