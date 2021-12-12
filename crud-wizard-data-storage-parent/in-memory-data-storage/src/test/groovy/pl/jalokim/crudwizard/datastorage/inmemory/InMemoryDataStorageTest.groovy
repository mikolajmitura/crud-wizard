package pl.jalokim.crudwizard.datastorage.inmemory

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

    def "should save, update, delete few entities in in memory data storage"() {
        given:
        def inMemoryStorage = new InMemoryDataStorage(IdGenerators.INSTANCE, new InMemoryWhereExpressionTranslator())
        def firstPerson = [
            name     : DataFakerHelper.randomText(),
            firstName: DataFakerHelper.randomText()
        ]

        def secondPerson = [
            name     : DataFakerHelper.randomText(),
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
        Long firstPersonId = inMemoryStorage.saveEntity(personMetaModel, firstPerson)
        Long secondPersonId = inMemoryStorage.saveEntity(personMetaModel, secondPerson)
        String addressId = inMemoryStorage.saveEntity(addressMetaModel, address)
        Integer firstJobId = inMemoryStorage.saveEntity(jobMetaModel, firstJob)
        Integer secondJobId = inMemoryStorage.saveEntity(jobMetaModel, secondJob)
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

        and: "get entity by id"
        when:
        def newFirstPerson = inMemoryStorage.getEntityById(personMetaModel, firstPersonId)

        then:
        newFirstPerson == firstPerson

        and: "update to new entity for certain id"
        when:
        def updatedFirstPerson = firstPerson
        updatedFirstPerson.name = firstPerson.name + DataFakerHelper.randomText()

        Long updatedFirstPersonId = inMemoryStorage.saveEntity(personMetaModel, updatedFirstPerson)
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
