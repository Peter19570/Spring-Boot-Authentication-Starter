package com.example.authstarter.features.shared.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    // Project info lives in application.properties (app.swagger.*), so edit it there per project.
    // Use @Operation to customize endpoints, @Hidden to hide them, and @Tag to rename API groups.
    @Bean
    public OpenAPI openAPI(
            @Value("${app.swagger.title:Authentication Starter}") String title,
            @Value("${app.swagger.description:}") String description,
            @Value("${app.swagger.version:1.0.0}") String version,
            @Value("${app.swagger.contact.name:}") String contactName,
            @Value("${app.swagger.contact.email:}") String contactEmail
    ) {
        Info info = new Info().title(title).description(description).version(version);

        if (StringUtils.hasText(contactName) || StringUtils.hasText(contactEmail)) {
            info.contact(new Contact()
                    .name(StringUtils.hasText(contactName) ? contactName : null)
                    .email(StringUtils.hasText(contactEmail) ? contactEmail : null));
        }

        return new OpenAPI()
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token here")));
    }
}