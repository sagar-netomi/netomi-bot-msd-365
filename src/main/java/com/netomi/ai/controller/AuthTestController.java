package com.netomi.ai.controller;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @Value("${MicrosoftAppId}")
    private String appId;

    @Value("${MicrosoftAppPassword}")
    private String appPassword;

    @Value("${MicrosoftAppTenantId}")
    private String tenantId;

    @Value("${MicrosoftAppType}")
    private String appType;

    @Value("${AuthorityHostUrl}")
    private String authorityHostUrl;

    @Value("${ToChannelFromBotLoginUrl}")
    private String toChannelFromBotLoginUrl;

    @Value("${ToChannelFromBotOAuthScope}")
    private String toChannelFromBotOAuthScope;

    @PostConstruct
    public void initSystemProperties() {
        // Set system properties from Spring @Value properties
        System.setProperty("MicrosoftAppId", appId);
        System.setProperty("MicrosoftAppPassword", appPassword);
        System.setProperty("MicrosoftAppTenantId", tenantId);
        System.setProperty("MicrosoftAppType", appType);
        System.setProperty("AuthorityHostUrl", authorityHostUrl);
        System.setProperty("ToChannelFromBotLoginUrl", toChannelFromBotLoginUrl);
        System.setProperty("ToChannelFromBotOAuthScope", toChannelFromBotOAuthScope);
    }

    @GetMapping("/test-auth-diag")
    public Map<String, Object> testAuth() {
        Map<String, Object> result = new HashMap<>();
        result.put("appId", appId);
        result.put("appIdLength", appId != null ? appId.length() : 0);
        result.put("passwordPresent", appPassword != null && !appPassword.isEmpty());

        // Add environment information
        result.put("environment", new HashMap<String, String>() {{
            put("MicrosoftAppId", System.getenv("MicrosoftAppId"));
            put("MicrosoftAppPassword", System.getenv("MicrosoftAppPassword") != null ? "[PRESENT]" : "[MISSING]");
            put("MicrosoftAppTenantId", System.getenv("MicrosoftAppTenantId"));
            put("MicrosoftAppType", System.getenv("MicrosoftAppType"));
            put("AuthorityHostUrl", System.getenv("AuthorityHostUrl"));
            put("ToChannelFromBotLoginUrl", System.getenv("ToChannelFromBotLoginUrl"));
            put("ToChannelFromBotOAuthScope", System.getenv("ToChannelFromBotOAuthScope"));
            put("ChannelService", System.getenv("ChannelService"));
        }});

        // Add system properties
        result.put("systemProperties", new HashMap<String, String>() {{
            put("MicrosoftAppId", System.getProperty("MicrosoftAppId"));
            put("MicrosoftAppPassword", System.getProperty("MicrosoftAppPassword") != null ? "[PRESENT]" : "[MISSING]");
            put("MicrosoftAppTenantId", System.getProperty("MicrosoftAppTenantId"));
            put("MicrosoftAppType", System.getProperty("MicrosoftAppType"));
            put("AuthorityHostUrl", System.getProperty("AuthorityHostUrl"));
            put("ToChannelFromBotLoginUrl", System.getProperty("ToChannelFromBotLoginUrl"));
            put("ToChannelFromBotOAuthScope", System.getProperty("ToChannelFromBotOAuthScope"));
            put("ChannelService", System.getProperty("ChannelService"));
        }});

        try {
            // Create credentials with explicit settings
            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);

            // Get token and analyze it
            String token = credentials.getToken().get();
            result.put("tokenSuccess", token != null && !token.isEmpty());
            result.put("tokenLength", token != null ? token.length() : 0);
            result.put("tokenPrefix", token != null && token.length() > 10 ? token.substring(0, 10) + "..." : "N/A");

            // Decode and analyze token
            if (token != null) {
                String[] tokenParts = token.split("\\.");
                if (tokenParts.length >= 2) {
                    // Decode payload
                    String payload;
                    try {
                        payload = new String(java.util.Base64.getDecoder().decode(tokenParts[1]), "UTF-8");
                    } catch (java.io.UnsupportedEncodingException e) {
                        result.put("tokenDecodeError", e.getMessage());
                        payload = "";
                    }
                    result.put("tokenPayload", payload);

                    // Try to extract key information
                    try {
                        // This is a simple approach - in production you'd use a JSON parser
                        result.put("tokenIssuer", extractJsonValue(payload, "iss"));
                        result.put("tokenAudience", extractJsonValue(payload, "aud"));
                        result.put("tokenExpiration", extractJsonValue(payload, "exp"));
                        result.put("tokenIssuedAt", extractJsonValue(payload, "iat"));
                    } catch (Exception e) {
                        result.put("tokenParseError", e.getMessage());
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
            if (e.getCause() != null) {
                result.put("errorCause", e.getCause().getMessage());
                result.put("errorCauseType", e.getCause().getClass().getName());
            }
        }


        return result;
    }

    // Simple helper to extract values from JSON string
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^\",}]*)\"?";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
