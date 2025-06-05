// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.netomi.ai;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

//
// This is the starting point of the Sprint Boot Bot application.
//
@SpringBootApplication

// Use the default BotController to receive incoming Channel messages. A custom
// controller could be used by eliminating this import and creating a new
// org.springframework.web.bind.annotation.RestController.
// The default controller is created by the Spring Boot container using
// dependency injection. The default route is /api/messages.
@Import({BotController.class})

/**
 * This class extends the BotDependencyConfiguration which provides the default
 * implementations for a Bot application.  The Application class should
 * override methods in order to provide custom implementations.
 */
public class Application extends BotDependencyConfiguration {

    public static void main(String[] args) {
        // Print environment values before starting the application
        System.out.println("Environment Values:");
        System.out.println("MicrosoftAppId: " + System.getenv("MicrosoftAppId"));
        System.out.println("MicrosoftAppPassword: " + (System.getenv("MicrosoftAppPassword") != null ? "[PRESENT]" : "[MISSING]"));
        System.out.println("MicrosoftAppTenantId: " + System.getenv("MicrosoftAppTenantId"));
        System.out.println("MicrosoftAppType: " + System.getenv("MicrosoftAppType"));
        System.out.println("AuthorityHostUrl: " + System.getenv("AuthorityHostUrl"));

        SpringApplication.run(Application.class, args);

        System.out.println("Application started successfully!");
    }

    /**
     * Returns the Bot for this application.
     *
     * <p>
     *     The @Component annotation could be used on the Bot class instead of this method
     *     with the @Bean annotation.
     * </p>
     *
     * @return The Bot implementation for this application.
     */
    @Bean
    public Bot getBot() {
        return new EchoBot();
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new AdapterWithErrorHandler(configuration);
    }
}
