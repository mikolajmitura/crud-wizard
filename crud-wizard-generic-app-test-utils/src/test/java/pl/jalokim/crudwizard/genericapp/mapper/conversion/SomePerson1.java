package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import lombok.Value;

@Value
public class SomePerson1 {
    Long id;
    String name;
    String surname;
    SomeDocument1 passport;
    SomeDocument1 idCard;
    SomeContact1 phoneContact;
    SomeContact1 emailContact;
}
