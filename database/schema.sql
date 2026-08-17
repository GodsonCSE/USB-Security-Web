-- ======================================================
-- USB Device Security Monitor — Reference Schema
-- ======================================================
-- NOTE: Spring Boot (spring.jpa.hibernate.ddl-auto=update)
-- creates / updates these tables automatically on startup.
-- This file is provided for reference and for manual setup.
--
--   mysql -u root -p < database/schema.sql
-- ======================================================

CREATE DATABASE IF NOT EXISTS usb_security_monitor
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE usb_security_monitor;

CREATE TABLE IF NOT EXISTS usb_devices (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_key          VARCHAR(200) NOT NULL UNIQUE,
  device_name         VARCHAR(200),
  manufacturer        VARCHAR(200),
  vendor_id           VARCHAR(10),
  product_id          VARCHAR(10),
  serial_number       VARCHAR(200),
  device_type         VARCHAR(60),
  currently_connected BOOLEAN NOT NULL DEFAULT FALSE,
  first_seen_at       DATETIME,
  last_seen_at        DATETIME,
  INDEX idx_device_vid_pid (vendor_id, product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS trusted_devices (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_id   BIGINT NOT NULL UNIQUE,
  label       VARCHAR(200),
  created_at  DATETIME,
  CONSTRAINT fk_trusted_device FOREIGN KEY (device_id) REFERENCES usb_devices(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usb_events (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_id   BIGINT,
  event_type  VARCHAR(30) NOT NULL,
  risk_level  VARCHAR(10),
  reason      VARCHAR(500),
  timestamp   DATETIME,
  CONSTRAINT fk_event_device FOREIGN KEY (device_id) REFERENCES usb_devices(id) ON DELETE SET NULL,
  INDEX idx_event_time (timestamp),
  INDEX idx_event_type (event_type),
  INDEX idx_event_risk (risk_level)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS security_alerts (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_id    BIGINT,
  severity     VARCHAR(10),
  message      VARCHAR(500),
  status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  created_at   DATETIME,
  resolved_at  DATETIME,
  CONSTRAINT fk_alert_device FOREIGN KEY (device_id) REFERENCES usb_devices(id) ON DELETE SET NULL,
  INDEX idx_alert_status (status)
) ENGINE=InnoDB;
