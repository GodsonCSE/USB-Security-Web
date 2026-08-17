package com.usbsecurity.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.UnitValue;
import com.usbsecurity.model.UsbEvent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportCsv(List<UsbEvent> events) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Writer w = new OutputStreamWriter(out);
             CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.builder()
                     .setHeader("Timestamp","Event","Device","Manufacturer",
                                "VID","PID","Serial","Risk","Reason").build())) {
            for (UsbEvent e : events) {
                p.printRecord(
                    sanitizeCsv(fmt(e)),
                    sanitizeCsv(str(e.getEventType())),
                    sanitizeCsv(dv(e, "deviceName")),
                    sanitizeCsv(dv(e, "manufacturer")),
                    sanitizeCsv(dv(e, "vendorId")),
                    sanitizeCsv(dv(e, "productId")),
                    sanitizeCsv(dv(e, "serialNumber")),
                    sanitizeCsv(str(e.getRiskLevel())),
                    sanitizeCsv(e.getReason())
                );
            }
        }
        return out.toByteArray();
    }

    private String sanitizeCsv(String val) {
        if (val == null) return "";
        if (val.startsWith("\t") || val.startsWith("\r") || val.startsWith("\n")) {
            return "'" + val;
        }
        String s = val.trim();
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            return "'" + val;
        }
        return val;
    }

    public byte[] exportPdf(List<UsbEvent> events) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(w);
             Document doc = new Document(pdf)) {

            doc.add(new Paragraph("USB Device Security Monitor — Activity Report")
                    .setBold().setFontSize(16));
            doc.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(FMT))
                    .setFontSize(9).setItalic());
            doc.add(new Paragraph("⚠ Risk levels are heuristic indicators, not proof of malicious activity.")
                    .setFontSize(9).setItalic());
            doc.add(new Paragraph(" "));

            Table t = new Table(UnitValue.createPercentArray(new float[]{13,9,16,13,7,7,13,7,15}))
                    .useAllAvailableWidth();
            for (String h : new String[]{"Timestamp","Event","Device","Manufacturer","VID","PID","Serial","Risk","Reason"})
                t.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontSize(8)));

            for (UsbEvent e : events) {
                t.addCell(cell(fmt(e)));
                t.addCell(cell(str(e.getEventType())));
                t.addCell(cell(dv(e, "deviceName")));
                t.addCell(cell(dv(e, "manufacturer")));
                t.addCell(cell(dv(e, "vendorId")));
                t.addCell(cell(dv(e, "productId")));
                t.addCell(cell(dv(e, "serialNumber")));
                t.addCell(cell(str(e.getRiskLevel())));
                t.addCell(cell(e.getReason() != null ? e.getReason() : ""));
            }
            doc.add(t);
        }
        return out.toByteArray();
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text == null ? "" : text).setFontSize(7));
    }
    private String fmt(UsbEvent e) { return e.getTimestamp() != null ? e.getTimestamp().format(FMT) : ""; }
    private String str(Object o) { return o != null ? o.toString() : ""; }
    private String dv(UsbEvent e, String field) {
        if (e.getDevice() == null) return "";
        return switch (field) {
            case "deviceName"   -> e.getDevice().getDeviceName();
            case "manufacturer" -> e.getDevice().getManufacturer();
            case "vendorId"     -> e.getDevice().getVendorId();
            case "productId"    -> e.getDevice().getProductId();
            case "serialNumber" -> e.getDevice().getSerialNumber();
            default             -> "";
        };
    }
}
