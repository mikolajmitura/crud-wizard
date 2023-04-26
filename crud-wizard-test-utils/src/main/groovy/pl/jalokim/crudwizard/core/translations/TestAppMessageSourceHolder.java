package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.mockito.Mockito;

@UtilityClass
public class TestAppMessageSourceHolder {

    public static final LocaleService LOCALE_SERVICE = Mockito.mock(LocaleService.class);

    static {
        Mockito.when(LOCALE_SERVICE.getAllSupportedLocales())
            .thenReturn(List.of(new Locale("en_US")));
    }

    public static void setAppMessageSource(AppMessageSource appMessageSource) {
        AppMessageSourceHolder.setAppMessageSource(appMessageSource);
    }

    public static LocaleService getLocaleService() {
        return LOCALE_SERVICE;
    }
}
