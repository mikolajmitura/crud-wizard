package pl.jalokim.crudwizard.datastorage.inmemory

import org.springframework.data.domain.Sort
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery
import pl.jalokim.crudwizard.core.datastorage.query.RealExpression
import pl.jalokim.crudwizard.core.datastorage.query.inmemory.InMemoryDsQueryRunner
import pl.jalokim.crudwizard.core.datastorage.query.inmemory.InMemoryOrderByTranslator
import pl.jalokim.crudwizard.core.datastorage.query.inmemory.InMemoryWhereExpressionTranslator
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators
import pl.jalokim.utils.test.DataFakerHelper
import spock.lang.Specification

class InMemoryDataStorageTest extends Specification {

    public static final String PERSONS = "persons"
    public static final String ADDRESSES = "addresses"
    public static final String JOBS = "jobs"
    private ClassMetaModel personMetaModel = newClassMetaModel(PERSONS, "id", Long)
    private ClassMetaModel addressMetaModel = newClassMetaModel(ADDRESSES, "uuid", String)
    private ClassMetaModel jobMetaModel = newClassMetaModel(JOBS, "idField", Integer)

    private InMemoryWhereExpressionTranslator inMemoryWhereExpressionTranslator = new InMemoryWhereExpressionTranslator()
    private InMemoryOrderByTranslator inMemoryOrderByTranslator = new InMemoryOrderByTranslator()
    private InMemoryDsQueryRunner inMemoryDsQueryRunner = new InMemoryDsQueryRunner(inMemoryWhereExpressionTranslator, inMemoryOrderByTranslator)

    def "should save, filter, order, update, delete few entities in in memory data storage"() {
        given:
        def inMemoryStorage = new InMemoryDataStorage(IdGenerators.INSTANCE, inMemoryDsQueryRunner)
        def firstPerson = [
            name     : "John",
            firstName: DataFakerHelper.randomText()
        ]

        def secondPerson = [
            name     : "Frank",
            firstName: DataFakerHelper.randomText()
        ]

        def address = [
            city  : DataFakerHelper.randomText(),
            street: DataFakerHelper.randomText()
        ]

        def firstJob = [
            jobName: DataFakerHelper.randomText()
        ]

        def secondJob = [
            jobName: DataFakerHelper.randomText()
        ]

        when: 'should save few new'
        Long firstPersonId = inMemoryStorage.saveOrUpdate(personMetaModel, firstPerson)
        Long secondPersonId = inMemoryStorage.saveOrUpdate(personMetaModel, secondPerson)
        String addressId = inMemoryStorage.saveOrUpdate(addressMetaModel, address)
        Integer firstJobId = inMemoryStorage.saveOrUpdate(jobMetaModel, firstJob)
        Integer secondJobId = inMemoryStorage.saveOrUpdate(jobMetaModel, secondJob)
        def entitiesByName = inMemoryStorage.entitiesByName

        then:
        entitiesByName.size() == 3
        entitiesByName.keySet() == [PERSONS, ADDRESSES, JOBS] as Set
        entitiesByName[PERSONS].entitiesById.size() == 2
        entitiesByName[ADDRESSES].entitiesById.size() == 1
        entitiesByName[JOBS].entitiesById.size() == 2
        firstPersonId == 0
        secondPersonId == 1
        firstPersonId == firstPerson.id
        firstJobId == 0
        secondJobId == 1
        addressId == address.uuid

        and: 'filter and sort person entities'
        when:
        def query1 = DataStorageQuery.builder()
            .selectFrom(personMetaModel)
            .where(RealExpression.isNotNull("name"))
            .sortBy(Sort.by("name"))
            .build()
        def results1 = inMemoryStorage.findEntities(query1)

        def query2 = DataStorageQuery.builder()
            .selectFrom(personMetaModel)
            .where(RealExpression.isEqualsTo("name", "John"))
            .build()
        def results2 = inMemoryStorage.findEntities(query2)

        def query3 = DataStorageQuery.builder()
            .selectFrom(personMetaModel)
            .build()
        def results3 = inMemoryStorage.findEntities(query3)

        then:
        results1 == [secondPerson, firstPerson]
        results2 == [firstPerson]
        results3 == [firstPerson, secondPerson]

        and: "get entity by id"
        when:
        def newFirstPerson = inMemoryStorage.getEntityById(personMetaModel, firstPersonId)

        then:
        newFirstPerson == firstPerson

        and: "update to new entity for certain id"
        when:
        def updatedFirstPerson = firstPerson
        updatedFirstPerson.name = firstPerson.name + DataFakerHelper.randomText()

        Long updatedFirstPersonId = inMemoryStorage.saveOrUpdate(personMetaModel, updatedFirstPerson)
        def getUpdatedFirstPerson = inMemoryStorage.getEntityById(personMetaModel, firstPersonId)

        then:
        updatedFirstPersonId == firstPersonId
        entitiesByName[PERSONS].entitiesById.size() == 2
        entitiesByName[ADDRESSES].entitiesById.size() == 1
        entitiesByName[JOBS].entitiesById.size() == 2
        getUpdatedFirstPerson == updatedFirstPerson

        and: "remove by id"
        when:
        inMemoryStorage.deleteEntity(personMetaModel, secondPersonId)

        then:
        entitiesByName[PERSONS].entitiesById.size() == 1
        entitiesByName[ADDRESSES].entitiesById.size() == 1
        entitiesByName[JOBS].entitiesById.size() == 2
        !entitiesByName[PERSONS].entitiesById.containsKey(secondPersonId)
        entitiesByName[PERSONS].entitiesById.containsKey(firstPersonId)
    }

    private ClassMetaModel newClassMetaModel(String name, String idFieldName, Class<?> typeOfId) {
        FieldMetaModel fieldMetaModel = FieldMetaModel.builder()
            .fieldName(idFieldName)
            .fieldType(ClassMetaModel.builder()
                .className(typeOfId.canonicalName)
                .realClass(typeOfId)
                .build())
            .additionalProperties([
                AdditionalPropertyDto.builder()
                    .name(FieldMetaModel.IS_ID_FIELD)
                    .build()
            ])
            .build()

        ClassMetaModel.builder()
            .name(name)
            .fields([fieldMetaModel])
            .build()
    }
}
