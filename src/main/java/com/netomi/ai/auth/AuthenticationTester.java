package com.netomi.ai.auth;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;


// Comment out the @Component annotation to prevent this from running at startup
// @Component
public class AuthenticationTester implements CommandLineRunner {

    @Value("${MicrosoftAppId}")
    private String appId;

    @Value("${MicrosoftAppPassword}")
    private String appPassword;

    @Value("${MicrosoftAppTenantId}")
    private String tenantId;

    @Value("${AuthorityHostUrl:https://login.microsoftonline.com/common}")
    private String authorityHostUrl;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Authentication Tester ===");
        System.out.println("AppId: " + appId);
        System.out.println("TenantId: " + tenantId);
        System.out.println("Authority Host URL: " + authorityHostUrl);

        // Test 1: Direct Microsoft App Credentials
        System.out.println("\nTest 1: Using MicrosoftAppCredentials directly");
        try {
            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);
            System.out.println("Requesting token...");
            String token = credentials.getToken().join();
            System.out.println("SUCCESS! Token received: " + (token != null ? token.substring(0, 15) + "..." : "null"));
        } catch (Exception e) {
            System.out.println("FAILED! Error: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 2: Manual OAuth token request
        System.out.println("\nTest 2: Manual OAuth token request");
        try {
            // Construct the token URL for multi-tenant
            String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
            System.out.println("Using token URL: " + tokenUrl);

            // Create connection
            URL url = new URL(tokenUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Prepare request body
            String requestBody = "grant_type=client_credentials" +
                    "&client_id=" + appId +
                    "&client_secret=" + appPassword +
                    "&scope=https://api.botframework.com/.default";

            System.out.println("Sending request with client_id: " + appId);

            // Send request
            connection.getOutputStream().write(requestBody.getBytes("UTF-8"));

            // Get response
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // Read response
            java.io.BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                System.out.println("SUCCESS! Response: ");
            } else {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getErrorStream()));
                System.out.println("FAILED! Error response: ");
            }

            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println("FAILED! Exception: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: Try with common endpoint
        System.out.println("\nTest 3: Using common endpoint");
        try {
            // Set system property to use common endpoint
            System.setProperty("MicrosoftAppId", appId);
            System.setProperty("MicrosoftAppPassword", appPassword);
            System.setProperty("AuthorityHostUrl", "https://login.microsoftonline.com/common");

            MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);
            System.out.println("Requesting token...");
            String token = credentials.getToken().join();
            System.out.println("SUCCESS! Token received: " + (token != null ? token.substring(0, 15) + "..." : "null"));
        } catch (Exception e) {
            System.out.println("FAILED! Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Authentication Tests Complete ===");
        System.out.println("The application will continue running to serve bot requests.");
    }
}
