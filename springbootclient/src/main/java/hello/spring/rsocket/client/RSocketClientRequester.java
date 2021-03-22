package hello.spring.rsocket.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RSocketClientRequester implements RSocketRequester {

    private String clientId;

    private RSocketRequester actual;

    public RSocketClientRequester(String clientId, RSocketRequester actual) {
        this.clientId = clientId;
        this.actual = actual;
    }

    @Override
    public RSocketClient rsocketClient() {
        return actual.rsocketClient();
    }

    @Override
    public RSocket rsocket() {
        return actual.rsocket();
    }

    @Override
    public MimeType dataMimeType() {
        return actual.dataMimeType();
    }

    @Override
    public MimeType metadataMimeType() {
        return actual.metadataMimeType();
    }

    @Override
    public RequestSpec route(String route, Object... routeVars) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("clientId", clientId);
        RequestSpec spec = actual.route(route, routeVars).metadata(headers, MediaType.APPLICATION_JSON);
        return new DefaultRequestSpec(spec);
    }

    @Override
    public RequestSpec metadata(Object metadata, MimeType mimeType) {
        return new DefaultRequestSpec(actual.metadata(metadata, mimeType));
    }

    private class DefaultRequestSpec implements RequestSpec {

        private RequestSpec actual;

        public DefaultRequestSpec(RequestSpec actual) {
            this.actual = actual;
        }

        @Override
        public RequestSpec metadata(Consumer<MetadataSpec<?>> configurer) {
            actual.metadata(configurer);
            return this;
        }

        @Override
        public Mono<Void> sendMetadata() {
            return actual.sendMetadata();
        }

        @Override
        public RetrieveSpec data(Object data) {
            actual.data(data);
            return this;
        }

        @Override
        public RetrieveSpec data(Object producer, Class<?> elementClass) {
            actual.data(producer, elementClass);
            return this;
        }

        @Override
        public RetrieveSpec data(Object producer, ParameterizedTypeReference<?> elementTypeRef) {
            actual.data(producer, elementTypeRef);
            return this;
        }

        @Override
        public RequestSpec metadata(Object metadata, MimeType mimeType) {
            actual.metadata(metadata, mimeType);
            return this;
        }

        @Override
        public Mono<Void> send() {
            return actual.send().publishOn(Schedulers.single());
        }

        @Override
        public <T> Mono<T> retrieveMono(Class<T> dataType) {
            return actual.retrieveMono(dataType).publishOn(Schedulers.single());
        }

        @Override
        public <T> Mono<T> retrieveMono(ParameterizedTypeReference<T> dataTypeRef) {
            return actual.retrieveMono(dataTypeRef).publishOn(Schedulers.single());
        }

        @Override
        public <T> Flux<T> retrieveFlux(Class<T> dataType) {
            return actual.retrieveFlux(dataType).publishOn(Schedulers.single());
        }

        @Override
        public <T> Flux<T> retrieveFlux(ParameterizedTypeReference<T> dataTypeRef) {
            return actual.retrieveFlux(dataTypeRef).publishOn(Schedulers.single());
        }
    }
}
