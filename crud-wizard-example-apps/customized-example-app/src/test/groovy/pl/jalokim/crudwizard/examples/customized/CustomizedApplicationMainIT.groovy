package pl.jalokim.crudwizard.examples.customized

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.CustomizedApplicationTestConfig
import pl.jalokim.crudwizard.examples.customized.repo.UserEntity
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener
import pl.jalokim.crudwizard.test.utils.random.DataFakerHelper

@ActiveProfiles("integration")
@SpringBootTest(classes = [CustomizedApplicationTestConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CustomizedApplicationMainIT extends BaseIntegrationSpecification {

    public static final String CUSTOM_USERS_URL = "/custom/users"

    // TODO test in future that was added some endpoint via maitenance and can be invoked

    def "should save few users and get all of them"() {
        given:
        List<UserEntity> users = [createUser(), createUser(), createUser()]
        users.each {
            def userId = operationsOnRestController.postAndReturnLong(CUSTOM_USERS_URL, it)
            it.setId(userId)
        }

        when:
        def foundUsers = operationsOnRestController.getAndReturnArrayJson(CUSTOM_USERS_URL)

        then:
        foundUsers.eachWithIndex { Object foundUser, int i ->
            UserEntity createdUser = users[i]
            verifyAll(foundUser) {
                id == createdUser.id
                name == createdUser.name
                surName == createdUser.surName
            }
        }
    }

    private static UserEntity createUser() {
        UserEntity.builder()
            .name(DataFakerHelper.randomText())
            .surName(DataFakerHelper.randomText())
            .build()
    }
}
