package dev.vality.magista.config.initializer;

import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationPropertiesInitializer extends ConfigDataApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "token-gen.key=" + "jXnZr4u7x!A%D*G-KaPvSgVkYp3s5v8t/B?E(H+MbQeThWmZq4t7w9z$C&F)J@Nc",
                "cache.invoiceData.size=10000",
                "cache.paymentData.size=10000")
                .applyTo(configurableApplicationContext);
    }
}
