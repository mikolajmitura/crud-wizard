package pl.jalokim.crudwizard.genericapp.service.results

import java.time.LocalDate
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DataStorageResultsJoinerMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.EqObjectsJoiner
import spock.lang.Specification

class DataStorageResultJoinerTest extends Specification {

    private static final EqObjectsJoiner EQ_OBJECTS_JOINER = new EqObjectsJoiner()

    DataStorageResultJoiner dataStorageResultJoiner = new DataStorageResultJoiner()

    def "return expected join results"() {
        given:
        def dataStorageResultsJoiners = createDataStorageResultsJoiners()
        def queriesResults = createQueriesResults()

        when:
        def result = dataStorageResultJoiner.getJoinedNodes(dataStorageResultsJoiners, queriesResults)

        then:
        result.find {
            containsPerson(it, "John", "Doe") && containsPermission(it, "Normal") &&
                containsDocument(it, "name1") && containsIssuer(it, "PL") &&
                containsExternalDocument(it, "11", 1) && containsCountryInfo(it, "Poland")
        } != null
        result.find {
            containsPerson(it, "John", "Jonson") && containsPermission(it, "Normal") &&
                containsDocument(it, "name2") && notContainsIssuer(it) &&
                notContainExternalDocument(it) && notContainCountryInfo(it)
        } != null
        result.find {
            containsPerson(it, "Mike", "Tyson") && containsPermission(it, "Vip") &&
                notContainDocument(it) && notContainsIssuer(it) &&
                notContainExternalDocument(it) && notContainCountryInfo(it)
        } != null
        result.find {
            notContainPerson(it) && containsPermission(it, "Super") &&
                notContainDocument(it) && notContainsIssuer(it) &&
                notContainExternalDocument(it) && notContainCountryInfo(it)
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                containsDocument(it, "name3") && notContainsIssuer(it) &&
                notContainExternalDocument(it) && notContainCountryInfo(it)
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                containsDocument(it, "name4") && containsIssuer(it, "US") &&
                containsExternalDocument(it, "14", 2) && containsCountryInfo(it, "United States")
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                notContainExternalDocument(it) && containsIssuer(it, "UK") &&
                notContainExternalDocument(it) && containsCountryInfo(it, "United Kingdom")
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                notContainExternalDocument(it) && containsIssuer(it, "DE") &&
                notContainExternalDocument(it) && containsCountryInfo(it, "Germany")
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                notContainExternalDocument(it) && containsIssuer(it, "ES") &&
                notContainExternalDocument(it) && notContainCountryInfo(it)
        } != null
        result.find {
            notContainPerson(it) && notContainPermission(it) &&
                notContainExternalDocument(it) && notContainsIssuer(it) &&
                notContainExternalDocument(it) && containsCountryInfo(it, "Slovakia")
        } != null
    }

    boolean containsPerson(JoinedResultsRow row, String name, String surname) {
        row.containsValueByQueryName("persons") &&
            row.joinedResultsByDsQueryName["persons"].name == name &&
            row.joinedResultsByDsQueryName["persons"].surname == surname
    }

    boolean notContainPerson(JoinedResultsRow row) {
        !row.containsValueByQueryName("persons")
    }

    boolean containsPermission(JoinedResultsRow row, String name) {
        row.containsValueByQueryName("permissions") &&
            row.joinedResultsByDsQueryName["permissions"].account.name == name
    }

    boolean notContainPermission(JoinedResultsRow row) {
        !row.containsValueByQueryName("permissions")
    }

    boolean containsDocument(JoinedResultsRow row, String name) {
        row.containsValueByQueryName("documents") &&
            row.joinedResultsByDsQueryName["documents"].name == name
    }

    boolean notContainDocument(JoinedResultsRow row) {
        !row.containsValueByQueryName("documents")
    }

    boolean containsExternalDocument(JoinedResultsRow row, String idUuid, Long issuer) {
        row.containsValueByQueryName("externalDocs") &&
            row.joinedResultsByDsQueryName["externalDocs"].idUuid == idUuid &&
            row.joinedResultsByDsQueryName["externalDocs"].issuer == issuer
    }

    boolean notContainExternalDocument(JoinedResultsRow row) {
        !row.containsValueByQueryName("externalDocs")
    }

    boolean containsIssuer(JoinedResultsRow row, String countryCode) {
        row.containsValueByQueryName("issuers") &&
            row.joinedResultsByDsQueryName["issuers"].countryCode == countryCode
    }

