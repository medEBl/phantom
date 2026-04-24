package services.export;

import entities.user.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public void exportUsersToPDF(List<User> users, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Users Report");
            contentStream.endText();
            
            // Generation date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Generated: " + java.time.LocalDateTime.now().format(DATE_FORMATTER));
            contentStream.endText();
            
            // Table headers
            float yPosition = 700;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("ID");
            contentStream.newLineAtOffset(60, 0);
            contentStream.showText("Username");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("Full Name");
            contentStream.newLineAtOffset(120, 0);
            contentStream.showText("Email");
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText("Role");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("Country");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("Status");
            contentStream.newLineAtOffset(60, 0);
            contentStream.showText("Created");
            contentStream.endText();
            
            // Draw line under headers
            contentStream.moveTo(50, yPosition - 5);
            contentStream.lineTo(550, yPosition - 5);
            contentStream.stroke();
            
            // Table data
            yPosition -= 20;
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            
            for (User user : users) {
                if (yPosition < 50) {
                    // Add new page if needed
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 750;
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                }
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                
                // ID
                contentStream.showText(String.valueOf(user.getId()));
                contentStream.newLineAtOffset(60, 0);
                
                // Username
                String username = user.getUsername() != null ? user.getUsername() : "";
                contentStream.showText(truncateString(username, 12));
                contentStream.newLineAtOffset(100, 0);
                
                // Full Name
                String fullName = user.getFullName() != null ? user.getFullName() : "";
                contentStream.showText(truncateString(fullName, 15));
                contentStream.newLineAtOffset(120, 0);
                
                // Email
                String email = user.getEmail() != null ? user.getEmail() : "";
                contentStream.showText(truncateString(email, 20));
                contentStream.newLineAtOffset(200, 0);
                
                // Role
                String role = user.getRole() != null ? user.getRole() : "";
                contentStream.showText(truncateString(role, 8));
                contentStream.newLineAtOffset(80, 0);
                
                // Country
                String country = user.getCountry() != null ? user.getCountry() : "";
                contentStream.showText(truncateString(country, 8));
                contentStream.newLineAtOffset(80, 0);
                
                // Status
                String status = user.isActive() ? "Active" : "Inactive";
                contentStream.showText(status);
                contentStream.newLineAtOffset(60, 0);
                
                // Created Date
                String createdDate = user.getCreatedAt() != null ? 
                    user.getCreatedAt().format(DATE_FORMATTER) : "";
                contentStream.showText(truncateString(createdDate, 12));
                
                contentStream.endText();
                yPosition -= 15;
            }
            
            // Footer
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.newLineAtOffset(50, 30);
            contentStream.showText("Total users: " + users.size());
            contentStream.endText();
            
            contentStream.close();
            document.save(filePath);
        }
    }
    
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
