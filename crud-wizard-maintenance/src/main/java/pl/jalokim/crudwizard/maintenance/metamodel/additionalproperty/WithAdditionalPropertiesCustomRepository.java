package pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty;

public interface WithAdditionalPropertiesCustomRepository {

    <T extends WithAdditionalPropertiesEntity> T persist(T withAdditionalPropertiesEntity);
}
