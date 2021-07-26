package pl.jalokim.crudwizard.core.translations;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestAppMessageSourceHolder {

    public static void setAppMessageSource(AppMessageSource appMessageSource) {
        AppMessageSourceHolder.setAppMessageSource(appMessageSource);
    }
}
