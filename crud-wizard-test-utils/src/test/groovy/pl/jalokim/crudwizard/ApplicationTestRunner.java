package pl.jalokim.crudwizard;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.translations.LocaleService;
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider;

@Component
@RequiredArgsConstructor
public class ApplicationTestRunner implements ApplicationRunner {

    private final List<MessageSourceProvider> providers;
    private final LocaleService localeService;

    @Override
    public void run(ApplicationArguments args) {
        for (MessageSourceProvider provider : providers) {
            provider.refresh(localeService.getAllSupportedLocales());
        }
    }
}
