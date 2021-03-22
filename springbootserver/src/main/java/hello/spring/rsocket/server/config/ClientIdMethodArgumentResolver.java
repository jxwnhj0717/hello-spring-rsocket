package hello.spring.rsocket.server.config;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

public class ClientIdMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String CLIENT_ID_HEADER = "clientId";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> type = parameter.getParameterType();
        return String.class.equals(type);
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, Message<?> message) {
        Object value = message.getHeaders().get(CLIENT_ID_HEADER);

        if(value == null) {
            //throw new IllegalArgumentException(String.format("Missing header '%s'", CLIENT_ID_HEADER));
            return Mono.empty();
        }

        String clientId = null;
        if(value instanceof String) {
            clientId = (String) value;
        } else {
            clientId = value.toString();
        }

        return Mono.just(clientId);
    }
}
