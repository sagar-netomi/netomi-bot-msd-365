package com.netomi.ai.response;


import com.netomi.ai.netomi.api.NetomiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TextResponse {

    @Autowired
    private NetomiClient netomiClient;

    public String prepareTextResponse(String userQuery,String conversationId,String messageId){
        Map<String, Object> socialResponse = netomiClient.sendRequestToNetomi(userQuery, conversationId, messageId);
        if (socialResponse == null) { // Social error Handling
            return "There is some error with Netomi Integration";
        }

        String requestId = ((List<String>)socialResponse.get("payload")).get(0);

        List<Map<String,Object>> attachmentList=netomiClient.fetchNetomiResponse(requestId);

        if (attachmentList == null || attachmentList.isEmpty()) { // Message History API Error Handling
            return "Error Fetching Messages from Netomi";
        }

        String finalText="";

        for (Map<String,Object> attachment : attachmentList) {
            if (attachment.get("type").equals("ai.msg.domain.responses.core.Text")) {
                finalText= (String) attachment.get("text");
                break;
            }
        }

        return finalText;

    }
}
