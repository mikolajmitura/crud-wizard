package pl.jalokim.crudwizard.genericapp.metamodel.translation

class TranslationDtoSamples {

    public static final TRANSLATIONS_SAMPLE = [en_US: "english-american version"]

    static TranslationDto sampleTranslationDtoWithKey(Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        TranslationDto.builder()
            .translationKey("some.key")
            .translationByCountryCode(translations)
            .build()
    }

    static TranslationDto sampleTranslationDto(Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        TranslationDto.builder()
            .translationByCountryCode(translations)
            .build()
    }
}
