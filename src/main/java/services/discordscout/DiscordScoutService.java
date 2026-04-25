package services.discordscout; // Ajustez selon votre arborescence

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordScoutService {

    // L'URL de votre .env
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1474004374526234679/M8uYWnswLS_z5iGzNgKeK3-cMqlxTklOSKHIHySK0P_jIkZfaStwntX4aovbOpBGZXSM";

    public static void sendHighScorerAlert(String pseudo, String game, int score, String profileUrl) {

        // 1. Création du Payload JSON pour Discord
        JSONObject payload = new JSONObject();
        payload.put("username", "Phantom Scout");
        payload.put("avatar_url", "https://cdn-icons-png.flaticon.com/512/4712/4712139.png");

        // 2. Création de l'Embed (La belle carte de présentation)
        JSONObject embed = new JSONObject();
        embed.put("title", "🚨 NOUVEAU TALENT DÉTECTÉ 🚨");
        embed.put("description", "**" + pseudo + "** vient de passer l'évaluation du Coach IA !");
        embed.put("color", 16724787); // Code couleur Phantom Force Red

        // 3. Ajout des champs (Fields)
        JSONArray fields = new JSONArray();
        fields.put(new JSONObject().put("name", "🎮 Jeu").put("value", game).put("inline", true));
        fields.put(new JSONObject().put("name", "🎯 Score IA").put("value", score + "/100").put("inline", true));

        // Si aucun lien n'est fourni, on met un texte par défaut
        String link = (profileUrl != null && !profileUrl.isEmpty()) ? profileUrl : "https://phantom-esport.com";
        fields.put(new JSONObject().put("name", "🔗 Lien").put("value", "[Voir le profil complet](" + link + ")").put("inline", false));

        embed.put("fields", fields);

        // 4. On attache l'embed au payload principal
        payload.put("embeds", new JSONArray().put(embed));

        // 5. Envoi de la requête HTTP en mode Asynchrone
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WEBHOOK_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // sendAsync permet de ne pas bloquer l'application JavaFX pendant l'envoi
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 204) {
                            System.err.println("Erreur Discord: " + response.body());
                        }
                    });

        } catch (Exception e) {
            System.err.println("Impossible d'envoyer le Webhook Discord : " + e.getMessage());
        }
    }
}