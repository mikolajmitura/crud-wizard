package pl.jalokim.crudwizard.core.exception

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage

import pl.jalokim.crudwizard.core.exception.handler.DummyDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.random.DataFakerHelper

class EntityNotFoundExceptionTest extends UnitTestSpec {

    def "should return expected message for EntityNotFoundException with id"() {
        given:
        Long id = DataFakerHelper.randomLong()

        when:
        throwEntityNotFoundException(id)

        then:
        EntityNotFoundException ex = thrown()
        ex.message == getMessage(EntityNotFoundException.EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY, id)
    }

    def "should return expected message for EntityNotFoundException with normal text"() {
        given:
        String text = DataFakerHelper.randomText()

        when:
        throwEntityNotFoundException(text)

        then:
        EntityNotFoundException ex = thrown()
        ex.message == text
    }

    def "should return expected message for EntityNotFoundException with id and normal text"() {
        given:
        Long id = DataFakerHelper.randomLong()
        String text = DataFakerHelper.randomText()

        when:
        throwEntityNotFoundException(id, text)

        then:
        EntityNotFoundException ex = thrown()
        ex.message == getMessage(EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY, id, text)
    }

    def "should return expected message for EntityNotFoundException with id and placeholder"() {
        given:
        Long id = DataFakerHelper.randomLong()

        when:
        throwEntityNotFoundException(id, "{entityNotFoundExceptionTest.message}")

        then:
        EntityNotFoundException ex = thrown()
        ex.message == getMessage(EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY, id, "real value from")
    }

    def "should return expected message for EntityNotFoundException with id and class type"() {
        given:
        Long id = DataFakerHelper.randomLong()

        when:
        throwEntityNotFoundException(id, DummyDto)

        then:
        EntityNotFoundException ex = thrown()
        ex.message == getMessage(EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY, id, "Some bean name")
    }

    private static void throwEntityNotFoundException(Long id) {
        throw new EntityNotFoundException(id)
    }

    private static void throwEntityNotFoundException(Long id, String translatedEntityNameOrPropertyKey) {
        throw new EntityNotFoundException(id, translatedEntityNameOrPropertyKey)
    }

    private static void throwEntityNotFoundException(Long id, Class<?> someEntity) {
        throw new EntityNotFoundException(id, someEntity)
    }

    private static void throwEntityNotFoundException(String message) {
        throw new EntityNotFoundException(message)
    }
}
