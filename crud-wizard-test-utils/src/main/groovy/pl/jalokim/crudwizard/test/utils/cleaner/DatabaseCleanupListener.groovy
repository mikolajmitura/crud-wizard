package pl.jalokim.crudwizard.test.utils.cleaner

import java.lang.reflect.Method
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener

class DatabaseCleanupListener extends AbstractTestExecutionListener {

    Map<Method, Long> invokedCleanupByMethod = [:]

    @Override
    void beforeTestMethod(TestContext testContext) throws Exception {
        if (cleanupOnceOnMethodLevel(testContext) && cleanupOnceOnClassLevel(testContext)) {
            throw new IllegalArgumentException("@CleanupOnce cannot be used on method level and class in the same time")
        }

        if (cleanupOnceOnMethodLevel(testContext)) {
            if (invokedCleanupByMethod.computeIfAbsent(testContext.testMethod, {key -> 0}) == 0) {
                cleanUpWithIncrementInvocation(testContext)
            }
        } else if (!cleanupOnceOnClassLevel(testContext)) {
            cleanUpWithIncrementInvocation(testContext)
        }
    }

    @Override
    void beforeTestClass(TestContext testContext) throws Exception {
        if (cleanupOnceOnClassLevel(testContext)) {
            cleanUp(testContext)
        }
    }

    @Override
    void afterTestClass(TestContext testContext) throws Exception {
        invokedCleanupByMethod.clear()
    }

    private static boolean cleanupOnceOnClassLevel(TestContext testContext) {
        testContext.testClass.isAnnotationPresent(CleanupOnce.class)
    }

    private static boolean cleanupOnceOnMethodLevel(TestContext testContext) {
        testContext.testMethod.isAnnotationPresent(CleanupOnce.class)
    }

    private static void cleanUp(TestContext testContext) {
        DatabaseCleanupService databaseCleanupService = testContext.getApplicationContext().getBean(DatabaseCleanupService)
        databaseCleanupService.cleanupDatabase()
    }

    private void cleanUpWithIncrementInvocation(TestContext testContext) {
        cleanUp(testContext)
        invokedCleanupByMethod.computeIfPresent(testContext.testMethod, {key, invoked -> ++invoked})
    }
}
