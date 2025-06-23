package com.netomi.ai.config;

import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class BotAuthenticationConfig {

    @Value("${MicrosoftAppId}")
    private String appId;

    @Value("${MicrosoftAppPassword}")
    private String appPassword;

    @Bean
    public CredentialProvider credentialProvider() {
        return new SimpleCredentialProvider(appId, appPassword);
    }

    @Bean
    public MicrosoftAppCredentials microsoftAppCredentials() {
        System.setProperty("AuthorityHostUrl", "https://login.microsoftonline.com/common");
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);

        return credentials;
    }

    // Use a simplified ChannelProvider that just specifies to use the default public channel
    @Bean
    public ChannelProvider channelProvider() {
        return new ChannelProvider() {
            @Override
            public CompletableFuture<String> getChannelService() {
                return CompletableFuture.completedFuture(null); // Return null to use the default public channel
            }

            @Override
            public boolean isPublicAzure() {
                return true;
            }

            @Override
            public boolean isGovernment() {
                return false;
            }
        };
    }
}
