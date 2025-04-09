package com.techsol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.techsol.models.Element;

public class App {
    public static void main(String[] args) throws Exception {
        String docxPath = "C:\\Users\\cgithogori\\Downloads\\Co-op-ServiceSpecification-TradeFinance-FBHistoryDtlsInq.Get-1.0.docx";
        String docxPath2 = "C:\\Users\\cgithogori\\Downloads\\Co-op-ServiceSpecification-TradeFinance - Invoke Inward BG-Post-1.0.docx";
        Map<String, Element> elements = new HashMap<>();
        // try (FileInputStream fis = new FileInputStream(docxPath2)) {
        //     XWPFDocument doc = new XWPFDocument(fis);
        //     List<XWPFTable> tables = doc.getTables();

        //     if (tables.size() == 0) {
        //         doc.close();
        //         System.out.println("No tables with data to parse found");
        //         return;
        //     }

        //     for (XWPFTable table : tables) {
        //         XWPFTableRow headerRow = table.getRow(0);
        //         if (headerRow.getTableCells().size() == 5) {
        //             if (isOutputTable(headerRow)) {
        //                 processTable(table, elements);
        //             }
        //         }
        //     }
        //     doc.close();
        // }

        String filePath = "C:\\Users\\cgithogori\\Downloads\\APP.BS.TradeFinance.InvokeInwardBG.Post.1.0_Mapping.xlsx";
        String sheetName = "Input Mapping";

        try (FileInputStream fis = new FileInputStream(new File(filePath));
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("Sheet not found: " + sheetName);
                return;
            }

            for (Row row : sheet) {
                // System.out.println(row.getCell(5).getStringCellValue());
                String fieldName = row.getCell(5).getStringCellValue().trim().replace("\n",
                        "").replace("\r", "");
                String type = row.getCell(6).getStringCellValue().trim();
                String occurrence = row.getCell(7) != null ? row.getCell(7).getStringCellValue().trim() : "0";

                if (fieldName.startsWith(".")) {
                    continue;
                }

                if (fieldName != null && type != null && occurrence != null) {
                    // System.out.println("Fieldname: " + fieldName);
                    Element parent = null;
                    String[] parts = fieldName.split("\\.");

                    for (int j = 0; j < parts.length; j++) {
                        String part = parts[j].trim();
                        if (part.isEmpty() || part.isBlank()) {
                            continue;
                        }

                        if (j == 0 && parts.length > 1) {
                            parent = elements.computeIfAbsent(part,
                                    k -> new Element(part, "complex", getMinOccurs(occurrence),
                                            getMaxOccurs(occurrence), false));
                        } else if (j == 0 && parts.length == 1) {
                            parent = elements.computeIfAbsent(part, k -> new Element(part,
                                    mapXsdType(type),
                                    getMinOccurs(occurrence), getMaxOccurs(occurrence), false));
                        } else {
                            Element current = findOrCreateChild(parent, part,
                                    j == parts.length - 1 ? mapXsdType(type) : "complex", occurrence);
                            parent = current;
                        }
                    }
                }
            }
        }

        if (elements.size() == 0) {
            System.out.println("Failed to find tables with data to parse. Check the file and try again.");
            return;
        }

        System.out.println("Completed parsing ... generating output.xsd");
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n");
        stringWriter.write("<xs:schema>\n");

        for (Element element : elements.values()) {
            if (!element.isHasParent()) {
                writeElement(stringWriter, element, 1);
            }
        }

