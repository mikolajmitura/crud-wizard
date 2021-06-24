package pl.jalokim.crudwizard.core.exception;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.existsAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translateWhenIsPlaceholder;

import lombok.Getter;
import pl.jalokim.crudwizard.core.translations.AppMessageSource;
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@Getter
public abstract class ApplicationException extends RuntimeException {

    private final String originalTextOrPlaceholder;
    private MessagePlaceholder messagePlaceHolder;

    /**
     * Will try translate messages which look like '{some.property.key}'
     * But to translate those necessary is that in static holder {@link AppMessageSourceHolder} must be added {@link AppMessageSource}
     *
     * @param messageOrPlaceholder - message to translate or just simply text message
     */
    public ApplicationException(String messageOrPlaceholder) {
        super(translateWhenCan(messageOrPlaceholder));
        this.originalTextOrPlaceholder = messageOrPlaceholder;
    }

    /**
     * Will try translate messages which look like '{some.property.key}'
     * But to translate those necessary is that in static holder {@link AppMessageSourceHolder} must be added {@link AppMessageSource}
     *
     * @param messageOrPlaceholder - message to translate or just simply text message
     */
    public ApplicationException(String messageOrPlaceholder, Throwable ex) {
        super(translateWhenCan(messageOrPlaceholder), ex);
        this.originalTextOrPlaceholder = messageOrPlaceholder;
    }

    /**
     * to use that constructor necessary is that in static holder {@link AppMessageSourceHolder} must be added {@link AppMessageSource}
     */
    public ApplicationException(MessagePlaceholder messagePlaceHolder) {
        this(messagePlaceHolder.translateMessage());
        this.messagePlaceHolder = messagePlaceHolder;
    }

    /**
     * to use that constructor necessary is that in static holder {@link AppMessageSourceHolder} must be added {@link AppMessageSource}
     */
    public ApplicationException(MessagePlaceholder messagePlaceHolder, Throwable ex) {
        this(messagePlaceHolder.translateMessage(), ex);
        this.messagePlaceHolder = messagePlaceHolder;
    }

    public boolean hasMessagePlaceholder() {
        return messagePlaceHolder != null;
    }

    private static String translateWhenCan(String messageOrPlaceholder) {
        if (existsAppMessageSource()) {
            return translateWhenIsPlaceholder(messageOrPlaceholder).toString();
        }
        return messageOrPlaceholder;
    }
}
