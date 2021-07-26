package pl.jalokim.crudwizard.test.utils

import static pl.jalokim.crudwizard.test.utils.datetime.UnitTestTimeProvider.TEST_CLOCK

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.validation.ClockProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import pl.jalokim.crudwizard.core.datetime.TimeProvider
import pl.jalokim.crudwizard.core.datetime.TimeProviderHolder
import pl.jalokim.crudwizard.core.translations.AppMessageSource
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder
import spock.lang.Shared
import spock.lang.Specification

@AutoConfigureMockMvc
class BaseIntegrationSpecification extends Specification implements UsesTimeProvider {

    @Autowired
    private TransactionTemplate transactionTemplate

    @Autowired
    protected TimeProvider timeProvider

    @Autowired
    protected AppMessageSource appMessageSource

    @Autowired
    @Qualifier("rawOperationsOnRestController")
    protected RawOperationsOnEndpoints operationsOnRestController

    @Shared Map<Integer, Boolean> executedClosuresByLineNumber = [:]

    @Override
    TimeProvider getTimeProvider() {
        return timeProvider
    }

    def setup() {
        AppMessageSourceHolder.setAppMessageSource(appMessageSource)
        TimeProviderHolder.setTimeProvider(timeProvider)
    }

    def cleanup() {
        resetFixedDate()
    }

    void inTransaction(Closure<Void> block) {
        transactionTemplate.execute(block)
    }

    def executeOnlyOnce(Closure<Object> resultActionsClosure) {
        def invokerClass = getClass()
        def foundWhereClosureInvoked = Thread.currentThread().getStackTrace()
        .find {it.getClassName().contains(invokerClass.canonicalName)}
        def lineNumber = foundWhereClosureInvoked.lineNumber
        executedClosuresByLineNumber.putIfAbsent(lineNumber, false)
        if (!executedClosuresByLineNumber.get(lineNumber)) {
            resultActionsClosure.run()
            executedClosuresByLineNumber.put(lineNumber, true)
        }
    }

    protected static void fixedDate(LocalDate date) {
        TEST_CLOCK.fixedInstant = date.atStartOfDay().atZone(TEST_CLOCK.zone).toInstant()
    }

    protected static void fixedOffsetDateTime(OffsetDateTime dateTime) {
        TEST_CLOCK.fixedInstant = dateTime.toInstant()
    }

    protected static void fixedInstant(Instant instant) {
        TEST_CLOCK.fixedInstant = instant
    }

    private static void resetFixedDate() {
        TEST_CLOCK.reset()
    }

    @Configuration
    static class TestConfig {

        @Bean
        @Primary
        Clock testClock() {
            return TEST_CLOCK
        }

        @Bean
        @Primary
        ClockProvider testClockProvider() {
            return {
                TEST_CLOCK
            }
        }

        @Bean
        LocalValidatorFactoryBean localValidatorFactoryBean() {
            return new LocalValidatorFactoryBean() {

                @Override
                ClockProvider getClockProvider() {
                    return testClockProvider()
                }

                @Override
                protected void postProcessConfiguration(javax.validation.Configuration<?> configuration) {
                    configuration.clockProvider(testClockProvider())
                }
            }
        }
    }
}