    boolean notContainsIssuer(JoinedResultsRow row) {
        !row.containsValueByQueryName("issuers")
    }

    boolean containsCountryInfo(JoinedResultsRow row, String name) {
        row.containsValueByQueryName("countriesInfo") &&
            row.joinedResultsByDsQueryName["countriesInfo"].name == name
    }

    boolean notContainCountryInfo(JoinedResultsRow row) {
        !row.containsValueByQueryName("countriesInfo")
    }

    List<DataStorageResultsJoinerMetaModel> createDataStorageResultsJoiners() {
        [
            createJoinMeta("persons", "accountType", "permissions", "account.type"),
            createJoinMeta("persons", "documentId", "documents", "id"),
            createJoinMeta("issuers", "id", "externalDocs", "issuer"),
            createJoinMeta("countriesInfo", "countryWrapper.code", "issuers", "countryCode"),
            createJoinMeta("externalDocs", "idUuid", "documents", "uuid"),
        ]
    }

    private DataStorageResultsJoinerMetaModel createJoinMeta(String leftQueryName, String leftPath,
        String rightQueryName, String rightPath) {
        DataStorageResultsJoinerMetaModel.builder()
            .leftNameOfQueryResult(leftQueryName)
            .leftPath(leftPath)
            .joinerVerifierInstance(EQ_OBJECTS_JOINER)
            .rightNameOfQueryResult(rightQueryName)
            .rightPath(rightPath)
            .build()
    }

    Map<String, List<Object>> createQueriesResults() {
        [persons      : [
            [
                name       : "John",
                surname    : "Doe",
                documentId : 1,
                accountType: "N"
            ],
            [
                name       : "John",
                surname    : "Jonson",
                documentId : 2,
                accountType: "N"
            ],
            [
                name       : "Mike",
                surname    : "Tyson",
                accountType: "V",
                insurance  : new Insurance(name: "normal", validTill: LocalDate.of(2021, 1, 1))
            ]
        ],
         permissions  : [
             new PermissionInfo(account: new Account(type: "N", name: "Normal"), permissions: []),
             new PermissionInfo(account: new Account(type: "S", name: "Super"), permissions: ['$all']),
             new PermissionInfo(account: new Account(type: "V", name: "Vip"), permissions: ["read", "write"])
         ],
         documents    : [
             new Document(id: 1, name: "name1", uuid: "11"),
             new Document(id: 2, name: "name2", uuid: "12"),
             new Document(id: 3, name: "name3", uuid: "13"),
             new Document(id: 4, name: "name4", uuid: "14"),
         ],
         externalDocs : [
             [
                 idUuid: "11",
                 issuer: 1
             ],
             [
                 idUuid: "14",
                 issuer: 2
             ]
         ],
         issuers      : [
             new Issuer(id: 1, countryCode: "PL"),
             new Issuer(id: 2, countryCode: "US"),
             new Issuer(id: 3, countryCode: "UK"),
             new Issuer(id: 4, countryCode: "DE"),
             new Issuer(id: 5, countryCode: "ES"),
         ],
         countriesInfo: [
             new CountryInfo(name: "Poland", countryWrapper: new CountryEnumWrapper(code: CountryEnum.PL)),
             new CountryInfo(name: "Germany", countryWrapper: new CountryEnumWrapper(code: CountryEnum.DE)),
             new CountryInfo(name: "United States", countryWrapper: new CountryEnumWrapper(code: CountryEnum.US)),
             new CountryInfo(name: "United Kingdom", countryWrapper: new CountryEnumWrapper(code: CountryEnum.UK)),
             new CountryInfo(name: "Slovakia", countryWrapper: new CountryEnumWrapper(code: CountryEnum.SK)),
         ]
        ]
    }

    static class Insurance {
        String name
        LocalDate validTill
    }

    static class PermissionInfo {
        Account account
        List<String> permissions
    }

    static class Account {
        String type
        String name
    }

    static class Document {
        Long id
        String name
        String uuid
    }

    static class Issuer {
        String id
        String countryCode
    }

    static class CountryInfo {
        String name
        CountryEnumWrapper countryWrapper
    }

    static class CountryEnumWrapper {
        CountryEnum code
    }

    static enum CountryEnum {
        PL, DE, UK, US, SK
    }
}
