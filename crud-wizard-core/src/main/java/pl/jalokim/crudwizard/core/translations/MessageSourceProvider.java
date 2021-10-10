package pl.jalokim.crudwizard.core.translations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

@Getter
@RequiredArgsConstructor
public class MessageSourceProvider {

    private final MessageSource messageSource;

}
