package services.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OAuth2CallbackServer {
    private HttpServer server;
    private CompletableFuture<GoogleIdToken> tokenFuture;
    private GoogleOAuth2Service oauth2Service;
    private String expectedState;

    public OAuth2CallbackServer(GoogleOAuth2Service oauth2Service, String expectedState) {
        this.oauth2Service = oauth2Service;
        this.expectedState = expectedState;
        this.tokenFuture = new CompletableFuture<>();
    }

    public void start() throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
            server.createContext("/callback", new CallbackHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("OAuth2 callback server started on http://127.0.0.1:8888");
        } catch (IOException e) {
            if (e.getMessage().contains("Address already in use")) {
                throw new IOException("Port 8888 is already in use. Please try a different port.");
            }
            throw e;
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("OAuth2 callback server stopped");
        }
    }

    public CompletableFuture<GoogleIdToken> getTokenFuture() {
        return tokenFuture;
    }

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String response;
            if (params.containsKey("error")) {
                response = createErrorResponse("Authentication cancelled or failed: " + params.get("error"));
                tokenFuture.completeExceptionally(new RuntimeException("OAuth2 error: " + params.get("error")));
            } else if (params.containsKey("code") && params.containsKey("state")) {
                String code = params.get("code");
                String state = params.get("state");
                
                if (!expectedState.equals(state)) {
                    response = createErrorResponse("Invalid state parameter. Possible CSRF attack.");
                    tokenFuture.completeExceptionally(new RuntimeException("Invalid state parameter"));
                } else {
                    try {
                        GoogleIdToken idToken = oauth2Service.exchangeCodeForIdToken(code);
                        response = createSuccessResponse();
                        tokenFuture.complete(idToken);
                    } catch (Exception e) {
                        response = createErrorResponse("Failed to exchange authorization code: " + e.getMessage());
                        tokenFuture.completeExceptionally(e);
                    }
                }
            } else {
                response = createErrorResponse("Missing required parameters (code and state)");
                tokenFuture.completeExceptionally(new RuntimeException("Missing OAuth2 parameters"));
            }
            
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
            // Stop the server after handling the callback
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return params;
        }

        private String createSuccessResponse() {
            return "<!DOCTYPE html><html><head><title>Authentication Successful</title>" +
                   "<style>body{font-family:Arial,sans-serif;text-align:center;margin-top:50px;}" +
                   ".success{color:#4CAF50;font-size:24px;margin-bottom:20px;}" +
                   ".info{color:#666;margin-bottom:30px;}" +
                   ".close-btn{background:#4CAF50;color:white;padding:10px 20px;border:none;border-radius:4px;cursor:pointer;}" +
                   "</style></head><body>" +
                   "<div class='success'>✅ Authentication Successful!</div>" +
                   "<div class='info'>You can now close this window and return to the application.</div>" +
                   "<button class='close-btn' onclick='window.close()'>Close Window</button>" +
                   "<script>setTimeout(window.close, 5000);</script>" +
                   "</body></html>";
        }

        private String createErrorResponse(String error) {
            return "<!DOCTYPE html><html><head><title>Authentication Failed</title>" +
                   "<style>body{font-family:Arial,sans-serif;text-align:center;margin-top:50px;}" +
                   ".error{color:#f44336;font-size:24px;margin-bottom:20px;}" +
                   ".info{color:#666;margin-bottom:30px;}" +
                   ".close-btn{background:#f44336;color:white;padding:10px 20px;border:none;border-radius:4px;cursor:pointer;}" +
                   "</style></head><body>" +
                   "<div class='error'>❌ Authentication Failed</div>" +
                   "<div class='info'>" + error + "</div>" +
                   "<button class='close-btn' onclick='window.close()'>Close Window</button>" +
                   "</body></html>";
        }
    }
}
