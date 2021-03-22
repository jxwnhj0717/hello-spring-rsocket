package hello.spring.rsocket.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String origin;
    private String value;
    private long created = Instant.now().getEpochSecond();

    public Message(String origin, String value) {
        this.origin = origin;
        this.value = value;
    }

}
