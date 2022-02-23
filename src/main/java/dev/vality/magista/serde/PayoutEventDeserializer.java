package dev.vality.magista.serde;

import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import dev.vality.payout.manager.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayoutEventDeserializer extends AbstractThriftDeserializer<Event> {

    @Override
    public Event deserialize(String topic, byte[] data) {
        return deserialize(data, new Event());
    }
}
