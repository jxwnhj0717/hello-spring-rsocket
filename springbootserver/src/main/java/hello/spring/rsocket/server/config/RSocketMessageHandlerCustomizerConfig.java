package hello.spring.rsocket.server.config;

import hello.spring.rsocket.server.config.ClientIdMethodArgumentResolver;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessageHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSocketMessageHandlerCustomizerConfig {

    @Bean
    public RSocketMessageHandlerCustomizer getRSocketMessageHandlerCustomizer() {
        return messageHandler -> {
            messageHandler.getArgumentResolverConfigurer().addCustomResolver(new ClientIdMethodArgumentResolver());
        };
    }
}
