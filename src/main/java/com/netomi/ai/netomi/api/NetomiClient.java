package com.netomi.ai.netomi.api;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NetomiClient {

    private final RestTemplate restTemplate;

    public NetomiClient() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> sendRequestToNetomi(String userMessage, String conversationId, String messageId) {
        String url = "https://api.netomi.com/v1/webhook";

        // Request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("conversationId", conversationId);

        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("text", userMessage);
        messagePayload.put("label", userMessage);
        messagePayload.put("messageId", messageId);
        messagePayload.put("timestamp", System.currentTimeMillis());
        requestBody.put("messagePayload", messagePayload);

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", "f3b83e65-e629-4b64-85a3-345b421e8e29");
        userDetails.put("emailId", "");
        requestBody.put("userDetails", userDetails);

        requestBody.put("additionalAttributes", null);
        requestBody.put("attachmentList", null);
        requestBody.put("ownerType", "BOT");

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-channel", "CHAT");
        headers.set("x-integration", "API");
        headers.set("x-bot-ref-id", "0cfdf5c7-fd9b-42c4-ace4-88629911a195");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Error while calling Netomi Social API");
            }
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> fetchNetomiResponse(String requestId) {
        String url = "https://api.netomi.com/v2/ceaas/messages?requestId=" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-channel", "CHAT");
        headers.set("x-integration", "API");
        headers.set("x-bot-ref-id", "0cfdf5c7-fd9b-42c4-ace4-88629911a195");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for (int i = 0; i < 80; i++) {
            try {
                Thread.sleep(100); // waitforme

                ResponseEntity<List> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        List.class
                );

                if (response.getStatusCode() != HttpStatus.OK) {
                    continue;
                }

                List<?> responseList = response.getBody();

                if (responseList != null && !responseList.isEmpty()) {
                    Object firstItemObj = responseList.get(0);
                    if (firstItemObj instanceof Map) {
                        Map<?, ?> firstItem = (Map<?, ?>) firstItemObj;
                        Object responsesObj = firstItem.get("responses");

                        if (responsesObj instanceof List) {
                            List<?> responsesList = (List<?>) responsesObj;

                            if (!responsesList.isEmpty()) {
                                Object responseEntry = responsesList.get(0);

                                if (responseEntry instanceof Map) {
                                    Map<?, ?> responseMap = (Map<?, ?>) responseEntry;
                                    Object attachmentsObj = responseMap.get("attachments");

                                    if (attachmentsObj instanceof List) {
                                        return (List<Map<String, Object>>) attachmentsObj;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while sleeping: " + e.getMessage());
                return null;
            } catch (Exception e) {
                System.err.println("Error while calling Netomi fetch API: " + e.getMessage());
            }
        }

        System.err.println("Retry limit exceeded.");
        return null;
    }


}

