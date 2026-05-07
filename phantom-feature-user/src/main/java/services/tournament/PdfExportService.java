package services.tournament;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entities.tournament.Registration;
import entities.tournament.Tournament;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    public void exportTournamentsList(List<Tournament> tournaments, File file) throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("Tournaments List", TITLE_FONT);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f, 2f, 2f, 2f});

        addTableHeader(table, "Name", "Game", "Start Date", "Phase", "Status");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Tournament t : tournaments) {
            table.addCell(new Phrase(t.getName(), NORMAL_FONT));
            table.addCell(new Phrase(t.getGame(), NORMAL_FONT));
            table.addCell(new Phrase(t.getStartDate() != null ? t.getStartDate().format(formatter) : "N/A", NORMAL_FONT));
            table.addCell(new Phrase(t.getPhase(), NORMAL_FONT));
            table.addCell(new Phrase(t.isActive() ? "Active" : "Inactive", NORMAL_FONT));
        }

        document.add(table);
        document.close();
    }

    public void exportRegistrationsList(Tournament tournament, List<Registration> registrations, File file) throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("Registered Teams", TITLE_FONT);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        Paragraph subTitle = new Paragraph("Tournament: " + tournament.getName(), HEADER_FONT);
        subTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(20);
        document.add(subTitle);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 3f, 3f});

        addTableHeader(table, "Team Name", "Contact Email", "Registration Date");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Registration r : registrations) {
            table.addCell(new Phrase(r.getTeamName(), NORMAL_FONT));
            table.addCell(new Phrase(r.getContactEmail(), NORMAL_FONT));
            table.addCell(new Phrase(r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : "N/A", NORMAL_FONT));
        }

        document.add(table);
        document.close();
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
