package com.microsoft.bot.integration;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.schema.ActivityTypes;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdapterWithErrorHandler extends BotFrameworkHttpAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterWithErrorHandler.class);
    private ConversationState conversationState;

    public AdapterWithErrorHandler(Configuration configuration) {
        super(configuration);

        // Log authentication settings for debugging
        LOGGER.info("Bot Authentication Settings:");
        LOGGER.info("MicrosoftAppId: {}", configuration.getProperty("MicrosoftAppId"));
        LOGGER.info("MicrosoftAppTenantId: {}", configuration.getProperty("MicrosoftAppTenantId"));
        LOGGER.info("MicrosoftAppType: {}", configuration.getProperty("MicrosoftAppType"));
        LOGGER.info("AuthorityHostUrl: {}", configuration.getProperty("AuthorityHostUrl"));
        LOGGER.info("ToChannelFromBotLoginUrl: {}", configuration.getProperty("ToChannelFromBotLoginUrl"));
        LOGGER.info("ToChannelFromBotOAuthScope: {}", configuration.getProperty("ToChannelFromBotOAuthScope"));

        setOnTurnError((context, exception) -> {
            // Log the exception
            LOGGER.error("onTurnError", exception);

            // Try to get more details about the authentication error
            if (exception.getCause() != null && exception.getCause().getMessage() != null) {
                LOGGER.error("Error cause: {}", exception.getCause().getMessage());
            }

            // Send a message to the user
            return context.sendActivity(
                MessageFactory.text("The bot encountered an error or bug.")
            ).thenCompose(result -> {
                if (exception.getMessage().contains("401") ||
                    (exception.getCause() != null && exception.getCause().getMessage().contains("401"))) {

                    // Log authentication details for debugging
                    try {
                        LOGGER.error("Authentication error details:");
                        LOGGER.error("AppId: {}", configuration.getProperty("MicrosoftAppId"));
                        LOGGER.error("TenantId: {}", configuration.getProperty("MicrosoftAppTenantId"));
                        LOGGER.error("AppType: {}", configuration.getProperty("MicrosoftAppType"));
                        LOGGER.error("AuthorityHostUrl: {}", configuration.getProperty("AuthorityHostUrl"));
                        LOGGER.error("ToChannelFromBotLoginUrl: {}", configuration.getProperty("ToChannelFromBotLoginUrl"));

                        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(
                            configuration.getProperty("MicrosoftAppId"),
                            configuration.getProperty("MicrosoftAppPassword")
                        );

                        LOGGER.info("Attempting to get token for debugging...");
                        credentials.getToken().thenAccept(token ->
                            LOGGER.info("Token obtained: {} (Length: {})",
                                token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "FAILED",
                                token != null ? token.length() : 0)
                        ).exceptionally(ex -> {
                            LOGGER.error("Token retrieval error: {}", ex.getMessage());
                            if (ex.getCause() != null) {
                                LOGGER.error("Token error cause: {}", ex.getCause().getMessage());
                            }
                            return null;
                        });
                    } catch (Exception e) {
                        LOGGER.error("Error during token debugging: {}", e.getMessage());
                    }

                    return context.sendActivity(
                        MessageFactory.text("Authentication error (401). Please check bot credentials.")
                    );
                }

                return CompletableFuture.completedFuture(null);
            }).thenCompose(result -> {
                if (context.getActivity().getType().equals(ActivityTypes.MESSAGE)) {
                    return context.sendActivity(
                        MessageFactory.text("To continue to run this bot, please fix the bot source code.")
                    );
                }
                return CompletableFuture.completedFuture(null);
            }).thenCompose(result -> {
                if (conversationState != null) {
                    // Delete the conversationState for this conversation to prevent the
                    // bot from getting stuck in an error-loop caused by being in a bad state.
                    return conversationState.delete(context);
                }
                return CompletableFuture.completedFuture(null);
            });
        });
    }

    public AdapterWithErrorHandler(Configuration configuration, ConversationState conversationState) {
        this(configuration);
        this.conversationState = conversationState;
    }
}
