package hello.spring.rsocket.server.config;

import hello.spring.rsocket.server.SchedulerPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessageHandlerCustomizer;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
public class RSocketMessageHandlerConfig {

    private static final String PATHPATTERN_ROUTEMATCHER_CLASS = "org.springframework.web.util.pattern.PathPatternRouteMatcher";

    @Bean
    public RSocketStrategies getRSocketStrategies(ObjectProvider<RSocketStrategiesCustomizer> customizers) {
        RSocketStrategies.Builder builder = RSocketStrategies.builder();
        if (ClassUtils.isPresent(PATHPATTERN_ROUTEMATCHER_CLASS, null)) {
            builder.routeMatcher(new PathPatternRouteMatcher());
        }
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        builder.metadataExtractorRegistry(registry -> registry.metadataToExtract(
                MimeTypeUtils.APPLICATION_JSON,
                new ParameterizedTypeReference<Map<String, String>>() {},
                (jsonMap, outputMap) -> {
                    outputMap.putAll(jsonMap);
                }
        ));
        return builder.build();
    }

    @Bean
    public RSocketMessageHandler getRSocketMessageHandler(RSocketStrategies rSocketStrategies,
            ObjectProvider<RSocketMessageHandlerCustomizer> customizers) {
        RSocketMessageHandler messageHandler = new RSocketMessageHandler() {
            @Override
            public Mono<Void> handleMessage(Message<?> message) throws MessagingException {
                Mono<Void> mono = super.handleMessage(message);
                String clientId = getClientId(message);
                if(clientId != null) {
                    mono = mono.subscribeOn(SchedulerPool.getInstance().getScheduler(clientId));
                }
                return mono;
            }

            private String getClientId(Message<?> message) {
                Object clientId = message.getHeaders().get(ClientIdMethodArgumentResolver.CLIENT_ID_HEADER);
                if(clientId instanceof String) {
                    return (String) clientId;
                } else if(clientId != null) {
                    return clientId.toString();
                }
                return null;
            }
        };
        messageHandler.setRSocketStrategies(rSocketStrategies);
        customizers.orderedStream().forEach(customizer -> customizer.customize(messageHandler));
        return messageHandler;
    }

}
