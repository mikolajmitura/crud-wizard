package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

public interface WithAdditionalPropertiesCustomRepository<T extends WithAdditionalPropertiesEntity> {

    T persist(T withAdditionalPropertiesEntity);
}
