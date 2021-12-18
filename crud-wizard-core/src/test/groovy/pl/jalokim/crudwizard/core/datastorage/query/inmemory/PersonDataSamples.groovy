package pl.jalokim.crudwizard.core.datastorage.query.inmemory

class PersonDataSamples {

    static Person PERSON1 = new Person(name: "John", surname: "Doe", someDouble: 0.3)
    static Person PERSON2 = new Person(name: "Jonathan", surname: "Does", father: PERSON1, someDouble: 1.1)
    static Person PERSON3 = new Person(name: "Marry", surname: "Jane", father: PERSON1, someLong: 1)
    static Person PERSON4 = new Person(name: "Nathan", surname: "Jane", father: PERSON2, someLong: 13)
    static Person PERSON5 = new Person(name: "Jane", surname: "Hajanesons", father: PERSON2, someLong: 12)

    static List<Person> peopleList() {
        return [PERSON1, PERSON2, PERSON3, PERSON4, PERSON5]
    }
}
