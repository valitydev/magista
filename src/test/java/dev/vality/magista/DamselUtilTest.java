package dev.vality.magista;

import dev.vality.damsel.domain.InvoiceCart;
import dev.vality.geck.serializer.kit.mock.MockTBaseProcessor;
import dev.vality.geck.serializer.kit.tbase.TBaseHandler;
import dev.vality.magista.util.DamselUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DamselUtilTest {

    @Test
    public void jsonTest() throws IOException {
        InvoiceCart cart = new InvoiceCart();
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor();
        mockTBaseProcessor.addFieldHandler((t) -> {
            t.beginMap(0);
            t.endMap();
        }, "metadata");
        cart = mockTBaseProcessor.process(cart, new TBaseHandler<>(InvoiceCart.class));

        String jsonCart = DamselUtil.toJsonString(cart);
        assertEquals(cart, DamselUtil.fromJson(jsonCart, InvoiceCart.class));

    }

}
