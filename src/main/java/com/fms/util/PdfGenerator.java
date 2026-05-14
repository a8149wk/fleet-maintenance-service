package com.fms.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.util.List;

public final class PdfGenerator {

    private PdfGenerator() {
    }

    public static byte[] simpleTable(String title, List<String> headers, List<List<String>> rows) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        try (PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {

            doc.add(new Paragraph(title).setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Fleet Maintenance System").setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(headers.size())).useAllAvailableWidth();
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()));
            }
            for (List<String> row : rows) {
                for (String v : row) {
                    table.addCell(new Cell().add(new Paragraph(v == null ? "" : v)));
                }
            }
            doc.add(table);
        }
        return baos.toByteArray();
    }
}
