package pl.jalokim.crudwizard.core.datastorage

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath

class RawEntityObjectSamples {

    private static final JsonObjectMapper JSON_OBJECT_MAPPER = new JsonObjectMapper(createObjectMapper())

    static Map<String, Object> createRequestBodyAsMap() {
        [
            bankField          : "",
            name               : "John",
            surname            : "Doe",
            birthDate          : "1990-01-14",
            applicationDateTime: "2021-04-12T12:01:15",
            personData         : [
                name   : "XYZ",
                surname: "QWERTY"
            ],
            age                : 12,
            addresses          : [
                [
                    street  : "mainStreet",
                    houseNr : "12/1",
                    someEnum: "enum1"
                ],
                [
                    street  : "second Street",
                    houseNr : "15",
                    someEnum: "enum2"
                ]
            ],
            hobbies            : ["sport", "music"],
            contactData        : [
                phoneNumber: "+48 123 456 789",
                email      : "test12@domain.com",
            ],
            someNumbersByEnums : [
                "ENUM1": 12,
                "enum2": "13",
            ]
        ]
    }

    static String createRequestBodyAsRawJson() {
        JSON_OBJECT_MAPPER.asJsonValue(ObjectNodePath.rootNode(), createRequestBodyAsMap())
    }

    static JsonNode createRequestBody() {
        def rawJson = createRequestBodyAsRawJson()
        JSON_OBJECT_MAPPER.asJsonNode(ObjectNodePath.rootNode(), rawJson)
    }

    static Map<String, Object> createRequestBodyTranslated() {
        [
            bankField          : null,
            name               : "John",
            surname            : "Doe",
            birthDate          : LocalDate.of(1990, 1, 14),
            applicationDateTime: LocalDateTime.of(2021, 4, 12, 12, 01, 15),
            personData         : new SamplePersonDto("XYZ", "QWERTY"),
            age                : 12,
            addresses          : [
                [
                    street  : "mainStreet",
                    houseNr : "12/1",
                    someEnum: ExampleEnum.ENUM1
                ],
                [
                    street  : "second Street",
                    houseNr : "15",
                    someEnum: ExampleEnum.ENUM2
                ]
            ],
            hobbies            : ["sport", "music"] as Set,
            contactData        : [
                phoneNumber: "+48 123 456 789",
                email      : "test12@domain.com",
            ],
            someNumbersByEnums : Map.of(
                ExampleEnum.ENUM1, 12,
                ExampleEnum.ENUM2, 13,
            )
        ]
    }

    static Map<String, Object> createHttpQueryParams() {
        [
            lastContact : "2021-01-14",
            lastText    : "some text",
            numberAsText: "12",
        ]
    }

    static Map<String, Object> createHttpQueryParamsTranslated() {
        [
            lastContact : LocalDate.of(2021, 1, 14),
            lastText    : "some text",
            numberAsText: "12"
        ]
    }

}
