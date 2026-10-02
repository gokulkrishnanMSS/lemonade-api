package com.lemon.lemonade.config;

import com.lemon.lemonade.controllers.AuthController;
import com.lemon.lemonade.controllers.MarketController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigurationTest {

    private final OpenApiConfiguration config = new OpenApiConfiguration();

    @Test
    void customOpenApiDefinesBearerSecurityScheme() {
        OpenAPI openAPI = config.customOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");

        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().get(0)).containsKey("BearerAuth");
    }

    @Test
    void operationCustomizerAddsHeaderParameterToSecuredControllers() throws NoSuchMethodException {
        OperationCustomizer customizer = config.addHeaderOperationCustomizer();
        Operation operation = new Operation();

        Method method = MarketController.class.getMethod("listCardsInSale", org.springframework.data.domain.Pageable.class);
        HandlerMethod handlerMethod = new HandlerMethod(new Object(), method) {
            @Override
            public Class<?> getBeanType() {
                return MarketController.class;
            }
        };

        customizer.customize(operation, handlerMethod);

        assertThat(operation.getParameters()).isNotNull();
        assertThat(operation.getParameters()).anyMatch(param ->
                "header".equalsIgnoreCase(param.getIn()) && "Authorization".equalsIgnoreCase(param.getName()));
    }

    @Test
    void operationCustomizerSkipsAuthEndpoints() throws NoSuchMethodException {
        OperationCustomizer customizer = config.addHeaderOperationCustomizer();
        Operation operation = new Operation();

        Method method = AuthController.class.getMethod("login", com.lemon.lemonade.dto.LoginRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new Object(), method) {
            @Override
            public Class<?> getBeanType() {
                return AuthController.class;
            }
        };

        customizer.customize(operation, handlerMethod);

        assertThat(operation.getParameters()).isNullOrEmpty();
    }
}
