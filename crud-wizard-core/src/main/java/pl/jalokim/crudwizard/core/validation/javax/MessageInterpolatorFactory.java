package pl.jalokim.crudwizard.core.validation.javax;

import javax.validation.MessageInterpolator;
import lombok.experimental.UtilityClass;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;
import pl.jalokim.crudwizard.core.translations.HibernateMessageSourceDelegator;

@UtilityClass
public class MessageInterpolatorFactory {

    public static MessageInterpolator createMessageInterpolator(MessageSource messageSource) {
        MessageSource delegatedMessageSource = new HibernateMessageSourceDelegator(messageSource);
        return new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(delegatedMessageSource), false);
    }
}
