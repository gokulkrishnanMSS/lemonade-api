package com.lemon.lemonade.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
public class OpenApiConfiguration {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lemonade API")
                        .version("1.0")
                        .description("Lemonade API documentation with Authorization header support"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token. You can provide it with or without the 'Bearer ' prefix.")));
    }

    @Bean
    public OperationCustomizer addHeaderOperationCustomizer() {
        return (operation, handlerMethod) -> {
            RequestMapping requestMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
            boolean isAuthEndpoint = requestMapping != null && Arrays.asList(requestMapping.value()).contains("/auth");

            if (!isAuthEndpoint) {
                if (operation.getParameters() == null) {
                    operation.setParameters(new ArrayList<>());
                }

                boolean hasAuthHeader = operation.getParameters().stream()
                        .anyMatch(p -> "header".equalsIgnoreCase(p.getIn()) && "Authorization".equalsIgnoreCase(p.getName()));

                if (!hasAuthHeader) {
                    operation.addParametersItem(new HeaderParameter()
                            .name("Authorization")
                            .description("JWT access token (e.g. 'Bearer <token>' or '<token>')")
                            .required(false)
                            .schema(new StringSchema()));
                }
            }
            return operation;
        };
    }
}
