package pl.jalokim.crudwizard.core.translations;

import java.util.Locale;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAppMessageSource implements AppMessageSource {

    @Getter
    private final MessageSource messageSource;

    @PostConstruct
    public void postInit() {
        AppMessageSourceHolder.setAppMessageSource(this);
    }

    @Override
    public String getMessage(String propertyKey) {
        return messageSource.getMessage(propertyKey, null, Locale.getDefault());
    }

}
