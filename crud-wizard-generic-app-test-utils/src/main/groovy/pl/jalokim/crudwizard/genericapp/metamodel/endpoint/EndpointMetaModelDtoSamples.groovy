package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.additionalPropertyRawJsonString
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createIgnoredForQueryFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createPageWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel1
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel2
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider

@Component
class EndpointMetaModelDtoSamples {

    static EndpointMetaModelDto createValidPostEndpointMetaModelDto() {
        EndpointMetaModelDto.builder()
            .baseUrl("users")
            .operationName("createUser")
            .apiTag(ApiTagDto.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.POST)
            .payloadMetamodel(createValidClassMetaModelDtoWithName())
            .responseMetaModel(createValidEndpointResponseMetaModelDto())
            .build()
    }

    static EndpointMetaModelDto createValidPostExtendedUserWithValidators() {
        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(extendedPersonClassMetaModel1())
            .payloadMetamodelAdditionalValidators(createAdditionalValidatorsForExtendedPerson())
            .build()
    }

    static EndpointMetaModelDto createValidPostExtendedUserWithValidators2() {
        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(extendedPersonClassMetaModel2())
            .payloadMetamodelAdditionalValidators(createAdditionalValidatorsForExtendedPerson())
            .build()
    }

    static EndpointMetaModelDto createValidPostWithSimplePerson() {
        createValidPostEndpointMetaModelDto()
            .toBuilder()
            .payloadMetamodel(simplePersonClassMetaModel())
            .build()
    }

    static EndpointMetaModelDto createValidPutEndpointMetaModelDto() {
        EndpointMetaModelDto.builder()
            .baseUrl("base-path/{basePath}/next-url/{nextId}")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.PUT)
            .operationName(randomText())
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("basePath", String),
                    createValidFieldMetaModelDto("nextId", Long)
                ])
                .build())
            .payloadMetamodel(createValidClassMetaModelDtoWithName())
            .responseMetaModel(createValidEndpointResponseMetaModelDto())
            .build()
    }

    static EndpointMetaModelDto createValidGetListOfPerson(ClassMetaModelDto elementTypeInCollection = simplePersonClassMetaModel()) {
        EndpointMetaModelDto.builder()
            .baseUrl("domain/person")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.GET)
            .operationName("getListOfPerson")
            .queryArguments(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("surname", String),
                    createValidFieldMetaModelDto("sort", String, [], [
                        additionalPropertyRawJsonString(DefaultDataStorageQueryProvider.IGNORE_IN_QUERY_PARAM, "true")
                    ]),
                    createValidFieldMetaModelDto("name", String, [],
                        [additionalPropertyRawJsonString(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "EQUALS")])
                ])
                .build()
            )
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([elementTypeInCollection])
                    .build())
                .build()
            )
            .build()
    }

    static EndpointMetaModelDto createValidGetPageOfPerson(ClassMetaModelDto pageContentType = simplePersonClassMetaModel()) {
        EndpointMetaModelDto.builder()
            .baseUrl("domain/person/by-page")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.GET)
            .operationName("getPageOfPerson")
            .queryArguments(ClassMetaModelDto.builder()
                .name("")
                .fields([
                    createIgnoredForQueryFieldMetaModelDto("size", Integer),
                    createIgnoredForQueryFieldMetaModelDto("page", Integer),
                    createIgnoredForQueryFieldMetaModelDto("sort", String),
                    createValidFieldMetaModelDto("surname", String),
                    createValidFieldMetaModelDto("name", String, [],
                        [additionalPropertyRawJsonString(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "EQUALS")])
                ])
                .build()
            )
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createPageWithMetaModel(pageContentType))
                .build()
            )
            .build()
    }

    static EndpointResponseMetaModelDto createValidEndpointResponseMetaModelDto() {
        EndpointResponseMetaModelDto.builder()
            .classMetaModel(createClassMetaModelDtoFromClass(Long))
            .successHttpCode(201)
            .build()
    }

    static EndpointMetaModelDto emptyEndpointMetaModelDto() {
        EndpointMetaModelDto.builder().build()
    }
}
