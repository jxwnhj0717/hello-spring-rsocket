package hello.spring.rsocket.server.config;

import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class RSocketServerResumptionConfig implements RSocketServerCustomizer {
    @Override
    public void customize(RSocketServer rSocketServer) {
        rSocketServer.resume(new Resume());
    }
}
