package pl.jalokim.crudwizard.genericapp.mapper.instance;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonCreateEvent;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonEntity;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonOneDto;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;

public class MapperWithNotGenericArguments {

    public PersonEntity mapToPersonEntity(Map<String, Object> personModel,
        @RequestHeader("x-logged-user") String loggedUserName, @PathVariable Long parentId) {

        PersonEntity personEntity = new PersonEntity();
        personEntity.setName((String) personModel.get("name"));
        personEntity.setSurname((String) personModel.get("surname"));
        personEntity.setExternalId((Long) personModel.get("otherId"));
        personEntity.setParentId(parentId);
        personEntity.setUpdatedBy(loggedUserName);
        return personEntity;
    }

    public PersonCreateEvent mapPersonEvent(Map<String, Object> personModel, GenericMapperArgument genericMapperArgument) {
        Map<String, Object> mappingContext = genericMapperArgument.getMappingContext();
        Long personEntityId = (Long) mappingContext.get("entities");
        String name = (String) personModel.get("name");
        String surname = (String) personModel.get("surname");

        return PersonCreateEvent.builder()
            .fullName(name + " " + surname)
            .dbId(personEntityId)
            .build();
    }

    public Long getFinalCreateId(Map<String, Object> mappingContext) {
        return (Long) mappingContext.get("entities");
    }

    public PersonOneDto getFinalPersonOneDto(Map<String, Object> mappingContext) {
        PersonEntity personEntity = (PersonEntity) mappingContext.get("entities");
        PersonCreateEvent personEvent = (PersonCreateEvent) mappingContext.get("events");
        return PersonOneDto.builder()
            .dbId(personEntity.getId())
            .name(personEntity.getName())
            .surname(personEntity.getSurname())
            .externalId(personEntity.getExternalId())
            .parentId(personEntity.getParentId())
            .updatedBy(personEntity.getUpdatedBy())
            .eventUuid(personEvent.getId())
            .build();
    }

    public PersonOneDto mapFinalPersonOneDtoByJoinedResultsRow(JoinedResultsRow joinedResultsRow) {
        Map<String, Object> mappingContext = joinedResultsRow.getJoinedResultsByDsQueryName();
        PersonEntity personEntity = (PersonEntity) mappingContext.get("entities");
        PersonCreateEvent personEvent = (PersonCreateEvent) mappingContext.get("events");
        return PersonOneDto.builder()
            .dbId(personEntity.getId())
            .name(personEntity.getName())
            .surname(personEntity.getSurname())
            .externalId(personEntity.getExternalId())
            .parentId(personEntity.getParentId())
            .updatedBy("****")
            .eventUuid(personEvent.getId())
            .build();
    }
}
