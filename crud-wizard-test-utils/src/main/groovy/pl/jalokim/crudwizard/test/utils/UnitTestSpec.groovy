package pl.jalokim.crudwizard.test.utils

import org.springframework.context.ApplicationContext
import pl.jalokim.crudwizard.core.datetime.TimeProviderHolder
import pl.jalokim.crudwizard.core.translations.LocaleHolder
import pl.jalokim.crudwizard.core.utils.InstanceLoader
import pl.jalokim.crudwizard.test.utils.datetime.UnitTestTimeProvider
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl
import pl.jalokim.crudwizard.test.utils.validation.BeanCreationArgsResolver
import spock.lang.Shared
import spock.lang.Specification

abstract class UnitTestSpec extends Specification implements UsesTimeProvider {

    protected ApplicationContext applicationContext = Mock()
    protected InstanceLoader instanceLoader = new InstanceLoader(applicationContext)

    @Shared
    UnitTestTimeProvider sharedTimeProvider = new UnitTestTimeProvider()

    Map<Class, Object> applicationContextMapping = [:]

    def setupSpec() {
        LocaleHolder.setLocale(LocaleHolder.defaultLocale)
        AppMessageSourceTestImpl.initStaticAppMessageSource()
    }

    def setup() {
        instanceLoader.init()
        LocaleHolder.setLocale(LocaleHolder.defaultLocale)
        TimeProviderHolder.setTimeProvider(sharedTimeProvider)
        applicationContextMapping.put(ApplicationContext, applicationContext)
        applicationContextMapping.put(InstanceLoader, instanceLoader)

        applicationContext.getBean(_ as Class) >> {args ->
            Class type = args[0]
            def bean = applicationContextMapping.get(type)
            if (bean) {
                return bean
            }
            bean = BeanCreationArgsResolver.createInstance(type, applicationContextMapping.values() as List)
            applicationContextMapping.put(type, bean)
            bean
        }
    }

    def cleanup() {
        sharedTimeProvider.resetFixedDate()
    }

    UnitTestTimeProvider getTimeProvider() {
        sharedTimeProvider
    }
}
