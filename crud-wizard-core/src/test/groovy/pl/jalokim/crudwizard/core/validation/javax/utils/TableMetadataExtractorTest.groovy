package pl.jalokim.crudwizard.core.validation.javax.utils

import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getNameOfIdFieldInEntity
import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getTableNameFromEntity

import pl.jalokim.crudwizard.core.example.OtherPersonEntity
import pl.jalokim.crudwizard.core.example.RawPersonEntity
import spock.lang.Specification
import spock.lang.Unroll

class TableMetadataExtractorTest extends Specification {

    @Unroll
    def "extracts expected table name"() {
        when:
        def result = getTableNameFromEntity(entityClass)

        then:
        result == expected

        where:
        entityClass       | expected
        RawPersonEntity   | "RAW_PERSON"
        OtherPersonEntity | "some_name_of_table"
    }

    @Unroll
    def "extracts expected if field name"() {
        when:
        def result = getNameOfIdFieldInEntity(entityClass)

        then:
        result == expected

        where:
        entityClass       | expected
        RawPersonEntity   | "SOME_STRING_ID"
        OtherPersonEntity | "id_raw_name"
    }
}
