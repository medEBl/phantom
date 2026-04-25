package services.ai;

import entities.questionnaire.Questionnaire;
import entities.reponse.Reponse;
import org.json.JSONObject;
import tools.AiEvaluationResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiService {

    // 1. Utilisez la clé que vous avez générée (pensez à la supprimer/régénérer si elle est publique !)
    private static final String API_KEY = "AIzaSyCDtEqMq1PmgUuI";

    // 2. CORRECTION : gemini-1.5-flash au lieu de 2.5
    // Remplacez l'ancienne URL par celle-ci (v1 stable)
    // L'URL exacte tirée de votre code PHP fonctionnel
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY;

    private final HttpClient client;

    public GeminiService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public AiEvaluationResult evaluate(Questionnaire questions, Reponse answers) {
        AiEvaluationResult result = new AiEvaluationResult();
        String game = (questions.getGame() != null) ? questions.getGame() : "Esports";

        // Construction du Prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Act as a professional Esports Coach for '").append(game).append("'.\n");
        promptBuilder.append("Evaluate these interview answers (answer in only frensh):\n");
        promptBuilder.append("Q1: ").append(questions.getQues1()).append(" -> A1: ").append(answers.getRep1()).append("\n");
        promptBuilder.append("Q2: ").append(questions.getQues2()).append(" -> A2: ").append(answers.getRep2()).append("\n");

        if (questions.getQues3() != null && !questions.getQues3().isEmpty()) {
            promptBuilder.append("Q3: ").append(questions.getQues3()).append(" -> A3: ").append(answers.getRep3()).append("\n");
        }

        promptBuilder.append("\nReturn ONLY a raw JSON object. No intro, no outro, no markdown.\n");
        promptBuilder.append("Format: { \"score\": 85, \"global_feedback\": \"...\", \"suggestions\": { \"rep1\": \"...\", \"rep2\": \"...\" } }");

        try {
            // Création du JSON pour Google
            JSONObject payload = new JSONObject();
            JSONObject textPart = new JSONObject().put("text", promptBuilder.toString());
            JSONObject content = new JSONObject().put("parts", new JSONObject[]{textPart});
            payload.put("contents", new JSONObject[]{content});

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                result.setScore(0);
                result.setGlobalFeedback("⚠️ Erreur API (" + response.statusCode() + "). Réessayez dans un instant.");
                return result;
            }

            // Extraction du texte de la réponse
            JSONObject jsonResponse = new JSONObject(response.body());
            String rawText = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Nettoyage Markdown
            rawText = rawText.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();

            // Mapping vers l'objet résultat
            JSONObject data = new JSONObject(rawText);
            result.setScore(data.optInt("score", 0));
            result.setGlobalFeedback(data.optString("global_feedback", "Analyse indisponible."));

            if (data.has("suggestions")) {
                JSONObject sug = data.getJSONObject("suggestions");
                for (String key : sug.keySet()) {
                    result.addSuggestion(key, sug.getString(key));
                }
            }

        } catch (Exception e) {
            result.setScore(0);
            result.setGlobalFeedback("⚠️ Erreur : " + e.getMessage());
        }

        return result;
    }
}