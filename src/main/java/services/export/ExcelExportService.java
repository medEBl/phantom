package services.export;

import entities.user.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public void exportUsersToExcel(List<User> users, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // Create title style
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Users Report");
            titleCell.setCellStyle(titleStyle);
            
            // Generation date row
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated: " + java.time.LocalDateTime.now().format(DATE_FORMATTER));
            
            // Header row
            Row headerRow = sheet.createRow(3);
            String[] headers = {"ID", "Username", "Full Name", "Email", "Role", "Country", "Status", "Achievement Points", "Created Date", "Last Login"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            int rowNum = 4;
            for (User user : users) {
                Row row = sheet.createRow(rowNum);
                
                // ID
                Cell idCell = row.createCell(0);
                idCell.setCellValue(user.getId());
                idCell.setCellStyle(dataStyle);
                
                // Username
                Cell usernameCell = row.createCell(1);
                usernameCell.setCellValue(user.getUsername() != null ? user.getUsername() : "");
                usernameCell.setCellStyle(dataStyle);
                
                // Full Name
                Cell fullNameCell = row.createCell(2);
                fullNameCell.setCellValue(user.getFullName() != null ? user.getFullName() : "");
                fullNameCell.setCellStyle(dataStyle);
                
                // Email
                Cell emailCell = row.createCell(3);
                emailCell.setCellValue(user.getEmail() != null ? user.getEmail() : "");
                emailCell.setCellStyle(dataStyle);
                
                // Role
                Cell roleCell = row.createCell(4);
                roleCell.setCellValue(user.getRole() != null ? user.getRole() : "");
                roleCell.setCellStyle(dataStyle);
                
                // Country
                Cell countryCell = row.createCell(5);
                countryCell.setCellValue(user.getCountry() != null ? user.getCountry() : "");
                countryCell.setCellStyle(dataStyle);
                
                // Status
                Cell statusCell = row.createCell(6);
                statusCell.setCellValue(user.isActive() ? "Active" : "Inactive");
                statusCell.setCellStyle(dataStyle);
                
                // Achievement Points
                Cell achievementPointsCell = row.createCell(7);
                achievementPointsCell.setCellValue(user.getAchievementPoints());
                achievementPointsCell.setCellStyle(dataStyle);
                
                // Created Date
                Cell createdDateCell = row.createCell(8);
                if (user.getCreatedAt() != null) {
                    createdDateCell.setCellValue(user.getCreatedAt().format(DATE_FORMATTER));
                } else {
                    createdDateCell.setCellValue("");
                }
                createdDateCell.setCellStyle(dataStyle);
                
                // Last Login
                Cell lastLoginCell = row.createCell(9);
                if (user.getLastLoginAt() != null) {
                    lastLoginCell.setCellValue(user.getLastLoginAt().format(DATE_FORMATTER));
                } else {
                    lastLoginCell.setCellValue("");
                }
                lastLoginCell.setCellStyle(dataStyle);
                
                rowNum++;
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some extra width for better readability
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Merge title cells
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, headers.length - 1));
            
            // Footer row
            Row footerRow = sheet.createRow(rowNum + 1);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("Total users: " + users.size());
            footerCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum + 1, rowNum + 1, 0, headers.length - 1));
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }
}
