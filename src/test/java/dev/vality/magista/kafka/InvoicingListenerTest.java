package dev.vality.magista.kafka;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.magista.config.KafkaPostgresqlSpringBootITest;
import dev.vality.magista.converter.SourceEventParser;
import dev.vality.magista.service.HandlerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@KafkaPostgresqlSpringBootITest
public class InvoicingListenerTest {

    @Value("${kafka.topics.invoicing.id}")
    private String invoicingTopicName;

    @MockitoBean
    private HandlerManager handlerManager;

    @MockitoBean
    private SourceEventParser eventParser;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Captor
    private ArgumentCaptor<MachineEvent> arg;

    @BeforeEach
    void waitForKafkaListenersAssignment() {
        KafkaTestSupport.waitForAssignments(kafkaListenerEndpointRegistry, embeddedKafkaBroker);
    }

    @Test
    public void shouldInvoicingSinkEventListen() throws Exception {
        var message = new MachineEvent();
        message.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceId("source_id");
        message.setSourceNs("source_ns");
        var data = new dev.vality.machinegun.msgpack.Value();
        data.setBin(new byte[0]);
        message.setData(data);
        var sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);
        when(eventParser.parseEvent(any())).thenReturn(EventPayload.invoice_changes(List.of()));
        KafkaTestSupport.send(embeddedKafkaBroker, invoicingTopicName, sinkEvent);
        verify(eventParser, timeout(5000).times(1)).parseEvent(arg.capture());
        assertThat(arg.getValue())
                .isEqualTo(message);
    }
}
