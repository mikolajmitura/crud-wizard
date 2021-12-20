package pl.jalokim.crudwizard.core.datastorage.query.inmemory

class PersonDataSamples {

    final static PERSON1 = new Person(name: "John", surname: "Doe", someDouble: 0.3)
    final static PERSON2 = new Person(name: "Jonathan", surname: "Does", father: PERSON1, someDouble: 1.1)
    final static PERSON3 = new Person(name: "Marry", surname: "Jane", father: PERSON1, someLong: 1)
    final static PERSON4 = new Person(name: "Nathan", surname: "Jane", father: PERSON2, someLong: 13)
    final static PERSON5 = new Person(name: "Jane", surname: "Hajanesons", father: PERSON2, someLong: 12)

    static List<Person> peopleList() {
        return [PERSON1, PERSON2, PERSON3, PERSON4, PERSON5]
    }
}
