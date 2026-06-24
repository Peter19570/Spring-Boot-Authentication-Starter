package com.example.authstarter.features.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Project Title",
                description = "Project Description",
                contact = @Contact(
                        name = "Peter Nwaogu",
                        email = "peternwaogu05@gmail.com"
                ),
                version = "2.0"
        ),

        security = {
                @SecurityRequirement(name = "bearerAuth")
        }

)
@SecurityScheme(
        name = "bearerAuth",
        description = "Enter JWT token here",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

//    Use @Operation to customize API endpoints for more clarity
//    Use @Hidden (Class / Endpoint level) to hide APIs from documentation
//    Use @Tag to rename API groups (swagger uses ur class / controller name by default)

}
