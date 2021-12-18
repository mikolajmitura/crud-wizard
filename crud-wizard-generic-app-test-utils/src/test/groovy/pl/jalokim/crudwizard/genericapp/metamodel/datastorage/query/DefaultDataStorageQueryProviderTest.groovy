package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query

import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument.buildForPath
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createHttpQueryParamsForPerson
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import pl.jalokim.crudwizard.core.datastorage.query.AbstractExpression
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments
import pl.jalokim.crudwizard.core.datastorage.query.EmptyExpression
import pl.jalokim.crudwizard.core.datastorage.query.OrderPath
import pl.jalokim.crudwizard.core.datastorage.query.RealExpression
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import spock.lang.Specification
import spock.lang.Unroll

class DefaultDataStorageQueryProviderTest extends Specification {

    def dataStorageQueryProvider = new DefaultDataStorageQueryProvider()

    @Unroll
    def "return expected ds query"() {
        when:
        def result = dataStorageQueryProvider.createQuery(dsQueryArgument)

        then:
        verifyAll(result) {
            selectFrom == expectedQuery.selectFrom
            where == expectedQuery.where
            sortBy == expectedQuery.sortBy
        }

        where:
        dsQueryArgument             | expectedQuery

        queryArgs(ClassMetaModel.builder()
            .name("person-queryParams")
            .fields([
                createValidFieldMetaModel("name", String, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "LIKE"))])
            .build())               | createDataStorageQuery(new EmptyExpression())

        queryArgs()                 | createDataStorageQuery(new EmptyExpression()
            .and(RealExpression.isEqualsTo(buildForPath("document.type"), buildForPath("rightPath.otherValue"))))

        queryArgs(name: "someName") | createDataStorageQuery(new EmptyExpression()
            .and(RealExpression.like("name", "someName"))
            .and(RealExpression.isEqualsTo(buildForPath("document.type"), buildForPath("rightPath.otherValue"))))

        queryArgs(name: "someName2",
            age: 12,
            other_number: 15,
            some_texts: List.of("text1", "text2", "text3"),
            some_numbers: List.of(12, 11, 10),
            "pesel": "true",
            "nationality": "true",
            surname: "surname2")    | createDataStorageQuery(new EmptyExpression()
            .and(RealExpression.like("name", "someName2"))
            .and(RealExpression.likeIgnoreCase("surname", "surname2"))
            .and(RealExpression.greaterThan("age", 12))
            .and(RealExpression.lowerThan("otherNumber", 15))
            .and(RealExpression.in("someTexts", List.of("text1", "text2", "text3")))
            .and(RealExpression.in("someNumbers", List.of(12, 11, 10)))
            .and(RealExpression.isNull("pesel"))
            .and(RealExpression.isNotNull("nationality"))
            .and(RealExpression.isEqualsTo(buildForPath("document.type"), buildForPath("rightPath.otherValue"))))
    }

    private static DataStorageQueryArguments queryArgs(Map<String, Object> requestParams = [:]) {
        queryArgs(createHttpQueryParamsForPerson(), requestParams)
    }

    private static DataStorageQueryArguments queryArgs(ClassMetaModel httpClassMetaModel,
        Map<String, Object> requestParams = [:]) {

        DataStorageQueryArguments.builder()
            .requestParamsClassMetaModel(httpClassMetaModel)
            .queriedClassMetaModels([queriedClassMetaModel()])
            .requestParams(requestParams)
            .build()
    }

    private static DataStorageQuery createDataStorageQuery(AbstractExpression whereExpression, List<OrderPath> sortBy = []) {
        DataStorageQuery.builder()
            .selectFrom(queriedClassMetaModel())
            .sortBy(sortBy)
            .where(whereExpression)
            .build()
    }

    private static ClassMetaModel queriedClassMetaModel() {
        ClassMetaModel.builder()
            .name("person")
            .build()
    }
}
