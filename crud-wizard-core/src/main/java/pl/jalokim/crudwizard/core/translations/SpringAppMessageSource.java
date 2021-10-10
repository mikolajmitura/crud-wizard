package pl.jalokim.crudwizard.core.translations;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;

import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.groups.ContextFromPlaceholderArgs;

@Component
@RequiredArgsConstructor
public class SpringAppMessageSource implements AppMessageSource {

    @Getter
    private final MessageSource messageSource;
    @Getter
    private final ValidatorFactory validatorFactory;

    @PostConstruct
    public void postInit() {
        AppMessageSourceHolder.setAppMessageSource(this);
    }

    @Override
    public String getMessage(String propertyKey) {
        return messageSource.getMessage(propertyKey, null, Locale.getDefault());
    }

    @Override
    public String getMessage(String propertyKey, Map<String, Object> placeholderArgs) {
        getMessage(propertyKey);
        MessageInterpolator messageInterpolator = getValidatorFactory().getMessageInterpolator();
        return messageInterpolator.interpolate(wrapAsPlaceholder(propertyKey), new ContextFromPlaceholderArgs(placeholderArgs),
            Locale.getDefault());
    }
}
