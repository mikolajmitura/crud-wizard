package pl.jalokim.crudwizard.test.utils

import pl.jalokim.crudwizard.core.datetime.TimeProviderHolder
import pl.jalokim.crudwizard.test.utils.datetime.UnitTestTimeProvider
import spock.lang.Shared
import spock.lang.Specification

abstract class UnitTestSpec extends Specification implements UsesTimeProvider {

    @Shared
    UnitTestTimeProvider sharedTimeProvider = new UnitTestTimeProvider()

    def setup() {
        TimeProviderHolder.setTimeProvider(sharedTimeProvider)
    }

    def cleanup() {
        sharedTimeProvider.resetFixedDate()
    }

    UnitTestTimeProvider getTimeProvider() {
        sharedTimeProvider
    }
}
