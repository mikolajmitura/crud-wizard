package pl.jalokim.crudwizard.test.utils

import pl.jalokim.crudwizard.core.datetime.TimeProvider

interface UsesTimeProvider {

    TimeProvider getTimeProvider()
}
