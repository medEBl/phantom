package services.tournament;

import entities.tournament.Tournament;
import entities.user.User;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private static final String MAIL_FROM     = "noreply@phantomforce.com";
    private static final String MAIL_HOST     = "smtp.gmail.com";
    private static final String MAIL_PORT     = "587";
    private static final String MAIL_USER     = "phantomforce619@gmail.com";
    private static final String MAIL_PASSWORD = "ribsuezxxzpxnqiz";
    private static final int    TOKEN_EXPIRY_HOURS = 1;

    private Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);
        props.put("mail.smtp.ssl.trust", MAIL_HOST);
        return props;
    }

    public void sendTournamentNotification(Tournament tournament, List<User> players) {
        if (players == null || players.isEmpty()) {
            System.out.println("ℹ️ No players to notify.");
            return;
        }

        // Run in a separate thread to avoid blocking the UI/Main flow
        new Thread(() -> {
            Session session = Session.getInstance(getProperties(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
                }
            });

            for (User player : players) {
                if (player.getEmail() == null || player.getEmail().isBlank()) continue;
                
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(MAIL_FROM, "Phantom Tournament"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(player.getEmail()));
                    message.setSubject("🏆 New Tournament Alert: " + tournament.getName());

                    String content = String.format(
                            "Hello %s,\n\n" +
                            "We are excited to announce a new tournament on Phantom!\n\n" +
                            "--------------------------------------------------\n" +
                            "Tournament Details:\n" +
                            "--------------------------------------------------\n" +
                            "🎮 Game: %s\n" +
                            "🏷️ Name: %s\n" +
                            "📅 Start Date: %s\n" +
                            "🏁 End Date: %s\n" +
                            "👥 Max Teams: %d\n" +
                            "--------------------------------------------------\n\n" +
                            "Don't miss your chance to participate! Head over to the app to register.\n\n" +
                            "Best regards,\n" +
                            "The Phantom Team",
                            player.getFullName() != null ? player.getFullName() : player.getUsername(),
                            tournament.getGame(),
                            tournament.getName(),
                            tournament.getStartDate(),
                            tournament.getEndDate(),
                            tournament.getMaxTeams()
                    );

                    message.setText(content);
                    Transport.send(message);
                    System.out.println("✅ Email notification sent to: " + player.getEmail());

                } catch (Exception e) {
                    System.err.println("❌ Failed to send email to " + player.getEmail() + ": " + e.getMessage());
                }
            }
        }).start();
    }
}
