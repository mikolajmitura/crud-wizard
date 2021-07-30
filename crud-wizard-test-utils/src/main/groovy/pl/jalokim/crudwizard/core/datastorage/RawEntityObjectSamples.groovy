package pl.jalokim.crudwizard.core.datastorage

import java.time.LocalDate
import java.time.LocalDateTime

class RawEntityObjectSamples {

    static Map<String, Object> createRequestBody() {
        [
            bankField          : "",
            name               : "John",
            surname            : "Doe",
            birthDate          : "1990-01-14",
            applicationDateTime: "2021-04-12T12:01:15",
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
            ]
        ]
    }

    static Map<String, Object> createRequestBodyTranslated() {
        [
            bankField          : null,
            name               : "John",
            surname            : "Doe",
            birthDate          : LocalDate.of(1990, 1, 14),
            applicationDateTime: LocalDateTime.of(2021, 4, 12, 12, 01, 15),
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
            ]
        ]
    }

    static Map<String, Object> createHttpQueryParams() {
        [
            lastContact: "2021-01-14",
            lastText   : "some text",
        ]
    }

    static Map<String, Object> createHttpQueryParamsTranslated() {
        [
            lastContact: LocalDate.of(2021, 1, 14),
            lastText   : "some text",
        ]
    }

}
