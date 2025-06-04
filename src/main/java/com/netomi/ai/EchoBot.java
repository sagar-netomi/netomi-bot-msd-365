// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.netomi.ai;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ChannelAccount;
import com.netomi.ai.response.TextResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */
public class EchoBot extends ActivityHandler {

    @Autowired
    public TextResponse textResponse;

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {

        String conversationId=turnContext.getActivity().getConversation().getId();
        String messageId=turnContext.getActivity().getId();

        String inputText=turnContext.getActivity().getText();
        String finalResponse=textResponse.prepareTextResponse(inputText,conversationId,messageId);

        return turnContext.sendActivity(
            MessageFactory.text("Echo: " + finalResponse)
        ).thenApply(sendResult -> null);
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            ).map(channel -> turnContext.sendActivity(MessageFactory.text("Hello and welcome!")))
            .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }
}