        stringWriter.write("</xs:schema>");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("output.xsd")));
        bufferedWriter.write(stringWriter.toString());
        bufferedWriter.close();

        System.out.println("XSD generated: output.xsd");
    }

    private static void writeElement(StringWriter writer, Element element, int indentLevel) throws IOException {
        String indent = "    ".repeat(indentLevel);

        if (element.isComplex() && !element.getMaxOccurs().equals("unbounded")) {
            writer.write(indent + "<xs:element name=\"" + element.getName() + "\">\n");
            writer.write(indent + "    <xs:complexType>\n");
            writer.write(indent + "        <xs:sequence>\n");

            for (Element child : element.getChildren()) {
                writeElement(writer, child, indentLevel + 2);
            }

            writer.write(indent + "        </xs:sequence>\n");
            writer.write(indent + "    </xs:complexType>\n");
            writer.write(indent + "</xs:element>\n");
        } else if (element.isComplex() && element.getMaxOccurs().equals("unbounded")) {
            if (element.getName().contains("amountValue")) {
                System.out.println("Writing amountValue");
            }
            writer.write(indent + "<xs:element name=\"" + element.getName() + "\" minOccurs=\"" + element.getMinOccurs()
                    + "\" maxOccurs=\"unbounded\">\n");
            writer.write(indent + "    <xs:complexType>\n");
            writer.write(indent + "        <xs:sequence>\n");

            for (Element child : element.getChildren()) {
                writeElement(writer, child, indentLevel + 3);
            }

            writer.write(indent + "        </xs:sequence>\n");
            writer.write(indent + "    </xs:complexType>\n");
            writer.write(indent + "</xs:element>\n");
        } else {
            writer.write(indent + "<xs:element name=\"" + element.getName() + "\" type=\"" + element.getType()
                    + "\" minOccurs=\"" + element.getMinOccurs() + "\"");
            if (element.getMaxOccurs().equals("unbounded")) {
                writer.write(" maxOccurs=\"unbounded\"");
            }
            writer.write("/>\n");
        }
    }

    private static boolean isOutputTable(XWPFTableRow headerRow) {
        return headerRow.getCell(0).getText().contains("FieldName")
                || headerRow.getCell(0).getText().contains("Field Name")
                || headerRow.getCell(0).getText().trim().toLowerCase().equals("field") &&
                        headerRow.getCell(3).getText().contains("Type") &&
                        headerRow.getCell(2).getText().contains("Occurrence")
                || headerRow.getCell(2).getText().contains("Occurence");
    }

    private static void processTable(XWPFTable table, Map<String, Element> elements) {
        for (int i = 1; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            String fieldName = row.getCell(0).getText().trim().replace("\n", "").replace("\r", "");
            // System.out.println("Field : " + fieldName);
            String type = row.getCell(3) == null ? "string" : row.getCell(3).getText().split("\\(")[0].trim();
            String occurrence = row.getCell(2) == null ? "0...1" : row.getCell(2).getText().trim();

            Element parent = null;
            String[] parts = fieldName.split("\\.");

            for (int j = 0; j < parts.length; j++) {
                String part = parts[j].trim();
                if (part.isEmpty() || part.isBlank()) {
                    continue;
                }

                if (j == 0 && parts.length > 1) {
                    parent = elements.computeIfAbsent(part, k -> new Element(part, "complex", getMinOccurs(occurrence),
                            getMaxOccurs(occurrence), false));
                } else if (j == 0 && parts.length == 1) {
                    parent = elements.computeIfAbsent(part, k -> new Element(part, mapXsdType(type),
                            getMinOccurs(occurrence), getMaxOccurs(occurrence), false));
                } else {
                    Element current = findOrCreateChild(parent, part.trim(),
                            j == parts.length - 1 ? mapXsdType(type) : "complex", occurrence);
                    parent = current;
                }
            }
        }
    }

    private static Element findOrCreateChild(Element parent, String childName, String type, String occurrence) {
        return parent.getChildren().stream()
                .filter(child -> child.getName().equals(childName))
                .findFirst()
                .orElseGet(() -> {
                    Element newChild = new Element(childName, type, getMinOccurs(occurrence), getMaxOccurs(occurrence),
                            true);
                    parent.addChild(newChild);
                    return newChild;
                });
    }

    private static String mapXsdType(String type) {
        if (type.toLowerCase().contains("double")) {
            return "xs:decimal";
        } else if (type.toLowerCase().contains("long")) {
            return "xs:decimal";
        } else if (type.toLowerCase().contains("date")) {
            return "xs:dateTime";
        } else if (type.toLowerCase().contains("number")) {
            return "xs:integer";
        } else {
            return "xs:string";
        }
    }

    private static String getMinOccurs(String occurrence) {
        return occurrence.startsWith("0") ? "0" : "1";
    }

    private static String getMaxOccurs(String occurrence) {
        return occurrence.endsWith("1") ? "1" : occurrence.endsWith("N") ? "unbounded" : "0";
    }

}
