package dev.vality.magista.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "token-gen")
@Validated
public class TokenGenProperties {

    @NotEmpty
    private String key;

}
