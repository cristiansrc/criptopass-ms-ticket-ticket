package com.criptopass.ms.ticket.ticket.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${app.gateway.base-path}") private val basePath: String,
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("CriptoPass MS Ticket Ticket API")
                    .description("Microservicio de gestión de boletas para el portal de venta de boletas en línea CriptoPass.")
                    .version("1.0.0")
                    .contact(Contact().name("CriptoPass Team"))
                    .license(License().name("Proprietary").identifier("UNLICENSED"))
            )
            .addServersItem(Server().url(basePath).description("API base path"))
            .addSecurityItem(
                SecurityRequirement().addList("BearerJWT")
            )
            .schemaRequirement(
                "BearerJWT",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Bearer token emitido por Keycloak (OAuth2/OIDC)")
            )
            .schemaRequirement(
                "WebhookSignature",
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-Webhook-Signature")
                    .description("Firma HMAC-SHA256 del payload para verificar la autenticidad del webhook")
            )
    }
}
