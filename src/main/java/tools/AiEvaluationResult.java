package tools; // <-- THIS WAS THE MISSING PIECE!

import java.util.HashMap;
import java.util.Map;

public class AiEvaluationResult {
    private int score;
    private String globalFeedback;
    private Map<String, String> suggestions = new HashMap<>();

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getGlobalFeedback() { return globalFeedback; }
    public void setGlobalFeedback(String globalFeedback) { this.globalFeedback = globalFeedback; }

    public Map<String, String> getSuggestions() { return suggestions; }
    public void addSuggestion(String key, String value) { this.suggestions.put(key, value); }
}