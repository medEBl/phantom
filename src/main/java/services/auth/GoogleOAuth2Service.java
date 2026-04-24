package services.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import entities.user.User;

import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

public class GoogleOAuth2Service {
    private static final String CLIENT_ID = "git";
    private static final String CLIENT_SECRET = "git";
    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String APPLICATION_NAME = "Phantom App";
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    
    private GoogleAuthorizationCodeFlow flow;
    private String stateToken;

    public GoogleOAuth2Service() {
        try {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(CLIENT_ID);
            web.setClientSecret(CLIENT_SECRET);
            web.setAuthUri("https://accounts.google.com/o/oauth2/auth");
            web.setTokenUri("https://oauth2.googleapis.com/token");
            web.setRedirectUris(Collections.singletonList(REDIRECT_URI));
            
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            clientSecrets.setWeb(web);
            
            this.flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                    Arrays.asList("email", "profile", "openid"))
                    .setAccessType("offline")
                    .build();
                    
            generateStateToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Google OAuth2 service", e);
        }
    }

    private void generateStateToken() {
        SecureRandom random = new SecureRandom();
        byte[] state = new byte[16];
        random.nextBytes(state);
        this.stateToken = Base64.getUrlEncoder().withoutPadding().encodeToString(state);
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .setState(stateToken)
                .setScopes(Arrays.asList("email", "profile", "openid"))
                .build();
    }

    public String getStateToken() {
        return stateToken;
    }

    public GoogleIdToken exchangeCodeForIdToken(String authorizationCode) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(REDIRECT_URI)
                .execute();
        
        return tokenResponse.parseIdToken();
    }

    public User createUserFromGoogleProfile(GoogleIdToken.Payload payload) {
        User user = new User();
        
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");
        
        user.setEmail(email);
        user.setFullName(name != null ? name : givenName + " " + familyName);
        user.setUsername(email.split("@")[0]); // Use email prefix as username
        user.setGoogleId(payload.getSubject());
        user.setRole("PLAYER"); // Default role for Google users
        user.setRoles("[\"ROLE_USER\",\"ROLE_PLAYER\"]"); // Set roles field in proper array format
        user.setActive(true);
        
        return user;
    }

    public void refreshAccessToken(String refreshToken) throws IOException {
        // For now, this is a placeholder method
        // In a real implementation, you would use GoogleCredential or similar to refresh tokens
        // This method is not currently used in the basic OAuth2 flow
        System.out.println("Token refresh requested for refresh token: " + refreshToken.substring(0, 10) + "...");
    }
}
