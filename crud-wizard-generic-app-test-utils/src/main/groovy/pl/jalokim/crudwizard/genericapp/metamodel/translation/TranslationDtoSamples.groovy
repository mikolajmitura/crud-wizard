package pl.jalokim.crudwizard.genericapp.metamodel.translation

class TranslationDtoSamples {

    public static final def TRANSLATIONS_SAMPLE = [en_US: "english-american version"]

    static TranslationDto sampleTranslationDto(Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        TranslationDto.builder()
            .translationByCountryCode(translations)
            .build()
    }
}
