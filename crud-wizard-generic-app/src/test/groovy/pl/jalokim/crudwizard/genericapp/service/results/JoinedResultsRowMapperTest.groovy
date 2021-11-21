package pl.jalokim.crudwizard.genericapp.service.results

import spock.lang.Specification

class JoinedResultsRowMapperTest extends Specification {

    private JoinedResultsRowMapper testCase = new JoinedResultsRowMapper()

    def "return expected map"() {
        given:
        JoinedResultsRow joinedResultsRow = new JoinedResultsRow()
        joinedResultsRow.addJoinedObject("ds1", [
            person   : [
                name   : "name1",
                surname: "surname1"
            ],
            documents: ["text1", "text2"]
        ])
        joinedResultsRow.addJoinedObject("ds2", [
            person          : [
                name   : "name2",
                surname: "surname2"
            ],
            motherMaidenName: "Doe"
        ])

        MetaData otherMetaData = new MetaData(additionalFields: ["1", "2"])
        Person person = new Person()
        person.otherMetaData = otherMetaData
        joinedResultsRow.addJoinedObject("ds3", person)

        when:
        def result = testCase.mapToObject(joinedResultsRow)

        then:
        result.person == [
            name   : "name1",
            surname: "surname1"
        ]
        result.documents == ["text1", "text2"]
        result.motherMaidenName == "Doe"
        result.otherPersonField == person.otherPersonField
        result.otherMetaData == person.otherMetaData
        result.additionalFields == ["ad1", "ad2"]
    }


    static class Person extends MetaData {
        String person = "personValue"
        String otherPersonField = "otherPersonField1"
        MetaData otherMetaData
    }

    static class MetaData {
        List<String> additionalFields = ["ad1", "ad2"]
    }
}
